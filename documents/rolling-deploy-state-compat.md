# سازگاری state در rolling / blue-green deploy

این سند قانون‌های کار با state مشترک را وقتی **نسخه قدیمی و جدید برنامه هم‌زمان** بالا هستند جمع می‌کند
(rolling / blue-green deploy). هر دو cohort روی backendهای مشترک کار می‌کنند: Redis، Postgres/Oracle،
Consul KV، ZooKeeper، Kafka. تغییر شکل serialization یا کلید در نسخه جدید نباید نسخه قدیمیِ هنوز در حال اجرا را
خراب کند و برعکس.

> backward-compatibility لازم نیست (هنوز production نیست). کلیدهای قدیمیِ یتیم با TTL پاک می‌شوند؛ نیازی به
> migration داده نیست.

## اصل اصلی: فقط cache نسخه‌دار می‌شود، coordination نه

نسخه‌دار کردن برای **cache** درست است (جدا بودن keyspace دو cohort مطلوب است؛ یک entry با فرمت قدیمی فقط
miss می‌شود و دوباره پر می‌شود). اما برای **coordination state** غلط است، چون کارشان همین اشتراک بین دو cohort
است:

| مکانیزم | اگر نسخه‌دارش کنی هنگام rolling deploy |
| --- | --- |
| کلید idempotency / dedupe | دو cohort «قبلاً پردازش‌شده» همدیگر را نمی‌بینند → اجرای تکراری |
| قفل توزیع‌شده (ShedLock، Consul lease) | هر cohort قفل خودش را می‌گیرد → هر دو وارد ناحیه بحرانی |
| consumer-group کافکا | دو group → هر پیام دوبار مصرف |

پس: **cache نسخه‌دار، coordination ثابت.**

## نسخه per-cache (نه یک نسخه سراسری)

هر cache **کلید version مستقل خودش** را دارد. چرا per-cache و نه یک کلید مرکزی: یک bump سراسری همهٔ cacheها را
هم‌زمان باطل می‌کند → cold-start سنگین و افت کارایی روی برنامهٔ در حال اجرا. با کلید مستقل فقط cacheی که
serialization‌اش عوض شده bump می‌شود؛ بقیه گرم می‌مانند.

| cache | کلید config | پیش‌فرض روی missing |
| --- | --- | --- |
| query cache (pangaea) | `platform.dispatcher.query.cache-version` | boot fail |
| OAuth2 token cache (pangaea) | `platform.security.oauth2-client.cache.version` | boot fail |
| JWKS L2 (pangaea) | `platform.security.jwks.cache.version` | boot fail |
| envelope trusted-key (pangaea) | `platform.envelope.trusted.cache.version` | boot fail |
| Spring `@Cacheable` (nova RedisCacheManager) | `nova.cache.version` | boot fail |
| branch-coverage (nova) | `nova.branch-coverage.cache.version` | boot fail |
| reply cache (FCB `NovaReplyCache`) | ZK `NOVA_REPLY_CACHE_VERSION` | fallback `v1` |

ابزار مشترک: `CacheKeyspace.versioned(base, version)` در `pangaea-commons-core` — فقط رشته می‌سازد
(`base + version + ":"`)، نسخه را مرکزی نمی‌کند.

## چرا «خواندن یک‌بار در startup» امن است

نسخه از config server خوانده می‌شود ولی **یک‌بار هنگام boot** (نه `@RefreshScope`). موقع شروعِ roll مقدار را
bump کن: instanceهای قدیمی مقدارِ قبل از bump را در boot خودشان گرفته‌اند و تا پایان عمرشان نگه می‌دارند؛
instanceهای جدید بعد از bump مقدار جدید را می‌خوانند → دو cohort جدا. (instance قدیمی که restart شود به
keyspace جدید می‌پیوندد — بی‌ضرر، cache خودش را دوباره پر می‌کند.)

- nova / pangaea: کلید **اجباری**؛ نبودنش boot را fail می‌کند (هیچ fallback خاموشی نیست) — منبع: nova-config
  روی Consul KV.
- FCB: کلید **fallback `v1`** دارد تا core قدیمی روی کلید گمشده fail نکند — منبع: nova-fcb-zk-config روی
  ZooKeeper.

> cacheهای JWKS سمت FCB (`NovaJwksRedisCache`، bundle در `SsoTokenKeyResolver`) self-healing هستند (miss →
> refetch، بدون وابستگی به FQN) و فعلاً نسخه‌دار نشده‌اند؛ در صورت نیاز با همین الگوی fallback اضافه می‌شوند.

## بقیهٔ stateهای مشترک که هنگام deploy شکننده‌اند (قانون عملیاتی)

این‌ها در این تغییر **نسخه‌دار نشده‌اند**؛ قانون رعایت‌شان این است:

1. **outbox / inbox payload** (`AbstractOutboxEventEntity.payload` + ستون `event_type` به‌صورت FQN؛ inbox
   pangaea با `@type` و allowlist `ir.dotin.*`؛ FCB `NovaOutboxEntry` با `Class.forName`):
   Jackson با `FAIL_ON_UNKNOWN_PROPERTIES=false` تغییرِ **افزایشی** فیلد را امن می‌کند. **rename/جابه‌جایی
   کلاس و حذف فیلد شکننده است** → نام FQN و رشته‌های discriminator (`@JsonSubTypes name=`) را ثابت نگه دار؛
   قبل از deploy شکننده outbox/inbox را **drain** کن.
2. **workflow run state** (`run_data` JSONB + `run_data_class` FQN + `step_results`): runهای نیمه‌تمام یک
   cohort ممکن است روی cohort دیگر resume نشوند → قبل از deploy شکننده runهای در جریان را drain کن.
3. **DB migration**: سیاست **expand/contract** — deploy N فقط افزایشی (بدون `DROP`/`RENAME`/`NOT NULL` جدید)؛
   تغییر مخرب در deploy N+1 بعد از رفتن cohort قدیمی.
4. **قرارداد wire (Kafka/Artemis)**: تغییر شکننده ⇒ topic جدید `.vN+1` (pangaea ADR-0021)؛ گیت per-message
   header **نه** (در LN-59375 برگشت خورد).
5. **config (Consul KV / ZooKeeper)**: فقط افزایشی؛ هر کلید جدیدِ اجباری باید روی هر دو cohort موجود باشد
   (یا سمت کد default داشته باشد) تا cohort قدیمی هم boot شود.

## گارد رگرسیون

تست `CacheKeyVersioningGuardTest` (در `pangaea-service-layer-starter`) دو چیز را قفل می‌کند: query cache
نسخه‌دارِ config است، و prefixهای idempotency (`idempotency:` / `idempotency:lock:`) **بدون version** می‌مانند
تا این طبقه‌بندی به‌اشتباه نشکند.
