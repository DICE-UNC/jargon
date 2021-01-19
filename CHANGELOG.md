# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


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
