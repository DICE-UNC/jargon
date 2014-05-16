#Project: Jargon-core API
###Date: 03/20/2014
###Release Version: 3.3.2.1
###git tag: 3.3.2.1-SNAPSHOT

##News

master branch reflects post 3.3.2 fixes and updates

=======
Please go to [[https://github.com/DICE-UNC/jargon]] for the latest news and info.

Jargon-core consists of the following libraries

* jargon-core - base libraries, implementation of the iRODS protocol
* jargon-data-utils - additional functionality for dealing with iRODS data, such as building trees, storing information in iRODS on behalf of applications, and doing diffs between local and iRODS
* jargon-security - code for use with Spring security
* jargon-user-tagging - code for using free tagging and other metadata metaphors on top of iRODS
* jargon-transfer - transfer manager for managing and synchronizing data with iRODS
* jargon-ticket - support for ticket processing
* jargon-httpstream - stream http content into iRODS via Jargon


##Requirements

*Jargon depends on Java 1.6+
*Jargon is built using Apache Maven2, see POM for dependencies
*Jargon supports iRODS 2.5 through iRODS 3.3.1 community, as well as iRODS 4.0 consortium

##Libraries

Jargon-core uses Maven for dependency management.  See the pom.xml file for references to various dependencies.

Note that the following bug and feature requests are logged in GForge with related commit information [[https://github.com/DICE-UNC/jargon/issues]]

##Bug Fixes

* https://github.com/DICE-UNC/jargon/issues/17 - added support for IN queries (need to add between, etc)

##Features
