[build-status]: https://github.com/avereon/seenc/actions/workflows/ci.yml/badge.svg "Build status"

# Seenc ![alt text][build-status]

Clone or pull Git repositories from providers like GitHub, BitBucket, etc. 

The motivation for the project was to have command line tool to synchronize
all repositories for a user, team, account or other group defined by a provider. 
This allows the user to get up-to-date in one step instead of having to update 
each repository individually. This is especially helpful cloning initial copies 
of all repositories in the group when setting up a new computer.

## Repository Types

| Key | Provider  | API | Parameters |
|-----|-----------|:----|------------|
| GH3 | GitHub    | v3  | orgs       |
| BB2 | Bitbucket | 2.0 | teams      |
| BB1 | Bitbucket | 1.0 | projects   |

## Configuration Files

### Config File
Seenc configurations are provided in one or more JSON config files. The file 
provides the information required to connect to a Git provider, the repositories
to clone or pull, and the target of the repository. Authentication is provided 
in a separate location to allow configuration files to be safely stored in 
source control. 

For example, here is the config file to get the `seenc` repository: 

```
[
  {
    "id":"avn",
    "name":"Avereon",
    "type":"GH3",
    "orgs":"avereon",
    "include":["seenc"],
    "target":"/home/ecco/Data/avn/code/resource/{repo}"
  }
]
```

### Auth File
Seenc authentication information is stored in one or more JSON config files. The
file provides the authentication information to connect to each configuration 
id. The authentication files should NOT be stored in source control.

For example, here is a mock authentication file for the `avn` config:

```
[
  {
    "id":"avn",
    "username":"githubuser",
    "password":"ghp_notarealgithubpersonalaccesstoken"
  }
]
```

### Configuration Parameters
 * type - The repository type key
 * uri - The default provider URI override (can use variables)
 * username - The username if required
 * password - The password if required
 * target - The local file system target (can use variables)

### Repository Parameters
 * orgs - The GitHub v3 organization names as seen in the URL
 * teams - The Bitbucket v2 team names as seen in the URL
 * projects - The Bitbucket v1 project names as seen in the URL
 * include - The list of specific projects to include. If not specified, all 
 projects will be included.
 * exclude - The list of specific projects to exclude. If not specified, does 
 exclude any projects. 

### Variables
Any of the configuration or repository parameters can be used as replacement 
variables in other parameters. Nested parameters are not supported. Other
parameters that are available are:
 * repo - The repository name
 * project - The project name (Bitbucket only)
