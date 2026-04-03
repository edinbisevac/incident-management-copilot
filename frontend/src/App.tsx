import { type FormEvent, useEffect, useState } from 'react'
import './App.css'

type IncidentStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED'
type IncidentPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
type AlarmSeverity = 'MINOR' | 'MAJOR' | 'CRITICAL'

type Incident = {
  id: number
  title: string
  description: string
  status: IncidentStatus
  priority: IncidentPriority
  source: string
}

type Alarm = {
  id: number
  source: string
  message: string
  severity: AlarmSeverity
  createdAt: string
}

type IncidentFormState = {
  title: string
  description: string
  status: IncidentStatus
  priority: IncidentPriority
  source: string
}

type AlarmFormState = {
  source: string
  message: string
  severity: AlarmSeverity
}

const incidentStatusOptions: IncidentStatus[] = [
  'OPEN',
  'IN_PROGRESS',
  'RESOLVED',
  'CLOSED',
]

const incidentPriorityOptions: IncidentPriority[] = [
  'LOW',
  'MEDIUM',
  'HIGH',
  'CRITICAL',
]

const alarmSeverityOptions: AlarmSeverity[] = ['MINOR', 'MAJOR', 'CRITICAL']

const initialIncidentForm: IncidentFormState = {
  title: '',
  description: '',
  status: 'OPEN',
  priority: 'LOW',
  source: '',
}

const initialAlarmForm: AlarmFormState = {
  source: '',
  message: '',
  severity: 'MINOR',
}

const statusLabels: Record<IncidentStatus, string> = {
  OPEN: 'Open',
  IN_PROGRESS: 'In Progress',
  RESOLVED: 'Resolved',
  CLOSED: 'Closed',
}

const priorityLabels: Record<IncidentPriority, string> = {
  LOW: 'Low',
  MEDIUM: 'Medium',
  HIGH: 'High',
  CRITICAL: 'Critical',
}

const severityLabels: Record<AlarmSeverity, string> = {
  MINOR: 'Minor',
  MAJOR: 'Major',
  CRITICAL: 'Critical',
}

async function apiRequest<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers ?? {}),
    },
  })

  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || 'Request failed.')
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}

function toErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message
  }

  return 'Unexpected error.'
}

function formatDateTime(value: string): string {
  return new Date(value).toLocaleString('de-DE')
}

