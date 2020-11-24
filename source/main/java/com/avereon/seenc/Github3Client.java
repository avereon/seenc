package com.avereon.seenc;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class Github3Client extends RepoClient {

	private static final Logger log = LoggerFactory.getLogger( Github3Client.class );

	protected Github3Client( RepoClientConfig config ) {
		super( config );
	}

	@Override
	public Set<GitRepo> getRemotes() {
		Set<GitRepo> repos = new HashSet<>();
		String login = getGithubUser();

		for( String org : getConfig().getAll( "orgs" ) ) {
			URI uri = getUriTemplate( "/orgs/{org}/repos" ).expand( org );
			if( org.equals( login ) ) uri = getUriTemplate( "/user/repos" ).expand();

			for( JsonNode json : getRest( uri ).getForObject( uri, JsonNode.class ) ) {
				if( json.get( "fork" ).asBoolean() ) continue;
				repos.add( createRepo( json ) );
			}
		}

		return repos;
	}

	private String getGithubUser() {
		URI uri = getUriTemplate( "/user" ).expand();
		JsonNode user = getRest( uri ).getForObject( uri, JsonNode.class );
		return user.findValue( "login" ).asText();
	}

	private GitRepo createRepo( JsonNode repoNode ) {
		String repoName = repoNode.get( "name" ).asText().toLowerCase();
		UriTemplate targetUri = new UriTemplate( "file:" + getConfig().get( "target" ) );
		Path targetPath = Paths.get( targetUri.expand( repoName ) );

		GitRepo gitRepo = new GitRepo();
		gitRepo.setName( repoName );
		gitRepo.setRemote( repoNode.get( "clone_url" ).asText() );
		gitRepo.setLocalPath( targetPath );
		return gitRepo;
	}

}
