#! /bin/bash

#topo_provider="$1"

# Wait until the provider is up and accepting connections.
#until nc -z icat.example.org 1247; do
    #sleep 1
#done

# Set up iRODS.
/var/lib/irods/packaging/setup_irods.sh < /irods_consumer.input

# Keep container running if the test fails.
tail -f /dev/null
# Is this better? sleep 2147483647d

