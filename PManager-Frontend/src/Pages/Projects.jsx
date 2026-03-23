import { useEffect, useState } from "react"
import { useNavigate } from 'react-router-dom';
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
  CardActionArea
} from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import AddIcon from '@mui/icons-material/Add'
import FolderIcon from '@mui/icons-material/Folder'

function Projects() {
  const [projects, setProjects] = useState([])
  const [name, setName] = useState("")
  const [description, setDescription] = useState("")
  const [showForm, setShowForm] = useState(false)
  const navigate = useNavigate()

  useEffect(() => { fetchProjects() }, [])

  function fetchProjects() {
    fetch("http://localhost:8080/api/projects")
      .then(res => res.json())
      .then(data => setProjects(data))
  }

  function addProject() {
    if (!name) return
    fetch("http://localhost:8080/api/projects", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name, description })
    }).then(() => { 
      setName(""); 
      setDescription(""); 
      setShowForm(false);
      fetchProjects() 
    })
  }

  function deleteProject(id, event) {
    fetch(`http://localhost:8080/api/projects/${id}`, { method: "DELETE" })
      .then(() => fetchProjects())
  }

  const handleCardClick = (projectId) => {
    navigate(`/project/${projectId}`);
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Typography variant="h4" component="h1">
          Projects
        </Typography>
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
          <Typography variant="h6" gutterBottom>
            Create New Project
          </Typography>
          <Box component="form" sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              fullWidth
              label="Project name"
              variant="outlined"
              value={name}
              onChange={e => setName(e.target.value)}
              autoFocus
            />
            <TextField
              fullWidth
              label="Description"
              variant="outlined"
              multiline
              rows={3}
              value={description}
              onChange={e => setDescription(e.target.value)}
            />
            <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
              <Button onClick={() => setShowForm(false)}>
                Cancel
              </Button>
              <Button 
                variant="contained" 
                onClick={addProject}
                disabled={!name}
              >
                Create Project
              </Button>
            </Box>
          </Box>
        </Paper>
      )}

      {projects.length === 0 && (
        <Alert severity="info" sx={{ mt: 2 }}>
          No projects yet. Click "New Project" to create one.
        </Alert>
      )}

      <Grid container spacing={3}>
        {projects.map(project => (
          <Grid item xs={12} sm={6} md={4} key={project.id}>
            <Card 
              sx={{ 
                height: 225, 
                width: 300,
                display: 'flex', 
                flexDirection: 'column',
                transition: 'transform 0.2s, box-shadow 0.2s',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: 6
                }
              }}
            >
              <CardActionArea onClick={() => handleCardClick(project.id)}>
                <CardContent sx={{ flexGrow: 1 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <FolderIcon sx={{ mr: 1, color: 'primary.main' }} />
                    <Typography variant="h6" component="h2" noWrap>
                      {project.name}
                    </Typography>
                  </Box>
                  <Typography 
                    variant="body2" 
                    color="text.secondary"
                    sx={{
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      display: '-webkit-box',
                      WebkitLineClamp: 3,
                      WebkitBoxOrient: 'vertical',
                    }}
                  >
                    {project.description || "No description provided."}
                  </Typography>
                  <Chip 
                    label={`Tasks: ${project.tasks?.length || 0}`} 
                    size="small" 
                    sx={{ mt: 2 }}
                  />
                </CardContent>
              </CardActionArea>
              <CardActions sx={{ justifyContent: 'flex-end', p: 1 }}>
                <IconButton 
                  size="small" 
                  color="error"
                  onClick={(e) => deleteProject(project.id, e)}
                >
                  <DeleteIcon />
                </IconButton>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Container>
  )
}

export default Projects