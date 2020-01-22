#! /bin/bash

topo_provider="irods-catalog-provider-4-2-7"

# Wait until the provider is up and accepting connections.
until nc -z $topo_provider 1247; do
    sleep 1
done

# Set up iRODS.
sed -i '5s/.*/'$topo_provider'/'
python /var/lib/irods/scripts/setup_irods.py < /irods_consumer.input

# Keep container running if the test fails.
tail -f /dev/null
# Is this better? sleep 2147483647d

