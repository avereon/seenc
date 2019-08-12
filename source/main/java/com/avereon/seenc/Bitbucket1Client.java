package com.avereon.seenc;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Bitbucket1Client extends Bitbucket0Client {

	private static final Logger log = LoggerFactory.getLogger( Bitbucket1Client.class );

	protected Bitbucket1Client( RepoClientConfig config ) {
		super( config );
	}

	@Override
	public Set<GitRepo> getRepos() {
		Set<GitRepo> repos = new HashSet<>();

		List<String> projects = getConfig().getAll( "projects" );

		for( String project : projects ) {
			URI uri = getUriTemplate( "/projects/{project}/repos" ).expand( project );
			ObjectNode node = getRest().getForObject( uri, ObjectNode.class );
			repos.addAll( parseBitbucketRepos( project, node ) );
		}

		return repos;
	}

}
