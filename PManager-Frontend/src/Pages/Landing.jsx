import { useNavigate } from "react-router-dom"

function Landing() {
  const navigate = useNavigate()

  return (
    <div style={{
      height: "100vh", display: "flex", flexDirection: "column",
      justifyContent: "center", alignItems: "center", fontFamily: "sans-serif"
    }}>
      <h1 style={{ fontSize: "32px", fontWeight: "600", marginBottom: "8px" }}>Project Manager</h1>
      <p style={{ color: "#999", marginBottom: "32px", fontSize: "15px" }}>Keep track of your work in one place</p>
      <button
        onClick={() => navigate("/projects")}
        style={{
          padding: "12px 28px", backgroundColor: "#000", color: "#fff",
          border: "none", borderRadius: "6px", fontSize: "14px", cursor: "pointer"
        }}
      >
        Get Started
      </button>
    </div>
  )
}

export default Landing