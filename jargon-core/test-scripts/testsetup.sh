#!/bin/sh
exec `iadmin mkuser test1 rodsadmin`

exec `iadmin moduser test1 password test`

exec `iadmin aua test1 test1DN`

exec `iadmin mkuser test2 rodsuser`

exec `iadmin moduser test2 password test`

exec `iadmin mkuser test3 rodsuser`

exec `iadmin moduser test3 password test`

exec `iadmin mkresc test1-resc "unix file system" cache localhost "/opt/iRODS/iRODS2.5/Vault1"`

exec `iadmin mkresc test1-resc2 "unix file system" cache localhost "/opt/iRODS/iRODS2.5/Vault2"`

exec `iadmin atrg testResourceGroup test1-resc2`

