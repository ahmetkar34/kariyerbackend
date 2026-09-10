import { describe, expect, it } from 'vitest'
import { isEmployerRole } from './roles'

describe('isEmployerRole', () => {
  it('returns true for the EMPLOYER role', () => {
    expect(isEmployerRole('EMPLOYER')).toBe(true)
  })

  it('is case-insensitive', () => {
    expect(isEmployerRole('employer')).toBe(true)
  })

  it('returns false for the USER role', () => {
    expect(isEmployerRole('USER')).toBe(false)
  })

  it('returns false for missing or non-string roles', () => {
    expect(isEmployerRole(null)).toBe(false)
    expect(isEmployerRole(undefined)).toBe(false)
  })
})
