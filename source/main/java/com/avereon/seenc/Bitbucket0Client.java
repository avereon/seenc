package com.avereon.seenc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriTemplate;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public abstract class Bitbucket0Client extends RepoClient {

	private static final Logger log = LoggerFactory.getLogger( Bitbucket0Client.class );

	protected Bitbucket0Client( RepoClientConfig config ) {
		super( config );
	}

	public void processRepositories() {
		System.out.println( "Requesting repositories ..." );
		super.processRepositories();
	}

	Set<GitRepo> parseBitbucketRepos( String project, ObjectNode node ) {
		Set<GitRepo> repos = new HashSet<>();

		// Parse the Bitbucket data into repo objects
		for( JsonNode repoNode : node.get( "values" ) ) {
			repos.add( createRepo( repoNode, project ) );
		}

		return repos;
	}

	private GitRepo createRepo( JsonNode repoNode, String project ) {
		String repoName = repoNode.get( "name" ).asText().toLowerCase();
		String projectName = repoNode.get( "project" ).get( "name" ).asText().toLowerCase();

		// Project name override
		if( !TextUtils.isBlank( project ) ) projectName = project.toLowerCase();

		UriTemplate targetUri = new UriTemplate( getConfig().get( "target" ) );
		Path targetPath = Paths.get( targetUri.expand( projectName, repoName ) );

		GitRepo gitRepo = new GitRepo();
		gitRepo.setName( repoName );
		gitRepo.setProject( projectName );
		gitRepo.setRemote( getCloneUri( repoNode ) );
		gitRepo.setLocalPath( targetPath );
		return gitRepo;
	}

	private String getCloneUri( JsonNode repo ) {
		for( JsonNode clone : repo.get( "links" ).get( "clone" ) ) {
			String protocol = clone.get( "name" ).asText().toLowerCase();
			if( protocol.equals( "https" ) || protocol.equals( "http" ) ) {
				return clone.get( "href" ).asText();
			}
		}
		return null;
	}

}
