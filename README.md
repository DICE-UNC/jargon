
Jargon Core API

work on milestone: https://github.com/DICE-UNC/jargon/issues?milestone=4&state=open

# Project: Jargon-core API
### Date: 03/21/2014
### Release Version: 4.0.2-SNAPSHOT
### git tag: 

## News

This version of Jargon is a release candidate for the next feature release of Jargon. This version contains many large and small upgrades in support of 

* Workflows
* Editing and running rules from interfaces
* iDrop transfer client, including a completely refactored transfer management service called 'jargon-conveyor' that replaces the old 'transfer-engine'
* Support for the irods-rest API with support for course-grained actions for ACLs and AVUs
* API support as needed to support JBoss ModeShape

It is important to note that the follow on to this release will see the wiring in of a client side action engine, allowing pluggable client workflows as
part of the transfer process in conveyor.  In doing so, we will need to alter the TransferStatusCallbackListener to give an option for listeners to intervene and
alter the processing of a transfer.  This may impact any code that implements TransferStatusCallbackListener.

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

*Jargon depends on Java 1.6+
*Jargon is built using Apache Maven2, see POM for dependencies
*Jargon supports iRODS 2.5 through iRODS 3.3.1 community, as well as iRODS 4.0.2 consortium

## Libraries

Jargon-core uses Maven for dependency management.  See the pom.xml file for references to various dependencies.

Note that the following bug and feature requests are logged in GForge with related commit information [[https://github.com/DICE-UNC/jargon/issues]]

## Bug Fixes

#### implement checksum variants #24

implement pluggable checksum generation/validation (https://github.com/DICE-UNC/jargon/issues/24)

#### remove cache of objStat for IRODSFile operations #34

IRODSFile uses a scheme to cache information to respond to exists, isFile, length, and other requests.

As these sorts of requests are made multiple times in client scenarios, it was originally coded to cache that information once (it obtains an objStat in the background), rather than calling iRODS each time a file.xxx() method was called.

That can save a good deal of traffic, but requires calling reset() to clear the cache on the client side.

The cache semantics were removed and reset() now is deprecated and has no effect.

#### PAM/SSL issues and slowness in workflow processing #27

Fixed PAM flush behavior for versions of iRODS > 3.2, avoiding those flushes when not necessary.  This can cause significant response time issues and was only needed to work around a bug in earlier versions of iRODS PAM processing.

=======
## Features

#### Significant development of new transfer framework (conveyor) to replace older transfer engine.

Conveyor is a drop-in framework to manage a persistent queue of transfers with file-by-file accounting. This will be extended in later releases to provide a client-side rule
engine that can manage pre and post transfer and pre and post file operation workflows on the client side.  Conveyor is embedded within iDrop and can also be easily incorporated
into other interfaces and tools.

#### Mounted collection support

Support has been added to interact with iRODS mounted collections, including list/read/write operations

#### Support for rule editing and execution from interfaces

Support for interactive rule editing, with extended methods and classes to assist in interactive editing and running of iRODS rules, has been added
in the jargon-ruleservice project

##### iRODS Workflow support

Basic workflow support has been added in the jargon-workflow subproject to be able to parse and execute iRODS workflows

##### Other Changes

Added capability to compute a SHA1 checksum via streaming to support ModeShape

