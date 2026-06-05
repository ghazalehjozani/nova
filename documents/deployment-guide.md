<div dir="rtl">

# راهنمای استقرار Nova (Trade-Loan)

راهنمای کوتاه و کاربردی برای استقرار سرویس Nova و وابستگی‌هایش. برای جزئیات عملیاتی هر مؤلفه به runbookهای
`documents/runbooks/` مراجعه کنید (Consul: RB-0003، Liquibase: RB-0004، کلید/JWKS: RB-0005، TLS: RB-0006،
Redis: RB-0007، Artemis: RB-0008، Postgres: RB-0009).

## مخازن (origin)

| مخزن                          | نقش                                       | origin                                                                            |
|-------------------------------|-------------------------------------------|-----------------------------------------------------------------------------------|
| `nova`                        | سرویس trade-loan (همین مخزن)              | `https://hub.dotin.ir/gitlab/expenditures/customer-based/nova.git`                |
| `nova-config`                 | کانفیگ runtime روی Consul KV              | `https://hub.dotin.ir/gitlab/expenditures/customer-based/nova-config.git`         |
| `nova-fcb-zk-config`          | کانفیگ سمت FCB روی ZooKeeper              | `https://hub.dotin.ir/gitlab/expenditures/customer-based/nova-fcb-zk-config.git`  |
| `platform-consul`             | image سرور Consul + حلقهٔ git2consul-sync | `https://hub.dotin.ir/gitlab/platform/platform-consul.git`                        |

## پیش‌نیازها

- Java 25+، Maven 3.9.11+
- Docker / Docker Compose (برای infra لوکال و build image)
- دسترسی به Consul، ZooKeeper، Kafka، broker و Postgres محیط مقصد
- Kubernetes (محیط prod) یا Docker (محیط لوکال/تست)

## مدل استقرار در یک نگاه

- کانفیگ سرویس در **Consul KV** است (مخزن `nova-config`)؛ روی host یک `bootstrap.yml` فقط آدرس Consul و
  `spring.application.name=nova-service` را دارد. هیچ `application.yml` لوکالی وجود ندارد.
- **secretها هیچ‌وقت در Consul نیستند.** به‌صورت env (`${ENV_VAR}`) تزریق می‌شوند و Spring بعد از pull کانفیگ
  از Consul آن‌ها را resolve می‌کند. لوکال → `.env` (gitignore)، prod → k8s secret / Vault.
- سمت FCB کانفیگش از **ZooKeeper** می‌آید (مخزن `nova-fcb-zk-config`، هر فایل = یک znode).
- Nova و FCB فقط از طریق Kafka/Artemis (corridor) با هم حرف می‌زنند؛ وابستگی type-level ندارند.

## ترتیب استقرار

<div dir="ltr">

```text
1. dependencyها را install/از Nexus بگیر: pangaea, base-loan, expression-kit
2. nova را build کن: jar + Docker image
3. Consul را آماده کن (ACL + KV) و nova-config را sync کن            → RB-0003
4. infra را بالا بیاور: Postgres (RB-0009), Redis Sentinel (RB-0007), Artemis (RB-0008)
5. secretها را ست کن (env یا k8s secret)
6. migration دیتابیس را اجرا کن (Liquibase به‌صورت Job)               → RB-0004
7. nova را deploy کن (k8s یا docker)
8. سمت FCB: znodeها را از nova-fcb-zk-config در ZooKeeper بارگذاری کن
```

</div>

### build و deploy (خلاصه)

<div dir="ltr">

```bash
# 1) dependencyها (اگر روی Nexus نیستند)
mvn -f pangaea/pom.xml clean install -DskipTests
mvn -f base-loan/pom.xml clean install -DskipTests
mvn -f expression-kit/pom.xml clean install -DskipTests

# 2) build سرویس (jar + image)
cd nova
mvn clean package -Pk8s,spring-boot-application -pl :trade-loan-container -am
docker build -t trade-loan-service:latest container/

# 7) deploy روی k8s
kubectl apply -f container/k8s/base/
```

</div>

## ست کردن envهای سرویس (`nova/container/.env.example`)

`cp .env.example .env` و مقادیر را پر کن. این فایل **فقط** چیزی است که خود اپ در runtime می‌خواند؛ هرگز commit
نمی‌شود و هرگز در Consul نمی‌رود. در prod این مقادیر را orchestrator / secret manager تزریق می‌کند.

🔑 = secret (خالی نگذار، در prod از secret store)، ⚲ = با `nova-dev-stack/.env` مشترک است و باید یکسان باشد.

