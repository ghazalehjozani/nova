#!/bin/bash
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
  # <group-name> pins a backup to one primary: a backup only pairs with a primary announcing the same
  # group. Without it pairing is first-come-first-served across all three pairs, so a restart can leave
  # one primary unbacked and another double-backed (observed: backup1 re-synced to a different primary).
  # Valid inside both <primary> and <backup> (replicationPrimaryPolicyType/replicationBackupPolicyType,
  # both xsd:all, so position within the element does not matter). Empty ARTEMIS_HA_GROUP = old behaviour.
  GROUP_XML=""
  if [ -n "${ARTEMIS_HA_GROUP:-}" ]; then
    GROUP_XML="<group-name>${ARTEMIS_HA_GROUP}</group-name>"
  fi
  if [ "$ROLE" = "live" ]; then
    HA="<ha-policy><replication><primary>${GROUP_XML}<check-for-active-server>true</check-for-active-server></primary></replication></ha-policy>"
  else
    HA="<ha-policy><replication><backup>${GROUP_XML}<allow-failback>true</allow-failback></backup></replication></ha-policy>"
  fi
  sed -i "s|</cluster-connections>|</cluster-connections>\n      $HA|" "$BROKER_XML"

  # ---- persist delivery count before delivery (exact DLQ accounting under max-delivery-attempts) ----
  # This is a CORE-level element (child of <core>), NOT an address-setting: inside <address-setting>
  # it fails XSD validation (cvc-complex-type.2.4.a) and the broker refuses to boot.
  # configurationType is xsd:all, so any position inside <core> is valid.
  sed -i "s|</core>|      <persist-delivery-count-before-delivery>true</persist-delivery-count-before-delivery>\n   </core>|" "$BROKER_XML"

  # ---- global-max-size: deliberate, not inherited ----
  # The template leaves it commented out, so it defaults to half of -Xmx — a number nobody chose and
  # that moves whenever the heap does. Six brokers share one host; an explicit cap makes each one start
  # paging at a known point instead of racing the others for memory and then for disk.
  sed -i "s|</core>|      <global-max-size>${ARTEMIS_GLOBAL_MAX_SIZE:-512Mb}</global-max-size>\n   </core>|" "$BROKER_XML"

  # ---- Artemis Studio prerequisites: notifications, slow-consumer visibility, unbounded management reads ----
  # Studio needs a live feed of CONSUMER_CREATED/MESSAGE_DELIVERED/etc (activemq.notifications), needs
  # slow-consumer-* to be present on the catch-all to report native slow-consumer detection instead of
  # falling back to its own ackRatePerConsumer rule, and needs message bodies/properties unmuted in
  # management responses (default truncates them). SEND_SESSION_NOTIFICATIONS does not exist on this
  # plugin (verified against the shipped 2.44 class — only Connection/Address/Delivered/Expired do);
  # session lifecycle rides SEND_CONNECTION_NOTIFICATIONS instead.
  sed -i 's|<address-setting match="#">|<address-setting match="#">\n            <slow-consumer-threshold>1</slow-consumer-threshold>\n            <slow-consumer-threshold-measurement-unit>MESSAGES_PER_SECOND</slow-consumer-threshold-measurement-unit>\n            <slow-consumer-check-period>5</slow-consumer-check-period>\n            <slow-consumer-policy>NOTIFY</slow-consumer-policy>\n            <management-message-attribute-size-limit>-1</management-message-attribute-size-limit>|' "$BROKER_XML"
  sed -i "s|</core>|      <broker-plugins>\n         <broker-plugin class-name=\"org.apache.activemq.artemis.core.server.plugin.impl.NotificationActiveMQServerPlugin\">\n            <property key=\"SEND_CONNECTION_NOTIFICATIONS\" value=\"true\"/>\n            <property key=\"SEND_ADDRESS_NOTIFICATIONS\"    value=\"true\"/>\n            <property key=\"SEND_DELIVERED_NOTIFICATIONS\"  value=\"true\"/>\n            <property key=\"SEND_EXPIRED_NOTIFICATIONS\"    value=\"true\"/>\n         </broker-plugin>\n      </broker-plugins>\n   </core>|" "$BROKER_XML"

  # ---- Studio management write access on the two management-adjacent namespaces ----
  # security-settings is most-specific-match-wins, not merged: the catch-all match="#" already grants
  # role amq every permission listed here (send/consume/browse/manage/create+deleteNonDurableQueue on
  # activemq.management.# and activemq.notifications), so these two blocks are functionally redundant
  # today. They exist to mirror docs/standards/artemis-studio-broker-settings.md exactly, so that doc's
  # corrected (browse-inclusive, role-correct) form is proven here before the prod team applies it.
  sed -i 's|</security-settings>|         <security-setting match="activemq.management.#">\n            <permission type="createNonDurableQueue" roles="amq"/>\n            <permission type="deleteNonDurableQueue" roles="amq"/>\n            <permission type="createAddress"         roles="amq"/>\n            <permission type="deleteAddress"         roles="amq"/>\n            <permission type="send"                  roles="amq"/>\n            <permission type="consume"               roles="amq"/>\n            <permission type="browse"                roles="amq"/>\n            <permission type="manage"                roles="amq"/>\n         </security-setting>\n         <security-setting match="activemq.notifications">\n            <permission type="createNonDurableQueue" roles="amq"/>\n            <permission type="deleteNonDurableQueue" roles="amq"/>\n            <permission type="consume"               roles="amq"/>\n            <permission type="browse"                roles="amq"/>\n         </security-setting>\n      </security-settings>|' "$BROKER_XML"

  # NOTE: <redistribution-delay> is an ADDRESS-SETTING element. It must NOT be placed inside
  # <cluster-connection> (it is not valid there) — redistribution is enabled per matching
  # address in the address-settings below. Message load balancing stays ON_DEMAND in the
  # cluster-connection; the two work together.

  # ---- address-settings for the nova.fcb namespace ----
  # Address settings merge hierarchically: the reply-namespace settings below inherit
  # everything from nova.fcb.# and only override auto-delete and redistribution.
  # NOTE: unquoted heredoc delimiter (not <<'XML') — deliberate, so
  # ${ARTEMIS_NOVA_FCB_PAGE_LIMIT_BYTES:-...} below expands. No other $ or ` appears in this block.
  read -r -d '' ADDR <<XML || true

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
            <!-- Auto-create is off for the namespace as a whole: a typo'd destination would otherwise
                 silently create a live address that nothing ever reads. The per-instance reply
                 namespaces re-enable it below (their names are not knowable up front); the two fixed
                 request addresses are pre-declared in <addresses>. -->
            <auto-create-queues>false</auto-create-queues>
            <auto-delete-queues>false</auto-delete-queues>
            <auto-create-addresses>false</auto-create-addresses>
            <auto-delete-addresses>false</auto-delete-addresses>
            <!-- PAGE with no page-limit spills to disk without bound; the only backstop is
                 max-disk-usage, which blocks every producer on every address once hit — the
                 ~54k-message DLQ/ExpiryQueue incident below was the graveyard half of this same
                 failure mode. FAIL (not DROP) here: a lost FCB request/reply is a lost business
                 operation and the caller must see the exception, not silence. Size this against
                 real disk headroom for the environment it runs in — it is NOT the same number on
                 every host (dev's /var has ~7.5G free across all six brokers; do not copy this
                 figure onto a host with different capacity without recomputing it). -->
            <page-limit-bytes>${ARTEMIS_NOVA_FCB_PAGE_LIMIT_BYTES:-536870912}</page-limit-bytes>
            <page-full-policy>FAIL</page-full-policy>
         </address-setting>

         <!-- Per-instance reply queues are ephemeral by nature: self-clean 60s after the last
              consumer detaches (covers failover gaps; prevents stale reply queues piling up as
              instances churn). Requests queues are deliberately NOT auto-deleted — an auto-deleted
              queue takes its unconsumed (TTL-less) request messages with it: silent loss. -->
         <!-- redistribution-delay -1: a per-instance reply queue has one consumer on one node.
              Inheriting nova.fcb.#'s 1000 ships its messages to dead copies on the other nodes. -->
         <!-- auto-delete-addresses: the reply ADDRESS name is <prefix><instanceId> and instanceId is
              KUBERNETES_POD_NAME (config/nova-config fcb.yml), a new value on every pod restart/deploy.
              auto-delete-queues alone reaps the queue 60s after its one consumer detaches, but
              auto-delete-addresses is inherited from nova.fcb.# above, where it is deliberately false —
              so without this override the ADDRESS survives forever and every rollout leaks two more of
              them into the bindings journal on all six brokers. -->
         <address-setting match="nova.fcb.integration.reply.#">
            <auto-create-queues>true</auto-create-queues>
            <auto-create-addresses>true</auto-create-addresses>
            <auto-delete-queues>true</auto-delete-queues>
            <auto-delete-queues-delay>60000</auto-delete-queues-delay>
            <auto-delete-addresses>true</auto-delete-addresses>
            <auto-delete-addresses-delay>60000</auto-delete-addresses-delay>
            <redistribution-delay>-1</redistribution-delay>
         </address-setting>
         <address-setting match="nova.fcb.jwks.reply.#">
            <auto-create-queues>true</auto-create-queues>
            <auto-create-addresses>true</auto-create-addresses>
            <auto-delete-queues>true</auto-delete-queues>
            <auto-delete-queues-delay>60000</auto-delete-queues-delay>
            <auto-delete-addresses>true</auto-delete-addresses>
            <auto-delete-addresses-delay>60000</auto-delete-addresses-delay>
            <redistribution-delay>-1</redistribution-delay>
         </address-setting>

         <!-- DLQ and ExpiryQueue have no consumers. Uncapped they grow until the disk fills, which is
              exactly what happened: ~54k messages accumulated silently and took max-disk-usage with
              them, blocking every producer. DROP is the only sane full-policy for a graveyard — PAGE
              is what filled the disk, and BLOCK would stall the address that feeds them. -->
         <address-setting match="DLQ">
            <address-full-policy>DROP</address-full-policy>
            <max-size-bytes>52428800</max-size-bytes>
         </address-setting>
         <address-setting match="ExpiryQueue">
            <address-full-policy>DROP</address-full-policy>
            <max-size-bytes>52428800</max-size-bytes>
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

  # ---- pre-declare the two fixed request addresses (auto-create is off for nova.fcb.#) ----
  # Names and routing type mirror exactly what auto-create used to produce: queue name == address name,
  # ANYCAST. FCB consumes from these; Nova only produces.
  if ! grep -q 'name="nova.fcb.integration.request.v1"' "$BROKER_XML"; then
    read -r -d '' REQ_ADDRS <<'XML' || true
         <address name="nova.fcb.integration.request.v1">
            <anycast>
               <queue name="nova.fcb.integration.request.v1" />
            </anycast>
         </address>
         <address name="nova.fcb.jwks.request.v1">
            <anycast>
               <queue name="nova.fcb.jwks.request.v1" />
            </anycast>
         </address>
