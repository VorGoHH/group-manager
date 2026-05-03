import { useState, useEffect } from 'react'

const ROLE_COLORS = { 1: 'role-1', 2: 'role-2', 3: 'role-3', 4: 'role-4' }
const ROLE_NAMES = { 1: 'Черговий ПУ', 2: 'Помічник ЧПУ', 3: 'Днювальний', 4: 'Їдальня' }

const today = () => new Date().toISOString().split('T')[0]

export default function Duties() {
  const [currentDate, setCurrentDate] = useState(today())
  const [duties, setDuties] = useState([])
  const [toast, setToast] = useState(null)
  const [replaceModal, setReplaceModal] = useState(null)
  const [candidates, setCandidates] = useState([])
  const [sending, setSending] = useState(false)

  const showToast = (msg, ok = true) => {
    setToast({ msg, ok })
    setTimeout(() => setToast(null), 4000)
  }

  const load = async (date) => {
    const res = await fetch(`/api/duties?date=${date}`)
    const d = await res.json()
    setDuties(d)
  }

  useEffect(() => { load(currentDate) }, [currentDate])

  const generate = async (type) => {
    const res = await fetch(`/api/duties/generate?date=${currentDate}&type=${type}`, { method: 'POST' })
    if (res.ok) { showToast('Наряд успішно згенеровано!'); load(currentDate) }
    else showToast(await res.text(), false)
  }

  const deleteDuty = async () => {
    if (!confirm('Видалити наряд на цю дату?')) return
    await fetch(`/api/duties?date=${currentDate}`, { method: 'DELETE' })
    showToast('Наряд видалено')
    load(currentDate)
  }

  const openReplaceModal = async (duty) => {
    const res = await fetch(`/api/duties/${duty.id}/candidates`)
    const c = await res.json()
    setCandidates(c)
    setReplaceModal(duty)
  }

  const confirmReplace = async (soldierId) => {
    await fetch(`/api/duties/${replaceModal.id}/replace/${soldierId}`, { method: 'PUT' })
    setReplaceModal(null)
    load(currentDate)
  }

  const replaceRandom = async (dutyId) => {
    const res = await fetch(`/api/duties/${dutyId}/replace`, { method: 'PUT' })
    if (res.ok) load(currentDate)
    else showToast(await res.text(), false)
  }

  const sendReport = async () => {
    setSending(true)
    try {
      const res = await fetch(`/api/duties/report/send?date=${currentDate}`, { method: 'POST' })
      showToast(res.ok ? '✅ Надіслано в Telegram' : '❌ Помилка відправки', res.ok)
    } catch {
      showToast('❌ Помилка відправки', false)
    }
    setSending(false)
  }

  return (
    <main className="main">
      <div className="toolbar" style={{ flexWrap: 'wrap', gap: 8 }}>
        <input
          type="date"
          className="date-input"
          value={currentDate}
          onChange={e => setCurrentDate(e.target.value)}
        />
        <button className="btn btn-primary" onClick={() => generate('NORMAL')}>+ Звичайний</button>
        <button className="btn btn-primary" onClick={() => generate('WITH_PU')}>+ З ПУ</button>
        <button className="btn btn-danger" onClick={deleteDuty}>Видалити</button>
        <button className="btn btn-tg" onClick={sendReport} disabled={sending}>
          {sending ? '⏳' : '📤'} <span className="btn-label">Telegram</span>
        </button>
      </div>

      <section className="card">
        <div className="card-title">
          Наряд на {new Date(currentDate + 'T00:00:00').toLocaleDateString('uk-UA', { day: 'numeric', month: 'long', year: 'numeric' })}
        </div>

        {duties.length === 0
          ? <div className="empty">Наряд не знайдено. Натисніть "+ Звичайний" або "+ З ПУ"</div>
          : duties.map(d => (
            <div key={d.id} className="duty-row">
              <span className={`role-badge ${ROLE_COLORS[d.role.id]}`}>{d.role.name}</span>
              <div className="duty-info">
                <div className="duty-name">
                  {d.soldier.lastName} {d.soldier.firstName} {d.soldier.middleName}
                  {d.isManual && <span className="manual-tag"> (вручну)</span>}
                </div>
                <div className="duty-meta">{d.soldier.rank} · {d.soldier.platoon} відділення</div>
              </div>
              <div className="duty-actions">
                <button className="btn-icon" title="Вибрати вручну" onClick={() => openReplaceModal(d)}>🔄</button>
                <button className="btn-icon" title="Випадкова заміна" onClick={() => replaceRandom(d.id)}>🎲</button>
              </div>
            </div>
          ))
        }
      </section>

      {replaceModal && (
        <div className="overlay" onClick={e => e.target === e.currentTarget && setReplaceModal(null)}>
          <div className="modal">
            <div className="modal-header">
              <h3>Замінити — {replaceModal.role.name}</h3>
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