| گروه                | کلیدها                                                                                                                              | توضیح                                                                                                              |
|---------------------|-------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| Profile             | `SPRING_PROFILES_ACTIVE`                                                                                                            | `dev` لوکال؛ در prod معمولاً unset یا مقدار محیط. `e2e` مخصوص تست است، در deploy واقعی نزن.                        |
| Consul              | `CONSUL_HOST` `CONSUL_PORT` `CONSUL_SCHEME` `CONSUL_ACL_TOKEN`🔑                                                                    | آدرس Consul + توکن ACL با policy `nova-service-config-read` (RB-0003). TLS با `CONSUL_TLS_*` (RB-0006).            |
| Datasource          | `DB_HOST` `DB_PORT` `DB_NAME` `DB_USERNAME`⚲🔑 `DB_PASSWORD`⚲🔑                                                                     | اتصال Postgres. در prod پشت HAProxy/Patroni، `DB_HOST`/`DB_PORT` را روی پورت write (leader) بگذار (RB-0009).       |
| Redis               | `REDIS_MASTER_NAME`⚲ `REDIS_PASSWORD`⚲🔑 `REDIS_SENTINEL_NODES` `REDIS_TLS_ENABLED`                                                 | discovery از طریق Sentinel. `REDIS_SENTINEL_NODES` لیست `host:port` سه sentinel است (RB-0007).                     |
| Kafka               | `KAFKA_BOOTSTRAP_SERVERS` `KAFKA_USER`🔑 `KAFKA_PASSWORD`🔑 `KAFKA_SECURITY_PROTOCOL` `KAFKA_SASL_MECHANISM` `KAFKA_CONSUMER_GROUP` | brokerها (`SASL_PLAINTEXT`/`SCRAM-SHA-256`). hardening به `SASL_SSL` در RB-0006.                                   |
| Artemis             | `ACTIVEMQ_HOST` `ACTIVEMQ_PORT` `ARTEMIS_USER`⚲🔑 `ARTEMIS_PASSWORD`⚲🔑                                                             | اتصال broker (فقط client). انتخاب transport (`transport-mode`) در Consul است نه اینجا (RB-0008/RB-0013).           |
| FCB (legacy runner) | `FCB_BASE_URL` `FCB_USERNAME`🔑 `FCB_PASSWORD`🔑                                                                                    | فقط برای usecase-runner منسوخ. مسیر اصلی Nova↔FCB از Kafka/Artemis است.                                            |
| OAuth2              | `OAUTH2_CLIENT_ID` `OAUTH2_CLIENT_SECRET`🔑                                                                                         | client اعتبارسنجی توکن.                                                                                            |
| Envelope keystore   | `ENVELOPE_SIGNER_PRIVATE_KEY_REF` `ENVELOPE_KEYSTORE_PASSWORD`🔑 `ENVELOPE_KEY_PASSWORD`🔑                                          | keystore امضای پاکت (PKCS12). در prod از مسیر mount شدهٔ secret (`file:/run/secrets/...`)، نه default (RB-0005).   |
| MCP                 | `MCP_BUSINESS_TOKEN`🔑 `MCP_OPS_TOKEN`🔑                                                                                            | bearer هر endpoint MCP. خالی = آن endpoint fail-closed (هر request برابر 401). تولید با `openssl rand -base64 36`. |
| OpenTelemetry       | `OTEL_EXPORTER_OTLP_ENDPOINT` `OTEL_EXPORTER_OTLP_HEADERS`🔑 `OTEL_SERVICE_NAME` `OTEL_TRACES_SAMPLER_ARG` …                        | export به collector. `OTEL_SERVICE_NAME=nova-service`.                                                             |
| Platform            | `PLATFORM_ENV` `HIBERNATE_DDL_AUTO` `SHOW_SQL` `SERVER_PORT`                                                                        | `HIBERNATE_DDL_AUTO=validate` (هیچ‌وقت `update`/`create` در prod).                                                 |

> نکته: کلیدهای ⚲ (یعنی `DB_NAME` `DB_USERNAME` `DB_PASSWORD` `REDIS_PASSWORD` `REDIS_MASTER_NAME`
> `ARTEMIS_USER` `ARTEMIS_PASSWORD`) دو نسخه دارند — اینجا سمت client و در `nova-dev-stack/.env` سمت server.
> مقدارشان باید **دقیقاً یکی** باشد.

## infra لوکال (`nova-dev-stack/.env.example`)

برای dev، infra را با این مخزن بالا می‌آوری (در prod این‌ها سرویس‌های واقعی‌اند):

<div dir="ltr">

```bash
cd nova-dev-stack
cp .env.example .env && nano .env          # DB_*, REDIS_PASSWORD, ARTEMIS_* — هم‌مقدار با nova/container/.env
./redis/render-sentinel-conf.sh            # sentinel-{1,2,3}.conf را می‌سازد (قبل از up حتماً)
docker compose up -d                       # postgres + redis HA + artemis (broker اصلی)
```

