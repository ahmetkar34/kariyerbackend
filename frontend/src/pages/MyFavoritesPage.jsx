import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import JobCard from '../components/JobCard'
import { getMyFavorites } from '../lib/jobsStore'
import { isEmployerRole } from '../lib/roles'
import { useRequireAuth } from '../lib/useRequireAuth'
import './HomePage.css'
import './Employer.css'

function MyFavoritesPage() {
  const auth = useRequireAuth({ allowRole: (role) => !isEmployerRole(role), redirectTo: '/isveren' })
  const [favorites, setFavorites] = useState([])

  useEffect(() => {
    if (!auth) return
    getMyFavorites()
      .then(setFavorites)
      .catch(() => setFavorites([]))
  }, [auth])

  if (!auth) {
    return null
  }

  return (
    <div className="employer-page">
      <div className="container">
        <div className="employer-header">
          <div>
            <h1>Favorilerim</h1>
            <p>Kaydettiğiniz ilanları buradan takip edebilirsiniz.</p>
          </div>
        </div>

        {favorites.length === 0 ? (
          <div className="card employer-empty">
            <p className="empty-hint">Henüz hiçbir ilanı kaydetmediniz.</p>
            <Link to="/" className="btn btn-primary">
              İlanları Keşfet
            </Link>
          </div>
        ) : (
          <div className="job-grid">
            {favorites.map((job) => (
              <JobCard key={job.id} job={job} />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default MyFavoritesPage
