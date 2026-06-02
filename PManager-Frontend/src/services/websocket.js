import { Client } from "@stomp/stompjs"

export function connectToProject(projectId, onEvent) {
    const client = new Client({
        brokerURL: "ws://localhost:8080/ws",
        reconnectDelay: 5000,
        onConnect: () => {
            client.subscribe(`/topic/projects/${projectId}`, (message) => {
                onEvent(JSON.parse(message.body))
            })
        },
    })
    client.activate()
    return () => client.deactivate()
}
