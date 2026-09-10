# KariyerBul

Türkçe bir iş ilanı / kariyer platformu. Spring Boot REST API backend'i ve React (Vite) SPA frontend'inden oluşan, tek repo içinde tutulan (monorepo) tam-stack bir uygulama.

```
kariyerbackend/
├── src/            ← Spring Boot backend (Java 21)
├── frontend/        ← React frontend
├── docker-compose.yml
└── Dockerfile
```

## İçindekiler

- [Özellikler](#özellikler)
- [Teknoloji yığını](#teknoloji-yığını)
- [Mimari ve kimlik doğrulama](#mimari-ve-kimlik-doğrulama)
- [Kurulum](#kurulum)
- [Proje yapısı](#proje-yapısı)
- [API özeti](#api-özeti)
- [Veri modeli](#veri-modeli)
- [Test](#test)
- [Bilinen sınırlamalar](#bilinen-sınırlamalar)

## Özellikler

**Aday (USER)**
- Kayıt, 6 haneli kodla e-posta doğrulama, giriş/çıkış, şifremi unuttum
- İlan arama: anahtar kelime, konum, çalışma şekli, sadece-uzaktan filtreleri
- İlana ön yazı ekleyerek başvurma, başvuru durumunu takip etme (`Başvurularım`)
- Başvuru kabul/red edildiğinde e-posta bildirimi
- İlan favorileme (`Favorilerim`)
- Kayıtlı arama kriterine uyan yeni ilan yayınlandığında e-posta uyarısı (`Uyarılarım`)
- Profil: kişisel bilgiler, eğitim, sertifikalar; CV'yi PDF olarak yazdırma

**İşveren (EMPLOYER)**
- İlan oluşturma/düzenleme/silme, ilan bazında görüntülenme sayacı
- Başvuranları listeleme, ön yazıyı görüntüleme, başvuru durumu güncelleme
- Şirket profili: ad, website, logo, açıklama — ilan detay sayfasında adaylara gösterilir

**Admin (ADMIN)**
- İstatistik paneli (kullanıcı/ilan/başvuru sayıları)
- Kullanıcı arama ve silme (ilişkili tüm veriler cascade temizlenir)
- İlan moderasyonu (silme)

## Teknoloji yığını

| | |
|---|---|
| Backend | Spring Boot 4.1, Java 21, Spring Security 7.1, Spring Data JPA / Hibernate 7.4 |
| Veritabanı | Microsoft SQL Server 2022 |
| Auth | JWT (httpOnly cookie), BCrypt, CSRF (cookie tabanlı double-submit) |
| E-posta | Spring Mail + Mailpit (dev SMTP yakalayıcı) |
| API dokümantasyonu | springdoc-openapi (Swagger UI) |
| Frontend | React 19, Vite 8, React Router 7 |
| Test | JUnit 5 + Mockito + MockMvc (backend), Vitest + Testing Library (frontend) |
| Altyapı | Docker Compose (backend + SQL Server + Mailpit) |

## Mimari ve kimlik doğrulama

Backend klasik katmanlı mimaride: `controller` → `service` → `repository`, `dto` ile giriş/çıkış ayrımı, `entity` ile JPA modeli. Servisler arası ilişkiler bilinçli olarak JPA `@ManyToOne` ilişkileri yerine çoğunlukla düz `Long` id alanlarıyla kuruludur (ör. `JobApplication.candidateId`) — bu, N+1 sorgu riskini azaltır ama uygulama katmanında cascade-silme mantığı gerektirir (`AdminService.deleteUser` bunu elle yapar).

**Kimlik doğrulama:** JWT, backend tarafından `access_token` adında **httpOnly** bir cookie'ye yazılır — token hiçbir zaman JSON body'de veya `localStorage`'da bulunmaz, bu yüzden bir XSS açığı token'ı çalamaz. `POST /api/auth/logout` cookie'yi temizler (JS bunu kendi başına yapamaz).

**CSRF:** Cookie tabanlı auth CSRF riski taşıdığından, `/api/auth/**` dışındaki tüm mutasyon istekleri (`POST`/`PUT`/`PATCH`/`DELETE`) `X-XSRF-TOKEN` header'ı gerektirir. Frontend'in `apiFetch` fonksiyonu bunu `XSRF-TOKEN` cookie'sinden otomatik okuyup ekler; backend `CookieCsrfTokenRepository` + `CsrfCookieFilter` ile bu cookie'yi her istekte tazeler.

**Yetkilendirme:** Rol bazlı, `SecurityConfig`'te URL/HTTP-method eşleştirmeleriyle tanımlı (`hasRole("EMPLOYER")` vb.), servis katmanında ayrıca sahiplik kontrolleri (`assertOwner`) yapılır.

**Rate limiting:** `/api/auth/login`, `/register`, `/forgot-password`, `/resend-verification` uç noktaları IP başına dakikada 5 istekle sınırlıdır (bellek içi, tek instance için; yatay ölçeklenirse paylaşımlı bir store'a — ör. Redis — taşınmalı).

## Kurulum

### Tüm sistemi Docker Compose ile çalıştırma (önerilen)

```bash
cp .env.example .env   # DB_*, JWT_* değerlerini doldurun
docker compose up -d --build
```

Bu; SQL Server'ı (`localhost:1434`), Mailpit'i (SMTP: `localhost:1025`, UI: `http://localhost:8025`) ve backend'i (`http://localhost:8081`) ayağa kaldırır. Backend ilk açılışta şemayı otomatik oluşturur (`ddl-auto=update`).

Kod değişikliğinden sonra backend'i yeniden build etmek için:
```bash
docker compose up -d --build backend
```

### Frontend'i ayrı çalıştırma

```bash
cd frontend
npm install
npm run dev
```

`http://localhost:5173`'te açılır. Backend'in `http://localhost:8081`'de (veya `frontend/.env`'deki `VITE_API_URL`) çalışıyor olması, backend'deki `CORS_ALLOWED_ORIGINS`'in bu adresle eşleşmesi gerekir.

### Backend'i Docker olmadan çalıştırma

Yerel bir SQL Server'a `.env`'deki `DB_*` değişkenleriyle bağlanıp:
```bash
./mvnw spring-boot:run
```

### Ortam değişkenleri (root `.env`)

| Değişken | Açıklama |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | SQL Server bağlantısı |
| `JWT_SECRET`, `JWT_EXPIRATION_MS` | Token imzalama/geçerlilik süresi |
| `FRONTEND_URL` | E-posta linklerinde kullanılır (varsayılan `http://localhost:5173`) |
| `CORS_ALLOWED_ORIGINS` | İzinli frontend origin'leri (virgülle ayrık) |
| `COOKIE_SECURE` | `access_token`/CSRF cookie'lerinde `Secure` bayrağı — HTTPS arkasında `true` olmalı |
| `SMTP_HOST`, `SMTP_PORT` | Varsayılan Mailpit (`localhost:1025`) |

Swagger UI: `http://localhost:8081/swagger-ui.html`

## Proje yapısı

**Backend** (`src/main/java/org/example/kariyerbackend/`)
- `controller` — REST uç noktaları
- `service` — iş mantığı, yetkilendirme, e-posta tetikleme
- `repository` — Spring Data JPA arayüzleri
- `entity` — JPA varlıkları
- `dto` — istek/yanıt kayıtları (Java `record`)
- `security` — JWT filtresi, rate limiting, CSRF cookie filtresi
- `config` — `SecurityConfig`, `OpenApiConfig`

**Frontend** (`frontend/src/`)
- `src/pages` — rota bazlı sayfalar (aday, işveren, admin akışları)
- `src/components` — paylaşılan UI bileşenleri (`Navbar`, `JobCard`)
- `src/lib` — backend API istemcileri (`api.js`, `auth.js`, `jobsStore.js`, `alerts.js` vb.) ve `useRequireAuth` gibi paylaşılan hook'lar

Sayfa bazlı erişim kontrolü `frontend/src/lib/useRequireAuth.js` üzerinden:
- Aday (`USER`): `/profile`, `/basvurularim`, `/favorilerim`, `/uyarilarim`
- İşveren (`EMPLOYER`): `/isveren`, `/isveren/profil`, `/isveren/ilan-olustur`, `/isveren/ilan/:id/duzenle`, `/isveren/ilan/:id/basvuranlar`
- Admin (`ADMIN`): `/admin`

## API özeti

| Uç nokta | Yetki |
|---|---|
| `POST /api/auth/{register,login,verify-email,resend-verification,forgot-password,reset-password,logout}` | Herkese açık |
| `GET/PUT /api/employer/profile` | İşveren (kendi profili) |
| `GET /api/companies/{employerId}` | Herkese açık (aday tarafında şirket bilgisi göstermek için) |
| `GET /api/jobs`, `GET /api/jobs/{id}` | Herkese açık |
| `GET /api/jobs/me` | İşveren |
| `POST/PUT/DELETE /api/jobs`, `/api/jobs/{id}` | İşveren + sahiplik kontrolü |
| `POST /api/jobs/{jobId}/applications` | Aday |
| `GET /api/jobs/{jobId}/applications` | İşveren + sahiplik kontrolü |
| `PATCH /api/jobs/{jobId}/applications/{id}/status` | İşveren + sahiplik kontrolü |
| `GET /api/applications/me` | Aday |
| `GET/POST/DELETE /api/jobs/{jobId}/favorite`, `GET /api/favorites/me` | Aday |
| `GET/POST/DELETE /api/alerts`, `/api/alerts/me` | Aday |
| `GET/PUT /api/profile/me` | Aday |
| `GET /api/admin/**` | Admin |

## Veri modeli

`User` (rol: USER/EMPLOYER/ADMIN) → `CandidateProfile`/`EmployerProfile` (1:1), `JobPosting` (işverenin ilanları), `JobApplication` (başvuru anındaki aday/ilan bilgisini denormalize saklar — profil sonradan değişse de geçmiş başvuru bozulmaz), `SavedJob` (favoriler), `JobAlert` (ilan uyarıları), `VerificationToken` (e-posta doğrulama/şifre sıfırlama).

Tüm metin kolonları `NVARCHAR`'dır (`hibernate.use_nationalized_character_data=true`) — bu, Türkçe karakterlerin (ı, ş, ğ, ü, ö, ç) SQL Server'da sessizce ASCII'ye dönüştürülüp bozulmasını önler.

## Test

```bash
./mvnw test        # backend: servis katmanı (Mockito) + güvenlik/controller (MockMvc)
cd frontend && npm test   # frontend: Vitest
```

## Bilinen sınırlamalar

- Şema, Flyway/Liquibase yerine Hibernate `ddl-auto=update` ile yönetiliyor; dev/prod profil ayrımı yok.
- Şirket logosu ve CV gerçek dosya yükleme değil, URL alanı (dosya depolama altyapısı yok).
- Rate limiting bellek içi — birden fazla backend instance'ında paylaşılan bir store gerekir.
- Maaş alanı serbest metin; sayısal aralık filtresi için yapılandırılmış min/max alanlara geçiş gerekir.
