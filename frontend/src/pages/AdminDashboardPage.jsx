import { useEffect, useState } from 'react'
import { deleteAdminJob, deleteAdminUser, getAdminStats, getAdminUsers } from '../lib/admin'
import { getAllJobs } from '../lib/jobsStore'
import { useRequireAuth } from '../lib/useRequireAuth'
import './Employer.css'
import './Admin.css'

function AdminDashboardPage() {
  const auth = useRequireAuth({ allowRole: (role) => role === 'ADMIN', redirectTo: '/' })
  const [stats, setStats] = useState(null)
  const [userKeyword, setUserKeyword] = useState('')
  const [users, setUsers] = useState([])
  const [jobs, setJobs] = useState([])

  useEffect(() => {
    if (!auth) return
    getAdminStats().then(setStats).catch(() => {})
    getAdminUsers().then((data) => setUsers(data.items)).catch(() => {})
    getAllJobs({ size: 50 }).then((data) => setJobs(data.items)).catch(() => {})
  }, [auth])

  if (!auth) {
    return null
  }

  function handleUserSearch(e) {
    e.preventDefault()
    getAdminUsers({ keyword: userKeyword }).then((data) => setUsers(data.items))
  }

  function handleDeleteUser(id) {
    if (!window.confirm('Bu kullanıcıyı silmek istediğinize emin misiniz?')) return
    deleteAdminUser(id).then(() => setUsers((prev) => prev.filter((u) => u.id !== id)))
  }

  function handleDeleteJob(id) {
    if (!window.confirm('Bu ilanı silmek istediğinize emin misiniz?')) return
    deleteAdminJob(id).then(() => setJobs((prev) => prev.filter((j) => j.id !== id)))
  }

  return (
    <div className="employer-page">
      <div className="container">
        <div className="employer-header">
          <div>
            <h1>Yönetim Paneli</h1>
            <p>Kullanıcıları ve ilanları yönetin.</p>
          </div>
        </div>

        {stats && (
          <div className="stats-grid">
            <div className="card stat-card">
              <div className="stat-value">{stats.totalUsers}</div>
              <div className="stat-label">Toplam Kullanıcı</div>
            </div>
            <div className="card stat-card">
              <div className="stat-value">{stats.totalCandidates}</div>
              <div className="stat-label">Aday</div>
            </div>
            <div className="card stat-card">
              <div className="stat-value">{stats.totalEmployers}</div>
              <div className="stat-label">İşveren</div>
            </div>
            <div className="card stat-card">
              <div className="stat-value">{stats.totalJobs}</div>
              <div className="stat-label">İlan</div>
            </div>
            <div className="card stat-card">
              <div className="stat-value">{stats.totalApplications}</div>
              <div className="stat-label">Başvuru</div>
            </div>
          </div>
        )}

        <div className="admin-section">
          <h2>Kullanıcılar</h2>
          <form className="admin-search" onSubmit={handleUserSearch}>
            <input
              type="text"
              placeholder="Ad, soyad veya e-posta ara"
              value={userKeyword}
              onChange={(e) => setUserKeyword(e.target.value)}
            />
          </form>

          {users.length === 0 ? (
            <div className="card employer-empty">
              <p className="empty-hint">Kullanıcı bulunamadı.</p>
            </div>
          ) : (
            <div className="employer-job-list">
              {users.map((user) => (
                <div className="card employer-job-item" key={user.id}>
                  <div>
                    <h3>
                      {user.firstName} {user.lastName}
                    </h3>
                    <p className="job-card-company">
                      {user.email} · {user.role}
                    </p>
                  </div>
                  <button
                    type="button"
                    className="remove-btn"
                    disabled={user.id === auth.user.id}
                    onClick={() => handleDeleteUser(user.id)}
                  >
                    Sil
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="admin-section">
          <h2>İlanlar</h2>

          {jobs.length === 0 ? (
            <div className="card employer-empty">
              <p className="empty-hint">İlan bulunamadı.</p>
            </div>
          ) : (
            <div className="employer-job-list">
              {jobs.map((job) => (
                <div className="card employer-job-item" key={job.id}>
                  <div>
                    <h3>{job.title}</h3>
                    <p className="job-card-company">{job.company}</p>
                  </div>
                  <button type="button" className="remove-btn" onClick={() => handleDeleteJob(job.id)}>
                    Sil
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default AdminDashboardPage
