# Portal synchronization with GitHub

This project provides synchronization of Anypoint account with GitHub by means of Graddle script. Tasks of this script are supposed to be launched inside Jenkins items.

## Locating RAML projects on GitHub

RAML projects are supposed to be stored in separate repositories inside an organization. Those APIs which are actually parts of some major API are allowed to form a single RAML project (with multiple main RAML files) contained in a single repository.

## Gradle tasks and Jenkins jobs

### syncAPI task
This task is supposed to be utilized by a job which transfers changes from a single repository to a single portal. The job must list all the repositories, and each repository must be given a Jenkins webhook. 

### syncAllAPIa task
This task is supposed to be utilized by a job which transfers changes from all the repositories (despite whether the hava a webhook or not) to anypoint account.

### cleanPortal task
This task is supposed to be utilized by a job which removes all the portals of your Anypoint account.

## Required environmental variables

All the tasks require following environmental variables:
- GITHUBLOGIN: your GitHub login
- GITHUBPASS: your GitHub password
- PORTALLOGIN: your AnypointPortal login
- PORTALPASSWORD: your AnypointPortal password

The 'syncALLAPIs' task also expects the following parameter: 
- GITHUB_ORGANIZATION: name of the organization which contains all the repositories with RAML projects to be synchronized