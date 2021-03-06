[build-status]: https://github.com/avereon/seenc/workflows/Avereon%20Seenc%20CI/badge.svg "Build status"

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
Seenc parameters can be provided as a configuration file in Java properties 
format:

```
type=GH3
orgs=myorg
username=myusername
password=mypassword
target=file:///home/myhome/code/github/{repo}
```

## Configuration Parameters
 * type - The repository type key
 * uri - The default provider URI override (can use variables)
 * username - The username if required
 * password - The password if required
 * target - The local file system target (can use variables)

## Repository Parameters
 * orgs - The GitHub v3 organization names as seen in the URL
 * teams - The Bitbucket v2 team names as seen in the URL
 * projects - The Bitbucket v1 project names as seen in the URL
 * include - The list of specific projects to include. If not specified, all 
 projects will be included.
 * exclude - The list of specific projects to exclude. If not specified, does 
 exclude any projects. 

## Variables
Any of the configuration or repository parameters can be used as replacement 
variables in other parameters. Nested parameters are not supported. Other
parameters that are available are:
 * repo - The repository name
 * project - The project name (Bitbucket only)
