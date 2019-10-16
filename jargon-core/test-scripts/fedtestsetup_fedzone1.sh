#!/bin/sh
iadmin mkzone zone2 remote zone2:1247

iadmin mkuser rods#zone2 rodsuser

iadmin mkuser test1#zone2 rodsuser

imkdir /zone1/home/test1/fedread

imkdir /zone1/home/test1/fedwrite

ichmod -r inherit /zone1/home/test1/fedread

ichmod -r inherit /zone1/home/test1/fedwrite

ichmod  read test1#zone2 /zone1/home/test1/fedread

ichmod  write test1#zone2 /zone1/home/test1/fedwrite	


