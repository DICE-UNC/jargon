## Introduction

The Jargon-core libraries are a set of core API, and higher-level libraries built on top of the Jargon-core, that allow 
general application development for iRODS clients written in Java, or in dynamic JVM languages such as Jython, JRuby, Groovy, or PHP. 
These libraries are used internally by iRODS developers for Swing and Spring-based web interfaces, as well as REST and SOAP capabilities.

Jargon communicates with iRODS by establishing a socket connection, and invoking iRODS services using the iRODS XML protocol. 
The Jargon core library functions primarily as an implementation of the iRODS protocol and packing instructions, as well as an implementation 
of the connection between iRODS and the client over which the iRODS packing instructions and related data are exchanged. 

### Table of Contents

* [Authentication](authentication.md)
* [Creating a Connection](connecting.md)