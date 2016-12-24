package com.parallelsymmetry.reposync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Bitbucket client class.
 */
public class BitbucketClient {

	private static final Logger log = LoggerFactory.getLogger( com.parallelsymmetry.reposync.BitbucketClient.class );

	private BitbucketConfig config;

	private RestTemplate rest;

	public BitbucketClient( BitbucketConfig config ) {
		this.config = config;

		// Set up REST template
		rest = new RestTemplate();
		rest.getInterceptors().add( new BasicAuthorizationInterceptor( config.getUsername(), config.getPassword() ) );
	}

	public Set<GitRepo> getBitbucketRepos() {
		Set<GitRepo> repos = new HashSet<GitRepo>();

		UriTemplate repoUri = new UriTemplate( config.getRepoUri() );
		URI nextUri = repoUri.expand( config.getTeam() );

		// Run through all the pages to get the repository parameters.
		int count = 0;
		while( nextUri != null ) {
			log.info( "Getting repositories page " + ++count + "..." );

			// Call Bitbucket for data
			ObjectNode node = rest.getForObject( nextUri, ObjectNode.class );

			// Parse and add the repos
			repos.addAll( parseBitbucketRepos( node ) );

			// Get the next page
			try {
				JsonNode nextNode = node.get( "next" );
				nextUri = nextNode == null ? null : new URI( node.get( "next" ).asText() );
			} catch( URISyntaxException exception ) {
				log.error( "Error parsing next URI", exception );
			}
		}

		return repos;
	}

	private Set<GitRepo> parseBitbucketRepos( ObjectNode node ) {
		Set<GitRepo> repos = new HashSet<GitRepo>();

		// Parse the Bitbucket data into repo objects
		try {
			for( JsonNode repoNode : node.get( "values" ) ) {
				String repoName = repoNode.get( "name" ).asText().toLowerCase();
				String projectName = repoNode.get( "project" ).get( "name" ).asText().toLowerCase();

				UriTemplate targetUri = new UriTemplate( config.getTarget() );
				Path targetPath = Paths.get( targetUri.expand( projectName, repoName ) );

				GitRepo gitRepo = new GitRepo();
				gitRepo.setName( repoName );
				gitRepo.setProject( projectName );
				gitRepo.setRemote( getCloneUri( repoNode ) );
				gitRepo.setLocalPath( targetPath );

				repos.add( gitRepo );
			}

		} catch( Exception exception ) {
			log.error( "Unable to retrieve project repository list", exception );
		}

		return repos;
	}

	private String getCloneUri( JsonNode repo ) {
		String protocol = config.getProtocol().toLowerCase();
		for( JsonNode clone : repo.get( "links" ).get( "clone" ) ) {
			if( clone.get( "name" ).asText().toLowerCase().equals( protocol ) ) {
				return clone.get( "href" ).asText();
			}
		}
		return null;
	}

	public void doGitPull( Path repo ) throws IOException, GitAPIException {
		Git.open( repo.toFile() ).pull().setCredentialsProvider( new UsernamePasswordCredentialsProvider( config.getUsername(), config.getPassword() ) ).call();
	}

	public void doGitClone( Path repo, String uri ) throws IOException, GitAPIException {
		Files.createDirectories( repo );
		Git.cloneRepository().setURI( uri ).setDirectory( repo.toFile() ).setCredentialsProvider( new UsernamePasswordCredentialsProvider( config.getUsername(), config.getPassword() ) ).call();
	}


}
