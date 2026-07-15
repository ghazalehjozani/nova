<div dir="rtl">

# Redis در پروژه Nova چه کاری انجام می‌دهد؟

## جواب خیلی کوتاه

Redis در Nova یک **حافظهٔ موقت، سریع و مشترک** است.

Nova بعضی اطلاعات را از سیستم‌های دیگر می‌گیرد یا محاسبه می‌کند. اگر برای هر درخواست دوباره همان کار را انجام دهد،
پاسخ‌گویی کندتر می‌شود و فشار بیشتری به سرویس‌های دیگر وارد می‌شود. Nova نتیجه را برای مدت محدودی در Redis نگه
می‌دارد تا درخواست بعدی بتواند آن را سریع‌تر دریافت کند.

Redis در این پروژه **پایگاه دادهٔ اصلی وام‌ها نیست**. اطلاعات اصلی و ماندگار کسب‌وکار در دیتابیس رابطه‌ای نگهداری
می‌شوند. حذف شدن یک cache entry باید باعث cache miss و دریافت دوبارهٔ اطلاعات شود، نه از بین رفتن اطلاعات وام.

## یک مثال ساده

فرض کنید Nova برای بررسی دسترسی یک شعبه باید از FCB بپرسد آن شعبه چه شعبه‌هایی را پوشش می‌دهد:

<div dir="ltr">

```text
Request
   |
   v
Check Redis ---- cache hit ----> Return cached result quickly
   |
 cache miss
   |
   v
Ask FCB ---> Return result ---> Save temporarily in Redis
```

</div>

در درخواست اول ممکن است Nova مجبور شود از FCB سؤال کند. درخواست‌های بعدی تا پایان TTL پاسخ ذخیره‌شده را از Redis
می‌گیرند. مقدار پیش‌فرض TTL برای `branch-coverage` در کد ۱۰ دقیقه است.

## Redis دقیقاً برای چه چیزهایی استفاده می‌شود؟

### ۱. cache اطلاعات امنیتی

در config پروژه cacheهایی مانند موارد زیر تعریف شده‌اند:

- `oauth2-tokens`
- `jwt-jwks`
- `delegation:accessTokenCache`

این اطلاعات برای احراز هویت، دریافت token و بررسی امضای پیام‌ها استفاده می‌شوند. cache کردن آن‌ها تعداد درخواست‌های
تکراری به سرویس‌های امنیتی را کم می‌کند و پاسخ‌گویی را سریع‌تر می‌کند.

### ۲. cache پوشش شعبه‌ها

کلاس `CachingBranchCoverageAdapter` فهرست شعبه‌های تحت پوشش هر شعبه را در Redis نگه می‌دارد. این داده بین همهٔ
instanceهای Nova مشترک است؛ بنابراین لازم نیست هر instance نتیجه را جداگانه از FCB بگیرد.

اگر مقدار در Redis وجود نداشته باشد، Nova آن را از FCB می‌گیرد و دوباره در Redis ذخیره می‌کند. اگر Redis موقتاً
خطا بدهد، این adapter به منبع اصلی یعنی FCB برمی‌گردد.

### ۳. cacheهای مشترک framework

بخش‌هایی از platform نیز برای query cache، token cache، JWKS و trusted key دارای keyspace نسخه‌دار هستند. نسخه‌دار
بودن کلیدها کمک می‌کند هنگام rolling deployment، نسخهٔ قدیمی و جدید برنامه cache ناسازگار یکدیگر را نخوانند.

## چرا فقط از حافظهٔ خود برنامه استفاده نمی‌کنیم؟

Nova می‌تواند چند instance داشته باشد. حافظهٔ معمولی هر instance فقط متعلق به همان process است:

- اگر instance شمارهٔ ۱ مقداری را در حافظهٔ خودش بگذارد، instance شمارهٔ ۲ آن را نمی‌بیند.
- با restart شدن instance، حافظهٔ محلی آن پاک می‌شود.
- هر instance ممکن است دوباره همان درخواست پرهزینه را به FCB یا سرویس امنیتی بفرستد.

