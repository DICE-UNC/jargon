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
python3 /var/lib/irods/scripts/setup_irods.py < /var/lib/irods/packaging/localhost_setup_postgres.input
su - irods -c './irodsctl -v start'

echo Running Test Setup Script
su irods -c '/testsetup-consortium.sh'
echo Completed Test Setup Script

# Keep container running if the test fails.
sleep 2147483647d
