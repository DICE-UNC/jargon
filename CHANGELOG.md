# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [4.3.6.0-RELEASE] - 2025-03-26

This release is primarily in support of the [Metalnx](https://github.com/irods-contrib/metalnx-web) web application.

### Fixed

- Use correct data type when calculating max total bytes for zip service configuration (#504).

## [4.3.5.0-RELEASE] - 2025-02-11

This release is primarily in support of the [Metalnx](https://github.com/irods-contrib/metalnx-web) web application.

Jargon now depends on log4j 2. Users of this library will need to adjust log4j configuration and recompile applications accordingly.

### Changed

- Improve support for 10-level iRODS permission model (irods-contrib/metalnx-web#342).
- Update log4j from v1 to v2 (#451).
- Replace `<tasks>` with `<target>` in pom.xml files (#486, #496).

### Removed

- Remove dependency on slf4j (#484).

### Fixed

- Catch `InvalidUserException` and cleanly disconnect from iRODS server (irods-contrib/metalnx-web#133).

## [4.3.4.0-RELEASE] - 2024-08-30

### Security

- Add wording about reporting security vulnerabilities [#480]

### Changed

- Modernize Docker test framework [#473] [#447] [#444] [#434] [#433] [#426] [#418]
- Disable port mappings in Docker test framework [#431]
- Temporarily skip broken tests [#435]

### Fixed

- Fix issues with iRODS Consumer server Docker container for testing [#474] [#446]
- Fix failing tests for replica truncate function call [#472]
- Fix duplicate entries appearing in large collection listing [#437]
- Fix sending of incorrect option value in StartupPack [#429]
- Fix test failure involving non-existent resource [#425]
- Fix addAVUMetadata returning error when communicating with iRODS 4.3.0 server [#415]
- Allow data object close operations with replica token to work when communicating with older iRODS versions [#402]

### Added

- Add version detection functions [#471] [#445] [#432]
- Add support for GenQuery2 API [#442]
- Add support for library features API [#441]
- Add support for replica truncate API [#440]
- Add support for new permission levels [#428]
- Add functions for manipulating metadata using rodsadmin level privileges [#420]
- Add support for reporting application name to iRODS server [#407] [#352]

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


