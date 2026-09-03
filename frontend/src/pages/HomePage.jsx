import { useEffect, useState } from 'react'
import JobCard from '../components/JobCard'
import { getAllJobs } from '../lib/jobsStore'
import './HomePage.css'

function HomePage() {
  const [keywordInput, setKeywordInput] = useState('')
  const [locationInput, setLocationInput] = useState('')
  const [search, setSearch] = useState({ keyword: '', location: '' })
  const [page, setPage] = useState(0)
  const [data, setData] = useState({ items: [], totalElements: 0, totalPages: 0 })

  useEffect(() => {
    getAllJobs({ keyword: search.keyword, location: search.location, page })
      .then(setData)
      .catch(() => setData({ items: [], totalElements: 0, totalPages: 0 }))
  }, [search, page])

  function handleSubmit(e) {
    e.preventDefault()
    setPage(0)
    setSearch({ keyword: keywordInput, location: locationInput })
  }

  return (
    <div className="home">
      <section className="hero">
        <div className="container hero-inner">
          <h1>Kariyerinizdeki bir sonraki adımı bulun</h1>
          <p className="hero-subtitle">
            Binlerce şirketten güncel iş ilanlarını keşfedin, size en uygun pozisyona
            hemen başvurun.
          </p>

          <form className="search-form" onSubmit={handleSubmit}>
            <input
              type="text"
              placeholder="Pozisyon, şirket veya anahtar kelime"
              value={keywordInput}
              onChange={(e) => setKeywordInput(e.target.value)}
              aria-label="Anahtar kelime"
            />
            <input
              type="text"
              placeholder="Şehir"
              value={locationInput}
              onChange={(e) => setLocationInput(e.target.value)}
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
          <span className="job-count">{data.totalElements} ilan bulundu</span>
        </div>

        {data.items.length > 0 ? (
          <div className="job-grid">
            {data.items.map((job) => (
              <JobCard key={job.id} job={job} />
            ))}
          </div>
        ) : (
          <p className="no-results">Aramanızla eşleşen ilan bulunamadı.</p>
        )}

        {data.totalPages > 1 && (
          <div className="pagination">
            <button
              type="button"
              className="btn btn-outline btn-sm"
              disabled={page === 0}
              onClick={() => setPage((p) => p - 1)}
            >
              ← Önceki
            </button>
            <span>
              Sayfa {page + 1} / {data.totalPages}
            </span>
            <button
              type="button"
              className="btn btn-outline btn-sm"
              disabled={page + 1 >= data.totalPages}
              onClick={() => setPage((p) => p + 1)}
            >
              Sonraki →
            </button>
          </div>
        )}
      </section>
    </div>
  )
}

export default HomePage
