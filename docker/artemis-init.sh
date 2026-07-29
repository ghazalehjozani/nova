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
    --no-autotune
  # NOTE: do NOT add --default-address-settings: it is not an `artemis create` option (never was).
  # The generated broker.xml already ships an <address-settings> section (activemq.management# +
  # catch-all # with DLQ/ExpiryQueue defaults), which is exactly what the insertions below extend.

  BROKER_XML="$INSTANCE/etc/broker.xml"

  # ---- HA policy (current element names) ----
  if [ "$ROLE" = "live" ]; then
    HA='<ha-policy><replication><primary><check-for-active-server>true</check-for-active-server></primary></replication></ha-policy>'
  else
    HA='<ha-policy><replication><backup><allow-failback>true</allow-failback></backup></replication></ha-policy>'
  fi
  sed -i "s|</cluster-connections>|</cluster-connections>\n      $HA|" "$BROKER_XML"

  # ---- persist delivery count before delivery (exact DLQ accounting under max-delivery-attempts) ----
  # This is a CORE-level element (child of <core>), NOT an address-setting: inside <address-setting>
  # it fails XSD validation (cvc-complex-type.2.4.a) and the broker refuses to boot.
  # configurationType is xsd:all, so any position inside <core> is valid.
  sed -i "s|</core>|      <persist-delivery-count-before-delivery>true</persist-delivery-count-before-delivery>\n   </core>|" "$BROKER_XML"

  # NOTE: <redistribution-delay> is an ADDRESS-SETTING element. It must NOT be placed inside
  # <cluster-connection> (it is not valid there) — redistribution is enabled per matching
  # address in the address-settings below. Message load balancing stays ON_DEMAND in the
  # cluster-connection; the two work together.

  # ---- address-settings for the nova.fcb namespace ----
  # Address settings merge hierarchically: the reply-namespace settings below inherit
  # everything from nova.fcb.# and only override the auto-delete attributes.
  read -r -d '' ADDR <<'XML' || true

         <address-setting match="nova.fcb.#">
            <dead-letter-address>DLQ</dead-letter-address>
            <expiry-address>ExpiryQueue</expiry-address>
            <max-delivery-attempts>3</max-delivery-attempts>
            <redelivery-delay>1000</redelivery-delay>
            <redelivery-delay-multiplier>2.0</redelivery-delay-multiplier>
            <max-redelivery-delay>10000</max-redelivery-delay>
            <redistribution-delay>1000</redistribution-delay>
            <address-full-policy>PAGE</address-full-policy>
            <max-size-bytes>104857600</max-size-bytes>
            <page-size-bytes>10485760</page-size-bytes>
            <default-queue-routing-type>ANYCAST</default-queue-routing-type>
            <default-address-routing-type>ANYCAST</default-address-routing-type>
            <auto-create-queues>true</auto-create-queues>
            <auto-delete-queues>false</auto-delete-queues>
            <auto-create-addresses>true</auto-create-addresses>
            <auto-delete-addresses>false</auto-delete-addresses>
         </address-setting>

         <!-- Per-instance reply queues are ephemeral by nature: self-clean 60s after the last
              consumer detaches (covers failover gaps; prevents stale reply queues piling up as
              instances churn). Requests queues are deliberately NOT auto-deleted — an auto-deleted
              queue takes its unconsumed (TTL-less) request messages with it: silent loss. -->
         <address-setting match="nova.fcb.integration.reply.#">
            <auto-delete-queues>true</auto-delete-queues>
            <auto-delete-queues-delay>60000</auto-delete-queues-delay>
         </address-setting>
         <address-setting match="nova.fcb.jwks.reply.#">
            <auto-delete-queues>true</auto-delete-queues>
            <auto-delete-queues-delay>60000</auto-delete-queues-delay>
         </address-setting>
XML
  # insert before the closing </address-settings>
  awk -v block="$ADDR" '{ if ($0 ~ /<\/address-settings>/ && !done) { print block; done=1 } print }' \
      "$BROKER_XML" > "$BROKER_XML.new" && mv "$BROKER_XML.new" "$BROKER_XML"

  # fail LOUD at init if the generated template ever stops shipping <address-settings>
  # (a silent no-op here cost a full debugging session once already)
  grep -q 'match="nova.fcb.#' "$BROKER_XML" \
    || { echo "FATAL: nova.fcb.# address-setting not present after insertion — template drift?"; exit 1; }

  # ---- pre-create DLQ + ExpiryQueue (deterministic; no reliance on auto-create-* defaults) ----
  # Recent `artemis create` templates may already contain them — insert only when absent.
  if ! grep -q 'name="DLQ"' "$BROKER_XML"; then
    read -r -d '' ADDRS <<'XML' || true
         <address name="DLQ">
            <anycast>
               <queue name="DLQ" />
            </anycast>
         </address>
         <address name="ExpiryQueue">
            <anycast>
               <queue name="ExpiryQueue" />
            </anycast>
         </address>
XML
    awk -v block="$ADDRS" '{ print; if ($0 ~ /<addresses>/ && !done) { print block; done=1 } }' \
        "$BROKER_XML" > "$BROKER_XML.new" && mv "$BROKER_XML.new" "$BROKER_XML"
  fi

  # ---- advertise host IP:port to external clients ----
  if [ -n "$ARTEMIS_ADVERTISE_PORT" ] && [ -n "$SERVER_IP" ]; then
    sed -i "s|<connector name=\"artemis\">tcp://[^<]*</connector>|<connector name=\"artemis\">tcp://${SERVER_IP}:${ARTEMIS_ADVERTISE_PORT}</connector>|" "$BROKER_XML"
  fi
fi

chown -R artemis:artemis "$INSTANCE"
exec su artemis -s /bin/sh -c "exec $INSTANCE/bin/artemis run"
