
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

Added a rename() operation to DataTransferOperations to clarify difference between a rename and a move

###  CollectionPager not navigating strict acl dirs with heuristics #148 

Add heuristic path guessing to CollectionPagerAO, which is an upgrade of the old CollectionAndDataObjectListAndSearchAOImpl
