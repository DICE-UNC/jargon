#!/bin/sh


exec `iadmin mkuser archappladm rodsuser`

exec `iadmin moduser archappladm password test`

exec `iadmin mkuser ruleadm rodsuser`

exec `iadmin moduser ruleadm password test`

exec `iadmin mkuser archadm rodsuser`

exec `iadmin moduser archadm password test`

exec `iadmin mkuser collcur rodsuser`

exec `iadmin moduser collcur password test`

exec `iadmin mkuser arch1 rodsuser`

exec `iadmin moduser arch1 password test`

exec `iadmin mkuser arch2 rodsuser`

exec `iadmin moduser arch2 password test`

exec `iadmin mkgroup archive_admin`

exec `iadmin mkgroup archive_appl_admin`

exec `iadmin mkgroup archive_rule_admin`

exec `iadmin mkgroup collection_curator`

exec `iadmin mkgroup archivist`

exec 'iadmin atg archive_admin archadm'

exec 'iadmin atg archive_appl_admin archappladm'

exec 'iadmin atg rule_admin ruleadm'

exec 'iadmin atg collection_curator collcur'

exec 'iadmin atg archivist arch1'

exec 'iadmin atg archivist arch2'

exec 'iadmin atg collection_curator arch2'