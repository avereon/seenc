package com.avereon.seenc;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
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

		// Collect remotes
		try {
			getConfig().getAll( "orgs" ).forEach( org -> {
				repos.addAll( org.equals( login ) ? collectUserRepos() : collectOrgRepos( org ) );
			} );
		} catch( HttpClientErrorException.NotFound exception ) {
			// Intentionally ignore this exception
			log.debug( exception.getMessage() );
		}

		// Require includes
		getConfig().getAll( "orgs" ).forEach( org -> {
			getConfig().getAll( "include" ).forEach( name -> {
				String remote = "https://github.com/" + org.toLowerCase() + "/" + name.toLowerCase() + ".git";
				repos.add( createRepo( name, remote ) );
			} );
		} );

		return repos;
	}

	private Set<GitRepo> collectOrgRepos( String org ) {
		Set<GitRepo> repos = new HashSet<>();

		int page = 0;
		int count;

		do {
			count = 0;
			URI reposUri = getUriTemplate( "/orgs/{org}/repos?page={page}" ).expand( org, page++ );
			for( JsonNode json : getRest( reposUri ).getForObject( reposUri, JsonNode.class ) ) {
				//if( json.get( "fork" ).asBoolean() ) continue;
				repos.add( createRepo( json ) );
				count++;
			}
		} while( count > 0 );

		return repos;
	}

	private Set<GitRepo> collectUserRepos() {
		Set<GitRepo> repos = new HashSet<>();

		int page = 0;
		int count;

		do {
			count = 0;
			URI reposUri = getUriTemplate( "/user/repos?page={page}" ).expand( page++ );
			for( JsonNode json : getRest( reposUri ).getForObject( reposUri, JsonNode.class ) ) {
				//if( json.get( "fork" ).asBoolean() ) continue;
				repos.add( createRepo( json ) );
				count++;
			}
		} while( count > 0 );

		return repos;
	}

	private String getGithubUser() {
		URI uri = getUriTemplate( "/user" ).expand();
		JsonNode user = getRest( uri ).getForObject( uri, JsonNode.class );
		return user.findValue( "login" ).asText();
	}

	protected GitRepo createRepo( JsonNode repoNode ) {
		String repoName = repoNode.get( "name" ).asText();
		return createRepo( repoName, repoNode.get( "clone_url" ).asText(), null );
	}

	protected GitRepo createRepo( String name, String remote ) {
		return createRepo( name, remote, null );
	}

	protected GitRepo createRepo( String name, String remote, Path target ) {
		if( target == null ) {
			UriTemplate targetUri = new UriTemplate( "file:" + getConfig().get( "target" ) );
			target = Paths.get( targetUri.expand( name.toLowerCase() ) );
		}
		GitRepo repo = new GitRepo();
		repo.setName( name.toLowerCase() );
		repo.setRemote( remote.toLowerCase() );
		repo.setLocalPath( target );
		return repo;
	}

}
