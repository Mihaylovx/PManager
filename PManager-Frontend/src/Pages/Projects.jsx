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
  Toolbar,
  Badge,
  Collapse,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  Avatar,
  Tooltip,
  Divider,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions
} from "@mui/material"
import DeleteIcon from "@mui/icons-material/Delete"
import AddIcon from "@mui/icons-material/Add"
import FolderIcon from "@mui/icons-material/Folder"
import LogoutIcon from "@mui/icons-material/Logout"
import NotificationsIcon from "@mui/icons-material/Notifications"
import CheckCircleIcon from "@mui/icons-material/CheckCircle"
import CancelIcon from "@mui/icons-material/Cancel"
import GroupIcon from "@mui/icons-material/Group"
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

  const [pendingInvites, setPendingInvites] = useState([])
  const [showInvites, setShowInvites] = useState(false)
  const [inviteActionMsg, setInviteActionMsg] = useState(null)

  const [deleteDialog, setDeleteDialog] = useState({ open: false, projectId: null, projectName: "" })

  const navigate = useNavigate()
  const user = getUser()

  useEffect(() => {
    fetchProjects()
    fetchPendingInvites()
  }, [])

  function fetchProjects() {
    fetch(`http://localhost:8080/api/projects?user=${encodeURIComponent(user.email)}`)
      .then(res => res.json())
      .then(data => setProjects(data))
      .catch(() => setLoadError("Failed to load projects. Is the server running?"))
  }

  function fetchPendingInvites() {
    fetch(`http://localhost:8080/api/invites?user=${encodeURIComponent(user.email)}`)
      .then(res => res.ok ? res.json() : [])
      .then(data => setPendingInvites(data))
      .catch(() => {})
  }

  async function handleInviteAction(inviteId, action) {
    try {
      const res = await fetch(`http://localhost:8080/api/invites/${inviteId}/${action}`, { method: "POST" })
      if (!res.ok) throw new Error("Action failed")
      setInviteActionMsg(action === "accept" ? "Invite accepted!" : "Invite declined.")
      setTimeout(() => setInviteActionMsg(null), 3000)
      fetchPendingInvites()
      fetchProjects()
    } catch {
      setInviteActionMsg("Something went wrong.")
    }
  }

  function addInviteEmail() {
    const email = inviteInput.trim().toLowerCase()
    if (!email) return
    if (email === user.email.toLowerCase()) {
      setInviteWarning("You're already the manager.")
      return
    }
    if (invitedEmails.includes(email)) return
    setInvitedEmails([...invitedEmails, email])
    setInviteInput("")
    setInviteWarning(null)
  }

  async function addProject() {
    if (!name) return
    setCreateError(null)
    try {
      const res = await fetch("http://localhost:8080/api/projects", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, description, managerEmail: user.email, memberEmails: invitedEmails })
      })
      if (!res.ok) throw new Error(await res.text() || "Failed to create project")
      setName(""); setDescription(""); setInviteInput(""); setInvitedEmails([])
      setShowForm(false)
      fetchProjects()
    } catch (err) {
      setCreateError(err.message)
    }
  }

  function confirmDeleteProject(id, projectName, e) {
    e.stopPropagation()
    setDeleteDialog({ open: true, projectId: id, projectName })
  }

  function executeDeleteProject() {
    fetch(`http://localhost:8080/api/projects/${deleteDialog.projectId}`, { method: "DELETE" })
      .then(() => { setDeleteDialog({ open: false, projectId: null, projectName: "" }); fetchProjects() })
  }

  function logout() {
    clearUser()
    navigate("/")
  }

  function getTaskStats(project) {
    const tasks = project.tasks || []
    return {
      total: tasks.length,
      done: tasks.filter(t => t.status === "DONE").length,
      inProgress: tasks.filter(t => t.status === "IN_PROGRESS").length,
      review: tasks.filter(t => t.status === "REVIEW").length,
      todo: tasks.filter(t => !t.status || t.status === "TODO").length
    }
  }

  return (
    <Box sx={{ minHeight: "100vh", bgcolor: "grey.50" }}>
      <AppBar position="static" color="default" elevation={1}>
        <Toolbar>
          <FolderIcon sx={{ mr: 1, color: "primary.main" }} />
          <Typography variant="h6" sx={{ flexGrow: 1, fontWeight: 700 }}>Project Manager</Typography>
          <Typography variant="body2" sx={{ mr: 2 }} color="text.secondary">
            {user.firstName} {user.lastName}
          </Typography>
          <Tooltip title={pendingInvites.length > 0 ? `${pendingInvites.length} pending invite(s)` : "No pending invites"}>
            <IconButton color={pendingInvites.length > 0 ? "primary" : "inherit"}
              onClick={() => setShowInvites(s => !s)} sx={{ mr: 1 }}>
              <Badge badgeContent={pendingInvites.length} color="error">
                <NotificationsIcon />
              </Badge>
            </IconButton>
          </Tooltip>
          <IconButton onClick={logout} color="inherit" title="Logout">
            <LogoutIcon />
          </IconButton>
        </Toolbar>
      </AppBar>

      <Collapse in={showInvites && pendingInvites.length > 0}>
        <Paper elevation={0} sx={{ borderRadius: 0, borderBottom: "1px solid", borderColor: "divider" }}>
          <Container maxWidth="lg" sx={{ py: 2 }}>
            <Typography variant="subtitle1" fontWeight={600} gutterBottom>Project Invitations</Typography>
            {inviteActionMsg && <Alert severity="info" sx={{ mb: 1 }}>{inviteActionMsg}</Alert>}
            <List disablePadding>
              {pendingInvites.map(invite => (
                <ListItem key={invite.id} disablePadding
                  sx={{ bgcolor: "background.paper", mb: 1, borderRadius: 2, border: "1px solid", borderColor: "primary.light", p: 1.5 }}
                  secondaryAction={
                    <Stack direction="row" spacing={1}>
                      <Button size="small" variant="contained" color="success" startIcon={<CheckCircleIcon />}
                        onClick={() => handleInviteAction(invite.id, "accept")}>Accept</Button>
                      <Button size="small" variant="outlined" color="error" startIcon={<CancelIcon />}
                        onClick={() => handleInviteAction(invite.id, "decline")}>Decline</Button>
                    </Stack>
                  }
                >
                  <ListItemAvatar>
                    <Avatar sx={{ bgcolor: "primary.main", width: 36, height: 36 }}>
                      <FolderIcon fontSize="small" />
                    </Avatar>
                  </ListItemAvatar>
                  <ListItemText
                    primary={<Typography fontWeight={600}>{invite.projectName}</Typography>}
                    secondary={`Invited by ${invite.invitedByEmail}`}
                  />
                </ListItem>
              ))}
            </List>
          </Container>
        </Paper>
      </Collapse>

      {pendingInvites.length > 0 && !showInvites && (
        <Alert severity="info" action={<Button size="small" onClick={() => setShowInvites(true)}>View</Button>}
          sx={{ borderRadius: 0 }}>
          You have {pendingInvites.length} pending invitation{pendingInvites.length > 1 ? "s" : ""}.
        </Alert>
      )}

      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 4 }}>
          <Typography variant="h4" fontWeight={700}>Projects</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => setShowForm(!showForm)}>
            New Project
          </Button>
        </Box>

        {showForm && (
          <Paper elevation={2} sx={{ p: 3, mb: 4, borderRadius: 3 }}>
            <Typography variant="h6" gutterBottom fontWeight={600}>Create New Project</Typography>
            <Stack spacing={2}>
              <TextField fullWidth label="Project name" value={name} onChange={e => setName(e.target.value)} autoFocus />
              <TextField fullWidth label="Description (optional)" multiline rows={2}
                value={description} onChange={e => setDescription(e.target.value)} />
              <Box>
                <Typography variant="subtitle2" gutterBottom>Invite developers (optional)</Typography>
                <Stack direction="row" spacing={1}>
                  <TextField fullWidth size="small" type="email" placeholder="developer@example.com"
                    value={inviteInput} onChange={e => setInviteInput(e.target.value)}
                    onKeyDown={e => { if (e.key === "Enter") { e.preventDefault(); addInviteEmail() } }} />
                  <Button onClick={addInviteEmail} variant="outlined">Add</Button>
                </Stack>
                {inviteWarning && <Alert severity="warning" sx={{ mt: 1 }}>{inviteWarning}</Alert>}
                {invitedEmails.length > 0 && (
                  <Box sx={{ mt: 1.5, display: "flex", flexWrap: "wrap", gap: 1 }}>
                    {invitedEmails.map(email => (
                      <Chip key={email} label={email} onDelete={() => setInvitedEmails(invitedEmails.filter(e => e !== email))} />
                    ))}
                  </Box>
                )}
              </Box>
              {createError && <Alert severity="error">{createError}</Alert>}
              <Box sx={{ display: "flex", gap: 2, justifyContent: "flex-end" }}>
                <Button onClick={() => { setShowForm(false); setCreateError(null) }}>Cancel</Button>
                <Button variant="contained" onClick={addProject} disabled={!name}>Create</Button>
              </Box>
            </Stack>
          </Paper>
        )}

        {loadError && <Alert severity="error" sx={{ mb: 2 }}>{loadError}</Alert>}
        {!loadError && projects.length === 0 && (
          <Alert severity="info">No projects yet. Click "New Project" to create one.</Alert>
        )}

        <Grid container spacing={3}>
          {projects.map(project => {
            const isManager = project.managerEmail === user.email
            const stats = getTaskStats(project)
            const memberCount = project.memberEmails?.length || 0

            return (
              <Grid item xs={12} sm={6} md={4} key={project.id}>
                <Card sx={{
                  height: "100%",
                  display: "flex",
                  flexDirection: "column",
                  borderRadius: 3,
                  transition: "transform 0.2s, box-shadow 0.2s",
                  "&:hover": { transform: "translateY(-4px)", boxShadow: 8 }
                }}>
                  <CardActionArea onClick={() => navigate(`/project/${project.id}`)} sx={{ flexGrow: 1 }}>
                    <CardContent>
                      <Box sx={{ display: "flex", alignItems: "flex-start", mb: 1.5 }}>
                        <FolderIcon sx={{ mr: 1, color: "primary.main", mt: 0.3, flexShrink: 0 }} />
                        <Typography variant="h6" fontWeight={600} sx={{ lineHeight: 1.3 }}>
                          {project.name}
                        </Typography>
                      </Box>

                      <Typography variant="body2" color="text.secondary" sx={{
                        overflow: "hidden", textOverflow: "ellipsis",
                        display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical",
                        minHeight: 40, mb: 2
                      }}>
                        {project.description || "No description."}
                      </Typography>

                      <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                        <Chip label={isManager ? "Manager" : "Developer"} size="small"
                          color={isManager ? "primary" : "default"} />
                        <Chip
                          icon={<GroupIcon sx={{ fontSize: "14px !important" }} />}
                          label={`${memberCount + 1}`}
                          size="small" variant="outlined"
                          title={`${memberCount + 1} team member${memberCount + 1 !== 1 ? "s" : ""}`}
                        />
                        <Chip label={`${stats.done}/${stats.total} done`} size="small" variant="outlined" />
                      </Stack>
                    </CardContent>
                  </CardActionArea>

                  <Divider />
                  <CardActions sx={{ justifyContent: "space-between", px: 2, py: 1 }}>
                    <Typography variant="caption" color="text.secondary">
                      {stats.inProgress > 0 ? `${stats.inProgress} in progress` : stats.total === 0 ? "No tasks" : `${stats.todo} to do`}
                    </Typography>
                    {isManager && (
                      <Tooltip title="Delete project">
                        <IconButton size="small" color="error"
                          onClick={e => confirmDeleteProject(project.id, project.name, e)}>
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    )}
                  </CardActions>
                </Card>
              </Grid>
            )
          })}
        </Grid>
      </Container>

      <Dialog open={deleteDialog.open} onClose={() => setDeleteDialog({ open: false, projectId: null, projectName: "" })}>
        <DialogTitle>Delete Project?</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete <strong>"{deleteDialog.projectName}"</strong>?
            This will permanently delete the project and all its tasks.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialog({ open: false, projectId: null, projectName: "" })}>Cancel</Button>
          <Button color="error" variant="contained" onClick={executeDeleteProject}>Delete</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

export default Projects
