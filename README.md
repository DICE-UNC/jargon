# Project: Jargon-core API
<<<<<<< HEAD

### Date:
### Release Version: 4.2.1.0-SNAPSHOT
### git tag: 
=======
#### Date: 2/9/2017 
#### Release Version: 4.2.0.0-SNAPSHOT 
#### git tag: 
>>>>>>> origin/master
#### Developer: Mike Conway - DICE

## News

<<<<<<< HEAD
Work for milestone https://github.com/DICE-UNC/jargon/milestone/13
=======
4.2.0 Compatability and maintenance
for milestone: https://github.com/DICE-UNC/jargon/milestone/16

>>>>>>> origin/master

This version of Jargon is currently targeted at Cloud Browser and REST.  There are still some features that are considered early access and may not support a full range
of use cases for general cases, and having a separate stream allows us flexibility to break API on these more advanced features, such as advanced paging and virtual collections support.

Please go to [[https://github.com/DICE-UNC/jargon]] for the latest news and info.

=======
Jargon-core consists of the following libraries

* jargon-core - base libraries, implementation of the iRODS protocol
* jargon-data-utils - additional functionality for dealing with iRODS data, such as building trees, storing information in iRODS on behalf of applications, and doing diffs between local and iRODS
* jargon-user-tagging - code for using free tagging and other metadata metaphors on top of iRODS
* jargon-user-profile - allows management of user profile and related configuration data in a user home directory
* jargon-ticket - support for ticket processing
* jargon-httpstream - stream http content into iRODS via Jargon
* jargon-ruleservice - support for running and managing rules from interfaces

## Requirements

* Jargon depends on Java 1.8+
* Jargon is built using Apache Maven2, see POM for dependencies
<<<<<<< HEAD
* Jargon supports iRODS iRODS 3.3.1 community, through iRODS 4.2.0 consortium
=======
* Jargon supports iRODS 3.0 through 4.2.0
>>>>>>> origin/master

## Libraries

Jargon-core uses Maven for dependency management.  See the pom.xml file for references to various dependencies.

Note that the following bug and feature requests are logged in GForge with related commit information [[https://github.com/DICE-UNC/jargon/issues]]

## Changes

<<<<<<< HEAD
#### Remove old thumbnail code #165 

Remove old image thumbnail code that relied on specific 'lifetime library' configuration.  This will later be replaced by a more globally applicable set of tools.  Likely in the jargon-extensions package

####  Add file to string and vice versa to support cloud browser editor #166 

Add file to string and vice versa in FileSamplerService of data utils.  This allows cloud browser to turn a file into an edit pane and store edits to irods.
=======
>>>>>>> origin/master
