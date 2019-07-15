package com.xeomar.seenc;

public class RepoClientFactory {

	public static RepoClient getRepoClient( BitbucketConfig config ) {
		// FIXME Determine client implementation from configuration
		return new BitbucketClient( config );
	}

}
