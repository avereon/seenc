package com.avereon.seenc;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class RepoClient {

	private static final Logger log = LoggerFactory.getLogger( RepoClient.class );

	private RepoClientConfig config;

	private RestTemplate rest;

	protected RepoClient( RepoClientConfig config ) {
		this.config = config;
		rest = new RestTemplate();
		rest.getInterceptors().add( new BasicAuthorizationInterceptor( config.get( "username" ), config.get( "password" ) ) );
	}

	public abstract Set<GitRepo> getRepos();

	public void processRepositories(){
		Set<GitRepo> repos = getRepos();
		System.out.println( "Repository count: " + repos.size() );

		List<GitRepo> sortedRepos = new ArrayList<>( repos );
		Collections.sort( sortedRepos );

		for( GitRepo repo : sortedRepos ) {
			Path localPath = repo.getLocalPath();
			String message = repo + ": " + localPath.toAbsolutePath();
			boolean exists = Files.exists( localPath );
			GitResult result;
			try {
				if( exists ) {
					result = doGitPull( localPath ) == 0 ? GitResult.PULL_UP_TO_DATE : GitResult.PULL_UPDATED;
				} else {
					doGitClone( localPath, repo.getRemote() );
					result = GitResult.CLONE_SUCCESS;
				}

			} catch( Exception exception ) {
				result = GitResult.ERROR;
				message += ": " + exception.getMessage();
			}
			System.out.println( result.getSymbol() + " " + message );
		}
	}

	public int doGitPull( Path repo ) throws IOException, GitAPIException {
		PullResult result = Git
			.open( repo.toFile() )
			.pull()
			.setCredentialsProvider( new UsernamePasswordCredentialsProvider( config.get( "username" ), config.get( "password" ) ) )
			.call();
		MergeResult.MergeStatus status = result.getMergeResult().getMergeStatus();
		return status == MergeResult.MergeStatus.ALREADY_UP_TO_DATE ? 0 : 1;
	}

	public int doGitClone( Path repo, String uri ) throws IOException, GitAPIException {
		Files.createDirectories( repo );
		Git
			.cloneRepository()
			.setURI( uri )
			.setDirectory( repo.toFile() )
			.setCredentialsProvider( new UsernamePasswordCredentialsProvider( config.get( "username" ), config.get( "password" ) ) )
			.call();
		return 0;
	}

	protected RepoClientConfig getConfig() {
		return config;
	}

	protected UriTemplate getUriTemplate() {
		UriTemplate template = null;
		if( getConfig().exists( "uri" ) ) template = new UriTemplate( getConfig().get( "uri" ) );
		if( template == null ) template = new UriTemplate( getConfig().get( getConfig().get( "type" ) + "-default-uri" ) );
		return template;
	}

	protected RestTemplate getRest() {
		return rest;
	}

}
