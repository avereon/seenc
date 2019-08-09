package com.avereon.seenc;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GithubClient extends RepoClient {

	private static final Logger log = LoggerFactory.getLogger( GithubClient.class );

	private RestTemplate rest;

	protected GithubClient( RepoClientConfig config ) {
		super( config );
		rest = new RestTemplate();
		rest.getInterceptors().add( new BasicAuthorizationInterceptor( config.get( "username" ), config.get( "password" ) ) );
	}

	@Override
	public Set<GitRepo> getRepos() {
		System.err.println( "Getting repositories..." );


		Set<GitRepo> repos = new HashSet<>();

		UriTemplate repoUri = new UriTemplate( getConfig().get( "GH-uri" ) );

		log.info( "Requesting organizations..." );
		URI uri = repoUri.expand( Map.of( "account", getConfig().get( "team" ) ) );

		uri = URI.create( "https://api.github.com/orgs/" + getConfig().get( "team" ) + "/repos" );
		//uri = URI.create( "https://api.github.com/users/" + getConfig().get( "username" ) + "/repos" );
		log.info( "URI: " + uri );

//		ArrayNode node = rest.getForObject( uri, ArrayNode.class );
//		for( JsonNode json : node ) {
		// TODO When going through repos, skip the forked repos
//			log.info( json.toString() );
//		}

		log.info( rest.getForObject( uri, JsonNode.class ).toString() );

		return repos;
	}

	public void processRepositories() {
		log.info( "Requesting repositories for " + getConfig().get( "team" ) + "..." );

		super.processRepositories();
	}

}
