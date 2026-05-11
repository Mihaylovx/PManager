import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import {
  Container,
  Typography,
  TextField,
  Button,
  Paper,
  Box,
  Grid,
  Card,
  CardContent,
  CardActions,
  IconButton,
  Alert,
  Chip,
  CardActionArea,
  Stack,
  AppBar,
  Toolbar
} from "@mui/material"
import DeleteIcon from "@mui/icons-material/Delete"
import AddIcon from "@mui/icons-material/Add"
import FolderIcon from "@mui/icons-material/Folder"
import LogoutIcon from "@mui/icons-material/Logout"
import { getUser, clearUser } from "../auth"

function Projects() {
  const [projects, setProjects] = useState([])
  const [name, setName] = useState("")
  const [description, setDescription] = useState("")
  const [inviteInput, setInviteInput] = useState("")
  const [invitedEmails, setInvitedEmails] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [inviteWarning, setInviteWarning] = useState(null)
  const [createError, setCreateError] = useState(null)
  const [loadError, setLoadError] = useState(null)
  const navigate = useNavigate()
  const user = getUser()

  useEffect(() => { fetchProjects() }, [])

  function fetchProjects() {
    fetch(`http://localhost:8080/api/projects?user=${encodeURIComponent(user.email)}`)
      .then(res => res.json())
      .then(data => setProjects(data))
      .catch(() => setLoadError("Failed to load projects. Is the server running?"))
  }

  function addInviteEmail() {
    const email = inviteInput.trim().toLowerCase()
    if (!email) return
    if (email === user.email.toLowerCase()) {
      setInviteWarning("You're already the manager — no need to invite yourself.")
      return
    }
    if (invitedEmails.includes(email)) return
    setInvitedEmails([...invitedEmails, email])
    setInviteInput("")
    setInviteWarning(null)
  }

  function removeInviteEmail(email) {
    setInvitedEmails(invitedEmails.filter(e => e !== email))
  }

  async function addProject() {
    if (!name) return
    setCreateError(null)
    try {
      const res = await fetch("http://localhost:8080/api/projects", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name,
          description,
          managerEmail: user.email,
          memberEmails: invitedEmails
        })
      })
      if (!res.ok) {
        const msg = await res.text()
        throw new Error(msg || "Failed to create project")
      }
      setName("")
      setDescription("")
      setInviteInput("")
      setInvitedEmails([])
      setShowForm(false)
      fetchProjects()
    } catch (err) {
      setCreateError(err.message)
    }
  }

  function deleteProject(id, e) {
    e.stopPropagation()
    fetch(`http://localhost:8080/api/projects/${id}`, { method: "DELETE" })
      .then(() => fetchProjects())
  }

  function logout() {
    clearUser()
    navigate("/")
  }

  function handleCardClick(projectId) {
    navigate(`/project/${projectId}`)
  }

  return (
    <Box>
      <AppBar position="static" color="default" elevation={1}>
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>Project Manager</Typography>
          <Typography variant="body2" sx={{ mr: 2 }} color="text.secondary">
            {user.firstName} {user.lastName}
          </Typography>
          <IconButton onClick={logout} color="inherit" title="Logout">
            <LogoutIcon />
          </IconButton>
        </Toolbar>
      </AppBar>

      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 4 }}>
          <Typography variant="h4" component="h1">Projects</Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => setShowForm(!showForm)}
          >
            New Project
          </Button>
        </Box>

        {showForm && (
          <Paper elevation={2} sx={{ p: 3, mb: 4 }}>
            <Typography variant="h6" gutterBottom>Create New Project</Typography>
            <Stack spacing={2}>
              <TextField
                fullWidth
                label="Project name"
                value={name}
                onChange={e => setName(e.target.value)}
                autoFocus
              />
              <TextField
                fullWidth
                label="Description"
                multiline
                rows={3}
                value={description}
                onChange={e => setDescription(e.target.value)}
              />

              <Box>
                <Typography variant="subtitle2" gutterBottom>
                  Invite developers (by email)
                </Typography>
                <Stack direction="row" spacing={1}>
                  <TextField
                    fullWidth
                    size="small"
                    type="email"
                    placeholder="developer@example.com"
                    value={inviteInput}
                    onChange={e => setInviteInput(e.target.value)}
                    onKeyDown={e => {
                      if (e.key === "Enter") {
                        e.preventDefault()
                        addInviteEmail()
                      }
                    }}
                  />
                  <Button onClick={addInviteEmail} variant="outlined">Add</Button>
                </Stack>
                {inviteWarning && <Alert severity="warning" sx={{ mt: 1 }}>{inviteWarning}</Alert>}
                {invitedEmails.length > 0 && (
                  <Box sx={{ mt: 2, display: "flex", flexWrap: "wrap", gap: 1 }}>
                    {invitedEmails.map(email => (
                      <Chip
                        key={email}
                        label={email}
                        onDelete={() => removeInviteEmail(email)}
                      />
                    ))}
                  </Box>
                )}
              </Box>

              {createError && <Alert severity="error">{createError}</Alert>}
              <Box sx={{ display: "flex", gap: 2, justifyContent: "flex-end" }}>
                <Button onClick={() => { setShowForm(false); setCreateError(null) }}>Cancel</Button>
                <Button variant="contained" onClick={addProject} disabled={!name}>
                  Create Project
                </Button>
              </Box>
            </Stack>
          </Paper>
        )}

        {loadError && <Alert severity="error" sx={{ mb: 2 }}>{loadError}</Alert>}
        {!loadError && projects.length === 0 && (
          <Alert severity="info" sx={{ mt: 2 }}>
            No projects yet. Click "New Project" to create one.
          </Alert>
        )}

        <Grid container spacing={3}>
          {projects.map(project => {
            const isManager = project.managerEmail === user.email
            return (
              <Grid item xs={12} sm={6} md={4} key={project.id}>
                <Card
                  sx={{
                    height: 240,
                    width: 300,
                    display: "flex",
                    flexDirection: "column",
                    transition: "transform 0.2s, box-shadow 0.2s",
                    "&:hover": {
                      transform: "translateY(-4px)",
                      boxShadow: 6
                    }
                  }}
                >
                  <CardActionArea onClick={() => handleCardClick(project.id)}>
                    <CardContent>
                      <Box sx={{ display: "flex", alignItems: "center", mb: 1 }}>
                        <FolderIcon sx={{ mr: 1, color: "primary.main" }} />
                        <Typography variant="h6" component="h2" noWrap>
                          {project.name}
                        </Typography>
                      </Box>
                      <Typography
                        variant="body2"
                        color="text.secondary"
                        sx={{
                          overflow: "hidden",
                          textOverflow: "ellipsis",
                          display: "-webkit-box",
                          WebkitLineClamp: 2,
                          WebkitBoxOrient: "vertical",
                          minHeight: "40px"
                        }}
                      >
                        {project.description || "No description provided."}
                      </Typography>
                      <Stack direction="row" spacing={1} sx={{ mt: 2, flexWrap: "wrap", gap: 1 }}>
                        <Chip
                          label={isManager ? "Manager" : "Developer"}
                          size="small"
                          color={isManager ? "primary" : "default"}
                        />
                        <Chip
                          label={`Tasks: ${project.tasks?.length || 0}`}
                          size="small"
                        />
                      </Stack>
                    </CardContent>
                  </CardActionArea>
                  <CardActions sx={{ justifyContent: "flex-end", p: 1 }}>
                    {isManager && (
                      <IconButton
                        size="small"
                        color="error"
                        onClick={(e) => deleteProject(project.id, e)}
                      >
                        <DeleteIcon />
                      </IconButton>
                    )}
                  </CardActions>
                </Card>
              </Grid>
            )
          })}
        </Grid>
      </Container>
    </Box>
  )
}

export default Projects
