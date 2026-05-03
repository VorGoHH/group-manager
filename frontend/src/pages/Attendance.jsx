import { useState, useEffect, useCallback } from 'react'

const REASON_LABELS = {
  SICK: 'Хворий', EXCUSED: 'Звільнення', BUSINESS_TRIP: 'Відрядження',
  INDIVIDUAL: 'І/з', ILLEGAL: 'Н/з', ON_DUTY: 'Наряд'
}

const REASON_COLORS = {
  SICK: 'badge-red', EXCUSED: 'badge-green', BUSINESS_TRIP: 'badge-blue',
  INDIVIDUAL: 'badge-purple', ILLEGAL: 'badge-red', ON_DUTY: 'badge-orange'
}

const today = () => new Date().toISOString().split('T')[0]

export default function Attendance() {
  const [currentDate, setCurrentDate] = useState(today())
  const [data, setData] = useState({ total: 0, presentCount: 0, absences: [], present: [] })
  const [modal, setModal] = useState(null)
  const [reason, setReason] = useState('SICK')
  const [note, setNote] = useState('')
  const [history, setHistory] = useState(null)
  const [sending, setSending] = useState(false)
  const [toast, setToast] = useState(null)

  const showToast = (msg, ok = true) => {
    setToast({ msg, ok })
    setTimeout(() => setToast(null), 3000)
  }

  const load = useCallback(async () => {
    const res = await fetch(`/api/attendance?date=${currentDate}`)
    const d = await res.json()
    setData(d)
  }, [currentDate])

  useEffect(() => { load() }, [load])

  const openModal = (id, name) => {
    setModal({ id, name })
    setReason('SICK')
    setNote('')
  }

  const confirmAbsent = async () => {
    await fetch('/api/attendance/absent', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ soldierId: modal.id, date: currentDate, reason, note })
    })
    setModal(null)
    load()
  }

  const markPresent = async (soldierId) => {
    await fetch(`/api/attendance/absent/${soldierId}?date=${currentDate}`, { method: 'DELETE' })
    load()
  }

  const openHistory = async () => {
    const res = await fetch(`/api/attendance/${modal.id}/history`)
    const items = await res.json()
    setHistory({ name: modal.name, items })
  }

  const sendReport = async () => {
    setSending(true)
    try {
      const res = await fetch(`/api/attendance/report/send?date=${currentDate}`, { method: 'POST' })
      showToast(res.ok ? '✅ Надіслано в Telegram' : '❌ Помилка відправки', res.ok)
    } catch {
      showToast('❌ Помилка відправки', false)
    }
    setSending(false)
  }

  const formatDate = (d) => new Date(d + 'T00:00:00').toLocaleDateString('uk-UA', {
    day: 'numeric', month: 'long', year: 'numeric'
  })

  return (
    <main className="main">
      <div className="toolbar">
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

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-num stat-blue">{data.total}</div>
          <div className="stat-label">За списком</div>
        </div>
        <div className="stat-card">
          <div className="stat-num stat-green">{data.presentCount}</div>
          <div className="stat-label">В наявності</div>
        </div>
        <div className="stat-card">
          <div className="stat-num stat-red">{data.absences?.length ?? 0}</div>
          <div className="stat-label">Відсутні</div>
        </div>
      </div>

      <section className="card">
        <div className="card-title">Особовий склад</div>
        <div className="soldiers-grid">
          {data.present?.map(s => (
            <button key={s.id} className="soldier-btn" onClick={() => openModal(s.id, s.lastName + ' ' + s.firstName)}>
              <div className="soldier-name">{s.lastName} {s.firstName}</div>
              <div className="soldier-meta">{s.rank}</div>
              <div className="soldier-platoon">{s.platoon} відд.</div>
            </button>
          ))}
        </div>
      </section>

      <section className="card">
        <div className="card-title">Відсутні</div>
        {data.absences?.length === 0
          ? <div className="empty">Всі присутні 👍</div>
          : data.absences?.map(a => (
            <div key={a.id} className="absence-row">
              <div className="absence-info">
                <span className="absence-name">{a.soldier.lastName} {a.soldier.firstName}</span>
                <span className={`badge ${REASON_COLORS[a.reason]}`}>{REASON_LABELS[a.reason]}</span>
                {a.note && <span className="absence-note">{a.note}</span>}
              </div>
              <button className="btn-return" onClick={() => markPresent(a.soldier.id)}>↩</button>
            </div>
          ))
        }
      </section>

      {modal && (
        <div className="overlay" onClick={e => e.target === e.currentTarget && setModal(null)}>
          <div className="modal">
            <div className="modal-header">
              <h3>{modal.name}</h3>
              <button className="modal-close" onClick={() => setModal(null)}>✕</button>
            </div>
            <select className="modal-select" value={reason} onChange={e => setReason(e.target.value)}>
              <option value="SICK">Хворий</option>
              <option value="EXCUSED">Звільнення</option>
              <option value="BUSINESS_TRIP">Відрядження</option>
              <option value="INDIVIDUAL">Індивідуальні заняття</option>
              <option value="ILLEGAL">Незаконно відсутній</option>
              <option value="ON_DUTY">Наряд</option>
            </select>
            <input
              className="modal-input"
              placeholder="Примітка (необов'язково)"
              value={note}
              onChange={e => setNote(e.target.value)}
            />
            <div className="modal-footer">
              <button className="btn btn-ghost" onClick={openHistory}>📋 Історія</button>
              <div style={{ display: 'flex', gap: 8 }}>
                <button className="btn btn-cancel" onClick={() => setModal(null)}>Скасувати</button>
                <button className="btn btn-primary" onClick={confirmAbsent}>Підтвердити</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {history && (
        <div className="overlay" onClick={e => e.target === e.currentTarget && setHistory(null)}>
          <div className="modal modal-wide">
            <div className="modal-header">
              <h3>Історія — {history.name}</h3>
              <button className="modal-close" onClick={() => setHistory(null)}>✕</button>
            </div>
            <div className="history-list">
              {history.items.length === 0
                ? <div className="empty">Відсутностей не зафіксовано</div>
                : history.items.map((a, i) => (
                  <div key={i} className="history-row">
                    <span className="history-date">{formatDate(a.absenceDate)}</span>
                    <div>
                      <span className={`badge ${REASON_COLORS[a.reason]}`}>{REASON_LABELS[a.reason]}</span>
                      {a.note && <div className="history-note">{a.note}</div>}
                    </div>
                  </div>
                ))
              }
            </div>
          </div>
        </div>
      )}

      {toast && <div className={`toast ${toast.ok ? 'toast-ok' : 'toast-err'}`}>{toast.msg}</div>}
    </main>
  )
}