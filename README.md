
# Project: Jargon-core API
#### Date: 04/05/2016
#### Release Version: 4.0.2.5-RELEASE
#### git tag: 4.0.2.5-RELEASE
#### Developer: Mike Conway - DICE

## News

Maintenance release of Jargon to support REST and WebDav.  This release consists mostly of many small tweaks, fixes, and additional tests accumulated since the last Jargon release, and represents 'house cleaning' before rolling in SSL transport security.

https://github.com/DICE-UNC/jargon/milestones/mx%20for%20rest/webdav%20-%204.0.2.5

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

###  move versus rename semantics, collection already exists on rename collection #161 

Further refinement to the move operations in DataTransferOperationsImpl surfaced through testing for WebDav integration.  Also added several new unit tests.

### fix exception expectations in unit tests for RENCI CI


###  pull ibiblio repo #163 

Remove ibiblio repo from maven, causing corruption of local repositories


### Misc

Numerous small fixes and additional tests 