function App() {
  const [incidents, setIncidents] = useState<Incident[]>([])
  const [alarms, setAlarms] = useState<Alarm[]>([])
  const [incidentForm, setIncidentForm] = useState(initialIncidentForm)
  const [alarmForm, setAlarmForm] = useState(initialAlarmForm)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    void loadData()
  }, [])

  async function loadData(showLoadingState = true) {
    if (showLoadingState) {
      setIsLoading(true)
    }

    try {
      const [incidentResponse, alarmResponse] = await Promise.all([
        apiRequest<Incident[]>('/api/incidents'),
        apiRequest<Alarm[]>('/api/alarms'),
      ])

      setIncidents(incidentResponse)
      setAlarms(alarmResponse)
      setErrorMessage('')
    } catch (error) {
      setErrorMessage(toErrorMessage(error))
    } finally {
      if (showLoadingState) {
        setIsLoading(false)
      }
    }
  }

  async function handleIncidentSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsSubmitting(true)

    try {
      await apiRequest<Incident>('/api/incidents', {
        method: 'POST',
        body: JSON.stringify(incidentForm),
      })

      setIncidentForm(initialIncidentForm)
      await loadData(false)
      setErrorMessage('')
    } catch (error) {
      setErrorMessage(toErrorMessage(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleAlarmSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsSubmitting(true)

    try {
      await apiRequest<Alarm>('/api/alarms', {
        method: 'POST',
        body: JSON.stringify(alarmForm),
      })

      setAlarmForm(initialAlarmForm)
      await loadData(false)
      setErrorMessage('')
    } catch (error) {
      setErrorMessage(toErrorMessage(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function updateIncidentStatus(id: number, status: IncidentStatus) {
    const previousIncidents = incidents

    setIncidents((items) =>
      items.map((incident) =>
        incident.id === id ? { ...incident, status } : incident,
      ),
    )

    try {
      const updatedIncident = await apiRequest<Incident>(
        `/api/incidents/${id}/status`,
        {
          method: 'PATCH',
          body: JSON.stringify({ status }),
        },
      )

      setIncidents((items) =>
        items.map((incident) =>
          incident.id === id ? updatedIncident : incident,
        ),
      )
      setErrorMessage('')
    } catch (error) {
      setIncidents(previousIncidents)
      setErrorMessage(toErrorMessage(error))
    }
  }

  async function updateIncidentPriority(id: number, priority: IncidentPriority) {
    const previousIncidents = incidents

    setIncidents((items) =>
      items.map((incident) =>
        incident.id === id ? { ...incident, priority } : incident,
      ),
    )

    try {
      const updatedIncident = await apiRequest<Incident>(
        `/api/incidents/${id}/priority`,
        {
          method: 'PATCH',
          body: JSON.stringify({ priority }),
        },
      )

      setIncidents((items) =>
        items.map((incident) =>
          incident.id === id ? updatedIncident : incident,
        ),
      )
      setErrorMessage('')
    } catch (error) {
      setIncidents(previousIncidents)
      setErrorMessage(toErrorMessage(error))
    }
  }

  async function deleteIncident(id: number) {
    try {
      await apiRequest<void>(`/api/incidents/${id}`, { method: 'DELETE' })
      await loadData(false)
      setErrorMessage('')
    } catch (error) {
      setErrorMessage(toErrorMessage(error))
    }
  }

  async function deleteAlarm(id: number) {
    try {
      await apiRequest<void>(`/api/alarms/${id}`, { method: 'DELETE' })
      setAlarms((items) => items.filter((alarm) => alarm.id !== id))
      setErrorMessage('')
    } catch (error) {
      setErrorMessage(toErrorMessage(error))
    }
  }

  return (
    <main className="app-shell">
      <header className="page-header">
        <div>
          <h1>Incident Management MVP</h1>
          <p>
            Einfache Oberflaeche fuer manuelle Incidents und eingehende Alarme.
          </p>
        </div>
        <button type="button" onClick={() => void loadData()} disabled={isLoading}>
          Aktualisieren
        </button>
      </header>

      {errorMessage ? <div className="message error">{errorMessage}</div> : null}
      {isLoading ? <div className="message">Daten werden geladen...</div> : null}

      <section className="form-grid">
        <form className="panel form-panel" onSubmit={handleIncidentSubmit}>
          <h2>Manuellen Incident erstellen</h2>
          <label>
            Titel
            <input
              value={incidentForm.title}
              onChange={(event) =>
                setIncidentForm((current) => ({
                  ...current,
                  title: event.target.value,
                }))
              }
              required
            />
          </label>
          <label>
            Beschreibung
            <textarea
              value={incidentForm.description}
              onChange={(event) =>
                setIncidentForm((current) => ({
                  ...current,
                  description: event.target.value,
                }))
              }
              rows={4}
              required
            />
          </label>
          <label>
            Source
            <input
              value={incidentForm.source}
              onChange={(event) =>
                setIncidentForm((current) => ({
                  ...current,
                  source: event.target.value,
                }))
              }
              required
            />
          </label>
          <div className="form-row">
            <label>
              Status
              <select
                value={incidentForm.status}
                onChange={(event) =>
                  setIncidentForm((current) => ({
                    ...current,
                    status: event.target.value as IncidentStatus,
                  }))
                }
              >
                {incidentStatusOptions.map((status) => (
                  <option key={status} value={status}>
                    {statusLabels[status]}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Prioritaet
              <select
                value={incidentForm.priority}
                onChange={(event) =>
                  setIncidentForm((current) => ({
                    ...current,
                    priority: event.target.value as IncidentPriority,
                  }))
                }
              >
                {incidentPriorityOptions.map((priority) => (
                  <option key={priority} value={priority}>
                    {priorityLabels[priority]}
                  </option>
                ))}
              </select>
            </label>
          </div>
          <button type="submit" disabled={isSubmitting}>
            Incident anlegen
          </button>
        </form>

        <form className="panel form-panel" onSubmit={handleAlarmSubmit}>
          <h2>Alarm senden</h2>
          <label>
            Source
            <input
              value={alarmForm.source}
              onChange={(event) =>
                setAlarmForm((current) => ({
                  ...current,
                  source: event.target.value,
                }))
              }
              required
            />
          </label>
          <label>
            Nachricht
            <textarea
              value={alarmForm.message}
              onChange={(event) =>
                setAlarmForm((current) => ({
                  ...current,
                  message: event.target.value,
                }))
              }
              rows={4}
              required
            />
          </label>
          <label>
            Severity
            <select
              value={alarmForm.severity}
              onChange={(event) =>
                setAlarmForm((current) => ({
                  ...current,
                  severity: event.target.value as AlarmSeverity,
                }))
              }
            >
              {alarmSeverityOptions.map((severity) => (
                <option key={severity} value={severity}>
                  {severityLabels[severity]}
                </option>
              ))}
            </select>
          </label>
          <button type="submit" disabled={isSubmitting}>
            Alarm senden
          </button>
        </form>
      </section>

      <section className="panel">
        <div className="section-header">
          <h2>Incidents</h2>
          <span>{incidents.length} Eintraege</span>
        </div>
        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Titel</th>
                <th>Beschreibung</th>
                <th>Source</th>
                <th>Status</th>
                <th>Prioritaet</th>
                <th>Aktionen</th>
              </tr>
            </thead>
            <tbody>
              {incidents.length === 0 ? (
                <tr>
                  <td colSpan={7} className="empty-state">
                    Keine Incidents vorhanden.
                  </td>
                </tr>
              ) : (
                incidents.map((incident) => (
                  <tr key={incident.id}>
                    <td>{incident.id}</td>
                    <td>{incident.title}</td>
                    <td>{incident.description}</td>
                    <td>{incident.source}</td>
                    <td>
                      <select
                        value={incident.status}
                        onChange={(event) =>
                          void updateIncidentStatus(
                            incident.id,
                            event.target.value as IncidentStatus,
                          )
                        }
                      >
                        {incidentStatusOptions.map((status) => (
                          <option key={status} value={status}>
                            {statusLabels[status]}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <select
                        value={incident.priority}
                        onChange={(event) =>
                          void updateIncidentPriority(
                            incident.id,
                            event.target.value as IncidentPriority,
                          )
                        }
                      >
                        {incidentPriorityOptions.map((priority) => (
                          <option key={priority} value={priority}>
                            {priorityLabels[priority]}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <button
                        type="button"
                        className="danger-button"
                        onClick={() => void deleteIncident(incident.id)}
                      >
                        Loeschen
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>

      <section className="panel">
        <div className="section-header">
          <h2>Alarme</h2>
          <span>{alarms.length} Eintraege</span>
        </div>
        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Source</th>
                <th>Nachricht</th>
                <th>Severity</th>
                <th>Erstellt am</th>
                <th>Aktionen</th>
              </tr>
            </thead>
            <tbody>
              {alarms.length === 0 ? (
                <tr>
                  <td colSpan={6} className="empty-state">
                    Keine Alarme vorhanden.
                  </td>
                </tr>
              ) : (
                alarms.map((alarm) => (
                  <tr key={alarm.id}>
                    <td>{alarm.id}</td>
                    <td>{alarm.source}</td>
                    <td>{alarm.message}</td>
                    <td>{severityLabels[alarm.severity]}</td>
                    <td>{formatDateTime(alarm.createdAt)}</td>
                    <td>
                      <button
                        type="button"
                        className="danger-button"
                        onClick={() => void deleteAlarm(alarm.id)}
                      >
                        Loeschen
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>
    </main>
  )
}

export default App
