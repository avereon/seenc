package com.parallelsymmetry.reposync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.cli.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * The main class for Reposync.
 */
public class Main {

	public static final String CONFIG_DEFAULT = "/config.default";

	private static final Logger log = LoggerFactory.getLogger( com.parallelsymmetry.reposync.Main.class );

	private BitBucketConfig config;

	private RestTemplate rest;

	public static final void main( String[] commands ) {
		new Main().run( commands );
	}

	public void run( String[] commands ) {
		// Print the program header
		printHeader();

		// Print the program help
		if( commands.length == 0 ) {
			printHelp();
			return;
		}

		// Process the command line parameters
		CommandLine cli = null;
		try {
			cli = new DefaultParser().parse( getOptions(), commands, false );
		} catch( ParseException exception ) {
			log.error( "Unable to parse command line parameters", exception );
		}

		// Load the configuration
		Map<String, String> properties = new HashMap<>();
		// Load defaults from resource
		loadPropertiesFromResource( properties );
		// Load config from file
		loadPropertiesFromConfig( cli.getOptionValue( "config" ), properties );
		// Load properties from command line
		loadPropertiesFromCli( cli, properties );
		config = configure( properties );

		// Set up REST template
		rest = new RestTemplate();
		rest.getInterceptors().add( new BasicAuthorizationInterceptor( config.getUsername(), config.getPassword() ) );

		processProjects();
	}

	private void processProjects() {
		log.info( "Requesting repositories for " + config.getTeam() + "..." );
		JsonNode repos = getProjectRepos( config.getTeam() );

		for( JsonNode repo : repos ) {
			String repoName = repo.get( "name" ).asText().toLowerCase();
			String projectName = repo.get( "project" ).get( "name" ).asText().toLowerCase();

			UriTemplate targetUri = new UriTemplate( config.getTarget() );
			Path targetPath = Paths.get( targetUri.expand( projectName, repoName ) );

			boolean exists = Files.exists( targetPath );

			try {
				if( exists ) {
					// Update
					int result = doGitPull( targetPath );
					if( result == 0 ) {
						log.info( "o " + projectName + "/" + repoName + ": " + targetPath.toAbsolutePath() );
					} else {
						log.warn( "! " + projectName + "/" + repoName + ": " + targetPath.toAbsolutePath() );
					}
				} else {
					// Clone
					int result = doGitClone( targetPath, getCloneUri( repo ) );
					if( result == 0 ) {
						log.info( "+ " + projectName + "/" + repoName + ": " + targetPath.toAbsolutePath() );
					} else {
						log.warn( "! " + projectName + "/" + repoName + ": " + targetPath.toAbsolutePath() );
					}
				}
			} catch( Exception exception ) {
				log.error("Unable to process " + projectName + "/" + repoName, exception );
			}
		}
	}

	private int doGitPull( Path repo ) throws IOException, GitAPIException {
		try {
			Git.open( repo.toFile() ).pull().setCredentialsProvider( new UsernamePasswordCredentialsProvider( config.getUsername(), config.getPassword() ) ).call();
			return 0;
		} catch( CheckoutConflictException cce ) {
			return -1;
		}
	}

	private int doGitClone( Path repo, String uri ) throws IOException, GitAPIException {
		Files.createDirectories( repo );
		Git.cloneRepository().setURI( uri ).setDirectory( repo.toFile() ).setCredentialsProvider( new UsernamePasswordCredentialsProvider( config.getUsername(), config.getPassword() ) ).call();
		return 0;
	}

	private JsonNode getProjectRepos( String username ) {
		try {
			UriTemplate repoUri = new UriTemplate( config.getRepoUri() );
			ObjectNode node = rest.getForObject( repoUri.expand( username ), ObjectNode.class );
			return node.get( "values" );
		} catch( Exception exception ) {
			log.error( "Unable to retrieve project repository list", exception );
			return JsonNodeFactory.instance.objectNode();
		}
	}

