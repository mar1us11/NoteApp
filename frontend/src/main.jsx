import { useEffect, useMemo, useState } from 'react'
import { createRoot } from 'react-dom/client'
import './styles.css'

const savedSession = sessionStorage.getItem('noteapp-session')

function readError(response, body) {
  return body?.message || body?.error || `Request failed (${response.status})`
}

function App() {
  const [session, setSession] = useState(() => savedSession ? JSON.parse(savedSession) : null)
  const [mode, setMode] = useState('signin')
  const [credentials, setCredentials] = useState({ username: '', password: '' })
  const [notes, setNotes] = useState([])
  const [selectedTitle, setSelectedTitle] = useState(null)
  const [editor, setEditor] = useState({ title: '', content: '' })
  const [account, setAccount] = useState({ username: '', password: '' })
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const selectedNote = useMemo(
    () => notes.find((note) => note.title === selectedTitle),
    [notes, selectedTitle]
  )

  useEffect(() => {
    if (!session) return
    sessionStorage.setItem('noteapp-session', JSON.stringify(session))
    setAccount({ username: session.username, password: '' })
    loadNotes(session)
  }, [session])

  async function api(path, options = {}, activeSession = session) {
    const headers = new Headers(options.headers)
    headers.set('Authorization', `Basic ${btoa(`${activeSession.username}:${activeSession.password}`)}`)
    if (options.body) headers.set('Content-Type', 'application/json')
    const response = await fetch(`/api${path}`, { ...options, headers })
    const text = await response.text()
    let body = null
    try { body = text ? JSON.parse(text) : null } catch { body = text }
    if (!response.ok) throw new Error(readError(response, body))
    return body
  }

  async function loadNotes(activeSession = session) {
    try {
      setError('')
      const list = await api('/notes', {}, activeSession)
      setNotes(list)
    } catch (err) {
      setError(err.message)
      if (err.message.includes('(401)')) signOut()
    }
  }

  async function submitAuth(event) {
    event.preventDefault()
    setBusy(true); setError(''); setMessage('')
    try {
      if (mode === 'register') {
        const response = await fetch('/api/auth/register', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(credentials)
        })
        const body = await response.json().catch(() => null)
        if (!response.ok) throw new Error(readError(response, body))
      }
      setSession(credentials)
      setMessage(mode === 'register' ? 'Account created. Welcome!' : 'Signed in.')
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  function chooseNote(note) {
    setSelectedTitle(note.title)
    setEditor({ title: note.title, content: note.content })
    setMessage('')
  }

  function newNote() {
    setSelectedTitle(null)
    setEditor({ title: '', content: '' })
    setMessage('')
  }

  async function saveNote(event) {
    event.preventDefault()
    setBusy(true); setError(''); setMessage('')
    try {
      const path = selectedTitle ? `/notes/${encodeURIComponent(selectedTitle)}` : '/notes'
      const note = await api(path, {
        method: selectedTitle ? 'PUT' : 'POST',
        body: JSON.stringify(editor)
      })
      setNotes((current) => {
        const remaining = current.filter((item) => item.title !== selectedTitle)
        return [...remaining, note].sort((a, b) => a.title.localeCompare(b.title))
      })
      setSelectedTitle(note.title)
      setEditor({ title: note.title, content: note.content })
      setMessage('Note saved.')
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  async function removeNote() {
    if (!selectedTitle || !window.confirm(`Delete “${selectedTitle}”?`)) return
    setBusy(true); setError(''); setMessage('')
    try {
      await api(`/notes/${encodeURIComponent(selectedTitle)}`, { method: 'DELETE' })
      setNotes((current) => current.filter((item) => item.title !== selectedTitle))
      newNote()
      setMessage('Note deleted.')
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  async function saveAccount(event) {
    event.preventDefault()
    const payload = { username: account.username, password: account.password || session.password }
    setBusy(true); setError(''); setMessage('')
    try {
      const user = await api('/users/me', { method: 'PUT', body: JSON.stringify(payload) })
      setSession({ username: user.username, password: payload.password })
      setMessage('Account updated.')
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  async function deleteAccount() {
    if (!window.confirm('Delete your account and all of its notes?')) return
    setBusy(true); setError('')
    try {
      await api('/users/me', { method: 'DELETE' })
      signOut()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  function signOut() {
    sessionStorage.removeItem('noteapp-session')
    setSession(null); setNotes([]); setSelectedTitle(null); setEditor({ title: '', content: '' })
    setCredentials({ username: '', password: '' }); setMessage('')
  }

  if (!session) {
    return <main className="auth-shell">
      <section className="auth-card">
        <p className="eyebrow">YOUR NOTES, SIMPLIFIED</p>
        <h1>A calm place for your ideas.</h1>
        <p className="subtle">Sign in to pick up where you left off, or create an account to get started.</p>
        <div className="tabs"><button className={mode === 'signin' ? 'active' : ''} onClick={() => setMode('signin')}>Sign in</button><button className={mode === 'register' ? 'active' : ''} onClick={() => setMode('register')}>Create account</button></div>
        <form onSubmit={submitAuth} className="stack">
          <label>Username<input minLength="3" maxLength="50" required value={credentials.username} onChange={(e) => setCredentials({ ...credentials, username: e.target.value })} /></label>
          <label>Password<input type="password" minLength="8" maxLength="100" required value={credentials.password} onChange={(e) => setCredentials({ ...credentials, password: e.target.value })} /></label>
          {error && <p className="notice error">{error}</p>}
          <button className="primary" disabled={busy}>{busy ? 'Please wait…' : mode === 'signin' ? 'Sign in' : 'Create account'}</button>
        </form>
      </section>
    </main>
  }

  return <main className="app-shell">
    <aside className="sidebar">
      <div><p className="eyebrow">NOTES</p><h1>Hi, {session.username}</h1></div>
      <button className="primary" onClick={newNote}>+ New note</button>
      <nav className="note-list">
        {notes.length === 0 && <p className="subtle">No notes yet. Create your first one.</p>}
        {notes.map((note) => <button key={note.id} className={selectedTitle === note.title ? 'note-item selected' : 'note-item'} onClick={() => chooseNote(note)}><strong>{note.title}</strong><span>{note.content || 'Empty note'}</span></button>)}
      </nav>
      <details className="account"><summary>Account settings</summary><form onSubmit={saveAccount} className="stack compact"><label>Username<input minLength="3" maxLength="50" required value={account.username} onChange={(e) => setAccount({ ...account, username: e.target.value })} /></label><label>New password <small>(leave blank to keep it)</small><input type="password" minLength="8" maxLength="100" value={account.password} onChange={(e) => setAccount({ ...account, password: e.target.value })} /></label><button disabled={busy}>Save account</button></form><button className="danger link" onClick={deleteAccount} disabled={busy}>Delete account</button></details>
      <button className="link" onClick={signOut}>Sign out</button>
    </aside>
    <section className="workspace">
      <div className="status">{error && <p className="notice error">{error}</p>}{message && <p className="notice success">{message}</p>}</div>
      <form className="editor" onSubmit={saveNote}>
        <input className="title-input" placeholder="Untitled note" minLength="1" required value={editor.title} onChange={(e) => setEditor({ ...editor, title: e.target.value })} />
        <textarea placeholder="Write something…" minLength="1" required value={editor.content} onChange={(e) => setEditor({ ...editor, content: e.target.value })} />
        <div className="editor-actions"><span>{selectedNote ? 'Editing saved note' : 'New note'}</span><div>{selectedTitle && <button type="button" className="danger" onClick={removeNote} disabled={busy}>Delete</button>}<button className="primary" disabled={busy}>{busy ? 'Saving…' : 'Save note'}</button></div></div>
      </form>
    </section>
  </main>
}

createRoot(document.getElementById('root')).render(<App />)
