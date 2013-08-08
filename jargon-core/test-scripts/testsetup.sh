#!/bin/sh
iadmin mkuser test1 rodsadmin

iadmin moduser test1 password test

iadmin aua test1 test1DN

iadmin mkuser test2 rodsuser

iadmin moduser test2 password test

iadmin mkuser test3 rodsuser

iadmin moduser test3 password test

iadmin mkresc test1-resc "unix file system" cache localhost "/opt/iRODS/iRODS3.3/Vault1"

iadmin mkresc test1-resc2 "unix file system" cache localhost "/opt/iRODS/iRODS3.3/Vault2"

iadmin atrg testResourceGroup test1-resc2

iadmin mkuser anonymous rodsuser

iadmin mkgroup jargonTestUg

iadmin atg jargonTestUg test1

iadmin atg jargonTestUg test3

