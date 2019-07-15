package com.xeomar.seenc;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public abstract class RepoClient {

	private BitbucketConfig config;

	protected RepoClient( BitbucketConfig config ) {
		this.config = config;
	}

	public abstract Set<GitRepo> getRepos();

	public int doGitPull( Path repo ) throws IOException, GitAPIException {
		PullResult result = Git
			.open( repo.toFile() )
			.pull()
			.setCredentialsProvider( new UsernamePasswordCredentialsProvider( config.getUsername(), config.getPassword() ) )
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
			.setCredentialsProvider( new UsernamePasswordCredentialsProvider( config.getUsername(), config.getPassword() ) )
			.call();
		return 0;
	}

	protected BitbucketConfig getConfig() {
		return config;
	}

}
