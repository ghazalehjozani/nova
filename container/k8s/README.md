# راهنمای استقرار Trade Loan Service در Kubernetes

## فهرست مطالب

- [نمای کلی](#نمای-کلی)
- [پیش‌نیازها](#پیش‌نیازها)
- [ساختار دایرکتوری](#ساختار-دایرکتوری)
- [راه‌اندازی اولیه](#راه‌اندازی-اولیه)
- [استقرار در محیط Dev](#استقرار-در-محیط-dev)
- [استقرار در محیط Production](#استقرار-در-محیط-production)
- [مدیریت ConfigMap](#مدیریت-configmap)
- [مدیریت Secrets](#مدیریت-secrets)
- [RefreshScope و به‌روزرسانی تنظیمات](#refreshscope-و-به‌روزرسانی-تنظیمات)
- [عملیات رایج](#عملیات-رایج)

## نمای کلی

این دایرکتوری شامل تمام فایل‌های Kubernetes مورد نیاز برای استقرار سرویس Trade Loan است. از Kustomize برای مدیریت تنظیمات مختلف محیط‌های Dev و Production استفاده می‌شود.

## پیش‌نیازها

### ابزارهای مورد نیاز
```bash
# kubectl
kubectl version --client

# kustomize (اختیاری - kubectl از نسخه 1.14 به بعد kustomize داخلی دارد)
kustomize version

# kubeseal (برای مدیریت secrets)
kubeseal --version

# minikube (برای محیط توسعه)
minikube version
```

### دسترسی‌های مورد نیاز
- دسترسی به Kubernetes cluster (dev یا prod)
- دسترسی به Docker registry برای push کردن image
- دسترسی به Sealed Secrets controller (برای محیط production)

## ساختار دایرکتوری

```
k8s/
├── README.md                          # این فایل
├── base/                              # منابع پایه مشترک
│   ├── namespace.yml                  # تعریف namespace
│   ├── rbac.yml                       # ServiceAccount و مجوزها
│   ├── configmap-trade-loan.yml       # تنظیمات غیرحساس
│   ├── deployment.yml                 # تعریف Deployment
│   └── service.yml                    # تعریف Service
└── overlays/                          # تنظیمات خاص هر محیط
    └── dev/
        ├── kustomization.yml          # تنظیمات Kustomize برای dev
        ├── sealed-secret.yml          # اطلاعات حساس رمزشده
        └── resources-patch.yml        # تنظیمات منابع production

```

## راه‌اندازی اولیه

### 1. راه‌اندازی Minikube (برای محیط Dev)

```bash
# شروع Minikube با منابع کافی
minikube start --cpus=4 --memory=8192 --driver=docker

# فعال‌سازی addon های مورد نیاز
minikube addons enable ingress
minikube addons enable metrics-server

# بررسی وضعیت
kubectl get nodes
```

### 2. نصب Sealed Secrets Controller

```bash
# نصب controller در cluster
kubectl apply -f https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.24.0/controller.yaml

# بررسی نصب
kubectl get pods -n kube-system | grep sealed-secrets
```

### 3. ساخت Docker Image

```bash
# از ریشه پروژه
mvn clean package -Pk8s,spring-boot-application

# ساخت image
docker build -t trade-loan-service:latest

# برای minikube
minikube image load trade-loan-service:latest

# برای registry
docker tag trade-loan-service:latest dotin-registry/trade-loan-service:latest
docker push dotin-registry/trade-loan-service:latest
```

## استقرار در محیط Dev

### مرحله 1: همگام‌سازی ConfigMap

```bash
# از container
./scripts/sync-configs.sh

# بررسی محتوای ConfigMap
cat k8s/base/configmap-trade-loan.yml
```

### مرحله 2: ایجاد Secrets

```bash
# ایجاد فایل secret موقت (این فایل را commit نکنید)
cat > /tmp/secret.yml <<EOF
apiVersion: v1
kind: Secret
metadata:
  name: trade-loan-service-secrets
  namespace: default
type: Opaque
stringData:
  spring.datasource.url: jdbc:postgresql://postgres:5432/postgres
  spring.datasource.username: sudoit
  spring.datasource.password: p@sSw0rd
  platform.messaging.encryption.password: DevEncryptionPassword123
EOF

# رمزنگاری secret
kubeseal -f /tmp/secret.yml -w k8s/overlays/dev/sealed-secret.yml

# حذف فایل موقت
rm /tmp/secret.yml
```

### مرحله 3: استقرار

```bash
# استقرار با kustomize
kubectl apply -k k8s/overlays/dev/

# بررسی وضعیت
kubectl get pods -w

# مشاهده logs
kubectl logs -f deployment/trade-loan-service

# بررسی سلامت سرویس
kubectl port-forward service/trade-loan-service 8080:8080
curl http://localhost:8080/actuator/health
```

### مرحله 4: دسترسی به سرویس

```bash
# Port forward برای دسترسی مستقیم
kubectl port-forward service/trade-loan-service 8080:8080

# یا با NodePort
kubectl expose deployment trade-loan-service --type=NodePort --port=8080
minikube service trade-loan-service --url
```

## استقرار در محیط Production

### تفاوت‌های محیط Production

1. تعداد replica بیشتر (3 عدد)
2. محدودیت منابع تعریف شده
3. ddl-auto روی validate
4. سطح logging کمتر
5. استفاده از secrets واقعی

### مرحله 1: آماده‌سازی Namespace

```bash
# ایجاد namespace production
kubectl create namespace production

# تنظیم context
kubectl config set-context --current --namespace=production
```

### مرحله 2: ایجاد Production Secrets

```bash
# ایجاد secret با اطلاعات واقعی production
cat > /tmp/prod-secret.yml <<EOF
apiVersion: v1
kind: Secret
metadata:
  name: trade-loan-service-secrets
  namespace: production
type: Opaque
stringData:
  spring.datasource.url: jdbc:postgresql://prod-postgres.database:5432/trade-loan
  spring.datasource.username: trade_loan_user
  spring.datasource.password: STRONG_PRODUCTION_PASSWORD_HERE
  platform.messaging.encryption.password: STRONG_ENCRYPTION_KEY_HERE
EOF

# رمزنگاری با کلید public cluster production
kubeseal -f /tmp/prod-secret.yml -w k8s/overlays/prod/sealed-secret.yml \
  --controller-namespace kube-system \
  --controller-name sealed-secrets-controller

# حذف ایمن فایل موقت
shred -u /tmp/prod-secret.yml
```

### مرحله 3: استقرار

```bash
# استقرار در production
kubectl apply -k k8s/overlays/prod/

# مانیتور کردن rollout
kubectl rollout status deployment/trade-loan-service -n production

# بررسی pods
kubectl get pods -n production

# بررسی logs
kubectl logs -f deployment/trade-loan-service -n production
```

### مرحله 4: بررسی سلامت

```bash
# بررسی health endpoint
kubectl port-forward -n production service/trade-loan-service 8080:8080
curl http://localhost:8080/actuator/health

# بررسی metrics
curl http://localhost:8080/actuator/metrics

# بررسی readiness
kubectl get pods -n production -o wide
```

## مدیریت ConfigMap

### مشاهده ConfigMap فعلی

```bash
# مشاهده محتوای ConfigMap
kubectl get configmap trade-loan-service-config -o yaml

# یا با فرمت خوانا
kubectl describe configmap trade-loan-service-config
```

### به‌روزرسانی ConfigMap

```bash
# روش 1: ویرایش مستقیم (موقت)
kubectl edit configmap trade-loan-service-config

# روش 2: به‌روزرسانی از سورس (توصیه می‌شود)
# ویرایش container/src/main/resources/application.yml
# سپس:
./scripts/sync-configs.sh
kubectl apply -k k8s/overlays/dev/
```

### حذف و ایجاد مجدد ConfigMap

```bash
kubectl delete configmap trade-loan-service-config
kubectl apply -k k8s/overlays/dev/
```

## مدیریت Secrets

### نکات امنیتی

1. هرگز فایل‌های secret خام را commit نکنید
2. همیشه از SealedSecret استفاده کنید
3. دسترسی به kubectl را محدود کنید
4. secrets را به صورت دوره‌ای rotate کنید

### چرخش (Rotation) Secrets

```bash
# ایجاد secret جدید
cat > /tmp/new-secret.yml <<EOF
apiVersion: v1
kind: Secret
metadata:
  name: trade-loan-service-secrets
  namespace: default
stringData:
  spring.datasource.password: NEW_PASSWORD_HERE
  platform.messaging.encryption.password: NEW_ENCRYPTION_KEY
EOF

# رمزنگاری
kubeseal -f /tmp/new-secret.yml -w k8s/overlays/dev/sealed-secret.yml

# اعمال تغییرات
kubectl apply -k k8s/overlays/dev/

# restart pods برای اعمال secret جدید
kubectl rollout restart deployment/trade-loan-service

# حذف ایمن
shred -u /tmp/new-secret.yml
```

### مشاهده Secrets (فقط در صورت ضرورت)

```bash
# مشاهده نام secrets
kubectl get secrets

# مشاهده محتوا (نیاز به دسترسی admin)
kubectl get secret trade-loan-service-secrets -o jsonpath='{.data.spring\.datasource\.password}' | base64 -d
```

## RefreshScope و به‌روزرسانی تنظیمات

Spring Cloud Kubernetes از reload خودکار ConfigMap پشتیبانی می‌کند.

### فعال‌سازی Auto-Reload

در `bootstrap.yml` تنظیم شده است:

```yaml
spring:
  cloud:
    kubernetes:
      reload:
        enabled: true
        mode: event        # یا polling
        strategy: refresh  # استفاده از @RefreshScope
```

### به‌روزرسانی بدون Downtime

```bash
# 1. به‌روزرسانی ConfigMap
kubectl edit configmap trade-loan-service-config

# 2. فعال‌سازی refresh endpoint
POD_NAME=$(kubectl get pod -l app=trade-loan-service -o jsonpath='{.items[0].metadata.name}')
kubectl exec -it $POD_NAME -- curl -X POST http://localhost:8080/actuator/refresh

# یا با port-forward
kubectl port-forward service/trade-loan-service 8080:8080
curl -X POST http://localhost:8080/actuator/refresh
```

### Restart کامل (در صورت نیاز)

```bash
# Rolling restart
kubectl rollout restart deployment/trade-loan-service

# بررسی وضعیت
kubectl rollout status deployment/trade-loan-service
```

## عملیات رایج

### مقیاس‌دهی (Scaling)

```bash
# افزایش تعداد replicas
kubectl scale deployment/trade-loan-service --replicas=3

# بررسی
kubectl get pods

# برگشت به حالت قبل
kubectl scale deployment/trade-loan-service --replicas=1
```

### مشاهده Logs

```bash
# logs تمام pods
kubectl logs -l app=trade-loan-service

# logs یک pod خاص
kubectl logs trade-loan-service-xxxx-yyyy

# follow logs
kubectl logs -f deployment/trade-loan-service

# logs 100 خط آخر
kubectl logs --tail=100 deployment/trade-loan-service

# logs با فیلتر زمانی
kubectl logs --since=1h deployment/trade-loan-service
```

### اجرای دستورات داخل Pod

```bash
# دسترسی به shell
kubectl exec -it trade-loan-service-xxxx-yyyy -- /bin/sh

# اجرای دستور مستقیم
kubectl exec trade-loan-service-xxxx-yyyy -- env | grep SPRING

# بررسی متغیرهای محیطی
kubectl exec trade-loan-service-xxxx-yyyy -- printenv
```

### بررسی Resource Usage

```bash
# استفاده CPU و Memory
kubectl top pods

# جزئیات یک pod
kubectl top pod trade-loan-service-xxxx-yyyy

# describe برای اطلاعات کامل
kubectl describe pod trade-loan-service-xxxx-yyyy
```

### کپی فایل از/به Pod

```bash
# کپی از pod به local
kubectl cp trade-loan-service-xxxx-yyyy:/app/logs/app.log ./local-app.log

# کپی از local به pod
kubectl cp ./config.json trade-loan-service-xxxx-yyyy:/app/config/
```

## عیب‌یابی

### Pod راه‌اندازی نمی‌شود

```bash
# بررسی وضعیت pod
kubectl get pods
kubectl describe pod trade-loan-service-xxxx-yyyy

# بررسی events
kubectl get events --sort-by='.lastTimestamp'

# بررسی logs
kubectl logs trade-loan-service-xxxx-yyyy
kubectl logs trade-loan-service-xxxx-yyyy --previous  # logs قبل از restart
```

### مشکلات ConfigMap

```bash
# بررسی وجود ConfigMap
kubectl get configmap trade-loan-service-config

# بررسی محتوا
kubectl get configmap trade-loan-service-config -o yaml

# بررسی mount شدن در pod
kubectl describe pod trade-loan-service-xxxx-yyyy | grep -A 5 Mounts
```

### مشکلات Secret

```bash
# بررسی وجود secret
kubectl get secret trade-loan-service-secrets

# بررسی استفاده در pod
kubectl describe pod trade-loan-service-xxxx-yyyy | grep -A 10 Environment

# تست دسترسی به secret
kubectl exec trade-loan-service-xxxx-yyyy -- env | grep SPRING_DATASOURCE
```

### مشکلات شبکه

```bash
# بررسی Service
kubectl get service trade-loan-service
kubectl describe service trade-loan-service

# تست connectivity از داخل pod
kubectl exec -it trade-loan-service-xxxx-yyyy -- wget -O- http://localhost:8080/actuator/health

# بررسی DNS
kubectl exec -it trade-loan-service-xxxx-yyyy -- nslookup trade-loan-service
```

### مشکلات Database

```bash
# تست اتصال به database
kubectl exec -it trade-loan-service-xxxx-yyyy -- env | grep DATASOURCE

# اگر psql نصب باشد
kubectl exec -it postgres-pod -- psql -U sudoit -d postgres -c "SELECT 1"
```

### مشکلات Image

```bash
# بررسی image pull policy
kubectl describe pod trade-loan-service-xxxx-yyyy | grep -A 3 Image

# لیست images در minikube
minikube image ls | grep trade-loan

# load کردن دوباره image
minikube image load trade-loan-service:latest
kubectl rollout restart deployment/trade-loan-service
```

### بررسی Readiness و Liveness Probes

```bash
# وضعیت probes
kubectl describe pod trade-loan-service-xxxx-yyyy | grep -A 10 Liveness
kubectl describe pod trade-loan-service-xxxx-yyyy | grep -A 10 Readiness

# تست manual
kubectl exec trade-loan-service-xxxx-yyyy -- wget -O- http://localhost:8080/actuator/health/liveness
kubectl exec trade-loan-service-xxxx-yyyy -- wget -O- http://localhost:8080/actuator/health/readiness
```

### حذف و ایجاد مجدد

```bash
# حذف deployment
kubectl delete deployment trade-loan-service

# حذف کامل تمام منابع
kubectl delete -k k8s/overlays/dev/

# ایجاد مجدد
kubectl apply -k k8s/overlays/dev/
```

## Rollback

### برگشت به نسخه قبلی

```bash
# مشاهده تاریخچه
kubectl rollout history deployment/trade-loan-service

# برگشت به نسخه قبلی
kubectl rollout undo deployment/trade-loan-service

# برگشت به نسخه خاص
kubectl rollout undo deployment/trade-loan-service --to-revision=2
```

## پاکسازی

### حذف منابع Dev

```bash
kubectl delete -k k8s/overlays/dev/
```

### حذف منابع Production

```bash
kubectl delete -k k8s/overlays/prod/
```

### توقف Minikube

```bash
minikube stop
minikube delete
```

## نکات امنیتی

1. هرگز credentials را در ConfigMap قرار ندهید
2. همیشه از SealedSecret برای secrets استفاده کنید
3. دسترسی RBAC را محدود کنید
4. secrets را به صورت دوره‌ای rotate کنید
5. از Network Policies برای محدود کردن ترافیک استفاده کنید
6. لاگ‌ها را بررسی کنید تا مطمئن شوید secrets لو نرفته است
