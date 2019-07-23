package com.avereon.seenc;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class CjcGitClient extends BitbucketClient {

	private RestTemplate rest;

	protected CjcGitClient( RepoClientConfig config ) {
		super( config );

		// Set up REST template
		rest = new RestTemplate();
		rest.getInterceptors().add( new BasicAuthorizationInterceptor( config.get( "username" ), config.get( "password" ) ) );
	}

	@Override
	public Set<GitRepo> getRepos() {
		Set<GitRepo> repos = new HashSet<>();

		List<String> projects = Arrays.stream( getConfig().get( "projects" ).split( "," ) ).map( String::trim ).collect( Collectors.toList() );
		UriTemplate repoUri = new UriTemplate( getConfig().get( "CJC-rest-repo-uri" ) );

		for( String project : projects ) {
			URI uri = repoUri.expand( Map.of( "project", project ) );
			ObjectNode node = rest.getForObject( uri, ObjectNode.class );
			repos.addAll( parseBitbucketRepos( project, node ) );
		}

		return repos;
	}

	@Override
	public void processRepositories() {
		super.processRepositories();
	}

}
