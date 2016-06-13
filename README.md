
# Project: Jargon-core API
#### Date: 06/07/2016
#### Release Version: 4.0.2.6-RC1
#### git tag: 4.0.2.6-RC1 
#### Developer: Mike Conway - DICE

## News

Code fix and performance tweaks, certification with iRODS 4.1.9

https://github.com/DICE-UNC/jargon/milestones/iRODS%204.1.9%20support
=======

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
* Jargon supports iRODS 3.0 through iRODS 3.3.1 community, as well as iRODS 4.1.7 consortium

## Libraries

Jargon-core uses Maven for dependency management.  See the pom.xml file for references to various dependencies.

Note that the following bug and feature requests are logged in GForge with related commit information [[https://github.com/DICE-UNC/jargon/issues]]

## Changes

###   metadata query on replicated data object repeats metadata #178 

Additional tests, remove non-distinct extra information from list AVU queries, causing duplicate data on replicated data objects

###  Remove trim() from values in AVU queries #180 

Merged pull request from CyVerse to not trim() AVU queries

### Misc

Numerous small changes and additional test for 4.1.9

