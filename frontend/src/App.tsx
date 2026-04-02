import { FormEvent, useEffect, useState } from 'react'
import provadisLogo from './assets/provadis_logo.png'
import './App.css'

type IncidentStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED'
type IncidentPriority = 'LOW' | 'MEDIUM' | 'HIGH'
type AlarmSeverity = 'CRITICAL' | 'MAJOR' | 'MINOR' | 'INFO'

type Incident = {
  id: number
  title: string
  description: string
  status: IncidentStatus
  priority: IncidentPriority
  source: string | null
}

type Alarm = {
  id: number
  source: string
  message: string
  severity: AlarmSeverity
  createdAt: string
}

type Feedback = {
  tone: 'success' | 'error'
  text: string
}

const incidentStatusOptions: IncidentStatus[] = [
  'OPEN',
  'IN_PROGRESS',
  'RESOLVED',
  'CLOSED',
]
const incidentPriorityOptions: IncidentPriority[] = ['LOW', 'MEDIUM', 'HIGH']
const alarmSeverityOptions: AlarmSeverity[] = ['CRITICAL', 'MAJOR', 'MINOR', 'INFO']

const defaultAlarmForm = {
  source: '',
  message: '',
  severity: 'MAJOR' as AlarmSeverity,
}

const defaultIncidentForm = {
  title: '',
  description: '',
  status: 'OPEN' as IncidentStatus,
  priority: 'MEDIUM' as IncidentPriority,
}

async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers ?? {}),
    },
    ...init,
  })

  if (!response.ok) {
    const text = await response.text()
    throw new Error(text || 'Die Anfrage konnte nicht verarbeitet werden.')
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('de-DE', {
    dateStyle: 'short',
    timeStyle: 'medium',
  }).format(new Date(value))
}