</div>

کلیدهای مهم این فایل: bootstrap پستگرس (`DB_*`)، auth سمت server ردیس (`REDIS_PASSWORD` `REDIS_MASTER_NAME`)،
port mapping و **announce host** ردیس (`REDIS_*_ANNOUNCE_HOST/PORT` — اگر اپ روی host است
`host.docker.internal`؛ اگر هم‌شبکه است نام container)، و creds آرتمیس. هر بار announce host عوض شد دوباره
`render-sentinel-conf.sh` بزن. Artemis و ActiveMQ Classic هر دو پورت `61616`/`8161` را می‌گیرند و mutually
exclusive‌اند؛ ActiveMQ Classic منسوخ است.

## broker آرتمیس — کانفیگ لازم برای req/reply

مسیر sync نوا↔FCB روی آرتمیس از الگوی **named reply queue per-instance** استفاده می‌کند: هر instance نوا موقع
بالا آمدن دو صف نام‌دار می‌سازد — یکی برای req/reply یکپارچه (`nova.fcb.integration.reply.<instanceId>`) و یکی
برای JWKS (`nova.fcb.jwks.reply.<instanceId>`). FCB پاسخ را روی همان (`JMSReplyTo` + `JMSCorrelationID`) می‌فرستد
و نوا با یک correlation-map داخلی جواب را match می‌کند. از **temporary queue استفاده نمی‌کنیم** — temp queue هم
connection-scoped است (پاسخِ transacted روی session ورودیِ FCB به آن نمی‌رسد) و هم node-local است (روی broker
چندنودی/HA پاسخ روی نود اشتباه گیر می‌کند). همین دو دلیل، علتِ ۱۰۰٪ timeout قبلی بود. RB-0008.

**deadline به‌جای header، با TTL روی broker:** نوا روی پیام request یک `producer.setTimeToLive(timeout)` می‌گذارد،
پس اگر FCB در بازهٔ مجاز پیام را مصرف نکند، خودِ broker (یک ساعت واحد) آن را expire می‌کند. headerهای
`X-Request-Deadline-Epoch-Ms` / `X-Request-Timestamp-Epoch-Ms` و gateِ `NovaRequestDeadlineEvaluator` حذف شدند —
دیگر مقایسهٔ epoch بین ساعتِ دو host وجود ندارد (freshnessِ envelope با `platform.envelope.max-age` جداست و می‌ماند).
JWKS هم فقط روی آرتمیس است؛ znodeها و keyهای Kafka-JWKS حذف شدند.

**optionهای broker** (روی addressهای `nova.fcb.*.reply.#`): کلِ تنظیمات لازم در
`nova-dev-stack/artemis/broker-address-settings.xml` آماده است؛ آن `<address-settings>` را در broker.xml محیط prod
merge کن.

1. **lifecycle صف** — `auto-create-queues` + `auto-delete-queues` (با delay): reply queue هر instance خودکار ساخته
   و بعد از down شدن instance (قطع consumer) خودکار پاک شود. addressهای request/jwks پایدارند → `auto-delete` خاموش.
2. **redistribution-delay = 0** — broker چندنودی/cluster: پاسخی که روی نودی بدون consumer می‌نشیند به نودِ دارای
   consumer منتقل شود. پیش‌فرض `-1` یعنی بدون redistribution؛ روی تک‌نود no-op.
3. **DLA + expiry + paging** — پیام poison/expire/over-full به‌جای OOM به DLA/Expiry برود و backpressure بدهد.

<div dir="ltr">

```xml
<!-- broker.xml — merge from nova-dev-stack/artemis/broker-address-settings.xml -->
<address-setting match="nova.fcb.integration.reply.#">
  <auto-create-queues>true</auto-create-queues>
  <auto-delete-queues>true</auto-delete-queues>
  <auto-delete-queues-delay>10000</auto-delete-queues-delay>
  <default-address-routing-type>ANYCAST</default-address-routing-type>
  <redistribution-delay>0</redistribution-delay>
</address-setting>
<!-- same for nova.fcb.jwks.reply.# ; nova.fcb.# carries DLA/expiry/paging (auto-delete OFF) -->
```

</div>

> dev تک‌نود (`nova-dev-stack`) روی **پیش‌فرض‌های آرتمیس** کار می‌کند (auto-create/auto-delete روشن‌اند،
> redistribution روی تک‌نود بی‌اثر است)، پس آنجا broker.xml سفارشی mount نمی‌شود. در **prod / چندنودی** فایل
> `broker-address-settings.xml` را در broker.xml بگذار (نام دقیق elementها را با نسخهٔ آرتمیس مقصد تطبیق بده).
> صحت: یک `make manual STEP=originate` باید بدون `LOAN#3001` پاسخ بگیرد.

