# Project: Jargon-core API
### Date:
### Release Version: 4.3.0.2-SNAPSHOT
### git tag:

## News



https://github.com/DICE-UNC/jargon/milestone/25

This is a maintenance and feature development release to support Metalnx, REST2, iDrop, NFS4J, and other efforts

Please go to https://github.com/DICE-UNC/jargon for the latest news and info.

=======

Jargon-core consists of the following libraries

* jargon-core - base libraries, implementation of the iRODS protocol
* jargon-data-utils - additional functionality for dealing with iRODS data, such as building trees, storing information in iRODS on behalf of applications, and doing diffs between local and iRODS
* jargon-user-tagging - code for using free tagging and other metadata metaphors on top of iRODS
* jargon-user-profile - allows management of user profile and related configuration data in a user home directory
* jargon-ticket - support for ticket processing
* jargon-ruleservice - support for running and managing rules from interfaces
* jargon-pool - initial implementation of commons-pool caching of iRODS agent connections.  This is initially for WebDav, and will be utilized as an option in REST and cloud browser.  Consider this code experimental

## Requirements

* Jargon depends on Java 1.8+
* Jargon is built using Apache Maven2, see POM for dependencies
* Jargon supports iRODS current releases, it also maintains very reasonable backwards compatability to iRODS 3.3.1 however this is no longer actively tested

## Libraries

Jargon-core uses Maven for dependency management.  See the pom.xml file for references to various dependencies.

Note that the following bug and feature requests are logged in GitHub with related commit information https://github.com/DICE-UNC/jargon/issues

## Changes

#### Add 'noindex' semantics and ability to ignore data objects w/no metadata #314

Added DONOTINDEX support for collections being indexed using the org.irods.jargon.datautils.indexer services. This allows collections to be marked per-indexer as ignored.
For details see the INDEXING.md file located in the jargon-data-utils submodule.

#### Improve behavior of heuristic path guessing under home #313

The collection listing utilities have an ability to 'guess' through the top directories when StrictACLs are turned on. This was masking otherwise visible paths under 'home' when turned on. This
is corrected to better reflect the available directories under 'home'

#### Remove user profile subproject and migrate over to jargon-extensions #315

Remove user-profile submodule as old idrop-web legacy, this functionality is being revised and added to jargon-extensions-if for the purposes of standardizing MetaLnx

#### Update misc dependencies #316

Misc dependency updates via jargon-pom

#### enhance pools for NFS4J #317

Clean up confusing and unnecessary AbstractIRODSMidLevelProtocol -> IRODSMidLevelProtocol relationship,
working on updating pooling code for commons-pool2, more testing. This is for use in MetaLnx, NSF4J, REST, and GA4GH DOS
tools.

#### expose JargonProperties as JMX MBean #318

JargonProperties are now exposed as a JMXBean and real-time adjustment of properties is now possible via jconsole or other JMX tools

#### Fix javadoc #309

Cleanup of JavaDocs

#### Implement isysmeta functions #319

Added isysmeta functions into DataObjectAO. 

```Java

List<String> listDataTypes() throws JargonException

```

#### python rule engine errors in iRODS 4.2.4 #320

Updated test python rules to conform to new requirement to include a main() in client-submitted rules


#### UserFilePermissions issue #253

Added specific query based ACL listing if available on the target iRODS grid

#### Jargon Core tests fail when trying to delete a collection #305

Deleting a collection that contains data with the 'no force' option no longer causes an error in iRODS 4.2.4, therefore this 
unit test is reactivated and is applied in target iRODS servers 4.2.4 and greater. The testDeleteACollectionWithAmpInTheNameBug170
unit test still fails and this is being followed up as an iRODS bug.

#### Support for ILIKE case insensitive where classes #254

UserAO and UserGroupAO now provide search by name signatures that can support case-insensitive queries
