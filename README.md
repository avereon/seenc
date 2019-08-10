[build-status]: https://travis-ci.org/avereon/seenc.svg?branch=master "Build status"

# Seenc ![alt text][build-status]

Clone or pull repositories from Git providers like GitHub, BitBucket, etc. 

The motivation for the project was to have command line tool to synchronize
all repositories for a user, team, account or other group defined by a provider. 
This allows the user to get up-to-date in one step instead of having to update 
each repository individually. This is especially helpful cloning initial copies 
of all repositories in the group when setting up a new computer.

## Repository Types

| Key | Provider  | API | Parameters |
|-----|-----------|:----|------------|
| GH3 | GitHub    | v3  | org        |
| BB1 | Bitbucket | 1.0 | project    |
| BB2 | Bitbucket | 2.0 | account    |

## Configuration Files
Seenc parameters can be provided as a configuration file in Java properties 
format:

```
type=GH3
org=myorg
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
 * org - The GitHub organization name as seen in the URL
 * account - The Bitbucket account name as seen in the URL
 * project - The Bitbucket project name as seen in the URL

## Variables
Any of the configuration or repository parameters can be used as replacement 
variables in other parameters. Nested parameters are not supported. Other
parameters that are available are:
 * repo - The repository name