import { useEffect, useRef, useState } from "react"
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
  Avatar,
  Select,
  MenuItem,
  FormControl,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Tooltip,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow
} from "@mui/material"
import ArrowBackIcon from "@mui/icons-material/ArrowBack"
import AddIcon from "@mui/icons-material/Add"
import DeleteIcon from "@mui/icons-material/Delete"
import HomeIcon from "@mui/icons-material/Home"
import PlayArrowIcon from "@mui/icons-material/PlayArrow"
import StopIcon from "@mui/icons-material/Stop"
import RestartAltIcon from "@mui/icons-material/RestartAlt"
import AttachMoneyIcon from "@mui/icons-material/AttachMoney"
import { getUser } from "../auth"
import { connectToProject } from "../services/websocket"

const STATUS_CONFIG = {
  TODO:        { label: "To Do",       hex: "#9e9e9e" },
  IN_PROGRESS: { label: "In Progress", hex: "#1976d2" },
  REVIEW:      { label: "In Review",   hex: "#ed6c02" },
  DONE:        { label: "Done",        hex: "#2e7d32" }
}

function formatTime(totalSeconds) {
  const h = Math.floor(totalSeconds / 3600)
  const m = Math.floor((totalSeconds % 3600) / 60).toString().padStart(2, "0")
  const s = (totalSeconds % 60).toString().padStart(2, "0")
  return h > 0 ? `${h}:${m}:${s}` : `${m}:${s}`
}

