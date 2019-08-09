package com.avereon.seenc;

public class RepoClientFactory {

	public static RepoClient getRepoClient( RepoClientConfig config ) {
		String clientType = config.get( "type" );

		switch( clientType ) {
			case "BB1" : {
				return new Bitbucket1Client( config );
			}
			case "BB2" : {
				return new Bitbucket2Client( config );
			}
			case "GH3" : {
				return new Github3Client( config );
			}
		}

		return null;
	}

}
