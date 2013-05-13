#!/bin/sh
iadmin mkzone fedZone2 remote fedZone2:1247

iadmin mkuser rods#fedZone2 rodsuser

iadmin mkuser test1#fedZone2 rodsuser

imkdir /fedZone1/home/test1/fedread

imkdir /fedZone1/home/test1/fedwrite

ichmod -r inherit /fedZone1/home/test1/fedread

ichmod -r inherit /fedZone1/home/test1/fedwrite

ichmod  read test1#fedZone2 /fedZone1/home/test1/fedread

ichmod  write test1#fedZone2 /fedZone1/home/test1/fedwrite	