function shortEmail(email) {
  if (!email) return "—"
  return email.split("@")[0]
}

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
  const [inviteSuccess, setInviteSuccess] = useState(null)

  const [memberRates, setMemberRates] = useState({})
  const [savedMarks, setSavedMarks] = useState({})

  const [salaryData, setSalaryData] = useState(null)
  const [showSalary, setShowSalary] = useState(false)

  const [deleteDialog, setDeleteDialog] = useState({ open: false, task: null })

  const [activeTimers, setActiveTimers] = useState({})
  const [lastTick, setLastTick] = useState(0)
  const masterIntervalRef = useRef(null)

  useEffect(() => {
    fetchProject()
  }, [id])

  useEffect(() => {
    if (Object.keys(activeTimers).length > 0) {
      const tickId = setInterval(() => setLastTick(Date.now()), 1000)
      return () => clearInterval(tickId)
    } else {
      return undefined
    }
  }, [activeTimers])

  useEffect(() => () => clearInterval(masterIntervalRef.current), [])

  useEffect(() => {
    return connectToProject(id, (event) => {
      if (event.type === "TASK_ADDED") {
        setTasks(prev => [...prev, event.data])
      } else if (event.type === "TASK_DELETED") {
        setTasks(prev => prev.filter(t => t.id !== event.data))
      } else if (event.type === "TASK_UPDATED") {
        setTasks(prev => prev.map(t => t.id === event.data.id ? event.data : t))
      } else if (event.type === "MEMBER_ADDED") {
        setProject(prev => ({ ...prev, memberEmails: [...(prev.memberEmails || []), event.data] }))
      }
    })
  }, [id])

  function fetchProject() {
    fetch(`http://localhost:8080/api/projects/${id}`)
      .then(res => { if (!res.ok) throw new Error("Project not found"); return res.json() })
      .then(data => {
        setProject(data)
        setTasks(data.tasks || [])
        setLoading(false)
        const members = [data.managerEmail, ...(data.memberEmails ? Array.from(data.memberEmails) : [])].filter(Boolean)
        fetchMemberRates(members)
      })
      .catch(err => { setError(err.message); setLoading(false) })
  }

  function fetchMemberRates(members) {
    members.forEach(email => {
      fetch(`http://localhost:8080/api/users/${encodeURIComponent(email)}`)
        .then(res => res.ok ? res.json() : null)
        .then(data => {
          if (data?.hourlyRate != null) {
            setMemberRates(prev => ({ ...prev, [email]: String(data.hourlyRate) }))
          }
        })
        .catch(() => {})
    })
  }

  function fetchSalary() {
    fetch(`http://localhost:8080/api/projects/${id}/salary`)
      .then(res => res.json())
      .then(setSalaryData)
      .catch(() => {})
  }

  function addTask() {
    if (!newTask.trim()) return
    fetch(`http://localhost:8080/api/projects/${id}/tasks`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ title: newTask, status: "TODO", hoursWorked: 0 })
    })
      .then(res => { if (!res.ok) throw new Error("Failed to add task"); return res.json() })
      .then(() => { setNewTask(""); fetchProject() })
      .catch(err => alert(err.message))
  }

  function updateTaskStatus(taskId, status) {
    fetch(`http://localhost:8080/api/projects/${id}/tasks/${taskId}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ status })
    }).then(() => fetchProject())
  }

  function updateTaskAssignee(taskId, assignedTo) {
    fetch(`http://localhost:8080/api/projects/${id}/tasks/${taskId}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ assignedTo: assignedTo || null })
    }).then(() => fetchProject())
  }

  function startTimer(taskId) {
    const now = Date.now()
    setLastTick(now)
    setActiveTimers(prev => ({ ...prev, [taskId]: now }))
  }

  function stopAndSaveTimer(taskId, currentHours) {
    const startTime = activeTimers[taskId]
    setActiveTimers(prev => { const n = { ...prev }; delete n[taskId]; return n })

    if (!startTime) return
    const elapsedMs = Date.now() - startTime
    if (elapsedMs < 1000) return

    const elapsedHours = elapsedMs / 3600000
    const newHours = Math.round(((currentHours || 0) + elapsedHours) * 10000) / 10000

    fetch(`http://localhost:8080/api/projects/${id}/tasks/${taskId}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ hoursWorked: newHours })
    })
      .then(() => fetchProject())
      .then(() => { if (showSalary) fetchSalary() })
  }

  function resetHours(taskId) {
    fetch(`http://localhost:8080/api/projects/${id}/tasks/${taskId}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ hoursWorked: 0 })
    })
      .then(() => fetchProject())
      .then(() => { if (showSalary) fetchSalary() })
  }

  async function inviteMember() {
    setInviteError(null); setInviteSuccess(null)
    const email = inviteEmail.trim().toLowerCase()
    if (!email) return
    try {
      const res = await fetch(`http://localhost:8080/api/projects/${id}/invite`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, invitedBy: user.email })
      })
      if (!res.ok) throw new Error(await res.text() || "Could not invite")
      setInviteEmail("")
      setInviteSuccess(`Invite sent to ${email}.`)
    } catch (err) {
      setInviteError(err.message)
    }
  }

  async function saveRateFor(email) {
    const rate = parseFloat(memberRates[email])
    if (isNaN(rate) || rate < 0) return
    const res = await fetch(`http://localhost:8080/api/users/${encodeURIComponent(email)}/hourly-rate`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ hourlyRate: rate })
    })
    if (res.ok) {
      setSavedMarks(prev => ({ ...prev, [email]: true }))
      setTimeout(() => setSavedMarks(prev => ({ ...prev, [email]: false })), 2000)
      if (showSalary) fetchSalary()
    }
  }

  if (loading) return <Container sx={{ py: 4 }}><Typography>Loading...</Typography></Container>
  if (error) return (
    <Container sx={{ py: 4 }}>
      <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>
      <Button variant="contained" onClick={() => navigate("/projects")}>Back</Button>
    </Container>
  )

  const isManager = project?.managerEmail === user.email
  const memberEmails = project?.memberEmails ? Array.from(project.memberEmails) : []
  const allMembers = [project?.managerEmail, ...memberEmails].filter(Boolean)

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Breadcrumbs sx={{ mb: 3 }}>
        <Link color="inherit" onClick={() => navigate("/projects")}
          sx={{ display: "flex", alignItems: "center", cursor: "pointer" }}>
          <HomeIcon sx={{ mr: 0.5 }} fontSize="small" />Projects
        </Link>
        <Typography color="text.primary">{project?.name}</Typography>
      </Breadcrumbs>

      <Box sx={{ display: "flex", alignItems: "center", mb: 3, gap: 2 }}>
        <IconButton onClick={() => navigate("/projects")} color="primary">
          <ArrowBackIcon />
        </IconButton>
        <Box sx={{ flexGrow: 1 }}>
          <Typography variant="h4" fontWeight={700}>{project?.name}</Typography>
          {project?.description && (
            <Typography variant="body2" color="text.secondary">{project.description}</Typography>
          )}
        </Box>
        <Chip label={isManager ? "Manager" : "Developer"} color={isManager ? "primary" : "default"} />
      </Box>

      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" fontWeight={600} gutterBottom>
          Team ({allMembers.length})
        </Typography>
        <Divider sx={{ mb: 2 }} />

        <List dense disablePadding>
          {allMembers.map((email, i) => {
            const isMe = email === user.email
            const role = email === project?.managerEmail ? "Manager" : "Developer"
            return (
              <ListItem key={email} disablePadding sx={{ mb: 1.5, alignItems: "center" }}>
                <Avatar sx={{ bgcolor: i === 0 ? "primary.main" : "grey.400", width: 32, height: 32, mr: 1.5, fontSize: 14 }}>
                  {email[0].toUpperCase()}
                </Avatar>
                <ListItemText
                  primary={email}
                  secondary={role}
                  primaryTypographyProps={{ variant: "body2", fontWeight: isMe ? 600 : 400 }}
                  secondaryTypographyProps={{ variant: "caption" }}
                  sx={{ flex: 1 }}
                />
                {isManager && (
                  <Stack direction="row" spacing={0.5} alignItems="center">
                    <TextField
                      size="small"
                      type="number"
                      label="€/h"
                      value={memberRates[email] || ""}
                      onChange={e => setMemberRates(prev => ({ ...prev, [email]: e.target.value }))}
                      onKeyDown={e => e.key === "Enter" && saveRateFor(email)}
                      inputProps={{ min: 0, step: 1 }}
                      sx={{ width: 75 }}
                    />
                    <Button size="small" variant="outlined" onClick={() => saveRateFor(email)} sx={{ minWidth: 50 }}>
                      {savedMarks[email] ? "✓" : "Set"}
                    </Button>
                  </Stack>
                )}
              </ListItem>
            )
          })}
          {memberEmails.length === 0 && !isManager && (
            <Typography variant="body2" color="text.secondary">No other members yet.</Typography>
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
                onChange={e => setInviteEmail(e.target.value)}
                onKeyDown={e => e.key === "Enter" && inviteMember()}
                fullWidth
              />
              <Button variant="outlined" onClick={inviteMember}>Invite</Button>
            </Stack>
            {inviteError && <Alert severity="error" sx={{ mt: 1 }} onClose={() => setInviteError(null)}>{inviteError}</Alert>}
            {inviteSuccess && <Alert severity="success" sx={{ mt: 1 }} onClose={() => setInviteSuccess(null)}>{inviteSuccess}</Alert>}
          </Box>
        )}
      </Paper>

      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" fontWeight={600} gutterBottom>
          Tasks
          <Chip label={`${tasks.filter(t => t.status === "DONE").length}/${tasks.length} done`} size="small" sx={{ ml: 1 }} />
        </Typography>
        <Divider sx={{ mb: 2 }} />

        <Stack direction="row" spacing={1} sx={{ mb: 2 }}>
          <TextField
            fullWidth
            size="small"
            placeholder="Add a task and press Enter..."
            value={newTask}
            onChange={e => setNewTask(e.target.value)}
            onKeyDown={e => e.key === "Enter" && addTask()}
          />
          <Button variant="contained" onClick={addTask} disabled={!newTask.trim()}>
            <AddIcon />
          </Button>
        </Stack>

        {tasks.length > 0 && (
          <Box sx={{ display: "flex", alignItems: "center", px: 1.5, mb: 0.5, gap: 1 }}>
            <Typography variant="caption" color="text.secondary" sx={{ flex: 1 }}>Task</Typography>
            <Typography variant="caption" color="text.secondary" sx={{ width: 130, textAlign: "center" }}>Status</Typography>
            <Typography variant="caption" color="text.secondary" sx={{ width: 120, textAlign: "center" }}>Assigned to</Typography>
            <Typography variant="caption" color="text.secondary" sx={{ width: 120, textAlign: "center" }}>Timer</Typography>
            <Box sx={{ width: 32 }} />
          </Box>
        )}

        {tasks.length === 0 ? (
          <Typography color="text.secondary" align="center" sx={{ py: 4 }}>
            No tasks yet. Add one above!
          </Typography>
        ) : (
          <List disablePadding>
            {tasks.map(task => {
              const cfg = STATUS_CONFIG[task.status || "TODO"]
              const isRunning = !!activeTimers[task.id]
              const elapsedSeconds = isRunning
                ? Math.floor((lastTick - activeTimers[task.id]) / 1000)
                : 0

              return (
                <ListItem
                  key={task.id}
                  disablePadding
                  sx={{
                    display: "flex",
                    alignItems: "center",
                    gap: 1,
                    p: 1.5,
                    mb: 1,
                    borderRadius: 2,
                    border: "1px solid",
                    borderColor: task.status === "DONE" ? "success.light" : "divider",
                    bgcolor: task.status === "DONE" ? "#f0faf0" : "background.paper"
                  }}
                >
                  <Typography
                    sx={{
                      flex: 1,
                      fontWeight: 500,
                      textDecoration: task.status === "DONE" ? "line-through" : "none",
                      color: task.status === "DONE" ? "text.secondary" : "text.primary",
                      wordBreak: "break-word"
                    }}
                  >
                    {task.title}
                  </Typography>

                  <FormControl size="small" sx={{ width: 130 }}>
                    <Select
                      value={task.status || "TODO"}
                      onChange={e => updateTaskStatus(task.id, e.target.value)}
                      renderValue={val => {
                        const c = STATUS_CONFIG[val] || STATUS_CONFIG.TODO
                        return (
                          <Chip
                            label={c.label}
                            size="small"
                            sx={{ bgcolor: c.hex + "22", color: c.hex, border: `1px solid ${c.hex}44`, fontWeight: 600, cursor: "pointer" }}
                          />
                        )
                      }}
                      sx={{ "& .MuiSelect-select": { p: "4px 8px" } }}
                    >
                      {Object.entries(STATUS_CONFIG).map(([key, c]) => (
                        <MenuItem key={key} value={key}>
                          <Chip label={c.label} size="small"
                            sx={{ bgcolor: c.hex + "22", color: c.hex, border: `1px solid ${c.hex}44`, fontWeight: 600 }} />
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>

                  {isManager ? (
                    <FormControl size="small" sx={{ width: 120 }}>
                      <Select
                        value={task.assignedTo || ""}
                        onChange={e => updateTaskAssignee(task.id, e.target.value)}
                        displayEmpty
                        renderValue={v => (
                          <Typography variant="caption" color={v ? "text.primary" : "text.secondary"}>
                            {v ? shortEmail(v) : "Unassigned"}
                          </Typography>
                        )}
                        sx={{ "& .MuiSelect-select": { p: "4px 8px" } }}
                      >
                        <MenuItem value=""><em>Unassigned</em></MenuItem>
                        {allMembers.map(email => (
                          <MenuItem key={email} value={email}>
                            <Typography variant="caption">{email}</Typography>
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  ) : (
                    <Typography variant="caption" color="text.secondary" sx={{ width: 120, textAlign: "center" }}>
                      {task.assignedTo ? shortEmail(task.assignedTo) : "Unassigned"}
                    </Typography>
                  )}

                  <Box sx={{ display: "flex", alignItems: "center", gap: 0.5, width: 120 }}>
                    <Typography variant="caption" sx={{ fontFamily: "monospace", fontSize: 13, minWidth: 36 }}>
                      {isRunning
                        ? formatTime(elapsedSeconds)
                        : `${(task.hoursWorked || 0).toFixed(2)}h`}
                    </Typography>
                    {(isRunning || task.assignedTo === user.email) && (
                      <Tooltip title={isRunning ? "Stop and save time" : "Start timer"}>
                        <IconButton
                          size="small"
                          color={isRunning ? "error" : "default"}
                          onClick={() => isRunning
                            ? stopAndSaveTimer(task.id, task.hoursWorked)
                            : startTimer(task.id)}
                        >
                          {isRunning ? <StopIcon fontSize="small" /> : <PlayArrowIcon fontSize="small" />}
                        </IconButton>
                      </Tooltip>
                    )}
                    {isManager && !isRunning && (task.hoursWorked || 0) > 0 && (
                      <Tooltip title="Reset hours to 0">
                        <IconButton size="small" onClick={() => resetHours(task.id)}>
                          <RestartAltIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    )}
                  </Box>

                  <Tooltip title="Delete task">
                    <IconButton size="small" color="error" onClick={() => setDeleteDialog({ open: true, task })}>
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                </ListItem>
              )
            })}
          </List>
        )}
      </Paper>

      {isManager && (
        <Paper sx={{ p: 3 }}>
          <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 1 }}>
            <Typography variant="h6" fontWeight={600}>
              <AttachMoneyIcon sx={{ verticalAlign: "middle", mr: 0.5, fontSize: 20 }} />
              Salary Report
            </Typography>
            <Button
              size="small"
              variant={showSalary ? "outlined" : "contained"}
              onClick={() => { if (!showSalary) fetchSalary(); setShowSalary(s => !s) }}
            >
              {showSalary ? "Hide" : "Calculate"}
            </Button>
          </Box>
          {showSalary && (
            salaryData == null ? (
              <Typography variant="body2" color="text.secondary">Loading...</Typography>
            ) : salaryData.length === 0 ? (
              <Typography variant="body2" color="text.secondary">No developers to show.</Typography>
            ) : (
              <Table size="small" sx={{ mt: 1 }}>
                <TableHead>
                  <TableRow>
                    <TableCell>Developer</TableCell>
                    <TableCell align="right">Hours</TableCell>
                    <TableCell align="right">Rate</TableCell>
                    <TableCell align="right">Total</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {salaryData.map(row => (
                    <TableRow key={row.email}>
                      <TableCell><Typography variant="caption">{row.email}</Typography></TableCell>
                      <TableCell align="right">{row.totalHours}h</TableCell>
                      <TableCell align="right">€{row.hourlyRate}/h</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 600 }}>€{row.salary.toFixed(2)}</TableCell>
                    </TableRow>
                  ))}
                  <TableRow>
                    <TableCell colSpan={3} sx={{ fontWeight: 600 }}>Total</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 700, color: "success.main" }}>
                      €{salaryData.reduce((s, r) => s + r.salary, 0).toFixed(2)}
                    </TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            )
          )}
        </Paper>
      )}

      <Dialog open={deleteDialog.open} onClose={() => setDeleteDialog({ open: false, task: null })}>
        <DialogTitle>Delete Task?</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Delete <strong>"{deleteDialog.task?.title}"</strong>?
            {(deleteDialog.task?.hoursWorked || 0) > 0 && (
              <Alert severity="warning" sx={{ mt: 1.5 }}>
                This task has <strong>{deleteDialog.task.hoursWorked}h</strong> logged. Those hours will be lost.
              </Alert>
            )}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialog({ open: false, task: null })}>Cancel</Button>
          <Button
            color="error"
            variant="contained"
            onClick={() => {
              const taskId = deleteDialog.task.id
              setDeleteDialog({ open: false, task: null })
              fetch(`http://localhost:8080/api/projects/${id}/tasks/${taskId}`, { method: "DELETE" })
                .then(() => fetchProject())
            }}
          >
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  )
}

export default ProjectDetail
