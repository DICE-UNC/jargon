*'''Project''': Jargon-core API
*'''Date''': 01/24/2014
*'''Release Version''': 3.3.3-SNAPSHOT
*'''git tag''': 3.3.3-SNAPSHOT

==News==

Feature release for Jargon 3.3.3

=======
Please go to [[https://code.renci.org/gf/project/jargon/]] for the latest news and info.

Jargon-core consists of the following libraries

* jargon-core - base libraries, implementation of the iRODS protocol
* jargon-data-utils - additional functionality for dealing with iRODS data, such as building trees, storing information in iRODS on behalf of applications, and doing diffs between local and iRODS
* jargon-security - code for use with Spring security
* jargon-user-tagging - code for using free tagging and other metadata metaphors on top of iRODS
* jargon-conveyor - transfer manager for managing and synchronizing data with iRODS
* jargon-ticket - support for ticket processing
* jargon-httpstream - stream http content into iRODS via Jargon

==Requirements==

*Jargon depends on Java 1.6+
*Jargon is built using Apache Maven2, see POM for dependencies
*Jargon supports iRODS 2.5 through iRODS 3.3, as well as eIRODS 4.0 beta

==Libraries==

Jargon-core uses Maven for dependency management.  See the pom.xml file for references to various dependencies.

Note that the following bug and feature requests are logged in GForge with related commit information [[https://code.renci.org/gf/project/jargon/tracker/]]

==Bug Fixes==


==Features==

*Significant development of new transfer framework (conveyor) to replace older transfer engine.

*Added capability to compute a SHA1 checksum via streaming to support ModeShape