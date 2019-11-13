#!/bin/sh

iadmin mkzone zone1 remote zone1:1247

iadmin mkuser rods#zone1 rodsuser

iadmin mkuser test1#zone1 rodsuser

imkdir /zone2/home/test1/fedread

imkdir /zone2/home/test1/fedwrite

ichmod -r inherit /zone2/home/test1/fedread

ichmod -r inherit /zone2/home/test1/fedwrite

ichmod  read test1#zone1 /zone2/home/test1/fedread

ichmod  write test1#zone1 /zone2/home/test1/fedwrite	


