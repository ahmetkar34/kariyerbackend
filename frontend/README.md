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

Kimlik doğrulama JWT ile yapılır, token `localStorage`'da tutulur. Sayfa bazlı erişim kontrolü `src/lib/useRequireAuth.js` hook'u üzerinden sağlanır:

- Aday (`USER`): `/profile`, `/basvurularim`
- İşveren (`EMPLOYER`): `/isveren`, `/isveren/profil`, `/isveren/ilan-olustur`, `/isveren/ilan/:id/duzenle`, `/isveren/ilan/:id/basvuranlar`
- Admin (`ADMIN`): `/admin`
