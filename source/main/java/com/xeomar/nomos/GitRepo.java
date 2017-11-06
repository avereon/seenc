package com.xeomar.nomos;

import java.nio.file.Path;

/**
 * The GitRepo class represents a Git repository.
 */
public class GitRepo implements Comparable<GitRepo> {

	private String project;

	private String name;

	private String remote;

	private Path localPath;

	public String getProject() {
		return project;
	}

	public void setProject( String project ) {
		this.project = project;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public String getRemote() {
		return remote;
	}

	public void setRemote( String remote ) {
		this.remote = remote;
	}

	public Path getLocalPath() {
		return localPath;
	}

	public void setLocalPath( Path localPath ) {
		this.localPath = localPath;
	}

	@Override
	public String toString() {
		return getProject() + "/" + getName();
	}

	@Override
	public int compareTo( GitRepo that ) {
		return this.toString().compareTo( that.toString() );
	}

}
