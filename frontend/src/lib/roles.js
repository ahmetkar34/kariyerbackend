export function isEmployerRole(role) {
  return typeof role === 'string' && role.toLowerCase().includes('employer')
}
