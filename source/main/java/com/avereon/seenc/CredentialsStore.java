package com.avereon.seenc;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class CredentialsStore {

	Set<URI> uris;

	public CredentialsStore() {
		this( System.getProperty( "user.home" ) + File.separator + ".git-credentials" );
	}

	public CredentialsStore( String path ) {
		uris = new CopyOnWriteArraySet<>();
		try {
			Files.lines( Paths.get( path ) ).forEach( l -> uris.add( URI.create( l ) ) );
		} catch( Exception exception ) {
			exception.printStackTrace( System.err );
		}
	}

	public String getUsername( String host ) {
		return getUri( host )[0];
	}

	public String getPassword( String host ) {
		return getUri( host )[1];
	}

	private String[] getUri( String host ) {
		URI uri = uris.stream().filter( u -> host.endsWith( u.getHost() ) ).findFirst().orElse( null );
		if( uri == null ) return new String[] {null, null};
		return uri.getUserInfo().split( ":" );
	}

}
