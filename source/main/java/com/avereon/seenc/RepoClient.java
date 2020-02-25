package com.avereon.seenc;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class RepoClient {

	private static final Logger log = LoggerFactory.getLogger( RepoClient.class );

	private RepoClientConfig config;

	private RestTemplate rest;

	protected RepoClient( RepoClientConfig config ) {
		this.config = config;
		rest = new RestTemplate();
		rest.getInterceptors().add( new BasicAuthorizationInterceptor( config.get( "username" ), config.get( "password" ) ) );
	}

	public abstract Set<GitRepo> getRepos();

	public void processRepositories() {
		List<String> include = getConfig().getAll( "include" );
		List<String> exclude = getConfig().getAll( "exclude" );

		List<GitRepo> repos = new ArrayList<>( getRepos() )
			.stream()
			.filter( ( repo ) -> include.size() == 0 || include.contains( repo.getName() ) )
			.filter( ( repo ) -> !exclude.contains( repo.getName() ) )
			.sorted()
			.collect( Collectors.toList() );

		System.out.println( "Initializing..." );
		int[] counts = getCounts( repos );
		System.out.println( "Cloning " + counts[ 0 ] + " branches from " + counts[ 1 ] + " repositories and updating " + counts[ 2 ] + " branches in " + counts[ 3 ] + " repositories" );
		processRepos( repos );
	}

	private int[] getCounts( List<GitRepo> repos ) {
		int[] result = new int[ 4 ];
		for( GitRepo repo : repos ) {
			Path localPath = repo.getLocalPath();
			boolean exists = Files.exists( localPath );
			if( exists ) {
				try {
					Git git = Git.open( localPath.toFile() );
					List<Ref> branches = git.branchList().call();
					result[ 2 ] += branches.size();
					result[ 3 ]++;
				} catch( Exception exception ) {
					System.err.println( exception.getMessage() );
				}
			} else {
				try {
					Collection<Ref> heads = Git.lsRemoteRepository().setRemote( repo.getRemote() ).setHeads( true ).call();
					result[ 0 ] += heads.size();
					result[ 1 ]++;
				} catch( Exception exception ) {
					System.err.println( exception.getMessage() );
				}
			}
		}
		return result;
	}

	private void processRepos( List<GitRepo> repos ) {
		for( GitRepo repo : repos ) {
			Path localPath = repo.getLocalPath();
			boolean exists = Files.exists( localPath );
			if( exists ) {
				try {
					Git git = Git.open( localPath.toFile() );

					// Get the current branch
					String currentBranch = git.getRepository().getBranch();

					List<Ref> branches = git.branchList().call();
					for( Ref branch : branches ) {
						try {
							git.checkout().setName( branch.getName() ).call();
							int result = doGitPull( localPath );
							printResult( repo, branch, result == 0 ? GitResult.PULL_UP_TO_DATE : GitResult.PULL_UPDATED );
						} catch( RefNotAdvertisedException exception ) {
							if( log.isDebugEnabled() ) printResult( repo, branch, GitResult.MISSING );
						} catch( Exception exception ) {
							printResult( repo, branch, GitResult.ERROR, exception );
						}
					}

					// Go back to the current branch
					git.checkout().setName( currentBranch ).call();
				} catch( RefNotFoundException exception ) {
					if( log.isDebugEnabled() ) printResult( repo, GitResult.MISSING );
				} catch( Exception exception ) {
					System.err.println( "Outer Exception: " + exception.getClass().getSimpleName() );
					printResult( repo, GitResult.ERROR, exception );
				}
			} else {
				try {
					int result = doGitClone( localPath, repo.getRemote() );
					printResult( repo, result == 0 ? GitResult.CLONE_SUCCESS : GitResult.ERROR );
				} catch( Exception exception ) {
					printResult( repo, GitResult.ERROR, exception );
				}
			}
		}
	}

	private void printResult( GitRepo repo, GitResult result ) {
		printResult( repo, null, result );
	}

	private void printResult( GitRepo repo, Ref branch, GitResult result ) {
		printResult( repo, branch, result, null );
	}

	private void printResult( GitRepo repo, GitResult result, Exception exception ) {
		printResult( repo, null, result, exception );
	}

	private void printResult( GitRepo repo, Ref branch, GitResult result, Exception exception ) {
		String message = repo + ": " + repo.getLocalPath().toAbsolutePath();
		if( branch != null ) message += ":" + branch.getName();
		if( exception != null ) message += ": " + exception.getMessage();

		if( result == GitResult.ERROR ) {
			System.err.println( result.getSymbol() + " " + message );
		} else {
			System.out.println( result.getSymbol() + " " + message );
		}
	}

	public int doGitPull( Path repo ) throws IOException, GitAPIException {
		PullResult result = Git
			.open( repo.toFile() )
			.pull()
			.setCredentialsProvider( new UsernamePasswordCredentialsProvider( config.get( "username" ), config.get( "password" ) ) )
			.call();
		MergeResult.MergeStatus status = result.getMergeResult().getMergeStatus();
		return status == MergeResult.MergeStatus.ALREADY_UP_TO_DATE ? 0 : 1;
	}

	public int doGitClone( Path repo, String uri ) throws IOException, GitAPIException {
		Files.createDirectories( repo );
		Git
			.cloneRepository()
			.setURI( uri )
			.setDirectory( repo.toFile() )
			.setCredentialsProvider( new UsernamePasswordCredentialsProvider( config.get( "username" ), config.get( "password" ) ) )
			.call();
		return 0;
	}

	protected RepoClientConfig getConfig() {
		return config;
	}

	protected RestTemplate getRest() {
		return rest;
	}

	protected UriTemplate getUriTemplate( String path ) {
		String endpoint = null;
		if( getConfig().exists( "uri" ) ) endpoint = getConfig().get( "uri" );
		if( endpoint == null ) endpoint = getConfig().get( getConfig().get( "type" ) + "-default-uri" );
		return new UriTemplate( endpoint + path );
	}

}
