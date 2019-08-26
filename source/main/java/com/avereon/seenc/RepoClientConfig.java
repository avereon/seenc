package com.avereon.seenc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RepoClientConfig {

	private Map<String, String> values;

	public RepoClientConfig( Map<String, String> values ) {
		this.values = new HashMap<>( values );
	}

	public boolean exists( String key ) {
		return !"".equals( get( key ) );
	}

	public String get( String key ) {
		return values.getOrDefault( key, "" ).trim();
	}

	public List<String> getAll( String key ) {
		return Arrays.stream( get( key ).split( "," ) ).map( String::trim ).filter( ( s ) -> !"".equals( s ) ).collect( Collectors.toList() );
	}

	Map<String, String> getMap() {
		return values;
	}

}
