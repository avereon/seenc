package com.avereon.seenc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Bitbucket client class.
 */
public class BitbucketClient extends RepoClient {

	private static final Logger log = LoggerFactory.getLogger( BitbucketClient.class );

	private RestTemplate rest;

	BitbucketClient( BitbucketConfig config ) {
		super( config );

		// Set up REST template
		rest = new RestTemplate();
		rest.getInterceptors().add( new BasicAuthorizationInterceptor( config.getUsername(), config.getPassword() ) );
	}

	public Set<GitRepo> getRepos() {
		Set<GitRepo> repos = new HashSet<>();

		UriTemplate repoUri = new UriTemplate( getConfig().getRepoUri() );
		URI nextUri = repoUri.expand( Map.of( "account", getConfig().getTeam() ) );

		// Run through all the pages to get the repository parameters.
		int page = 1;
		while( nextUri != null ) {
			log.info( "Getting repositories page " + page + "..." );

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
			page++;
		}

		return repos;
	}

	private Set<GitRepo> parseBitbucketRepos( ObjectNode node ) {
		Set<GitRepo> repos = new HashSet<>();

		// Parse the Bitbucket data into repo objects
		try {
			for( JsonNode repoNode : node.get( "values" ) ) {
				//System.out.println( repoNode );
				String repoName = repoNode.get( "name" ).asText().toLowerCase();
				String projectName = repoNode.get( "project" ).get( "name" ).asText().toLowerCase();

				UriTemplate targetUri = new UriTemplate( getConfig().getTarget() );
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
		for( JsonNode clone : repo.get( "links" ).get( "clone" ) ) {
			if( clone.get( "name" ).asText().toLowerCase().equals( "https" ) ) {
				return clone.get( "href" ).asText();
			}
		}
		return null;
	}

}
