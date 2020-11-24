package com.avereon.seenc;

import java.net.URI;

public class Uris {

	public static String getUsername( String uri ) {
		return getUsername( URI.create( uri ) );
	}

	public static String getUsername( URI uri ) {
		if( uri.getUserInfo() == null ) return null;
		return uri.getUserInfo().split( ":" )[ 0 ];
	}

	public static String getPassword( String uri ) {
		return getPassword( URI.create( uri ) );
	}

	public static String getPassword( URI uri ) {
		if( uri.getUserInfo() == null ) return null;
		return uri.getUserInfo().split( ":" )[ 1 ];
	}

}
