import { Link } from 'react-router-dom'
import './JobCard.css'

function JobCard({ job }) {
  return (
    <Link to={`/ilan/${job.id}`} className="card job-card">
      <div className="job-card-header">
        <div>
          <h3>{job.title}</h3>
          <p className="job-card-company">{job.company}</p>
        </div>
        <span className="job-card-type">{job.type}</span>
      </div>

      <p className="job-card-description">{job.description}</p>

      <div className="job-card-tags">
        {job.tags.map((tag) => (
          <span key={tag} className="tag">
            {tag}
          </span>
        ))}
      </div>

      <div className="job-card-footer">
        <span>
          📍 {job.location}
          {job.remote ? ' · Uzaktan' : ''}
        </span>
        <span>💰 {job.salary}</span>
        <span className="job-card-date">{new Date(job.createdAt).toLocaleDateString('tr-TR')}</span>
      </div>
    </Link>
  )
}

export default JobCard
