package com.avereon.seenc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Bitbucket client class.
 */
public class Bitbucket2Client extends Bitbucket0Client {

	private static final Logger log = LoggerFactory.getLogger( Bitbucket2Client.class );

	Bitbucket2Client( RepoClientConfig config ) {
		super( config );
	}

	public Set<GitRepo> getRepos() {
		Set<GitRepo> repos = new HashSet<>();

		// Can be used to override the repo project name
		String project = getConfig().get( "project" );

		URI nextUri = getUriTemplate().expand( Map.of( "account", getConfig().get( "team" ) ) );

		// Run through all the pages to get the repository parameters.
		while( nextUri != null ) {
			// Call Bitbucket for data
			ObjectNode node = getRest().getForObject( nextUri, ObjectNode.class );

			// Parse and add the repos
			repos.addAll( parseBitbucketRepos( project, node ) );

			// Get the next page
			try {
				JsonNode nextNode = node.get( "next" );
				nextUri = nextNode == null ? null : new URI( nextNode.asText() );
			} catch( URISyntaxException exception ) {
				log.error( "Error parsing next URI", exception );
			}
		}

		return repos;
	}

}
