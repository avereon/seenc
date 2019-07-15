package com.xeomar.seenc;

import java.util.Set;

public class CjcGitClient extends RepoClient {

	protected CjcGitClient( BitbucketConfig config ) {
		super( config );
	}

	@Override
	public Set<GitRepo> getRepos() {
		return Set.of();
	}

}
