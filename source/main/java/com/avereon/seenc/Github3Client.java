package com.avereon.seenc;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Github3Client extends RepoClient {

	private static final Logger log = LoggerFactory.getLogger( Github3Client.class );

	private RestTemplate rest;

	protected Github3Client( RepoClientConfig config ) {
		super( config );
		rest = new RestTemplate();
		rest.getInterceptors().add( new BasicAuthorizationInterceptor( config.get( "username" ), config.get( "password" ) ) );
	}

	@Override
	public Set<GitRepo> getRepos() {
		URI uri = getUriTemplate().expand( Map.of( "org", getConfig().get( "org" ) ) );

		Set<GitRepo> repos = new HashSet<>();
		for( JsonNode json : rest.getForObject( uri, JsonNode.class ) ) {
			if( json.get( "fork" ).asBoolean() ) continue;
			repos.add( createRepo( json ) );
		}

		return repos;
	}

	public void processRepositories() {
		super.processRepositories();
	}

	private GitRepo createRepo( JsonNode repoNode ) {
		String repoName = repoNode.get( "name" ).asText().toLowerCase();
		UriTemplate targetUri = new UriTemplate( getConfig().get( "target" ) );
		Path targetPath = Paths.get( targetUri.expand( repoName ) );

		GitRepo gitRepo = new GitRepo();
		gitRepo.setName( repoName );
		gitRepo.setRemote( repoNode.get( "clone_url" ).asText() );
		gitRepo.setLocalPath( targetPath );
		return gitRepo;
	}

}
