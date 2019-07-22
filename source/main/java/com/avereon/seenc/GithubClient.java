package com.avereon.seenc;

import java.util.Set;

public class GithubClient extends RepoClient {

	protected GithubClient( RepoClientConfig config ) {
		super( config );
	}

	@Override
	public Set<GitRepo> getRepos() {
		return null;
	}

	@Override
	public void processRepositories() {

	}

}
