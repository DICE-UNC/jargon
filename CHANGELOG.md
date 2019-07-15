# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


### Added

#### Adding a function for rebalancing a resource #332

### Changed

#### When invoking a rule via jargon the microservice msiDataObjGet doesn't end #337

Added overhead code for client-side rule actions to send an oprComplete after a parallel get
operation. This prevents 'stuck' rules. Fix for user-reported issue.

### Removed
