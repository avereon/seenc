package com.avereon.seenc;

import java.util.HashMap;
import java.util.Map;

public class RepoClientConfig {

	private Map<String, String> values;

	public RepoClientConfig( Map<String, String> values ) {
		this.values = new HashMap<>( values );
	}

	public String get( String key ) {
		return values.getOrDefault( key, "" );
	}

}
