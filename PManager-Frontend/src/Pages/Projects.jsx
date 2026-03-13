import { useEffect, useState } from "react"

function Projects() {
  const [projects, setProjects] = useState([])
  const [name, setName] = useState("")
  const [description, setDescription] = useState("")
  const [expanded, setExpanded] = useState(null)

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
    }).then(() => { setName(""); setDescription(""); fetchProjects() })
  }

  function deleteProject(id) {
    fetch(`http://localhost:8080/api/projects/${id}`, { method: "DELETE" })
      .then(() => fetchProjects())
  }

  function toggleExpand(id) {
    setExpanded(expanded === id ? null : id)
  }

  return (
    <div style={{ maxWidth: "600px", margin: "60px auto", fontFamily: "sans-serif", padding: "0 20px" }}>
      <h1 style={{ fontSize: "24px", fontWeight: "600", marginBottom: "24px" }}>Projects</h1>

      <div style={{ display: "flex", flexDirection: "column", gap: "8px", marginBottom: "32px" }}>
        <input
          value={name}
          onChange={e => setName(e.target.value)}
          placeholder="Project name"
          style={{
            padding: "10px 14px", border: "1px solid #ddd",
            borderRadius: "6px", fontSize: "14px", outline: "none"
          }}
        />
        <textarea
          value={description}
          onChange={e => setDescription(e.target.value)}
          placeholder="Description (optional)"
          rows={3}
          style={{
            padding: "10px 14px", border: "1px solid #ddd", borderRadius: "6px",
            fontSize: "14px", outline: "none", resize: "none", fontFamily: "sans-serif"
          }}
        />
        <button
          onClick={addProject}
          style={{
            padding: "10px 20px", backgroundColor: "#000", color: "#fff",
            border: "none", borderRadius: "6px", fontSize: "14px", cursor: "pointer"
          }}
        >
          Add Project
        </button>
      </div>

      {projects.length === 0 && (
        <p style={{ color: "#999", fontSize: "14px" }}>No projects yet. Add one above.</p>
      )}

      <ul style={{ listStyle: "none", padding: 0, margin: 0 }}>
        {projects.map(p => (
          <li key={p.id} style={{
            border: "1px solid #eee", borderRadius: "6px",
            marginBottom: "8px", overflow: "hidden"
          }}>
            <div
              onClick={() => toggleExpand(p.id)}
              style={{
                display: "flex", justifyContent: "space-between", alignItems: "center",
                padding: "14px 16px", cursor: "pointer", fontSize: "14px"
              }}
            >
              <span style={{ fontWeight: "500" }}>{p.name}</span>
              <div style={{ display: "flex", gap: "8px", alignItems: "center" }}>
                <span style={{ color: "#999", fontSize: "12px" }}>{expanded === p.id ? "▲" : "▼"}</span>
                <button
                  onClick={e => { e.stopPropagation(); deleteProject(p.id) }}
                  style={{
                    background: "none", border: "1px solid #ddd", borderRadius: "4px",
                    padding: "4px 10px", fontSize: "12px", cursor: "pointer", color: "#666"
                  }}
                >
                  Delete
                </button>
              </div>
            </div>

            {expanded === p.id && (
              <div style={{
                padding: "12px 16px", borderTop: "1px solid #eee",
                fontSize: "14px", color: "#555", backgroundColor: "#fafafa"
              }}>
                {p.description ? p.description : <span style={{ color: "#bbb" }}>No description added.</span>}
              </div>
            )}
          </li>
        ))}
      </ul>
    </div>
  )
}

export default Projects