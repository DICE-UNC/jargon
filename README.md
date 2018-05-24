
# Project: Jargon-core API
#### Date: 05/24/2018
#### Release Version: 4.2.2.1-RELEASE
#### git tag: 4.2.2.1-RELEASE
#### Developer: Mike Conway

## News

4.2.3 Compatability and maintenance
for milestone: https://github.com/DICE-UNC/jargon/milestone/22

=======

Please go to https://github.com/DICE-UNC/jargon for the latest news and info.

Jargon-core consists of the following libraries

* jargon-core - base libraries, implementation of the iRODS protocol
* jargon-data-utils - additional functionality for dealing with iRODS data, such as building trees, storing information in iRODS on behalf of applications, and doing diffs between local and iRODS
* jargon-user-tagging - code for using free tagging and other metadata metaphors on top of iRODS
* jargon-user-profile - allows management of user profile and related configuration data in a user home directory
* jargon-ticket - support for ticket processing
* jargon-httpstream - stream http content into iRODS via Jargon
* jargon-ruleservice - support for running and managing rules from interfaces
* jargon-pool - initial implementation of commons-pool caching of iRODS agent connections.  This is initially for WebDav, and will be utilized as an option in REST and cloud browser.  Consider this code experimental

## Requirements

* Jargon depends on Java 1.8+
* Jargon is built using Apache Maven2, see POM for dependencies
* Jargon supports iRODS 4.1.0 through 4.2.2, it also maintains very reasonable backards compatability to iRODS 3.3.1 however this is no longer actively tested

## Libraries

Jargon-core uses Maven for dependency management.  See the pom.xml file for references to various dependencies.

Note that the following bug and feature requests are logged in GForge with related commit information https://github.com/DICE-UNC/jargon/issues

## Changes

#### Add trash operations service #280

Adding service object for managing trash to retrofit into MetaLnx

#### Escape spec chars in pam password #288

Added escaping of characters that cause problems with the kvp processing when sending PAM passwords and integrated into PAMAuth. This is linked to iRODS https://github.com/irods/irods/issues/3528

#### Indexer/visitor pattern for data utils #296

Add code to support easy impl of metadata/file indexers via a Visitor pattern on iRODS directories. This is added to the jargon-data-utils and 
provides a reliable way of traversing an iRODS hierarchy with control, applying a user-supplied procedure at each node via the Hierarchical Visitor pattern. This also has a dedicated implementation (at least an initial one) of a visitor with additional facilities for indexing crawlers. This will be optimized as time goes forward but is in employment already for several locations.

#### JavaDoc cleanups #279

Cleanups and fixes to javadocs to allow generation


