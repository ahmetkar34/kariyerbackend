import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getAuth } from './auth'

export function useRequireAuth({ allowRole, redirectTo = '/profile' } = {}) {
  const navigate = useNavigate()
  const [auth, setAuth] = useState(null)

  useEffect(() => {
    const currentAuth = getAuth()
    if (!currentAuth) {
      navigate('/login')
      return
    }
    if (allowRole && !allowRole(currentAuth.user.role)) {
      navigate(redirectTo)
      return
    }
    setAuth(currentAuth)
  }, [navigate])

  return auth
}
