
Jargon Core API


# Project: Jargon-core API
#### Date:
#### Release Version: 4.0.2.4-SNAPSHOT
#### git tag: 4.0.2.4.SNAPSHOT
#### Developer: Mike Conway - DICE

## News

Maintenance release of Jargon work in progress, focused on bug fixes, more 4.1.6 compatability, and stream performance for i/o and put/get

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

###  across federation browsing under strict acls doesn't interpolate home/ and find subdirs viewable #39 

More gracefully handle path guessing heuristics cross-federation browsing when drilling down and stict ACLs is on.  

###  CI integration with iRODS 4 #18 

Changes in build automation and testing to integrate Jargon testing into iRODS Consortium Continuous integration

###  copy of file to itself recursively endlessly copies until path gets too large #151 

Added checks to not copy a collection recursively under itself.  Revealed in cloud browser testing.

###  -832000 in cloud browser clicking on file in fed zone #152 

Removed resource group name from DataObject selects when querying for domain object. That is no longer useful with new resource domain hierarchy and was causing GenQuery exceptions with later iRODS versions
Update cross-zone path heuristic guessing so one can reasonably browse 'down' to home dirs in federated zones

###  CI integration with iRODS 4 #18 

Enhancements to testing setup to integrate Jargon into Consortium CI services

###  PEP on output stream #144 

Fixes and tests for firing postProcForPut after certain streaming i/o operations

### PackingIrodsInputStream and PackingIrodsOutputStream 

Major enhancements to the performance of streaming i/o though simple read-ahead and write-behind buffering streams, see:

https://github.com/DICE-UNC/jargon/blob/master/jargon-core/src/main/java/org/irods/jargon/core/pub/io/PackingIrodsInputStream.java

and

https://github.com/DICE-UNC/jargon/blob/master/jargon-core/src/main/java/org/irods/jargon/core/pub/io/PackingIrodsOutputStream.java

These enhancements are used in the ModeShape, WebDav, REST, and cloud browser code

###  PEP on output stream #144 

Fixed open flags on WRITE and WRITE_TRUNCATE in output stream code to fire postProcForPut policy enforcement point.  This also required an iRODS fix 4.1.6+

###  Jargon breaking GSI by zapping list of CAs #137 

Added check of empty CA since the default value of the certificateAuthority in GSIIRODSAccount is blank versus null.  User submitted patch.

### Misc

Lots of small fixes, pull requests, iRODS compatability tests and fixes through work with consortium