XML
    awk -v block="$REQ_ADDRS" '{ print; if ($0 ~ /<addresses>/ && !done) { print block; done=1 } }' \
        "$BROKER_XML" > "$BROKER_XML.new" && mv "$BROKER_XML.new" "$BROKER_XML"
  fi

  # ---- advertise host IP:port to external clients ----
  if [ -n "$ARTEMIS_ADVERTISE_PORT" ] && [ -n "$SERVER_IP" ]; then
    sed -i "s|<connector name=\"artemis\">tcp://[^<]*</connector>|<connector name=\"artemis\">tcp://${SERVER_IP}:${ARTEMIS_ADVERTISE_PORT}</connector>|" "$BROKER_XML"
  fi

  # ---- allow the console's real access origin through Jolokia's CORS check ----
  # `artemis create --http-host 0.0.0.0` auto-generates jolokia-access.xml with
  # <allow-origin>*://0.0.0.0*</allow-origin> — literally the bind host, not where
  # anyone actually browses the console from. Since the console is reached at
  # http://${SERVER_IP}:81xx, the browser's real Origin header fails Jolokia's
  # strict-checking CORS rule and the console falls back to its generic "No Artemis
  # broker at this agent" message (a CORS rejection, not a missing broker).
  JOLOKIA_ACCESS="$INSTANCE/etc/jolokia-access.xml"
  if [ -n "$SERVER_IP" ] && [ -f "$JOLOKIA_ACCESS" ]; then
    sed -i "s|<allow-origin>\*://0.0.0.0\*</allow-origin>|<allow-origin>*://0.0.0.0*</allow-origin>\n        <allow-origin>*://${SERVER_IP}*</allow-origin>|" "$JOLOKIA_ACCESS"
  fi
fi

chown -R artemis:artemis "$INSTANCE"
exec su artemis -s /bin/sh -c "exec $INSTANCE/bin/artemis run"
