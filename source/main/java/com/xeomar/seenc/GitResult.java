package com.xeomar.seenc;

public enum GitResult {

	CLONE_SUCCESS( '+' ), PULL_UPDATES( 'o' ), PULL_UP_TO_DATE( '-' );

	private char symbol;

	GitResult( char symbol ) {
		this.symbol = symbol;
	}

	char getSymbol() {
		return symbol;
	}

}
