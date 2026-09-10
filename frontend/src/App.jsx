import { Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import ProfilePage from './pages/ProfilePage'
import JobDetailPage from './pages/JobDetailPage'
import MyFavoritesPage from './pages/MyFavoritesPage'
import EmployerDashboardPage from './pages/EmployerDashboardPage'
import JobFormPage from './pages/JobFormPage'
import ApplicantsPage from './pages/ApplicantsPage'
import MyApplicationsPage from './pages/MyApplicationsPage'
import EmployerProfilePage from './pages/EmployerProfilePage'
import AdminDashboardPage from './pages/AdminDashboardPage'
import VerifyEmailPage from './pages/VerifyEmailPage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import ResetPasswordPage from './pages/ResetPasswordPage'

function App() {
  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/dogrula" element={<VerifyEmailPage />} />
        <Route path="/sifremi-unuttum" element={<ForgotPasswordPage />} />
        <Route path="/sifre-sifirla" element={<ResetPasswordPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/basvurularim" element={<MyApplicationsPage />} />
        <Route path="/favorilerim" element={<MyFavoritesPage />} />
        <Route path="/ilan/:id" element={<JobDetailPage />} />
        <Route path="/isveren" element={<EmployerDashboardPage />} />
        <Route path="/isveren/profil" element={<EmployerProfilePage />} />
        <Route path="/isveren/ilan-olustur" element={<JobFormPage />} />
        <Route path="/isveren/ilan/:id/duzenle" element={<JobFormPage />} />
        <Route path="/isveren/ilan/:id/basvuranlar" element={<ApplicantsPage />} />
        <Route path="/admin" element={<AdminDashboardPage />} />
      </Routes>
    </>
  )
}

export default App
