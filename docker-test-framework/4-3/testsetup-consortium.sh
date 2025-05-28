#!/bin/sh
iadmin mkuser test1 rodsadmin

iadmin moduser test1 password test

iadmin aua test1 test1DN

iadmin mkuser test2 rodsuser

iadmin moduser test2 password test

iadmin mkuser test3 rodsuser

iadmin moduser test3 password test

iadmin mkresc test1-resc "unix file system"  $HOSTNAME:/var/lib/irods/iRODS/Vault1

iadmin mkresc test1-resc2 "unix file system"  $HOSTNAME:/var/lib/irods/iRODS/Vault2

iadmin mkresc test1-resc3 "unix file system"  $HOSTNAME:/var/lib/irods/iRODS/Vault3

iadmin mkuser anonymous rodsuser

iadmin atg public anonymous

iadmin mkgroup jargonTestUg

iadmin atg jargonTestUg test1

iadmin atg jargonTestUg test3

# iRODS 4.3.4 made the hello script a template. Restore it to avoid test failures.
[ -f /var/lib/irods/msiExecCmd_bin/hello.template ] && cp /var/lib/irods/msiExecCmd_bin/hello.template /var/lib/irods/msiExecCmd_bin/hello
