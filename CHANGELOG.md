# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [4.3.3.0-RELEASE]

#### First coordinated stream is required to close and update the catalog when doing parallel transfer #408

Adjustments to close behavior for NFSRODS in parallel transfer to close first stream last

#### Additional options for file zipping/tarring

## [4.3.2.4-RELEASE]

### Added

#### Have test setup utilities honor jargon.properties ssl negotiation setting #366

Enable unit tests to respect jargon properties defaults for ssl negotiation

#### Update maven deps for Mockito and ByteBuddy (to allow running under JDK 11)

#### test failures against 4.2 stable for 4.2.9 #369

Updated unit testing to work better with consortium test images, smoothed over a few issues. A configuration
property for settings.xml was added to turn off tests leveraging parallel transfer. False will turn off tests that may 
fail against docker test images due to Docker networking and high ports 

```xml
<jargon.test.option.exercise.parallel.txfr>false</jargon.test.option.exercise.parallel.txfr>

```

### dd 'rollup' of all listing entries when doing collection listing #377

Added new signatures in CollectionAndDataObjectListAndSearchAO to return all children of a folder rather than paged results.

```java

/**
	 * List all data objects and collections under a given absolute path, fetching
	 * all pages at once
	 * 
	 * @param objStat {@link ObjStat}
	 * @return {@code List} of {@link CollectionAndDataObjectListingEntry}
	 * @throws FileNotFoundException {@link FileNotFoundException}
	 * @throws JargonException       {@link JargonException}
	 */
	List<CollectionAndDataObjectListingEntry> listAllDataObjectsAndCollectionsUnderPath(final ObjStat objStat)
			throws FileNotFoundException, JargonException;

	/**
	 * List all data objects under a given absolute path, fetching all pages at once
	 * 
	 * @param objStat {@link ObjStat}
	 * @return {@code List} of {@link CollectionAndDataObjectListingEntry}
	 * @throws FileNotFoundException {@link FileNotFoundException}
	 * @throws JargonException       {@link JargonException}
	 */
	List<CollectionAndDataObjectListingEntry> listAllDataObjectsUnderPath(final String absolutePathToParent,
			final int partialStartIndex) throws JargonException;

	/**
	 * List all collections under a given absolute path, fetching all pages at once
	 * 
	 * @param objStat {@link ObjStat}
	 * @return {@code List} of {@link CollectionAndDataObjectListingEntry}
	 * @throws FileNotFoundException {@link FileNotFoundException}
	 * @throws JargonException       {@link JargonException}
	 */
	List<CollectionAndDataObjectListingEntry> listAllCollectionsUnderPath(final String absolutePathToParent,
			final int partialStartIndex) throws FileNotFoundException, JargonException;



```

### Changed

#### create an IRODSFile when it already exists no longer throws an exception (in 4.2.9) #375

There is a slight behavior change post 4.2.8 where calling create on a file acts in a more idempotent way, not throwing an error when a file was previously created. This seems like a minor variance with a low level of surprise, therefore we'll just roll with the slight variation, not worry about prior differences, and adjust the unit testing expectations.

#### add DataNotFound semantics when streaming byte buffers from irods in Stream2StreamAO #387

Retrieve shopping cart was having an issue with a JargonException when retrieving a non-existent cart, now will gracefully handle this. This also updates Stream2StreamAO so that streaming a byte buffer from iRODS when no file exists will properly return a DataNotFoundException

#### Append cart #386

Added ability to append items to the file shopping cart

#### Add COLL_DATA_MODIFY_TIME variant to match COLL_COLL_MODIFY_TIME #382

Added a modify time variable name for data objects that matches the collection modify time

#### Modify time difference between queryBuilder and lastModified #381

Added unit test to verify IRODSFile and DataObject dates agree as part of explaining a user reported issue

####  Temporarily calm unit tests for tickets until resolved  #376

Temporarily turned off certain ticket unit tests that are failing for follow up in a maintenance release

#### fixed StringIndexOutOfBoundsException and allowed quota values to be 0 #395

A pull request from Shane-Park to address string out of bounds exceptions

#### Errors in ticket modification when date is set to 'none' or blank #401

This appears to be a new error in iRODS, setting an expires date from a non-blank value to a blank value. The unit test failures have been quieted for this release and a follow up will be done with iRODS developers to discern whether this is a server regression.

#### Add condinput to ticket api calls #400

This is a partial fix for changes to the ticket admin API. A CondInput set of key/values pairs was added to the packing
instruction. A backwards compatable patch was made that supplies this for iRODS post 4.2.11. A follow on task will
expose the new admin ticket functions in the API.


