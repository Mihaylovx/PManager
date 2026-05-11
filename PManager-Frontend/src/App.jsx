import { Routes, Route, Navigate } from "react-router-dom"
import Landing from "./Pages/Landing"
import Login from "./Pages/Login"
import Register from "./Pages/Register"
import Projects from "./Pages/Projects"
import ProjectDetail from "./Pages/ProjectDetail"
import { isLoggedIn } from "./auth"

function RequireAuth({ children }) {
  return isLoggedIn() ? children : <Navigate to="/login" replace />
}

function App() {
  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/projects" element={<RequireAuth><Projects /></RequireAuth>} />
      <Route path="/project/:id" element={<RequireAuth><ProjectDetail /></RequireAuth>} />
    </Routes>
  )
}

export default App
