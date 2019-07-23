package com.avereon.seenc;

public class RepoClientFactory {

	public static RepoClient getRepoClient( RepoClientConfig config ) {
		String clientType = config.get( "type" );

		switch( clientType ) {
			case "BB" : {
				return new BitbucketClient( config );
			}
			case "CJC" : {
				return new CjcGitClient( config );
			}
			case "GH" : {
				return new GithubClient( config );
			}
		}

		return null;
	}

}
