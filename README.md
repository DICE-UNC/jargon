
Jargon Core API

Work for milestone https://github.com/DICE-UNC/jargon/milestones/4.0.3.1%20second%20tier%20features%20branch

# Project: Jargon-core API

### Date:
### Release Version: 4.0.3.1-SNAPSHOT
### git tag: 
#### Developer: Mike Conway - DICE

## News



https://github.com/DICE-UNC/jargon/issues?q=is%3Aopen+is%3Aissue+milestone%3A%22features+branch+4.0.3%22

Please go to [[https://github.com/DICE-UNC/jargon]] for the latest news and info.

Jargon-core consists of the following libraries

* jargon-core - base libraries, implementation of the iRODS protocol
* jargon-data-utils - additional functionality for dealing with iRODS data, such as building trees, storing information in iRODS on behalf of applications, and doing diffs between local and iRODS
* jargon-user-tagging - code for using free tagging and other metadata metaphors on top of iRODS
* jargon-user-profile - allows management of user profile and related configuration data in a user home directory
* jargon-ticket - support for ticket processing
* jargon-httpstream - stream http content into iRODS via Jargon
* jargon-ruleservice - support for running and managing rules from interfaces
* jargon-workflow - support for iRODS workflows

## Requirements

* Jargon depends on Java 1.7+
* Jargon is built using Apache Maven2, see POM for dependencies
* Jargon supports iRODS 3.0 through iRODS 3.3.1 community, as well as iRODS 4.0.3 consortium

## Libraries

Jargon-core uses Maven for dependency management.  See the pom.xml file for references to various dependencies.

Note that the following bug and feature requests are logged in GitHub with related commit information [[https://github.com/DICE-UNC/jargon/issues]]

## Changes

### move semantics error in cloud browser moving a/path/a to a/path/b with collection already exists #140

Updated semantics of move collection to avoid 'collection already exists errors' by adding the source collection as the child of the target collection name

###  Rename operation in DTO #147 

###  across federation browsing under strict acls doesn't interpolate home/ and find subdirs viewable #39 

More gracefully handle path guessing heuristics cross-federation browsing when drilling down and stict ACLs is on.  

###  CI integration with iRODS 4 #18 

Changes in build automation and testing to integrate Jargon testing into iRODS Consortium Continuous integration


###  CollectionPager not navigating strict acl dirs with heuristics #148 

Add heuristic path guessing to CollectionPagerAO, which is an upgrade of the old CollectionAndDataObjectListAndSearchAOImpl
###  across federation browsing under strict acls doesn't interpolate home/ and find subdirs viewable #39 

Update cross-zone path heuristic guessing so one can reasonably browse 'down' to home dirs in federated zones

### Misc

Lots of small fixes, pull requests, iRODS compatability tests and fixes through work with consortium
