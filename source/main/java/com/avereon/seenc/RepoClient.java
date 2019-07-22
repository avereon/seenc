package com.avereon.seenc;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	protected RepoClient( RepoClientConfig config ) {
		this.config = config;
	}

	public abstract Set<GitRepo> getRepos();

	public void processRepositories(){
		Set<GitRepo> repos = getRepos();
		log.info( "Repository count: " + repos.size() );

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
			log.info( result.getSymbol() + " " + message );
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

}
