import { useEffect, useMemo, useState } from 'react'
import JobCard from '../components/JobCard'
import { getAllJobs } from '../lib/jobsStore'
import './HomePage.css'

function HomePage() {
  const [keyword, setKeyword] = useState('')
  const [location, setLocation] = useState('')
  const [jobs, setJobs] = useState([])

  useEffect(() => {
    getAllJobs()
      .then(setJobs)
      .catch(() => setJobs([]))
  }, [])

  const filteredJobs = useMemo(() => {
    const k = keyword.trim().toLowerCase()
    const l = location.trim().toLowerCase()

    return jobs.filter((job) => {
      const matchesKeyword =
        !k ||
        job.title.toLowerCase().includes(k) ||
        job.company.toLowerCase().includes(k) ||
        job.tags.some((tag) => tag.toLowerCase().includes(k))
      const matchesLocation =
        !l || job.location.toLowerCase().includes(l)
      return matchesKeyword && matchesLocation
    })
  }, [jobs, keyword, location])

  return (
    <div className="home">
      <section className="hero">
        <div className="container hero-inner">
          <h1>Kariyerinizdeki bir sonraki adımı bulun</h1>
          <p className="hero-subtitle">
            Binlerce şirketten güncel iş ilanlarını keşfedin, size en uygun pozisyona
            hemen başvurun.
          </p>

          <form className="search-form" onSubmit={(e) => e.preventDefault()}>
            <input
              type="text"
              placeholder="Pozisyon, şirket veya anahtar kelime"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              aria-label="Anahtar kelime"
            />
            <input
              type="text"
              placeholder="Şehir"
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              aria-label="Şehir"
            />
            <button type="submit" className="btn btn-primary">
              İlan Ara
            </button>
          </form>
        </div>
      </section>

      <section className="container job-list-section">
        <div className="job-list-header">
          <h2>Güncel İş İlanları</h2>
          <span className="job-count">{filteredJobs.length} ilan bulundu</span>
        </div>

        {filteredJobs.length > 0 ? (
          <div className="job-grid">
            {filteredJobs.map((job) => (
              <JobCard key={job.id} job={job} />
            ))}
          </div>
        ) : (
          <p className="no-results">Aramanızla eşleşen ilan bulunamadı.</p>
        )}
      </section>
    </div>
  )
}

export default HomePage
