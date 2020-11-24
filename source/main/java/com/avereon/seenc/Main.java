package com.avereon.seenc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The main class.
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

		//		// Load the default configuration
		//		Map<String, String> properties = new HashMap<>( loadPropertiesFromResource() );
		//
		//		// Process the command line parameters
		//		try {
		//			CommandLine cli = new DefaultParser().parse( getOptions(), commands, false );
		//			// Load config from file
		//			properties.putAll( loadPropertiesFromConfig( cli.getOptionValue( "config" ) ) );
		//			// Load properties from command line
		//			properties.putAll( loadPropertiesFromCli( cli ) );
		//		} catch( ParseException exception ) {
		//			log.error( "Unable to parse command line parameters", exception );
		//		}
		//
		//		// Determine repository client
		//		RepoClient client = RepoClientFactory.getRepoClient( new RepoClientConfig( properties ) );
		//		if( client == null ) {
		//			log.error( "Unable to configure client" );
		//			return;
		//		}
		//
		//		client.processRepositories();


		try {
			CommandLine cli = new DefaultParser().parse( getOptions(), commands, false );
			List<Map<String, String>> configs = loadConfigs( cli.getOptionValue( "config" ) );
			//System.out.println( "Config count=" + configs.size() );
			System.out.println( "Initializing..." );
			Map<String, String> defaults = new HashMap<>( loadPropertiesFromResource() );
			for( Map<String, String> config : configs ) {
				config.putAll( defaults );
				RepoClient client = RepoClientFactory.getRepoClient( new RepoClientConfig( config ) );
				if( client != null ) {
					client.processRepositories();
				} else {
					log.error( "Unable to configure client" );
				}
			}
		} catch( ParseException exception ) {
			log.error( "Unable to parse command line parameters", exception );
		}

	}

	protected Options getOptions() {
		Options options = new Options();
		options.addOption( Option.builder( "c" ).longOpt( "config" ).numberOfArgs( 1 ).argName( "file" ).build() );

		//options.addOption( Option.builder().longOpt( "projects" ).numberOfArgs( 1 ).argName( "project list" ).build() );
		options.addOption( Option.builder().longOpt( "target" ).numberOfArgs( 1 ).argName( "folder" ).build() );

		options.addOption( Option.builder().longOpt( "username" ).numberOfArgs( 1 ).argName( "username" ).build() );
		options.addOption( Option.builder().longOpt( "password" ).numberOfArgs( 1 ).argName( "password" ).build() );
		options.addOption( Option.builder().longOpt( "team" ).numberOfArgs( 1 ).argName( "team" ).build() );
		options.addOption( Option.builder().longOpt( "type" ).numberOfArgs( 1 ).argName( "type" ).build() );
		options.addOption( Option.builder().longOpt( "uri" ).numberOfArgs( 1 ).argName( "uri" ).build() );
		//options.addOption( Option.builder().longOpt( "git-protocol" ).numberOfArgs( 1 ).argName( "protocol" ).build() );

		return options;
	}

	@SuppressWarnings( "unchecked" )
	private List<Map<String, String>> loadConfigs( String config ) {
		List<Map<String, String>> configs = new ArrayList<>();

		try( FileInputStream input = new FileInputStream( new File( config ) ) ) {
			configs = new ObjectMapper().readValue( input, configs.getClass() );
		} catch( FileNotFoundException exception ) {
			throw new RuntimeException( "Missing config file: " + config, exception );
		} catch( IOException exception ) {
			log.error( "Unable to load config file: " + config, exception );
		}

		return configs;
	}

	private Map<String, String> loadPropertiesFromResource() {
		Properties resourceProperties = new Properties();
		try( InputStream input = getClass().getResourceAsStream( CONFIG_DEFAULT ) ) {
			resourceProperties.load( input );
		} catch( IOException exception ) {
			log.error( "Unable to load properties from resource: " + CONFIG_DEFAULT, exception );
		}

		Map<String, String> properties = new HashMap<>();
		for( Object keyObject : resourceProperties.keySet() ) {
			String key = keyObject.toString();
			properties.put( key, resourceProperties.getProperty( key ) );
		}

		return properties;
	}

	private Map<String, String> loadPropertiesFromCli( CommandLine cli ) {
		Map<String, String> properties = new HashMap<>();
		for( Option option : cli.getOptions() ) {
			String key = option.getLongOpt();
			if( key != null ) properties.put( key, option.getValue() );
		}

		return properties;
	}

	private Map<String, String> loadPropertiesFromConfig( String config ) {
		if( config == null ) return Map.of();

		Map<String, String> properties = new HashMap<>();
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

		return properties;
	}

	//	private BitbucketConfig configure( Map<String, String> properties ) {
	//		BitbucketConfig config = new BitbucketConfig();
	//
	//		config.setRepoUri( properties.get( "bitbucket-rest-repo-uri" ) );
	//		config.setTeam( properties.get( "bitbucket-team" ) );
	//		config.setUsername( properties.get( "bitbucket-username" ) );
	//		config.setPassword( properties.get( "bitbucket-password" ) );
	//		config.setTarget( properties.get( "target" ) );
	//
	//		return config;
	//	}

	private void describe() {
		try {
			InputStream input = getClass().getResourceAsStream( "/META-INF/product.info" );
			Properties properties = new Properties();
			properties.load( new InputStreamReader( input, StandardCharsets.UTF_8 ) );
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
		StringBuilder examples = new StringBuilder();
		examples.append( "\n" );
		examples.append( "Repos: avn, fab, mvs, psm, sbc, sod\n" );
		examples.append( "\n" );
		examples.append( "Config file example contents:\n" );
		examples.append( "  type=BB\n" );
		examples.append( "  team=avereon\n" );
		examples.append( "  username=<username>\n" );
		examples.append( "  password=<password>\n" );
		examples.append( "  target=file:/home/ecco/Data/avn/code/{project}/{repo}\n" );

		StringWriter writer = new StringWriter();
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(
			new PrintWriter( writer ),
			formatter.getWidth(),
			"seenc [OPTION]... <repo>",
			null,
			getOptions(),
			formatter.getLeftPadding(),
			formatter.getDescPadding(),
			examples.toString(),
			false
		);

		System.out.println();
		System.out.println( writer.toString() );
	}

}