	private String getCloneUri( JsonNode repo ) {
		String protocol = config.getProtocol().toLowerCase();
		for( JsonNode clone : repo.get( "links" ).get( "clone" ) ) {
			if( clone.get( "name" ).asText().toLowerCase().equals( protocol ) ) {
				return clone.get( "href" ).asText();
			}
		}
		return null;
	}

	protected Options getOptions() {
		Options options = new Options();
		options.addOption( Option.builder( "c" ).longOpt( "config" ).numberOfArgs( 1 ).argName( "file" ).build() );

		options.addOption( Option.builder().longOpt( "projects" ).numberOfArgs( 1 ).argName( "project list" ).build() );
		options.addOption( Option.builder().longOpt( "target" ).numberOfArgs( 1 ).argName( "folder" ).build() );

		options.addOption( Option.builder().longOpt( "bitbucket-username" ).numberOfArgs( 1 ).argName( "username" ).build() );
		options.addOption( Option.builder().longOpt( "bitbucket-password" ).numberOfArgs( 1 ).argName( "password" ).build() );
		options.addOption( Option.builder().longOpt( "bitbucket-team" ).numberOfArgs( 1 ).argName( "team" ).build() );

		options.addOption( Option.builder().longOpt( "bitbucket-rest-repo-uri" ).numberOfArgs( 1 ).argName( "uri" ).build() );

		options.addOption( Option.builder().longOpt( "bitbucket-git-protocol" ).numberOfArgs( 1 ).argName( "protocol" ).build() );

		return options;
	}

	private void loadPropertiesFromResource( Map<String, String> properties ) {
		Properties resourceProperties = new Properties();
		try( InputStream input = getClass().getResourceAsStream( CONFIG_DEFAULT ) ) {
			resourceProperties.load( input );
		} catch( IOException exception ) {
			log.error("Unable to load properties from resource: " + CONFIG_DEFAULT, exception );
		}
		for( Object keyObject : resourceProperties.keySet() ) {
			String key = keyObject.toString();
			properties.put( key, resourceProperties.getProperty( key ) );
		}
	}

	private void loadPropertiesFromCli( CommandLine cli, Map<String, String> properties ) {
		for( Option option : cli.getOptions() ) {
			String key = option.getLongOpt();
			if( key != null ) properties.put( key, option.getValue() );
		}
	}

	private void loadPropertiesFromConfig( String config, Map<String, String> properties ) {
		if( config == null ) return;

//		File file = new File( config );
//		if( !file.isAbsolute() ) file = new File( System.getProperty( "user.dir"), config );

		try( FileInputStream input = new FileInputStream( new File( config ) ) ) {
			Properties configProperties = new Properties();
			configProperties.load( input );
			for( Object keyObject : configProperties.keySet() ) {
				String key = keyObject.toString();
				properties.put( key, configProperties.getProperty( key ) );
			}
		} catch( IOException exception ) {
			log.error("Unable to load properties from config file: " + config, exception );
		}
	}

	private BitBucketConfig configure( Map<String, String> properties ) {
		BitBucketConfig config = new BitBucketConfig();

		config.setUsername( properties.get( "bitbucket-username" ) );
		config.setPassword( properties.get( "bitbucket-password" ) );
		config.setTeam( properties.get( "bitbucket-team" ) );

		config.setRepoUri( properties.get( "bitbucket-rest-repo-uri" ) );

		config.setProtocol( properties.get( "bitbucket-git-protocol" ) );

		config.setTarget( properties.get( "target" ) );

		return config;
	}

	private void printHeader() {
		System.out.println( "Parallel Symmetry Reposync 1.0" );
		System.out.println( "Copyright 2016 Parallel Symmetry" );
	}

	private void printHelp() {
		StringWriter writer = new StringWriter();
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( new PrintWriter( writer ), formatter.getWidth(), "reposync [OPTION]...", (String)null, getOptions(), formatter.getLeftPadding(), formatter.getDescPadding(), (String)null, false );

		System.out.println( "" );
		System.out.println( writer.toString() );
	}

}
