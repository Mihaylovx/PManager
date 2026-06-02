const API = 'http://localhost:8080'

//Fake user information
const fakeUser = {
  id: 1,
  email: 'test@example.com',
  firstName: 'Test',
  lastName: 'User',
}

//Fake project information
const fakeProject = {
  id: 42,
  name: 'E2E Test Project',
  description: 'Created during e2e test',
  managerEmail: 'test@example.com',
  tasks: [],
}

describe('Login and create a project', () => {
  beforeEach(() => {
    cy.intercept('POST', `${API}/api/auth/login`, { statusCode: 200, body: fakeUser }).as('login')
    cy.intercept('GET', `${API}/api/projects*`, { statusCode: 200, body: [] }).as('getProjects')
  })

  it('logs in, creates a project, and sees it in the list', () => {
    // Login
    cy.visit('/login')
    cy.get('input[type="email"]').type(fakeUser.email)
    cy.get('input[type="password"]').type('password123')
    cy.get('button[type="submit"]').click()

    cy.wait('@login')
    cy.url().should('include', '/projects')

    // Create a project
    cy.intercept('POST', `${API}/api/projects`, { statusCode: 200, body: fakeProject }).as('createProject')
    cy.intercept('GET', `${API}/api/projects*`, { statusCode: 200, body: [fakeProject] }).as('getProjectsAfterCreate')

    cy.contains('button', 'New Project').click()
    cy.get('input[type="text"]').first().type(fakeProject.name)
    cy.contains('button', 'Create Project').click()
    cy.wait('@createProject')
    cy.wait('@getProjectsAfterCreate')

   // Verify that project is created
    cy.contains(fakeProject.name).should('be.visible')
  })
})
