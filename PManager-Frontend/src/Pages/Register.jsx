import { useState } from "react"
import { useNavigate, Link as RouterLink } from "react-router-dom"
import {
  Alert,
  Box,
  Button,
  Container,
  Link,
  Paper,
  Stack,
  TextField,
  Typography
} from "@mui/material"
import { setUser } from "../auth"

function Register() {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    email: "",
    firstName: "",
    lastName: "",
    password: ""
  })
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  function update(field) {
    return (e) => setForm({ ...form, [field]: e.target.value })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const res = await fetch("http://localhost:8080/api/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(form)
      })
      if (!res.ok) {
        const msg = await res.text()
        throw new Error(msg || "Registration failed")
      }
      const user = await res.json()
      setUser(user)
      navigate("/projects")
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Box sx={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", py: 4 }}>
      <Container maxWidth="xs">
        <Paper elevation={2} sx={{ p: 4 }}>
          <Stack spacing={3}>
            <Box textAlign="center">
              <Typography variant="h5" fontWeight={600}>Create your account</Typography>
              <Typography variant="body2" color="text.secondary">
                Start managing your projects
              </Typography>
            </Box>

            {error && <Alert severity="error">{error}</Alert>}

            <Box component="form" onSubmit={handleSubmit}>
              <Stack spacing={2}>
                <TextField
                  label="Email"
                  type="email"
                  value={form.email}
                  onChange={update("email")}
                  fullWidth
                  required
                  autoFocus
                />
                <Stack direction="row" spacing={2}>
                  <TextField
                    label="First name"
                    value={form.firstName}
                    onChange={update("firstName")}
                    fullWidth
                    required
                  />
                  <TextField
                    label="Last name"
                    value={form.lastName}
                    onChange={update("lastName")}
                    fullWidth
                    required
                  />
                </Stack>
                <TextField
                  label="Password"
                  type="password"
                  value={form.password}
                  onChange={update("password")}
                  fullWidth
                  required
                  helperText="At least 6 characters"
                />
                <Button
                  type="submit"
                  variant="contained"
                  size="large"
                  disabled={submitting}
                >
                  {submitting ? "Creating account..." : "Register"}
                </Button>
              </Stack>
            </Box>

            <Typography variant="body2" textAlign="center" color="text.secondary">
              Already have an account?{" "}
              <Link component={RouterLink} to="/login">Login</Link>
            </Typography>
          </Stack>
        </Paper>
      </Container>
    </Box>
  )
}

export default Register