function App() {
  const [alarms, setAlarms] = useState<Alarm[]>([])
  const [incidents, setIncidents] = useState<Incident[]>([])
  const [alarmForm, setAlarmForm] = useState(defaultAlarmForm)
  const [incidentForm, setIncidentForm] = useState(defaultIncidentForm)
  const [selectedSource, setSelectedSource] = useState<string | null>(null)
  const [sourceMessages, setSourceMessages] = useState<Alarm[]>([])
  const [feedback, setFeedback] = useState<Feedback | null>(null)
  const [isBusy, setIsBusy] = useState(false)
  const [isMessageLoading, setIsMessageLoading] = useState(false)

  useEffect(() => {
    void loadDashboard()
  }, [])

  async function loadDashboard() {
    try {
      const [incidentData, alarmData] = await Promise.all([
        apiFetch<Incident[]>('/api/incidents'),
        apiFetch<Alarm[]>('/api/alarms'),
      ])
      setIncidents(incidentData)
      setAlarms(alarmData)
    } catch (error) {
      showError(error)
    }
  }

  async function loadMessages(source: string) {
    setIsMessageLoading(true)
    try {
      const params = new URLSearchParams({ source })
      const messageData = await apiFetch<Alarm[]>(`/api/alarms?${params.toString()}`)
      setSourceMessages(messageData)
    } catch (error) {
      showError(error)
    } finally {
      setIsMessageLoading(false)
    }
  }

  function showSuccess(text: string) {
    setFeedback({ tone: 'success', text })
  }

  function showError(error: unknown) {
    const text = error instanceof Error ? error.message : 'Ein unerwarteter Fehler ist aufgetreten.'
    setFeedback({ tone: 'error', text })
  }

  async function handleAlarmSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsBusy(true)
    try {
      await apiFetch<Alarm>('/api/alarms', {
        method: 'POST',
        body: JSON.stringify(alarmForm),
      })
      setAlarmForm(defaultAlarmForm)
      await loadDashboard()
      if (selectedSource === alarmForm.source) {
        await loadMessages(alarmForm.source)
      }
      showSuccess('Alarm gespeichert und Incident aktualisiert.')
    } catch (error) {
      showError(error)
    } finally {
      setIsBusy(false)
    }
  }

  async function handleIncidentSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsBusy(true)
    try {
      await apiFetch<Incident>('/api/incidents', {
        method: 'POST',
        body: JSON.stringify(incidentForm),
      })
      setIncidentForm(defaultIncidentForm)
      await loadDashboard()
      showSuccess('Incident angelegt.')
    } catch (error) {
      showError(error)
    } finally {
      setIsBusy(false)
    }
  }

  async function updateIncidentStatus(id: number, status: IncidentStatus) {
    try {
      await apiFetch<Incident>(`/api/incidents/${id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status }),
      })
      await loadDashboard()
      showSuccess('Incident-Status aktualisiert.')
    } catch (error) {
      showError(error)
    }
  }

  async function updateIncidentPriority(id: number, priority: IncidentPriority) {
    try {
      await apiFetch<Incident>(`/api/incidents/${id}/priority`, {
        method: 'PATCH',
        body: JSON.stringify({ priority }),
      })
      await loadDashboard()
      showSuccess('Incident-Priorität aktualisiert.')
    } catch (error) {
      showError(error)
    }
  }

  async function deleteIncident(id: number) {
    if (!window.confirm('Incident wirklich löschen?')) {
      return
    }

    try {
      await apiFetch<void>(`/api/incidents/${id}`, { method: 'DELETE' })
      await loadDashboard()
      showSuccess('Incident gelöscht.')
    } catch (error) {
      showError(error)
    }
  }

  async function deleteAlarm(id: number) {
    if (!window.confirm('Alarm wirklich löschen?')) {
      return
    }

    try {
      await apiFetch<void>(`/api/alarms/${id}`, { method: 'DELETE' })
      await loadDashboard()
      if (selectedSource) {
        await loadMessages(selectedSource)
      }
      showSuccess('Alarm gelöscht.')
    } catch (error) {
      showError(error)
    }
  }

  async function deleteAllAlarms() {
    if (!window.confirm('Alle Alarme wirklich löschen?')) {
      return
    }

    try {
      await apiFetch<void>('/api/alarms', { method: 'DELETE' })
      await loadDashboard()
      if (selectedSource) {
        setSourceMessages([])
      }
      showSuccess('Alle Alarme gelöscht.')
    } catch (error) {
      showError(error)
    }
  }

  async function openMessages(source: string) {
    setSelectedSource(source)
    await loadMessages(source)
  }

  function closeMessages() {
    setSelectedSource(null)
    setSourceMessages([])
  }

  return (
    <div className="app-shell">
      <header className="page-header">
        <div>
          <p className="eyebrow">Bachelorarbeit MVP</p>
          <h1>Incident Management</h1>
          <p className="intro">
            Netzwerkalarme erfassen, Incidents automatisch erzeugen und die
            Nachrichtenhistorie pro Quelle nachvollziehen.
          </p>
        </div>
        <img className="brand-logo" src={provadisLogo} alt="Provadis logo" />
      </header>

      {feedback ? (
        <div className={`feedback feedback-${feedback.tone}`}>{feedback.text}</div>
      ) : null}

      <section className="form-grid">
        <form className="panel" onSubmit={handleAlarmSubmit}>
          <div className="panel-header">
            <h2>Alarm senden</h2>
            <p>Neue Netzwerkalarme werden gespeichert und verknüpfen sich automatisch mit offenen Incidents.</p>
          </div>

          <label>
            Quelle
            <input
              value={alarmForm.source}
              onChange={(event) =>
                setAlarmForm((current) => ({ ...current, source: event.target.value }))
              }
              placeholder="z. B. firewall-01"
              required
            />
          </label>

          <label>
            Nachricht
            <textarea
              value={alarmForm.message}
              onChange={(event) =>
                setAlarmForm((current) => ({ ...current, message: event.target.value }))
              }
              placeholder="Beschreibe den Alarm kurz"
              rows={4}
              required
            />
          </label>

          <label>
            Schweregrad
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
                  {severity}
                </option>
              ))}
            </select>
          </label>

          <button type="submit" disabled={isBusy}>
            Alarm senden
          </button>
        </form>

        <form className="panel" onSubmit={handleIncidentSubmit}>
          <div className="panel-header">
            <h2>Incident anlegen</h2>
            <p>Manuelle Incidents bleiben bewusst einfach und werden ohne zusätzliche Felder angelegt.</p>
          </div>

          <label>
            Titel
            <input
              value={incidentForm.title}
              onChange={(event) =>
                setIncidentForm((current) => ({ ...current, title: event.target.value }))
              }
              placeholder="Kurzer Incident-Titel"
              required
            />
          </label>

          <label>
            Beschreibung
            <textarea
              value={incidentForm.description}
              onChange={(event) =>
                setIncidentForm((current) => ({ ...current, description: event.target.value }))
              }
              placeholder="Beschreibung des Vorfalls"
              rows={4}
              required
            />
          </label>

          <div className="field-row">
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
                    {status}
                  </option>
                ))}
              </select>
            </label>

            <label>
              Priorität
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
                    {priority}
                  </option>
                ))}
              </select>
            </label>
          </div>

          <button type="submit" disabled={isBusy}>
            Anlegen
          </button>
        </form>
      </section>

      <section className="panel table-panel">
        <div className="table-headline">
          <div>
            <h2>Alarme</h2>
            <p>Alle eingegangenen Alarmmeldungen mit Filterbasis für die Nachrichtenhistorie.</p>
          </div>
          <button type="button" className="button-secondary" onClick={deleteAllAlarms}>
            Alle Alarme löschen
          </button>
        </div>

        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Alarm-ID</th>
                <th>Quelle</th>
                <th>Schweregrad</th>
                <th>Nachricht</th>
                <th>Erfassungszeit</th>
                <th>Aktion</th>
              </tr>
            </thead>
            <tbody>
              {alarms.length === 0 ? (
                <tr>
                  <td colSpan={6} className="empty-row">
                    Noch keine Alarme vorhanden.
                  </td>
                </tr>
              ) : (
                alarms.map((alarm) => (
                  <tr key={alarm.id}>
                    <td>{alarm.id}</td>
                    <td>{alarm.source}</td>
                    <td>
                      <span className={`badge badge-${alarm.severity.toLowerCase()}`}>
                        {alarm.severity}
                      </span>
                    </td>
                    <td>{alarm.message}</td>
                    <td>{formatDate(alarm.createdAt)}</td>
                    <td>
                      <button type="button" className="button-danger" onClick={() => deleteAlarm(alarm.id)}>
                        Löschen
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>

      <section className="panel table-panel">
        <div className="table-headline">
          <div>
            <h2>Incidents</h2>
            <p>Status und Priorität lassen sich direkt in der Tabelle anpassen.</p>
          </div>
        </div>

        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Incident-ID</th>
                <th>Quelle</th>
                <th>Status</th>
                <th>Priorität</th>
                <th>Beschreibung</th>
                <th>Nachrichten</th>
                <th>Aktion</th>
              </tr>
            </thead>
            <tbody>
              {incidents.length === 0 ? (
                <tr>
                  <td colSpan={7} className="empty-row">
                    Noch keine Incidents vorhanden.
                  </td>
                </tr>
              ) : (
                incidents.map((incident) => (
                  <tr key={incident.id}>
                    <td>{incident.id}</td>
                    <td>{incident.source || incident.title}</td>
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
                            {status}
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
                            {priority}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td>{incident.description}</td>
                    <td>
                      {incident.source ? (
                        <button type="button" className="button-secondary" onClick={() => void openMessages(incident.source!)}>
                          Nachrichten
                        </button>
                      ) : (
                        <span className="muted">-</span>
                      )}
                    </td>
                    <td>
                      <button
                        type="button"
                        className="button-danger"
                        onClick={() => void deleteIncident(incident.id)}
                      >
                        Löschen
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>

      {selectedSource ? (
        <div className="modal-backdrop" onClick={closeMessages}>
          <div className="modal-card" onClick={(event) => event.stopPropagation()}>
            <div className="modal-header">
              <div>
                <p className="eyebrow">Nachrichtenhistorie</p>
                <h2>{selectedSource}</h2>
              </div>
              <button type="button" className="button-secondary" onClick={closeMessages}>
                Schließen
              </button>
            </div>

            {isMessageLoading ? (
              <p className="modal-state">Lade Alarmnachrichten ...</p>
            ) : sourceMessages.length === 0 ? (
              <p className="modal-state">Keine Alarmnachrichten für diese Quelle gefunden.</p>
            ) : (
              <div className="message-list">
                {sourceMessages.map((alarm) => (
                  <article key={alarm.id} className="message-card">
                    <div className="message-card-head">
                      <span className={`badge badge-${alarm.severity.toLowerCase()}`}>
                        {alarm.severity}
                      </span>
                      <span>{formatDate(alarm.createdAt)}</span>
                    </div>
                    <p>{alarm.message}</p>
                  </article>
                ))}
              </div>
            )}
          </div>
        </div>
      ) : null}
    </div>
  )
}

export default App
