package com.avereon.seenc;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

/**
 * The GitRepo class represents a Git repository.
 */
@Setter
@Getter
public class GitRepo implements Comparable<GitRepo> {

	private String project;

	private String name;

	private String remote;

	private Path localPath;

    @Override
	public int hashCode() {
		return remote.hashCode();
	}

	@Override
	public boolean equals( Object object ) {
		if( !(object instanceof GitRepo that)) return false;
        return this.remote.equals( that.remote );
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if( getProject() != null ) builder.append( getProject() ).append( "/" );
		builder.append( getName() );
		builder.append( " remote=" ).append( remote );
		builder.append( " target=" ).append( localPath );
		return builder.toString();
	}

	@Override
	public int compareTo( GitRepo that ) {
		return this.toString().compareTo( that.toString() );
	}

}
