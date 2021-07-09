#! /bin/bash

# Start the Postgres database.
service postgresql start
counter=0
until pg_isready -q
do
    sleep 1
    ((counter += 1))
done
echo Postgres took approximately $counter seconds to fully start ...

# Set up iRODS.
python /var/lib/irods/scripts/setup_irods.py < /var/lib/irods/packaging/localhost_setup_postgres.input
cp /server_config.json /etc/irods/server_config.json
cp /jargon_extras.re /etc/irods/jargon_extras.re


echo Now restart for new server_config.json
su irods -c 'python /var/lib/irods/scripts/irods_control.py restart'
echo Completed iRODS restart

echo Running Test Setup Script
su irods -c '/testsetup-consortium.sh'
echo Completed Test Setup Script

# Keep container running if the test fails.
tail -f /dev/null
# Is this better? sleep 2147483647d

© 2020 GitHub, Inc.

