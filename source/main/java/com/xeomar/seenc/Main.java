package com.xeomar.seenc;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * The main class for Nomos.
 */
public class Main {

	public static final String CONFIG_DEFAULT = "/default.properties";

	private static final Logger log = LoggerFactory.getLogger( Main.class );

	private String provider;

	private String product;

	private String version;

	private int inceptionYear;

	public static void main( String[] commands ) {
		try {
			new Main().run( commands );
		} catch( Exception exception ) {
			log.error( "Error running Reposync", exception );
		}
	}

	public void run( String[] commands ) {
		describe();

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

		processRepositories( configure( properties ) );
	}

	private void processRepositories( BitbucketConfig config ) {
		BitbucketClient client = new BitbucketClient( config );

		log.info( "Requesting repositories for " + config.getTeam() + "..." );

		Set<GitRepo> repos = client.getBitbucketRepos();
		log.info( "Repository count: " + repos.size() );

		List<GitRepo> sortedRepos = new ArrayList<>( repos );
		Collections.sort( sortedRepos );

		for( GitRepo repo : sortedRepos ) {
			Path localPath = repo.getLocalPath();
			String message = repo + ": " + localPath.toAbsolutePath();
			boolean exists = Files.exists( localPath );
			try {
				GitResult result;
				if( exists ) {
					result = client.doGitPull( localPath ) == 0 ? GitResult.PULL_UP_TO_DATE : GitResult.PULL_UPDATES;
				} else {
					client.doGitClone( localPath, repo.getRemote() );
					result = GitResult.CLONE_SUCCESS;
				}

				log.info( result.getSymbol() + " " + message );
			} catch( Exception exception ) {
				log.error( "X " + message, exception );
			}
		}
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
			log.error( "Unable to load properties from resource: " + CONFIG_DEFAULT, exception );
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
		} catch( FileNotFoundException exception ) {
			throw new RuntimeException( "Missing config file: " + config, exception );
		} catch( IOException exception ) {
			log.error( "Unable to load config file: " + config, exception );
		}
	}

	private BitbucketConfig configure( Map<String, String> properties ) {
		BitbucketConfig config = new BitbucketConfig();

		config.setUsername( properties.get( "bitbucket-username" ) );
		config.setPassword( properties.get( "bitbucket-password" ) );
		config.setTeam( properties.get( "bitbucket-team" ) );

		config.setRepoUri( properties.get( "bitbucket-rest-repo-uri" ) );

		config.setProtocol( properties.get( "bitbucket-git-protocol" ) );

		config.setTarget( properties.get( "target" ) );

		return config;
	}

	private void describe() {
		try {
			InputStream input = getClass().getResourceAsStream( "/product.properties" );
			Properties properties = new Properties();
			properties.load( new InputStreamReader( input, "utf-8" ) );
			provider = properties.getProperty( "provider" );
			product = properties.getProperty( "product" );
			version = properties.getProperty( "version" );
			inceptionYear = Integer.parseInt( properties.getProperty( "year" ) );
		} catch( Exception exception ) {
			log.error( "Error loading product description", exception );
		}
	}

	private void printHeader() {
		System.out.println( provider + " " + product + " " + version );
		System.out.println( "Copyright " + inceptionYear + " " + provider );
	}

	private void printHelp() {
		StringWriter writer = new StringWriter();
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( new PrintWriter( writer ), formatter.getWidth(), "seenc [OPTION]...", (String)null, getOptions(), formatter.getLeftPadding(), formatter.getDescPadding(), (String)null, false );

		System.out.println( "" );
		System.out.println( writer.toString() );
	}

}
