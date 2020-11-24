package com.avereon.seenc;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;

public class Bitbucket1Client extends Bitbucket0Client {

	private static final Logger log = LoggerFactory.getLogger( Bitbucket1Client.class );

	protected Bitbucket1Client( RepoClientConfig config ) {
		super( config );
	}

	@Override
	public Set<GitRepo> getRemotes() {
		Set<GitRepo> repos = new HashSet<>();

		List<String> projects = getConfig().getAll( "projects" );

		for( String project : projects ) {
			Map<String, String> variables = new HashMap<>( getConfig().getMap() );
			variables.put( "project", project );
			URI uri = getUriTemplate( "/projects/{project}/repos?limit=999" ).expand( variables );
			ObjectNode node = getRest( uri ).getForObject( uri, ObjectNode.class );
			repos.addAll( parseBitbucketRepos( project, node ) );
		}

		return repos;
	}

}
