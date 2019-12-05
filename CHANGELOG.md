# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


### Added

### Changed

#### Allow setting of proxy user/zone in IRODSAccount #338

Convenience method for creating IRODSAccount proxy user settings

#### Add tests/doc and clarify function of proxy users #339

Added functional tests and clarified setting of proxyUser and proxyZone in IRODSAccount.

#### cleanups and additions to groupAO and userAO handling for groups in metalnx #344

Clarified UserAO and GroupAO methods, fixed issues where users were appearing in GroupAO findXXX methods. Transitioned queries to builder model in GroupAO.

#### When invoking a rule via jargon the microservice msiDataObjGet doesn't end #337

Added overhead code for client-side rule actions to send an oprComplete after a parallel get
operation. This prevents 'stuck' rules. Fix for user-reported issue.

#### fix use of proxy in pam auth #347

Fixed PAMAuth so that username is propogated from IRODSAccount into the packing instruction. This had been set to proxyName().
