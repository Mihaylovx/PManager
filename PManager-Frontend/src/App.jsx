import { Routes, Route } from "react-router-dom"
import Landing from "./Pages/Landing"
import Projects from "./Pages/Projects"
import ProjectDetail from "./Pages/ProjectDetail"

function App() {
  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/projects" element={<Projects />} />
      <Route path="/project/:id" element={<ProjectDetail />} />
    </Routes>
  )
}

export default App