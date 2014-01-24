*'''Project''': Jargon-core API
*'''Date''': 01/24/2014
*'''Release Version''': 3.3.2-beta2
*'''git tag''': 3.3.2-beta2

==News==

Bug fix and eIRODS compatability release for Jargon.

=======
Please go to [[https://code.renci.org/gf/project/jargon/]] for the latest news and info.

Jargon-core consists of the following libraries

* jargon-core - base libraries, implementation of the iRODS protocol
* jargon-data-utils - additional functionality for dealing with iRODS data, such as building trees, storing information in iRODS on behalf of applications, and doing diffs between local and iRODS
* jargon-security - code for use with Spring security
* jargon-user-tagging - code for using free tagging and other metadata metaphors on top of iRODS
* jargon-transfer - transfer manager for managing and synchronizing data with iRODS
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

[#1635] ttl parameter in PAM missing
**Fix PAM login errors on iRODS 3.3 due to addition of timeToLive parameter

*[#1726] need to add back flushes for 3.2 compatability
**add a force.pam.flush jargon property for pre 3.3 servers.  This is set in jargon.properties and is false by default.  If you are running Jargon against iRODS 3.2, you may find that pam operations freeze, and this flag needs to be set to force some extra flushes that were required.  This setting will negatively impact performance so it should be turned off for iRODS 3.3.

*[#1840] error doing a rename in idrop
**error with rename of a folder under a parent adding intermediate directory

*[#1841] pre count of get transfers was counting replicas

*[#1842] [iROD-Chat:11109] imcoll symlinks across zones
**Added unit tests for creating soft links in federated zone and doing listings in that zone

*Numerous small fixes and improvements since the 3.3.1 release.

==Features==

*[#1603] EIRODS 3.0 compatability 
**Version detection and base compatability with eIRODS 3.0.1 beta release.  
**Removed 2.0.1 compatible packing instructions and replaced with current operations, especially for file operations (read/write/close)
**Beginning refactoring of connection, protocol manager code for pluggable auth, among other things.  This will evenually lead to a better factory based implementation with cleaner separation of concerns for authentication, as well as the ability to plug in different networking layers

*[#1796] pluggable auth support for eirods
**Added new auth plugin scheme support starting with PAM authentication for eIRODS