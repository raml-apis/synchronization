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
 
## Reference tags
Sharing portals between organizations or deploying new portal versions breaks links consistency. In order to avoid this effect you may use reference tags. These tags are recognised by the synchronization tool which replaces them by correct links in portal.

Following reference tags are available now:

- `#REF_TAG_API_REFERENCE` link to the 'API reference' page
- `#REF_TAG_DEFENITION` link to the API defenition utilized by `API.create()` method inside notebooks
- `#REF_TAG_ROOT_RAML` link to root RAML file
- `#REF_TAG_ABOUT_NOTEBOOKS` link to the 'About' page
 
You may use reference tags to access pages of other API portals. Format is as follows:
```
{TAG}_{API title as stated in root RAML}:
```
For example, that's how we initialize Google Mail client in Google Calendar notebooks:
```
API.createClient('mailClient', '#REF_TAG_DEFENITION_GMail:');
```
Note that Google Mail API has `GMail` title stated in RAML:
```
#%RAML 0.8
title: GMail
version: v1
baseUri: https://www.googleapis.com/gmail/{version}/users
...
```