Redis خارج از process برنامه اجرا می‌شود و همهٔ instanceها می‌توانند از یک cache مشترک استفاده کنند. این موضوع فشار
روی dependencyها را کمتر و رفتار instanceها را هماهنگ‌تر می‌کند.

## نکتهٔ مهم دربارهٔ cacheهای FCB

پنج lookup نسبتاً ثابت FCB در کد فعلی ابتدا به `Caffeine`، یعنی cache داخل حافظهٔ هر instance، هدایت می‌شوند:

- `fcb.economical-sector`
- `fcb.economical-sector-by-code`
- `fcb.sector-for-loan-type`
- `fcb.resource`
- `fcb.reason-type`

TTL این cache محلی ۳۰ ثانیه است. `LoanStaticCacheConfig` این پنج نام را به `CaffeineCacheManager` می‌دهد و نام‌های
دیگر را به `RedisCacheManager` می‌فرستد. بنابراین در مسیر عادی `@Cacheable`، این پنج مورد از cache محلی استفاده
می‌کنند؛ Redis همچنان cache مشترک سایر نام‌ها و دادهٔ `branch-coverage` است.

## اگر Redis قطع شود چه اتفاقی می‌افتد؟

برای cacheهای متعلق به Nova، طراحی تا حد ممکن **fail-open** است:

- خطای cache نباید مستقیماً عملیات اصلی کسب‌وکار را متوقف کند.
- `ResilientCacheErrorHandler` خطاهای Spring Cache را ثبت می‌کند و اجازه می‌دهد مسیر اصلی ادامه پیدا کند.
- `CachingBranchCoverageAdapter` هنگام خطای Redis مستقیماً سراغ FCB می‌رود.

نتیجه این است که قطعی Redis معمولاً باعث cache miss، درخواست بیشتر به dependencyها و کندتر شدن سیستم می‌شود؛ اما
نباید به معنی حذف اطلاعات اصلی وام باشد. البته اگر قطعی طولانی شود، فشار اضافه روی FCB و سرویس‌های امنیتی می‌تواند
خودش به یک مشکل عملیاتی تبدیل شود.

## Redis Cluster چه چیزی را تغییر می‌دهد؟

در working tree فعلی، اتصال Nova در حال تغییر از Redis Sentinel به Redis Cluster است. این تغییر دربارهٔ نحوهٔ اتصال،
توزیع داده و تحمل خرابی Redis است؛ **هدف کسب‌وکاری Redis را عوض نمی‌کند**. Redis همچنان cache سریع و مشترک برنامه
باقی می‌ماند.

## جمع‌بندی

هدف Redis در Nova این است که:

1. پاسخ اطلاعات تکراری سریع‌تر شود.
2. درخواست‌های غیرضروری به FCB و سرویس‌های امنیتی کمتر شود.
3. چند instance برنامه cache مشترک داشته باشند.
4. با TTL، اطلاعات موقت بعد از مدتی خودکار منقضی و دوباره از منبع اصلی دریافت شوند.

پس ساده‌ترین تعریف این است:

> Redis حافظهٔ موقت و مشترک Nova برای افزایش سرعت و کاهش فشار روی سیستم‌های دیگر است؛ نه محل اصلی نگهداری اطلاعات
> وام.

## فایل‌های مرتبط در پروژه

- `adapters/driven/persistence/.../config/RedisConfig.java`
- `adapters/driven/persistence/.../config/LoanStaticCacheConfig.java`
- `adapters/driven/persistence/.../config/ResilientCacheErrorHandler.java`
- `adapters/driven/persistence/.../authz/CachingBranchCoverageAdapter.java`
- `nova-config/kv/core/loan/nova/application/cache.yml`

</div>
