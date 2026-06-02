import { useEffect, useState } from "react"
import { useParams, useNavigate } from "react-router-dom"
import {
  Container,
  Typography,
  Button,
  Paper,
  Box,
  IconButton,
  Breadcrumbs,
  Link,
  Divider,
  List,
  ListItem,
  ListItemText,
  TextField,
  Chip,
  Alert,
  Stack,
  Avatar
} from "@mui/material"
import ArrowBackIcon from "@mui/icons-material/ArrowBack"
import AddIcon from "@mui/icons-material/Add"
import DeleteIcon from "@mui/icons-material/Delete"
import HomeIcon from "@mui/icons-material/Home"
import PersonIcon from "@mui/icons-material/Person"
import { getUser } from "../auth"
import { connectToProject } from "../services/websocket"

function ProjectDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const user = getUser()
  const [project, setProject] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [newTask, setNewTask] = useState("")
  const [tasks, setTasks] = useState([])
  const [inviteEmail, setInviteEmail] = useState("")
  const [inviteError, setInviteError] = useState(null)

  useEffect(() => {
    fetchProject()
  }, [id])

  useEffect(() => {
    return connectToProject(id, (event) => {
      if (event.type === "TASK_ADDED") {
        setTasks(prev => [...prev, event.data])
      } else if (event.type === "TASK_DELETED") {
        setTasks(prev => prev.filter(t => t.id !== event.data))
      } else if (event.type === "TASK_UPDATED") {
        setTasks(prev => prev.map(t => t.id === event.data.id ? event.data : t))
      } else if (event.type === "MEMBER_ADDED") {
        setProject(prev => ({
          ...prev,
          memberEmails: [...(prev.memberEmails || []), event.data]
        }))
      }
    })
  }, [id])

  function fetchProject() {
    fetch(`http://localhost:8080/api/projects/${id}`)
      .then(res => {
        if (!res.ok) throw new Error("Project not found")
        return res.json()
      })
      .then(data => {
        setProject(data)
        setTasks(data.tasks || [])
        setLoading(false)
      })
      .catch(err => {
        setError(err.message)
        setLoading(false)
      })
  }

  function addTask() {
    if (!newTask.trim()) return
    fetch(`http://localhost:8080/api/projects/${id}/tasks`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ title: newTask, completed: false })
    })
      .then(res => {
        if (!res.ok) throw new Error("Failed to add task")
        return res.json()
      })
      .then(() => {
        setNewTask("")
        fetchProject()
      })
      .catch(err => alert(err.message))
  }

  function deleteTask(taskId) {
    fetch(`http://localhost:8080/api/projects/${id}/tasks/${taskId}`, { method: "DELETE" })
      .then(() => fetchProject())
  }

  function toggleTaskComplete(taskId, completed) {
    fetch(`http://localhost:8080/api/projects/${id}/tasks/${taskId}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ completed: !completed })
    }).then(() => fetchProject())
  }

  async function inviteMember() {
    setInviteError(null)
    const email = inviteEmail.trim().toLowerCase()
    if (!email) return
    try {
      const res = await fetch(`http://localhost:8080/api/projects/${id}/invite`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email })
      })
      if (!res.ok) {
        const msg = await res.text()
        throw new Error(msg || "Could not invite member")
      }
      setInviteEmail("")
      fetchProject()
    } catch (err) {
      setInviteError(err.message)
    }
  }

  if (loading) {
    return (
      <Container sx={{ py: 4 }}>
        <Typography>Loading project...</Typography>
      </Container>
    )
  }

  if (error) {
    return (
      <Container sx={{ py: 4 }}>
        <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>
        <Button variant="contained" onClick={() => navigate("/projects")}>
          Back to Projects
        </Button>
      </Container>
    )
  }

  const isManager = project?.managerEmail === user.email
  const memberEmails = project?.memberEmails ? Array.from(project.memberEmails) : []

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Breadcrumbs sx={{ mb: 3 }}>
        <Link
          color="inherit"
          onClick={() => navigate("/projects")}
          sx={{ display: "flex", alignItems: "center", cursor: "pointer" }}
        >
          <HomeIcon sx={{ mr: 0.5 }} fontSize="small" />
          Projects
        </Link>
        <Typography color="text.primary">{project?.name}</Typography>
      </Breadcrumbs>

      <Box sx={{ display: "flex", alignItems: "center", mb: 3, gap: 2 }}>
        <IconButton onClick={() => navigate("/projects")} color="primary">
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4" component="h1">{project?.name}</Typography>
        <Chip
          label={isManager ? "You're the Manager" : "You're a Developer"}
          size="small"
          color={isManager ? "primary" : "default"}
        />
      </Box>

      {project?.description && (
        <Paper sx={{ p: 3, mb: 3 }}>
          <Typography variant="body1" color="text.secondary">
            {project.description}
          </Typography>
        </Paper>
      )}

      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom>Team</Typography>
        <Divider sx={{ mb: 2 }} />

        <List dense>
          {project?.managerEmail && (
            <ListItem>
              <Avatar sx={{ bgcolor: "primary.main", width: 32, height: 32, mr: 2 }}>
                <PersonIcon fontSize="small" />
              </Avatar>
              <ListItemText
                primary={project.managerEmail}
                secondary="Project Manager"
              />
            </ListItem>
          )}
          {memberEmails.map(email => (
            <ListItem key={email}>
              <Avatar sx={{ bgcolor: "grey.400", width: 32, height: 32, mr: 2 }}>
                <PersonIcon fontSize="small" />
              </Avatar>
              <ListItemText primary={email} secondary="Developer" />
            </ListItem>
          ))}
          {memberEmails.length === 0 && (
            <Typography variant="body2" color="text.secondary" sx={{ pl: 2 }}>
              No developers invited yet.
            </Typography>
          )}
        </List>

        {isManager && (
          <Box sx={{ mt: 2 }}>
            <Stack direction="row" spacing={1}>
              <TextField
                size="small"
                type="email"
                placeholder="Invite developer by email"
                value={inviteEmail}
                onChange={(e) => setInviteEmail(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter") {
                    e.preventDefault()
                    inviteMember()
                  }
                }}
                fullWidth
              />
              <Button variant="outlined" onClick={inviteMember}>Invite</Button>
            </Stack>
            {inviteError && <Alert severity="error" sx={{ mt: 1 }}>{inviteError}</Alert>}
          </Box>
        )}
      </Paper>

      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom>
          Tasks
          <Chip
            label={`${tasks.filter(t => t.completed).length}/${tasks.length} completed`}
            size="small"
            sx={{ ml: 2 }}
          />
        </Typography>

        <Divider sx={{ mb: 2 }} />

        <Box sx={{ display: "flex", gap: 1, mb: 3 }}>
          <TextField
            fullWidth
            size="small"
            placeholder="Add a new task..."
            value={newTask}
            onChange={(e) => setNewTask(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && addTask()}
          />
          <Button variant="contained" onClick={addTask} disabled={!newTask.trim()}>
            <AddIcon />
          </Button>
        </Box>

        {tasks.length === 0 ? (
          <Typography color="text.secondary" align="center" sx={{ py: 4 }}>
            No tasks yet. Add your first task above!
          </Typography>
        ) : (
          <List>
            {tasks.map(task => (
              <ListItem
                key={task.id}
                sx={{
                  bgcolor: "background.paper",
                  mb: 1,
                  borderRadius: 1,
                  border: "1px solid",
                  borderColor: "divider",
                  cursor: "pointer"
                }}
                secondaryAction={
                  <IconButton edge="end" onClick={() => deleteTask(task.id)} size="small">
                    <DeleteIcon />
                  </IconButton>
                }
              >
                <ListItemText
                  primary={task.title}
                  sx={{
                    textDecoration: task.completed ? "line-through" : "none",
                    color: task.completed ? "text.secondary" : "text.primary"
                  }}
                  onClick={() => toggleTaskComplete(task.id, task.completed)}
                />
              </ListItem>
            ))}
          </List>
        )}
      </Paper>
    </Container>
  )
}

export default ProjectDetail
