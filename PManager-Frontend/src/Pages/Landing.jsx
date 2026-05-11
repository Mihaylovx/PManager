import { useNavigate } from "react-router-dom"
import {
  Box,
  Button,
  Container,
  Stack,
  Typography
} from "@mui/material"
import AssignmentIcon from "@mui/icons-material/Assignment"
import { isLoggedIn } from "../auth"

function Landing() {
  const navigate = useNavigate()
  const loggedIn = isLoggedIn()

  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        bgcolor: "background.default"
      }}
    >
      <Container maxWidth="sm">
        <Stack spacing={4} alignItems="center" textAlign="center">
          <AssignmentIcon sx={{ fontSize: 64, color: "primary.main" }} />
          <Box>
            <Typography variant="h3" component="h1" fontWeight={600} gutterBottom>
              Project Manager
            </Typography>
            <Typography variant="h6" color="text.secondary" fontWeight={400}>
              Organize your projects and collaborate with your team in one place.
            </Typography>
          </Box>

          {loggedIn ? (
            <Button
              variant="contained"
              size="large"
              onClick={() => navigate("/projects")}
            >
              Go to Projects
            </Button>
          ) : (
            <Stack direction="row" spacing={2}>
              <Button
                variant="contained"
                size="large"
                onClick={() => navigate("/login")}
              >
                Login
              </Button>
              <Button
                variant="outlined"
                size="large"
                onClick={() => navigate("/register")}
              >
                Register
              </Button>
            </Stack>
          )}
        </Stack>
      </Container>
    </Box>
  )
}

export default Landing
