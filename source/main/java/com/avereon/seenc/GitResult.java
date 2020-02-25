package com.avereon.seenc;

public enum GitResult {

	CLONE_SUCCESS( '+' ),
	PULL_UPDATED( '>' ),
	PULL_UP_TO_DATE( '=' ),
	MISSING( '-' ),
	ERROR( 'x' );

	private char symbol;

	GitResult( char symbol ) {
		this.symbol = symbol;
	}

	char getSymbol() {
		return symbol;
	}

}
