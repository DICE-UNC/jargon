

# Project: Jargon-core API
<<<<<<< HEAD
<<<<<<< HEAD

### Date:
### Release Version: 4.0.3.1-SNAPSHOT
### git tag: 

=======
#### Date: 04/05/2016
#### Release Version: 4.0.2.6-SNAPSHOT
#### git tag: 
=======
#### Date: 06/07/2016
#### Release Version: 4.0.2.6-RC1
#### git tag: 4.0.2.6-RC1 
>>>>>>> origin/master
=======
#### Date: 
#### Release Version:
#### git tag: 4.1.10.0-SNAPSHOT
>>>>>>> origin/master
#### Developer: Mike Conway - DICE

## News

<<<<<<< HEAD
Work for milestone https://github.com/DICE-UNC/jargon/milestones/4.0.3.1%20second%20tier%20features%20branch

=======
4.1.10 Release compatability relese
for milestone: https://github.com/DICE-UNC/jargon/milestone/11

This includes support for client-server negotiation and SSL encryption of transport

=======
>>>>>>> origin/master

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

Note that the following bug and feature requests are logged in GitHub with related commit information [[https://github.com/DICE-UNC/jargon/issues]]

## Changes

<<<<<<< HEAD
#### Remove old thumbnail code #165 

Remove old image thumbnail code that relied on specific 'lifetime library' configuration.  This will later be replaced by a more globally applicable set of tools.  Likely in the jargon-extensions package

####  Add file to string and vice versa to support cloud browser editor #166 

Add file to string and vice versa in FileSamplerService of data utils.  This allows cloud browser to turn a file into an edit pane and store edits to irods.

#### remove resource group from data obj query #197
=======
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


>>>>>>> origin/master

Remove resc group from data object domain query 
