# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [4.3.0.2-RELEASE] -
### Added

#### Add 'noindex' semantics and ability to ignore data objects w/no metadata #314

Added DONOTINDEX support for collections being indexed using the org.irods.jargon.datautils.indexer services. This allows collections to be marked per-indexer as ignored.
For details see the INDEXING.md file located in the jargon-data-utils submodule.

#### UserFilePermissions issue #253

Added specific query based ACL listing if available on the target iRODS grid

#### Support for ILIKE case insensitive where classes #254

UserAO and UserGroupAO now provide search by name signatures that can support case-insensitive queries

#### add candidate rule engine listing #264

Added the ability to list available rule engines to RuleProcessingAO to support irule -a functionality. This lists the configured rule
engines on the target iRODS server

#### checksum enhancements #324

Added enhanced values in ChecksumValue so that binary, Base64, and Hex String representations of a checksum are always available in the
value object.

#### add asAvuData() method to MetaDataAndDomainData #326

Add convenience method to convert to AvuData object.

### Changed

#### Improve behavior of heuristic path guessing under home #313

The collection listing utilities have an ability to 'guess' through the top directories when StrictACLs are turned on. This was masking otherwise visible paths under 'home' when turned on. This
is corrected to better reflect the available directories under 'home'

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

#### python rule engine errors in iRODS 4.2.4 #320

Updated test python rules to conform to new requirement to include a main() in client-submitted rules

#### Jargon Core tests fail when trying to delete a collection #305

Deleting a collection that contains data with the 'no force' option no longer causes an error in iRODS 4.2.4, therefore this
unit test is reactivated and is applied in target iRODS servers 4.2.4 and greater. The testDeleteACollectionWithAmpInTheNameBug170
unit test still fails and this is being followed up as an iRODS bug.

#### ResourceAOImpl.listResourceNames() returns an empty list from Oracle #308

Added IS NULL support for GenQuery and merge resources with parent of "" and parent of NULL together to account for inconsistancies between
Oracle ad Postgres iCAT when querying top level resources


### Removed

#### Remove user profile subproject and migrate over to jargon-extensions #315

Remove user-profile submodule as old idrop-web legacy, this functionality is being revised and added to jargon-extensions-if for the purposes of standardizing MetaLnx


# Versions

[Unreleased]:
[2.0.0]:
