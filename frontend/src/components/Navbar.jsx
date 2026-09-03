import { useEffect, useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { clearAuth, getAuth } from '../lib/auth'
import { isEmployerRole } from '../lib/roles'
import './Navbar.css'

function Navbar() {
  const [auth, setAuthState] = useState(getAuth())
  const navigate = useNavigate()
  const isEmployer = Boolean(auth && isEmployerRole(auth.user?.role))

  useEffect(() => {
    function handleAuthChange() {
      setAuthState(getAuth())
    }
    window.addEventListener('authchange', handleAuthChange)
    window.addEventListener('storage', handleAuthChange)
    return () => {
      window.removeEventListener('authchange', handleAuthChange)
      window.removeEventListener('storage', handleAuthChange)
    }
  }, [])

  function handleLogout() {
    clearAuth()
    navigate('/')
  }

  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <Link to="/" className="navbar-logo">
          Kariyer<span>Bul</span>
        </Link>

        <nav className="navbar-links">
          <NavLink to="/" end>
            Ana Sayfa
          </NavLink>
        </nav>

        <div className="navbar-actions">
          {auth ? (
            <>
              {isEmployer ? (
                <>
                  <Link to="/isveren" className="btn btn-outline">
                    İlanlarım
                  </Link>
                  <Link to="/isveren/profil" className="btn btn-outline">
                    Şirket Profili
                  </Link>
                  <Link to="/isveren/ilan-olustur" className="btn btn-primary">
                    + İlan Ver
                  </Link>
                </>
              ) : (
                <>
                  <Link to="/basvurularim" className="btn btn-outline">
                    Başvurularım
                  </Link>
                  <Link to="/profile" className="btn btn-outline">
                    Profilim
                  </Link>
                </>
              )}
              <button
                type="button"
                className={`btn ${isEmployer ? 'btn-outline' : 'btn-primary'}`}
                onClick={handleLogout}
              >
                Çıkış Yap
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-outline">
                Giriş Yap
              </Link>
              <Link to="/register" className="btn btn-primary">
                Kayıt Ol
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  )
}

export default Navbar
