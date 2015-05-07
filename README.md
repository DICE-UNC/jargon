
Jargon Core API


# Project: Jargon-core API
#### Date:
#### Release Version: 4.0.2.3-SNAPSHOT
#### git tag:
#### Developer: Mike Conway - DICE

## News

Work in progress on misc fixes and features for iRODS 4.1 certification

Release  milestone https://github.com/DICE-UNC/jargon/milestones/4.0.2.3%20with%20iRODS%204.1

=======

Please go to [[https://github.com/DICE-UNC/jargon]] for the latest news and info.

Jargon-core consists of the following libraries

* jargon-core - base libraries, implementation of the iRODS protocol
* jargon-data-utils - additional functionality for dealing with iRODS data, such as building trees, storing information in iRODS on behalf of applications, and doing diffs between local and iRODS
* jargon-user-tagging - code for using free tagging and other metadata metaphors on top of iRODS
* jargon-user-profile - allows management of user profile and related configuration data in a user home directory
* jargon-conveyor - transfer manager for managing and synchronizing data with iRODS
* jargon-ticket - support for ticket processing
* jargon-httpstream - stream http content into iRODS via Jargon
* jargon-ruleservice - support for running and managing rules from interfaces
* jargon-workflow - support for iRODS workflows

## Requirements

*Jargon depends on Java 1.7+
*Jargon is built using Apache Maven2, see POM for dependencies
*Jargon supports iRODS 3.0 through iRODS 3.3.1 community, as well as iRODS 4.0.3 consortium

## Libraries

Jargon-core uses Maven for dependency management.  See the pom.xml file for references to various dependencies.

Note that the following bug and feature requests are logged in GForge with related commit information [[https://github.com/DICE-UNC/jargon/issues]]

## Changes

####  apparent new resource oriented error messages #105 

Added new exceptions in iRODS 4.1, especially for resource hierarchies, and did some organization of the excepton tree by introducing new superclasses.

####  resource avu queries failing #104 

Moved resource queries away from old string bulding approach to the composition by builder approach, enhanced the unit testing.  This fixed some errors in resource AVU queries with 4.1.
