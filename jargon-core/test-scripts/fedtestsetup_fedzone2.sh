#!/bin/sh

exec `iadmin mkzone test1 remote test1:1247`

exec `iadmin mkuser rods#test1 rodsuser`

exec `iadmin mkuser test1#test1 rodsuser`

exec `imkdir /fedzone2/home/test1/fedread`

exec `imkdir /fedzone2/home/test1/fedwrite`

exec `ichmod -r inherit /fedzone2/home/test1/fedread`

exec `ichmod -r inherit /fedzone2/home/test1/fedwrite`

exec `ichmod  read test1#fedzone1 /fedzone2/home/test1/fedread`

exec `ichmod  write test1#fedzone1 /fedzone2/home/test1/fedwrite`	


