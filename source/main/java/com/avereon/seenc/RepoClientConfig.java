package com.avereon.seenc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepoClientConfig {

	private final Map<String, String> values;

	public RepoClientConfig( Map<String, String> values ) {
		this.values = new HashMap<>( values );
	}

	public boolean exists( String key ) {
		return !"".equals( get( key ) );
	}

	public String get( String key ) {
		return getAll( key ).stream().findFirst().orElse( "" );
	}

	@SuppressWarnings( { "unchecked", "ConstantConditions" } )
	public List<String> getAll( String key ) {
		Object value = values.get( key );
		if( value == null ) {
			return List.of();
		} else if( value instanceof String ) {
			return List.of( String.valueOf( value ) );
		} else {
			return (List<String>)value;
		}
	}

	Map<String, String> getMap() {
		return values;
	}

}
