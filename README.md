
Jargon Core API


# Project: Jargon-core API
#### Date:
#### Release Version: 4.0.2.4-SNAPSHOT
#### git tag: 4.0.2.4.SNAPSHOT
#### Developer: Mike Conway - DICE

## News

Maintenance release of Jargon work in progress, focused on bug fixes, more 4.1 compatability, and stream performance for i/o and put/get

Release  https://github.com/DICE-UNC/jargon/milestones/Performance%20enhancements%20for%20streams%20and%20put/get%20-%204.0.2.4

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
* Jargon supports iRODS 3.0 through iRODS 3.3.1 community, as well as iRODS 4.0.3 consortium

## Libraries

Jargon-core uses Maven for dependency management.  See the pom.xml file for references to various dependencies.

Note that the following bug and feature requests are logged in GForge with related commit information [[https://github.com/DICE-UNC/jargon/issues]]

## Changes

### move semantics error in cloud browser moving a/path/a to a/path/b with collection already exists #140

Updated semantics of move collection to avoid 'collection already exists errors' by adding the source collection as the child of the target collection name
