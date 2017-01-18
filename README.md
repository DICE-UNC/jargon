
# Project: Jargon-core API
#### Date: 
#### Release Version:
#### git tag: 4.1.10.0-RC1
#### Developer: Mike Conway - DICE

## News

4.1.10 Release compatability release candidate
for milestone: https://github.com/DICE-UNC/jargon/milestone/11

This includes support for client-server negotiation and SSL encryption of transport

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

## Requirements

* Jargon depends on Java 1.7+
* Jargon is built using Apache Maven2, see POM for dependencies
* Jargon supports iRODS 3.0 through iRODS 3.3.1 community, as well as iRODS 4.2.0 consortium

## Libraries

Jargon-core uses Maven for dependency management.  See the pom.xml file for references to various dependencies.

Note that the following bug and feature requests are logged in GForge with related commit information [[https://github.com/DICE-UNC/jargon/issues]]

## Changes

#### FileNotFoundException declared in PackingIrodsOutputStream signature #150

Removed extraneous exceptions from method signature

#### deprecate WSO support in jargon altogether #51

Removed jargon-workflow code (WSO) as non-supported in iRODS

#### add speccol pi to dataobjinppi to avoid xml parse messages in irods log #149

Avoid logged iRODS errors for missing section of DataObjInp_PI by providing SpecColl_PI and KeyValPair_PI structures

#### Fix lexographic comparison of iRODS version #203

Changed from a string comparison of iRODS reported version to a more sophisticated comparator object 

#### Update FileCloseInp for in_pdmo for iRODS 4.1.9 #205

Update FileCloseInp packing instruction for additional resource information

#### Special character handling in delete operations seems to be off. #170

Directories can be created with multiple special chars (notably &) but problems may occur in delete. Unit test shows
an iRODS issue referenced at Create/delete file with & char allows create, causes -816000 error on delete #3398, will ignore unit test for now.

#### IRODSAccount fails with whitespace in default path #189

toURI with white spaces gives URISyntaxException, url encode that information

#### Add client/server negotiation support for SSL encryption #4

Honor client server negotiation and SSL transport encryption, as well as shared-key encryption for parallel file transfers.  (docs coming soon).  This is early access and should be used with caution until release time.

#### IndexOutOfBoundsException in PackingIrodsOutputStream #200

Simplified the buffering code and added unit and functional tests, especially for conditions when the putBufferSize is manipulated in jargon.properties.  

#### -23000 error code from iRODS with parallel file operations #199

Testing with CyberDuck.  Added a new IRODSAccessObjectFactory.authenticateIRODSAccountUtilizingCachedConnectionIfPresent to avoid double-opening a connection, reducing side effects
for some usage patterns.

#### add checksum support to Jargon streaming API #194

Added support for checksumming on close of an IRODSFileOutputStream, respecting checksum and checksum with verification flags in jargon.properties.  If either of those flags is true,
a checksum is computed and stored in iRODS.  Given the nature of the java.io API, actual verification is not done, just computation and storage in the iCAT.

#### PAM auth failure when password includes semicolon #195

Added KVP packing instruction support to escape ; character in PAM password, added unit test verification

#### failing parallel file operation should throw exception #133

pending...consider adding a fail fast option...

#### improve logging in CollectionAndDataObjectListAndSearchAOImpl #135

Added toString methods to result set and result row, limited debug logging of result rows to 100 rows.  Fixed a typo in the logging statement for collection listings.

#### iRODS CS_NEG_REQUIRE + PAM authentication #215

Fixed mismatch between local SSL negotiation stance enum values and iRODS negotiation enum values.  Fixed double send of SslEndInp.

#### "Catalog SQL error" in jargon-core when using Oracle #196
Replicated issue, identified as iRODS server issue, due to missing specific queries.  For cat sql errors when checking group authorization, a 'specific_query_patch-bug196.sh' demonstrates a repair to a 4.1.x iRODS installation with Oracle iCAT.  See https://github.com/DICE-UNC/jargon/issues/196 for details.


### Additional testing for reported issues, minor changes

#### Login fails if password contains linux escape characters #202 

Added test cases, fails to replicate

#### transfer get of file with parens and spaces in name gives file not found #1 

Added test case that fails to replicate






