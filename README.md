> [!IMPORTANT]
> Jargon is now deprecated. For new development, use the following library:
> - [https://github.com/irods/irods4j](https://github.com/irods/irods4j)
>
> Applications relying on Jargon should migrate to irods4j.

# Jargon

A Java client library for iRODS.

## Requirements

- Jargon depends on Java 1.8+ with a current focus on testing/running under OpenJDK 11
- Jargon is built using Apache Maven2, see POM for dependencies
- Jargon supports iRODS current releases, it also maintains very reasonable backwards compatability to iRODS 3.3.1 however this is no longer actively tested

## Libraries

- jargon-core - Base libraries, implementation of the iRODS protocol
- jargon-data-utils - Additional functionality for dealing with iRODS data, such as building trees, storing information in iRODS on behalf of applications, and doing diffs between local and iRODS
- jargon-user-tagging - Code for using free tagging and other metadata metaphors on top of iRODS
- jargon-user-profile - Allows management of user profile and related configuration data in a user home directory
- jargon-ticket - Support for ticket processing
- jargon-ruleservice - Support for running and managing rules from interfaces
- jargon-pool - Initial implementation of commons-pool caching of iRODS agent connections. This is initially for WebDav, and will be utilized as an option in REST and cloud browser. Consider this code experimental

## Changes

See [CHANGELOG](CHANGELOG.md) for updates.

## Reporting Security Vulnerabilities

See [SECURITY](SECURITY.md) for details.