## prod-hardening — TLS و secretها (پیش‌نیاز قبل از prod)

این موارد در ring تست (`LIN00127`) عمداً ساده‌اند؛ قبل از prod باید بسته شوند:

- **TLS روی brokerها.** Kafka اکنون `SASL_PLAINTEXT` و آرتمیس `tcp://` است (ترافیک broker روی شبکهٔ داخلی plaintext).
  برای prod: Kafka را روی `SASL_SSL` و آرتمیس را روی `tcp+ssl://` (با truststore) ببر. این کار brokerهای live تست را
  می‌شکند تا cert تأمین شود، پس به‌صورت کنترل‌شده در prod اعمال شود — در ring تست flip نکن.
- **secretها بیرونِ git.** در `nova-config` همه‌چیز `${ENV_VAR}` است (بدون literal). در `nova-fcb-zk-config` ringِ تست
  literal دارد (طبق طراحیِ همان repo)؛ برای prod یک env directory تازه با `${ENV_VAR}` بساز و CI را با
  `STRICT_SECRETS=1` بزن. هیچ literalِ `ARTEMIS_PASSWORD` / `*_PASSWORD` / `SSO_CLIENT_SECRET` / keystore `changeit` در
  مسیر prod نماند.
- **`auto.offset.reset` روی consumer درخواست.** در ring تست `earliest` است؛ برای prod روی znodeهای
  `nova-fcb-integration-consumer` آن را `latest` کن تا بعد از reset گروه، backlogِ کهنهٔ درخواست‌ها دوباره پخش نشود
  (idempotency پشتیبان است).

## کانفیگ Consul (`nova-config`)

`nova-config` منبع truth کانفیگ runtime است (YAML زیر `kv/core/loan/...`). container `platform-consul` با حلقهٔ
`git2consul-sync` آن را به Consul KV می‌ریزد (prod از طریق pipeline). جزئیات ACL، توکن و sync در RB-0003.
هیچ secretی در `nova-config` نیست — فقط placeholder `${ENV_VAR}`.

## کانفیگ سمت FCB روی ZooKeeper (`nova-fcb-zk-config`)

هر فایل `.properties` زیر `zk/<root>/<env>/<group>/<znode>.properties` دقیقاً یک znode است. محیط فعلی:
`zk/core-release/LIN00127/eventbus/`. znodeها:

<div dir="ltr">

```text
default-configuration            nova-fcb-jwks-request-consumer    nova-recon
nova-base-config                 nova-fcb-jwks-response-producer   nova-sso
nova-fcb-artemis                 nova-installment-operation-producer
nova-fcb-integration-consumer    nova_loan_consumer
nova-fcb-integration-producer    nova-outbox
```

</div>

نام فایل = نام znode؛ `_` و `-` معنادارند، تغییرشان نده. در این مخزن هر فایل «یک جفت KEY=VALUE در هر خط، مرتب
الفبایی، بدون `///`» است؛ روی wire، payload با `///` به هم وصل می‌شود.

### بارگذاری در ZooKeeper

<div dir="ltr">

```bash
cd nova-fcb-zk-config
make validate                              # چک layout/syntax/کلیدهای لازم
STRICT_SECRETS=1 make validate             # برای env prod: plaintext secret = خطا

make export FILE=zk/core-release/LIN00127/eventbus/nova-sso.properties   # یک znode (payload با /// )
make export-all                            # همهٔ znodeها، بخش‌بخش، آمادهٔ paste
```

</div>

خروجی `export`/`export-all` همان payloadِ `///`-joined است که در ZooKeeper سمت FCB paste/بارگذاری می‌شود
(هنوز اسکریپت write مستقیم به ZK وجود ندارد؛ paste دستی). نکتهٔ secret: env تست (`LIN00127`) عمداً مقدار
literal دارد (`nova-secret`/`changeit`)؛ برای env prod با `STRICT_SECRETS=1` و ارجاع `${ENV_VAR}` کار کن.

## بعد از استقرار (چک سریع)

- health: `GET /actuator/health` روی `UP`.
- کانفیگ زنده: `GET /actuator/configprops` (یا log مربوط به refresh) — مقدار آمد از Consul؟
- اتصال‌ها: Postgres (pool)، Redis (Sentinel master)، Kafka/Artemis (corridor)، JWKS بین Nova و FCB.
- جزئیات تشخیص هر مؤلفه: runbookهای `documents/runbooks/`.

</div>
