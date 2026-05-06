import { useState, useEffect } from 'react'

const today = () => new Date().toISOString().split('T')[0]

export default function Work() {
  const [currentDate, setCurrentDate] = useState(today())
  const [assignments, setAssignments] = useState([])
  const [workName, setWorkName] = useState('')
  const [count, setCount] = useState(1)
  const [toast, setToast] = useState(null)
  const [replaceModal, setReplaceModal] = useState(null)
  const [candidates, setCandidates] = useState([])
  const [sending, setSending] = useState(false)

  const sendReport = async () => {
    setSending(true)
    try {
      const res = await fetch(`/api/work/report/send?date=${currentDate}`, { method: 'POST' })
      showToast(res.ok ? '✅ Надіслано в Telegram' : '❌ Помилка', res.ok)
    } catch {
      showToast('❌ Помилка відправки', false)
    }
    setSending(false)
  }

  const showToast = (msg, ok = true) => {
    setToast({ msg, ok })
    setTimeout(() => setToast(null), 4000)
  }

  const load = async (date) => {
    const res = await fetch(`/api/work?date=${date}`)
    const d = await res.json()
    setAssignments(d)
  }

  useEffect(() => { load(currentDate) }, [currentDate])

  const generate = async () => {
    if (!workName.trim()) { showToast('Введіть назву роботи', false); return }
    const res = await fetch(
      `/api/work/generate?date=${currentDate}&workName=${encodeURIComponent(workName)}&count=${count}`,
      { method: 'POST' }
    )
    if (res.ok) { showToast('Роботу призначено!'); load(currentDate); setWorkName('') }
    else showToast(await res.text(), false)
  }

  const deleteWork = async (workName) => {
    if (!confirm(`Видалити роботу "${workName}"?`)) return
    await fetch(`/api/work?date=${currentDate}&workName=${encodeURIComponent(workName)}`, { method: 'DELETE' })
    showToast('Роботу видалено')
    load(currentDate)
  }

  const openReplaceModal = async (assignment) => {
    const res = await fetch(`/api/work/${assignment.id}/candidates`)
    const c = await res.json()
    setCandidates(c)
    setReplaceModal(assignment)
  }

  const confirmReplace = async (soldierId) => {
    await fetch(`/api/work/${replaceModal.id}/replace/${soldierId}`, { method: 'PUT' })
    setReplaceModal(null)
    load(currentDate)
  }

  // Групуємо по назві роботи
  const grouped = assignments.reduce((acc, a) => {
    if (!acc[a.workName]) acc[a.workName] = []
    acc[a.workName].push(a)
    return acc
  }, {})

 return (
   <main className="main">
     <div className="toolbar" style={{ flexWrap: 'wrap', gap: 8 }}>
       <input
         type="date"
         className="date-input"
         value={currentDate}
         onChange={e => setCurrentDate(e.target.value)}
       />
       <button className="btn btn-tg" onClick={sendReport} disabled={sending}>
         {sending ? '⏳' : '📤'} <span className="btn-label">Telegram</span>
       </button>
     </div>

      <section className="card">
        <div className="card-title">Додати роботу</div>
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
          <input
            className="modal-input"
            style={{ flex: 1, minWidth: 200, marginBottom: 0 }}
            placeholder="Назва роботи"
            value={workName}
            onChange={e => setWorkName(e.target.value)}
          />
          <input
            type="number"
            className="modal-input"
            style={{ width: 80, marginBottom: 0 }}
            min={1}
            max={20}
            value={count}
            onChange={e => setCount(Number(e.target.value))}
          />
          <button className="btn btn-primary" onClick={generate}>+ Призначити</button>
        </div>
      </section>

      {Object.keys(grouped).length === 0
        ? <section className="card"><div className="empty">Робіт не призначено</div></section>
        : Object.entries(grouped).map(([name, items]) => (
          <section key={name} className="card">
            <div className="card-title" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span>{name} ({items.length} ос.)</span>
              <button className="btn btn-danger" style={{ fontSize: 12, padding: '4px 10px' }} onClick={() => deleteWork(name)}>Видалити</button>
            </div>
            {items.map(a => (
              <div key={a.id} className="duty-row">
                <div className="duty-info">
                  <div className="duty-name">
                    {a.soldier.lastName} {a.soldier.firstName} {a.soldier.middleName}
                    {a.isManual && <span className="manual-tag"> (вручну)</span>}
                  </div>
                  <div className="duty-meta">{a.soldier.rank} · {a.soldier.platoon} відділення</div>
                </div>
                <button className="btn-icon" onClick={() => openReplaceModal(a)}>🔄</button>
              </div>
            ))}
          </section>
        ))
      }

      {replaceModal && (
        <div className="overlay" onClick={e => e.target === e.currentTarget && setReplaceModal(null)}>
          <div className="modal">
            <div className="modal-header">
              <h3>Замінити — {replaceModal.workName}</h3>
              <button className="modal-close" onClick={() => setReplaceModal(null)}>✕</button>
            </div>
            <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 14 }}>
              Зараз: <strong style={{ color: 'var(--text)' }}>
                {replaceModal.soldier.lastName} {replaceModal.soldier.firstName}
              </strong>
            </p>
            <div className="candidate-list">
              {candidates.length === 0
                ? <div className="empty">Немає доступних кандидатів</div>
                : candidates.map(s => (
                  <div key={s.id} className="candidate-item" onClick={() => confirmReplace(s.id)}>
                    <div className="candidate-name">{s.lastName} {s.firstName} {s.middleName}</div>
                    <div className="candidate-meta">{s.rank} · {s.platoon} відділення</div>
                  </div>
                ))
              }
            </div>
            <div style={{ marginTop: 14, textAlign: 'right' }}>
              <button className="btn btn-cancel" onClick={() => setReplaceModal(null)}>Скасувати</button>
            </div>
          </div>
        </div>
      )}

      {toast && <div className={`toast ${toast.ok ? 'toast-ok' : 'toast-err'}`}>{toast.msg}</div>}
    </main>
  )
}