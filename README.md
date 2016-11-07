
# Project: Jargon-core API
#### Date: 
#### Release Version:
#### git tag: 4.1.10.0-SNAPSHOT
#### Developer: Mike Conway - DICE

## News

4.1.10 Release compatability relese
for milestone: https://github.com/DICE-UNC/jargon/milestone/11

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
* Jargon supports iRODS 3.0 through iRODS 3.3.1 community, as well as iRODS 4.2.0 consortium

## Libraries

Jargon-core uses Maven for dependency management.  See the pom.xml file for references to various dependencies.

Note that the following bug and feature requests are logged in GForge with related commit information [[https://github.com/DICE-UNC/jargon/issues]]

## Changes

#### FileNotFoundException declared in PackingIrodsOutputStream signature #150

Removed extraneous exceptions from method signature

#### deprecate WSO support in jargon altogether #51

Removed jargon-workflow code (WSO) as non-supported in iRODS

