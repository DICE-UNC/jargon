
Jargon Core API


# Project: Jargon-core API
#### Date: 04/21/2015
#### Release Version: 4.0.2.2-SNAPSHOT
#### git tag:
#### Developer: Mike Conway - DICE

## News

Work in progress on misc fixes and features for Cyberduck integration

Release  milestone https://github.com/DICE-UNC/jargon/milestones/Maintenance%20release%204.0.2.2%20with%20misc%20Cyberduck%20integration

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

### Integration misc with CyberDuck #101

Misc small changes as we test Cyberduck integration with iRODS.  This is a catch-all issue for many minor changes.

### Authenticates as the proxy instead of client #100

Handle authentication with proxy user, per a patch supplied by Tony Edgin at iPlant (Thanks!) 

### Option to obtain MD5 from server after upload is complete #89

Breaking out checksum utilities to its own service object (DataObjectChecksumUtilitiesAO) and beginning to add richer hooks for
various checksum management scenarios.  This was originally done to have simpler hooks for CyberDuck.  Eventually checksum code in various places will be deprecated and will point to these consolidated services.

### jargon-conveyor migration to idrop #120

Took jargon-conveyor out of this project to clarify roles.  Conveyor is tied to iDrop, and we are making jargon core libs focused on iRODS interactions and protocols.  This also helps with the move to integrate jargon testing into CI at the consortium

