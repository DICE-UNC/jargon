#!/bin/sh

iadmin mkzone fedZone1 remote fedZone1:1247

iadmin mkuser rods#fedZone1 rodsuser

iadmin mkuser test1#fedZone1 rodsuser

imkdir /fedZone2/home/test1/fedread

imkdir /fedZone2/home/test1/fedwrite

ichmod -r inherit /fedZone2/home/test1/fedread

ichmod -r inherit /fedZone2/home/test1/fedwrite

ichmod  read test1#fedZone1 /fedZone2/home/test1/fedread

ichmod  write test1#fedZone1 /fedZone2/home/test1/fedwrite	


