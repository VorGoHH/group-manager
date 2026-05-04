import { useState, useEffect } from 'react'

const today = () => new Date().toISOString().split('T')[0]

export default function Cleaning() {
  const [currentDate, setCurrentDate] = useState(today())
  const [cleanings, setCleanings] = useState([])
  const [toast, setToast] = useState(null)
  const [replaceModal, setReplaceModal] = useState(null)
  const [candidates, setCandidates] = useState([])
  const [sending, setSending] = useState(false)

  const showToast = (msg, ok = true) => {
    setToast({ msg, ok })
    setTimeout(() => setToast(null), 4000)
  }

  const load = async (date) => {
    const res = await fetch(`/api/cleaning?date=${date}`)
    const d = await res.json()
    setCleanings(d)
  }

  useEffect(() => { load(currentDate) }, [currentDate])

  const generate = async () => {
    const res = await fetch(`/api/cleaning/generate?date=${currentDate}`, { method: 'POST' })
    if (res.ok) { showToast('Прибирання успішно згенеровано!'); load(currentDate) }
    else showToast(await res.text(), false)
  }

  const deleteCleaning = async () => {
    if (!confirm('Видалити прибирання на цю дату?')) return
    await fetch(`/api/cleaning?date=${currentDate}`, { method: 'DELETE' })
    showToast('Прибирання видалено')
    load(currentDate)
  }

  const openReplaceModal = async (cleaning) => {
    const res = await fetch(`/api/cleaning/${cleaning.id}/candidates`)
    const c = await res.json()
    setCandidates(c)
    setReplaceModal(cleaning)
  }

  const confirmReplace = async (soldierId) => {
    await fetch(`/api/cleaning/${replaceModal.id}/replace/${soldierId}`, { method: 'PUT' })
    setReplaceModal(null)
    load(currentDate)
  }
const sendReport = async () => {
  setSending(true)
  try {
    const res = await fetch(`/api/cleaning/report/send?date=${currentDate}`, { method: 'POST' })
    showToast(res.ok ? '✅ Надіслано в Telegram' : '❌ Помилка відправки', res.ok)
  } catch {
    showToast('❌ Помилка відправки', false)
  }
  setSending(false)
}

  const byPlatoon = [1, 2, 3].map(p => ({
    platoon: p,
    items: cleanings.filter(c => c.soldier.platoon === p)
  }))

  return (
    <main className="main">
      <div className="toolbar" style={{ flexWrap: 'wrap', gap: 8 }}>
        <input
          type="date"
          className="date-input"
          value={currentDate}
          onChange={e => setCurrentDate(e.target.value)}
        />
        <button className="btn btn-primary" onClick={generate}>+ Згенерувати</button>
        <button className="btn btn-danger" onClick={deleteCleaning}>Видалити</button>
        <button className="btn btn-tg" onClick={sendReport} disabled={sending}>
          {sending ? '⏳' : '📤'} <span className="btn-label">Telegram</span>
        </button>
      </div>

      <section className="card">
        <div className="card-title">
          Прибирання на {new Date(currentDate + 'T00:00:00').toLocaleDateString('uk-UA', { day: 'numeric', month: 'long', year: 'numeric' })}
        </div>

        {cleanings.length === 0
          ? <div className="empty">Прибирання не знайдено. Натисніть "+ Згенерувати"</div>
          : byPlatoon.map(({ platoon, items }) => (
            <div key={platoon} className="platoon-block">
              <div className="platoon-title">{platoon} відділення</div>
              {items.map(c => (
                <div key={c.id} className="cleaning-item">
                  <div>
                    <div className="duty-name">
                      {c.soldier.lastName} {c.soldier.firstName} {c.soldier.middleName}
                      {c.isManual && <span className="manual-tag"> (вручну)</span>}
                    </div>
                    <div className="duty-meta">{c.soldier.rank}</div>
                  </div>
                  <button className="btn-icon" onClick={() => openReplaceModal(c)}>🔄</button>
                </div>
              ))}
            </div>
          ))
        }
      </section>

      {replaceModal && (
        <div className="overlay" onClick={e => e.target === e.currentTarget && setReplaceModal(null)}>
          <div className="modal">
            <div className="modal-header">
              <h3>Замінити солдата</h3>
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
                    <div className="candidate-meta">{s.rank}</div>
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