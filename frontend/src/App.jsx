import { Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import ProfilePage from './pages/ProfilePage'
import JobDetailPage from './pages/JobDetailPage'
import EmployerDashboardPage from './pages/EmployerDashboardPage'
import JobFormPage from './pages/JobFormPage'
import ApplicantsPage from './pages/ApplicantsPage'

function App() {
  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/ilan/:id" element={<JobDetailPage />} />
        <Route path="/isveren" element={<EmployerDashboardPage />} />
        <Route path="/isveren/ilan-olustur" element={<JobFormPage />} />
        <Route path="/isveren/ilan/:id/duzenle" element={<JobFormPage />} />
        <Route path="/isveren/ilan/:id/basvuranlar" element={<ApplicantsPage />} />
      </Routes>
    </>
  )
}

export default App
