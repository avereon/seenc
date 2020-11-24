package com.avereon.seenc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

/**
 * Bitbucket client class.
 */
public class Bitbucket2Client extends Bitbucket0Client {

	private static final Logger log = LoggerFactory.getLogger( Bitbucket2Client.class );

	Bitbucket2Client( RepoClientConfig config ) {
		super( config );
	}

	public Set<GitRepo> getRemotes() {
		Set<GitRepo> repos = new HashSet<>();

		// The project can be null, and usually is with BB2 repos
		String project = getConfig().get( "project" );

		// Can be used to override the repo project name
		for( String team : getConfig().getAll( "teams" ) ) {
			URI uri = getUriTemplate( "/repositories/{team}" ).expand( team );

			// Run through all the pages to get the repository parameters.
			while( uri != null ) {
				// Call Bitbucket for data
				ObjectNode node = getRest( uri ).getForObject( uri, ObjectNode.class );

				// Parse and add the repos
				repos.addAll( parseBitbucketRepos( project, node ) );

				// Get the next page
				try {
					JsonNode nextNode = node.get( "next" );
					uri = nextNode == null ? null : new URI( nextNode.asText() );
				} catch( URISyntaxException exception ) {
					log.error( "Error parsing next URI", exception );
				}
			}
		}

		return repos;
	}

}
