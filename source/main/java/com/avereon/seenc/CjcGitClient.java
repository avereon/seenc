package com.avereon.seenc;

import java.util.Set;

public class CjcGitClient extends RepoClient {

	protected CjcGitClient( RepoClientConfig config ) {
		super( config );
	}

	@Override
	public Set<GitRepo> getRepos() {
		return Set.of();
	}

	@Override
	public void processRepositories() {

	}

}
