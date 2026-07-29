#!/bin/sh
# Creates an Artemis broker instance (primary or backup) with replication HA,
# static cluster connectors, redistribution, and enterprise address-settings.
set -e

INSTANCE=/var/lib/artemis-instance
ROLE=${ARTEMIS_ROLE:-live}                 # live | backup
NAME=${ARTEMIS_NODE_NAME:-broker}
BIND_HOST=$(hostname)
CLUSTER_NODES=${ARTEMIS_CLUSTER_NODES:?set ARTEMIS_CLUSTER_NODES e.g. tcp://host1:61616,tcp://host2:61616}

if [ ! -d "$INSTANCE/bin" ]; then
  /opt/activemq-artemis/bin/artemis create "$INSTANCE" \
    --name "$NAME" \
    --user "$ARTEMIS_USER" --password "$ARTEMIS_PASSWORD" \
    --cluster-user "$ARTEMIS_USER" --cluster-password "$ARTEMIS_PASSWORD" \
    --http-host 0.0.0.0 \
    --host "$BIND_HOST" \
    --allow-anonymous --require-login \
    --clustered --static-cluster "$CLUSTER_NODES" \
    --no-autotune \
    --default-address-settings

  BROKER_XML="$INSTANCE/etc/broker.xml"

  # ---- HA policy (current element names) ----
  if [ "$ROLE" = "live" ]; then
    HA='<ha-policy><replication><primary><check-for-active-server>true</check-for-active-server></primary></replication></ha-policy>'
  else
    HA='<ha-policy><replication><backup><allow-failback>true</allow-failback></backup></replication></ha-policy>'
  fi
  sed -i "s|</cluster-connections>|</cluster-connections>\n      $HA|" "$BROKER_XML"

  # ---- enable redistribution (generated config ships with redistribution-delay=-1 = OFF) ----
  sed -i "s|<message-load-balancing>ON_DEMAND</message-load-balancing>|<message-load-balancing>ON_DEMAND</message-load-balancing>\n            <redistribution-delay>1000</redistribution-delay>|" "$BROKER_XML"

  # ---- address-settings for the nova.fcb namespace ----
  read -r -d '' ADDR <<'XML' || true

         <address-setting match="nova.fcb.#">
            <dead-letter-address>DLQ</dead-letter-address>
            <expiry-address>ExpiryQueue</expiry-address>
            <max-delivery-attempts>3</max-delivery-attempts>
            <redelivery-delay>1000</redelivery-delay>
            <redelivery-delay-multiplier>2.0</redelivery-delay-multiplier>
            <max-redelivery-delay>10000</max-redelivery-delay>
            <address-full-policy>PAGE</address-full-policy>
            <max-size-bytes>104857600</max-size-bytes>
            <page-size-bytes>10485760</page-size-bytes>
            <default-queue-routing-type>ANYCAST</default-queue-routing-type>
            <default-address-routing-type>ANYCAST</default-address-routing-type>
            <auto-create-queues>true</auto-create-queues>
            <auto-delete-queues>false</auto-delete-queues>
            <auto-create-addresses>true</auto-create-addresses>
            <auto-delete-addresses>false</auto-delete-addresses>
            <persist-delivery-count-before-delivery>true</persist-delivery-count-before-delivery>
         </address-setting>
XML
  # insert before the closing </address-settings>
  awk -v block="$ADDR" '{ if ($0 ~ /<\/address-settings>/ && !done) { print block; done=1 } print }' \
      "$BROKER_XML" > "$BROKER_XML.new" && mv "$BROKER_XML.new" "$BROKER_XML"

  # ---- advertise host IP:port to external clients ----
  if [ -n "$ARTEMIS_ADVERTISE_PORT" ] && [ -n "$SERVER_IP" ]; then
    sed -i "s|<connector name=\"artemis\">tcp://[^<]*</connector>|<connector name=\"artemis\">tcp://${SERVER_IP}:${ARTEMIS_ADVERTISE_PORT}</connector>|" "$BROKER_XML"
  fi
fi

chown -R artemis:artemis "$INSTANCE"
exec su artemis -s /bin/sh -c "exec $INSTANCE/bin/artemis run"
