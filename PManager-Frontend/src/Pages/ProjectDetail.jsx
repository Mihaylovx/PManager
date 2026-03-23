import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
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
  Alert
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import HomeIcon from '@mui/icons-material/Home';

function ProjectDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [project, setProject] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [newTask, setNewTask] = useState("");
  const [tasks, setTasks] = useState([]);

  useEffect(() => {
    fetchProject();
  }, [id]);

  function fetchProject() {
    console.log(`Fetching project: http://localhost:8080/api/projects/${id}`); 
    fetch(`http://localhost:8080/api/projects/${id}`)
      .then(res => {
        if (!res.ok) throw new Error('Project not found');
        return res.json();
      })
      .then(data => {
        setProject(data);
        setTasks(data.tasks || []);
        setLoading(false);
      })
      .catch(err => {
        setError(err.message);
        setLoading(false);
      });
  }

function addTask() {
    if (!newTask.trim()) return;
    
    console.log("Adding task:", newTask);
    
    const taskData = {
        title: newTask,
        completed: false
    };
    
    fetch(`http://localhost:8080/api/projects/${id}/tasks`, {
        method: "POST",
        headers: { 
            "Content-Type": "application/json" 
        },
        body: JSON.stringify(taskData)
    })
    .then(response => {
        console.log("Response status:", response.status);
        
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(`HTTP error! status: ${response.status}, message: ${text}`);
            });
        }
        return response.json();
    })
    .then(data => {
        console.log("Task added successfully:", data);
        setNewTask("");
        fetchProject(); 
    })
    .catch(err => {
        console.error("Error adding task:", err);
        alert("Failed to add task: " + err.message);
    });
}

  function deleteTask(taskId) {
    fetch(`http://localhost:8080/api/projects/${id}/tasks/${taskId}`, {
      method: "DELETE"
    }).then(() => {
      fetchProject();
    });
  }

  function toggleTaskComplete(taskId, completed) {
    console.log("Toggling task:", taskId, "Current status:", completed);
    fetch(`http://localhost:8080/api/projects/${id}/tasks/${taskId}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ completed: !completed })
    }).then(() => {
      fetchProject();
    });
  }

  if (loading) {
    return (
      <Container sx={{ py: 4 }}>
        <Typography>Loading project...</Typography>
      </Container>
    );
  }

  if (error) {
    return (
      <Container sx={{ py: 4 }}>
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
        <Button variant="contained" onClick={() => navigate('/projects')}>
          Back to Projects
        </Button>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Breadcrumbs sx={{ mb: 3 }}>
        <Link 
          color="inherit" 
          onClick={() => navigate('/projects')}
          sx={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}
        >
          <HomeIcon sx={{ mr: 0.5 }} fontSize="small" />
          Projects
        </Link>
        <Typography color="text.primary">{project?.name}</Typography>
      </Breadcrumbs>

      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3, gap: 2 }}>
        <IconButton onClick={() => navigate('/projects')} color="primary">
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4" component="h1">
          {project?.name}
        </Typography>
      </Box>

      {project?.description && (
        <Paper sx={{ p: 3, mb: 4 }}>
          <Typography variant="body1" color="text.secondary">
            {project.description}
          </Typography>
        </Paper>
      )}

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

        <Box sx={{ display: 'flex', gap: 1, mb: 3 }}>
          <TextField
            fullWidth
            size="small"
            placeholder="Add a new task..."
            value={newTask}
            onChange={(e) => setNewTask(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && addTask()}
          />
          <Button 
            variant="contained" 
            onClick={addTask}
            disabled={!newTask.trim()}
          >
            <AddIcon />
          </Button>
        </Box>

        {tasks.length === 0 ? (
          <Typography color="text.secondary" align="center" sx={{ py: 4 }}>
            No tasks yet. Add your first task above!
          </Typography>
        ) : (
          <List>
            {tasks.map((task) => (
              <ListItem
                key={task.id}
                sx={{
                  bgcolor: 'background.paper',
                  mb: 1,
                  borderRadius: 1,
                  border: '1px solid',
                  borderColor: 'divider',
                  cursor: 'pointer'
                }}
                secondaryAction={
                  <IconButton 
                    edge="end" 
                    onClick={() => deleteTask(task.id)}
                    size="small"
                  >
                    <DeleteIcon />
                  </IconButton>
                }
              >
                <ListItemText
                  primary={task.title}
                  sx={{
                    textDecoration: task.completed ? 'line-through' : 'none',
                    color: task.completed ? 'text.secondary' : 'text.primary'
                  }}
                  onClick={() => toggleTaskComplete(task.id, task.completed)}
                />
              </ListItem>
            ))}
          </List>
        )}
      </Paper>
    </Container>
  );
}

export default ProjectDetail;