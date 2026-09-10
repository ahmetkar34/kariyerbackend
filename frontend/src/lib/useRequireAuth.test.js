import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderHook } from '@testing-library/react'
import { useRequireAuth } from './useRequireAuth'
import { getAuth } from './auth'

const navigateMock = vi.fn()

vi.mock('react-router-dom', () => ({
  useNavigate: () => navigateMock,
}))

vi.mock('./auth', () => ({
  getAuth: vi.fn(),
}))

describe('useRequireAuth', () => {
  beforeEach(() => {
    navigateMock.mockClear()
  })

  it('redirects to /login when there is no session', () => {
    getAuth.mockReturnValue(null)

    const { result } = renderHook(() => useRequireAuth())

    expect(navigateMock).toHaveBeenCalledWith('/login')
    expect(result.current).toBeNull()
  })

  it('redirects to redirectTo when the role check fails', () => {
    getAuth.mockReturnValue({ token: 't', user: { role: 'USER' } })

    const { result } = renderHook(() =>
      useRequireAuth({ allowRole: (role) => role === 'EMPLOYER', redirectTo: '/profile' }),
    )

    expect(navigateMock).toHaveBeenCalledWith('/profile')
    expect(result.current).toBeNull()
  })

  it('returns the session without redirecting when authorized', () => {
    const auth = { token: 't', user: { role: 'EMPLOYER' } }
    getAuth.mockReturnValue(auth)

    const { result } = renderHook(() => useRequireAuth({ allowRole: (role) => role === 'EMPLOYER' }))

    expect(navigateMock).not.toHaveBeenCalled()
    expect(result.current).toEqual(auth)
  })

  it('returns the session when no role restriction is given', () => {
    const auth = { token: 't', user: { role: 'USER' } }
    getAuth.mockReturnValue(auth)

    const { result } = renderHook(() => useRequireAuth())

    expect(navigateMock).not.toHaveBeenCalled()
    expect(result.current).toEqual(auth)
  })
})
