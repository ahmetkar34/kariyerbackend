# KariyerBul — Frontend

React (Vite) tabanlı iş ilanı platformu arayüzü. Backend olarak `../` altındaki Spring Boot servisini kullanır.

## Kurulum

```bash
npm install
cp .env.example .env   # gerekirse VITE_API_URL'i düzenleyin
npm run dev
```

Uygulama varsayılan olarak `http://localhost:5173` üzerinde açılır. Backend'in `http://localhost:8081`'de (veya `.env`'deki `VITE_API_URL`'de belirtilen adreste) çalışıyor olması gerekir; backend tarafında CORS izinli origin listesi (`CORS_ALLOWED_ORIGINS`) bu adresle eşleşmelidir.

## Komutlar

- `npm run dev` — geliştirme sunucusu
- `npm run build` — production build (`dist/`)
- `npm run preview` — production build'i yerelde önizleme
- `npm run lint` — oxlint ile statik analiz

## Proje yapısı

- `src/pages` — rota bazlı sayfalar (aday, işveren ve admin akışları)
- `src/components` — paylaşılan UI bileşenleri (Navbar, JobCard)
- `src/lib` — backend API istemcileri (`api.js`, `auth.js`, `jobsStore.js` vb.) ve `useRequireAuth` gibi paylaşılan hook'lar

## Roller ve korumalı sayfalar

Kimlik doğrulama JWT ile yapılır; token backend tarafından `access_token` adında **httpOnly** bir cookie olarak set edilir (JS tarafından okunamaz, `localStorage`'da tutulmaz). `localStorage`'daki `kariyer_auth` sadece Navbar/route-guard gibi UI amaçlı kullanıcı bilgisi önbelleğidir, gerçek kimlik doğrulama bilgisi değildir. Çıkış yapmak için `lib/auth.js`'teki `logout()` mutlaka `/api/auth/logout`'u çağırmalı — cookie'yi sadece backend temizleyebilir.

Cookie tabanlı auth CSRF riski taşıdığı için `/api/auth/**` dışındaki tüm mutasyon istekleri (`POST`/`PUT`/`PATCH`/`DELETE`) `X-XSRF-TOKEN` header'ı gerektirir; `apiFetch` bunu `XSRF-TOKEN` cookie'sinden otomatik okuyup ekler, ayrıca isteklere `credentials: 'include'` ekler. Elle `fetch` çağrısı yazmak yerine her zaman `apiFetch` kullanın.

Sayfa bazlı erişim kontrolü `src/lib/useRequireAuth.js` hook'u üzerinden sağlanır:

- Aday (`USER`): `/profile`, `/basvurularim`
- İşveren (`EMPLOYER`): `/isveren`, `/isveren/profil`, `/isveren/ilan-olustur`, `/isveren/ilan/:id/duzenle`, `/isveren/ilan/:id/basvuranlar`
- Admin (`ADMIN`): `/admin`
