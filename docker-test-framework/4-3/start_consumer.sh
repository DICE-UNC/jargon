#! /bin/bash

topo_provider="irods-catalog-provider"

# Wait until the provider is up and accepting connections.
echo 'Waiting for setup of iRODS Provider to accept connections ...'
until nc -z $topo_provider 1247; do
    sleep 1
done

# Wait until the provider is started again.
echo 'Giving the iRODS Provider a few seconds to restart ...'
sleep 10
until nc -z $topo_provider 1247; do
    sleep 1
done

# Rsyslog must be started before iRODS so that the log messages
# are written to the correct file.
rsyslogd

# Set up iRODS.
python3 /var/lib/irods/scripts/setup_irods.py < /irods_consumer.input
su - irods -c './irodsctl -v start'
echo 'iRODS Consumer is ready.'

# Keep container running if the test fails.
sleep 2147483647d
