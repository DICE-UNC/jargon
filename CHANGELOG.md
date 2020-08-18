# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


### Added

#### add docker test and build capacity to testing framework #359

Added a unified test framework that contains a preconfigured iRODS grid in Docker Compose as well as a connected test and build container
that can run pre-packaged unit tests.


#### add force flag to rename operations #363
Add a force option to the rename methods in IRODSFileSystemAO to support NFSRods requirements. This is an added signature with a force flag that does not change the original api

### Changed
