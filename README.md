
Jargon Core API


# Project: Jargon-core API
#### Date:
#### Release Version: 4.0.2.3-RC1
#### git tag: 4.0.2.3-RC1
#### Developer: Mike Conway - DICE

## News

Releae candidate for  misc fixes and features for iRODS 4.1 certification

Release  milestone https://github.com/DICE-UNC/jargon/milestones/4.0.2.3%20with%20iRODS%204.1

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

*Jargon depends on Java 1.7+
*Jargon is built using Apache Maven2, see POM for dependencies
*Jargon supports iRODS 3.0 through iRODS 3.3.1 community, as well as iRODS 4.0.3 consortium

## Libraries

Jargon-core uses Maven for dependency management.  See the pom.xml file for references to various dependencies.

Note that the following bug and feature requests are logged in GForge with related commit information [[https://github.com/DICE-UNC/jargon/issues]]

## Changes

####  apparent new resource oriented error messages #105 

Added new exceptions in iRODS 4.1, especially for resource hierarchies, and did some organization of the exception tree by introducing new super-classes.

####  resource avu queries failing #104 

Moved resource queries away from old string building approach to the composition by builder approach, enhanced the unit testing.  This fixed some errors in resource AVU queries with 4.1.

####  Read length set to zero on replication with iRODS 4.1 #106 

Added backwards-compatible processing for new replicate API number.  

####  Read length set to zero on copy with iRODS 4.1 #107 

Added backwards-compatible processing for copy API number and improved overwrite logic

####  Error listing replicas in a resource group (cat unknown table) #108 

Resource groups are no longer a concept, so some operations using resource groups now throw an UnsupportedOperationException when ussued against a 4.1+ iRODS host.  These operations continue to be supported pre 4.1.  Unit test code was adjusted.

#### testTrimReplicasForDataObjectByResourceNameInvalid fails with uncaught -78000 #109

Added a ResourceNotFoundException in the hierarchy.  For prior to 4.1, will maintain current behavior of silently ignoring, which in retrospect might be a bit odd.  But no surprises!  From 4.1+ will throw a DataNotFoundException.  It's a little messy, may rethink that later.

####  Read length set to 0 on phymove, likely protocol change #111 

Fixed phymove api numbers, maintains backwards compatability

#### Potential federation error 4.1 iRODS query across zones getting objStat #126

Added heruistic creation of ObjStats in certain occasions where strict ACLs would otherwise cause an error.  This is meant to assist interfaces that need to 'navigate' down from the root, to give these interfaces a chance to get to viewable directories.  This can be turned off with jargon properties.

#### Stream performance enhancements #87

Added PackingInputStream and PackingOutputStream that do read-ahead and write-behind caching so that reads and writes using small buffer sizes from the perspective of a client (e.g. WebDav implemenation)
so that more performant buffer sizes can be used in protocol operations with irods.