#!/bin/sh
exec `iadmin mkzone fedzone2 remote fedzone2:1247`

exec `iadmin mkuser rods#fedzone2 rodsuser`

exec `iadmin mkuser test1#fedzone2 rodsuser`

exec `imkdir /test1/home/test1/fedread`

exec `imkdir /test1/home/test1/fedwrite`

exec `ichmod -r inherit /test1/home/test1/fedread`

exec `ichmod -r inherit /test1/home/test1/fedwrite`

exec `ichmod  read test1#fedzone2 /test1/home/test1/fedread`

exec `ichmod  write test1#fedzone2 /test1/home/test1/fedwrite`	


