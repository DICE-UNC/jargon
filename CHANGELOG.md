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

### Changed
