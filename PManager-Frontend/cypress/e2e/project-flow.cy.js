const API = 'http://localhost:8080'

const fakeUser = {
  email: 'test@example.com',
  firstName: 'Test',
  lastName: 'User',
}

const fakeProject = {
  id: 42,
  name: 'E2E Test Project',
  description: 'Created during e2e test',
  managerEmail: 'test@example.com',
  memberEmails: [],
  tasks: [],
}

const fakeTask = {
  id: 1,
  title: 'Write documentation',
  status: 'TODO',
  hoursWorked: 0,
  assignedTo: null,
}

// Visits a route as an authenticated user by seeding localStorage before the page loads.
const visitAs = (path) => {
  cy.visit(path, {
    onBeforeLoad: (win) => {
      win.localStorage.setItem('pm_user', JSON.stringify(fakeUser))
    },
  })
}

// Authentication

describe('Authentication', () => {
  it('logs in and redirects to /projects', () => {
    cy.intercept('POST', `${API}/api/auth/login`, { statusCode: 200, body: fakeUser }).as('login')
    cy.intercept('GET', `${API}/api/projects*`, { statusCode: 200, body: [] }).as('getProjects')
    cy.intercept('GET', `${API}/api/invites*`, { statusCode: 200, body: [] }).as('getInvites')

    cy.visit('/login')
    cy.get('input[type="email"]').type(fakeUser.email)
    cy.get('input[type="password"]').type('password123')
    cy.get('button[type="submit"]').click()

    cy.wait('@login')
    cy.url().should('include', '/projects')
  })

  it('shows an error message on invalid credentials', () => {
    cy.intercept('POST', `${API}/api/auth/login`, {
      statusCode: 401,
      body: 'Invalid email or password.',
    }).as('loginFail')

    cy.visit('/login')
    cy.get('input[type="email"]').type('wrong@example.com')
    cy.get('input[type="password"]').type('wrongpass')
    cy.get('button[type="submit"]').click()

    cy.wait('@loginFail')
    cy.contains('Invalid email or password.').should('be.visible')
  })
})

// Projects page (no projects)

describe('Projects page — empty', () => {
  beforeEach(() => {
    cy.intercept('GET', `${API}/api/invites*`, { statusCode: 200, body: [] })
    cy.intercept('GET', `${API}/api/projects*`, { statusCode: 200, body: [] }).as('getProjects')
    visitAs('/projects')
    cy.wait('@getProjects')
  })

  it('shows the empty state', () => {
    cy.contains('No projects yet').should('be.visible')
  })

  it('creates a project and shows it in the list', () => {
    cy.intercept('POST', `${API}/api/projects`, { statusCode: 201, body: fakeProject }).as('createProject')
    cy.intercept('GET', `${API}/api/projects*`, { statusCode: 200, body: [fakeProject] }).as('getProjectsUpdated')

    cy.contains('button', 'New Project').click()
    cy.get('input[type="text"]').first().type(fakeProject.name)
    cy.contains('button', 'Create').click()

    cy.wait('@createProject')
    cy.wait('@getProjectsUpdated')
    cy.contains(fakeProject.name).should('be.visible')
  })
})

// Projects page (with projects)

describe('Projects page — with projects', () => {
  beforeEach(() => {
    cy.intercept('GET', `${API}/api/invites*`, { statusCode: 200, body: [] })
    cy.intercept('GET', `${API}/api/projects*`, { statusCode: 200, body: [fakeProject] }).as('getProjects')
    visitAs('/projects')
    cy.wait('@getProjects')
  })

  it('shows the project card', () => {
    cy.contains(fakeProject.name).should('be.visible')
    cy.contains('Manager').should('be.visible')
  })

  it('deletes a project after confirmation', () => {
    cy.intercept('DELETE', `${API}/api/projects/42`, { statusCode: 200 }).as('deleteProject')
    cy.intercept('GET', `${API}/api/projects*`, { statusCode: 200, body: [] }).as('getProjectsAfterDelete')

    cy.get('.MuiIconButton-colorError').click()
    cy.get('[role="dialog"]').contains('button', 'Delete').click()

    cy.wait('@deleteProject')
    cy.wait('@getProjectsAfterDelete')
    cy.contains(fakeProject.name).should('not.exist')
  })

  it('navigates to the project detail page on card click', () => {
    cy.intercept('GET', `${API}/api/projects/42`, { statusCode: 200, body: fakeProject }).as('getProjectDetail')
    cy.intercept('GET', /\/api\/users\/.*/, { statusCode: 200, body: { ...fakeUser, hourlyRate: 0 } })

    cy.contains(fakeProject.name).click()

    cy.wait('@getProjectDetail')
    cy.url().should('include', '/project/42')
    cy.contains(fakeProject.name).should('be.visible')
  })
})

// ─── Project detail ────────────────────────────────────────────────────────────

describe('Project detail', () => {
  beforeEach(() => {
    cy.intercept('GET', `${API}/api/projects/42`, { statusCode: 200, body: fakeProject }).as('getProject')
    cy.intercept('GET', /\/api\/users\/.*/, { statusCode: 200, body: { ...fakeUser, hourlyRate: 0 } })
    visitAs('/project/42')
    cy.wait('@getProject')
  })

  it('shows the project name and empty task state', () => {
    cy.contains(fakeProject.name).should('be.visible')
    cy.contains('No tasks yet').should('be.visible')
  })

  it('adds a task and shows it in the list', () => {
    cy.intercept('POST', `${API}/api/projects/42/tasks`, { statusCode: 201, body: fakeTask }).as('createTask')
    cy.intercept('GET', `${API}/api/projects/42`, {
      statusCode: 200,
      body: { ...fakeProject, tasks: [fakeTask] },
    }).as('getProjectWithTask')

    cy.get('input[placeholder="Add a task and press Enter..."]').type(`${fakeTask.title}{enter}`)

    cy.wait('@createTask')
    cy.wait('@getProjectWithTask')
    cy.contains(fakeTask.title).should('be.visible')
  })

  it('shows the Team section with the manager', () => {
    cy.contains('Team').should('be.visible')
    cy.contains(fakeUser.email).should('be.visible')
  })
})
