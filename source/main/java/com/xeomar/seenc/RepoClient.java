package com.xeomar.seenc;

import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Path;

public interface RepoClient {

	int doGitClone( Path repo, String uri ) throws IOException, GitAPIException;

	int doGitPull( Path repo ) throws IOException, GitAPIException;

}
