import { useState, useEffect } from 'react'

const ROLES = ['Черговий ПУ', 'Помічник ЧПУ', 'Днювальний', 'Їдальня']
const ROLE_SHORT = {
  'Черговий ПУ': 'Черг. ПУ',
  'Помічник ЧПУ': 'Пом. ЧПУ',
  'Днювальний': 'Днюв.',
  'Їдальня': 'Їдальня'
}

export default function Stats() {
  const [stats, setStats] = useState([])
  const [editMode, setEditMode] = useState(false)
  const [history, setHistory] = useState(null)
  const [historyTab, setHistoryTab] = useState('duties')
  const [toast, setToast] = useState(null)
  const [vals, setVals] = useState({})

  const showToast = (msg, ok = true) => {
    setToast({ msg, ok })
    setTimeout(() => setToast(null), 2500)
  }

  const load = async () => {
    const res = await fetch('/api/stats')
    const data = await res.json()
    setStats(data)
    const v = {}
    data.forEach(s => {
      ROLES.forEach(r => { v[`duty-${s.id}-${r}`] = s.dutyByRole[r] || 0 })
      v[`clean-${s.id}`] = s.totalCleaning || 0
      v[`total-${s.id}`] = s.totalDuty || 0
      v[`work-${s.id}`] = s.totalWork || 0
    })
    setVals(v)
  }

  useEffect(() => { load() }, [])

  const adjustDuty = async (soldierId, role, delta) => {
    const method = delta > 0 ? 'POST' : 'DELETE'
    const res = await fetch(`/api/stats/${soldierId}/duty?role=${encodeURIComponent(role)}`, { method })
    if (!res.ok) { showToast(await res.text(), false); return }
    setVals(prev => {
      const key = `duty-${soldierId}-${role}`
      const totalKey = `total-${soldierId}`
      return { ...prev, [key]: (prev[key] || 0) + delta, [totalKey]: (prev[totalKey] || 0) + delta }
    })
    showToast(delta > 0 ? 'Додано' : 'Видалено')
  }

  const adjustCleaning = async (soldierId, delta) => {
    const method = delta > 0 ? 'POST' : 'DELETE'
    const res = await fetch(`/api/stats/${soldierId}/cleaning`, { method })
    if (!res.ok) { showToast(await res.text(), false); return }
    setVals(prev => {
      const key = `clean-${soldierId}`
      return { ...prev, [key]: (prev[key] || 0) + delta }
    })
    showToast(delta > 0 ? 'Додано' : 'Видалено')
  }

  const openHistory = async (soldierId, name) => {
    const res = await fetch(`/api/stats/${soldierId}/history`)
    const data = await res.json()
    setHistory({ name, data })
    setHistoryTab('duties')
  }

  const formatDate = (d) => d === '1970-01-01'
    ? 'ручне коригування'
    : new Date(d + 'T00:00:00').toLocaleDateString('uk-UA', { day: 'numeric', month: 'long', year: 'numeric' })

  const byPlatoon = [1, 2, 3].map(p => ({
    platoon: p,
    soldiers: stats.filter(s => s.platoon === p)
  }))

  return (
    <main className="main" style={{ maxWidth: 1200 }}>
      <div className="stats-header">
        <h1 className="stats-title">Статистика</h1>
        <button
          className={`btn ${editMode ? 'btn-edit-active' : 'btn-primary'}`}
          onClick={() => setEditMode(e => !e)}
        >
          {editMode ? '👁 Перегляд' : '✏️ Редагувати'}
        </button>
      </div>

      {editMode && <p className="edit-hint">Натисніть + / − щоб відкоригувати кількість вручну</p>}

      {byPlatoon.map(({ platoon, soldiers }) => soldiers.length === 0 ? null : (
        <div key={platoon} className="platoon-section">
          <div className="platoon-label">{platoon} відділення</div>
          <div className="stats-table-wrap">
            <table className="stats-table">
              <thead>
                <tr>
                  <th>Прізвище</th>
                  <th>Звання</th>
                  {ROLES.map(r => <th key={r}>{ROLE_SHORT[r]}</th>)}
                  <th>Всього</th>
                  <th>Прибирань</th>
                  <th>Роботи</th>
                </tr>
              </thead>
              <tbody>
                {soldiers.map(s => (
                  <tr key={s.id}>
                    <td className="name-cell" onClick={() => openHistory(s.id, `${s.lastName} ${s.firstName[0]}.`)}>
                      {s.lastName} {s.firstName[0]}.
                    </td>
                    <td className="rank-cell">{s.rank}</td>
                    {ROLES.map(role => {
                      const val = vals[`duty-${s.id}-${role}`] ?? 0
                      return (
                        <td key={role} className="num-cell">
                          {editMode ? (
                            <div className="counter">
                              <button className="btn-adj minus" disabled={val === 0} onClick={() => adjustDuty(s.id, role, -1)}>−</button>
                              <span className={val === 0 ? 'zero' : ''}>{val}</span>
                              <button className="btn-adj plus" onClick={() => adjustDuty(s.id, role, 1)}>+</button>
                            </div>
                          ) : (
                            <span className={val === 0 ? 'zero' : ''}>{val}</span>
                          )}
                        </td>
                      )
                    })}
                    <td className="total-cell">{vals[`total-${s.id}`] ?? s.totalDuty}</td>
                    <td className="num-cell">
                      {editMode ? (
                        <div className="counter">
                          <button className="btn-adj minus" disabled={(vals[`clean-${s.id}`] ?? 0) === 0} onClick={() => adjustCleaning(s.id, -1)}>−</button>
                          <span className={(vals[`clean-${s.id}`] ?? 0) === 0 ? 'zero' : ''}>{vals[`clean-${s.id}`] ?? 0}</span>
                          <button className="btn-adj plus" onClick={() => adjustCleaning(s.id, 1)}>+</button>
                        </div>
                      ) : (
                        <span className={(vals[`clean-${s.id}`] ?? 0) === 0 ? 'zero' : ''}>{vals[`clean-${s.id}`] ?? 0}</span>
                      )}
                    </td>
                    <td className="num-cell">
                      {editMode ? (
                        <div className="counter">
                          <button className="btn-adj minus" disabled={(vals[`work-${s.id}`] ?? 0) === 0} onClick={() => adjustWork(s.id, -1)}>−</button>
                          <span className={(vals[`work-${s.id}`] ?? 0) === 0 ? 'zero' : ''}>{vals[`work-${s.id}`] ?? 0}</span>
                          <button className="btn-adj plus" onClick={() => adjustWork(s.id, 1)}>+</button>
                        </div>
                      ) : (
                        <span className={(vals[`work-${s.id}`] ?? 0) === 0 ? 'zero' : ''}>{vals[`work-${s.id}`] ?? 0}</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ))}

      {history && (
        <div className="overlay" onClick={e => e.target === e.currentTarget && setHistory(null)}>
          <div className="modal modal-wide">
            <div className="modal-header">
              <h3>{history.name}</h3>
              <button className="modal-close" onClick={() => setHistory(null)}>✕</button>
            </div>
            <div className="modal-tabs">
                <button className={`tab-btn ${historyTab === 'works' ? 'active' : ''}`} onClick={() => setHistoryTab('works')}>Роботи</button>
              <button className={`tab-btn ${historyTab === 'duties' ? 'active' : ''}`} onClick={() => setHistoryTab('duties')}>Наряди</button>
              <button className={`tab-btn ${historyTab === 'cleanings' ? 'active' : ''}`} onClick={() => setHistoryTab('cleanings')}>Прибирання</button>
            </div>
            <div className="history-list">
              {historyTab === 'duties'
                ? history.data.duties?.length === 0
                  ? <div className="empty">Немає записів</div>
                  : history.data.duties?.map((d, i) => (
                    <div key={i} className="history-row">
                      <span className="history-date">{formatDate(d.date)}</span>
                      <span>{d.role}{d.isManual && <span className="manual-tag"> (вручну)</span>}</span>
                    </div>
                  ))
                : historyTab === 'cleanings'
                  ? history.data.cleanings?.length === 0
                    ? <div className="empty">Немає записів</div>
                    : history.data.cleanings?.map((c, i) => (
                      <div key={i} className="history-row">
                        <span className="history-date">{formatDate(c.date)}</span>
                        <span>{c.territory}{c.isManual && <span className="manual-tag"> (вручну)</span>}</span>
                      </div>
                    ))
                  : history.data.works?.length === 0
                    ? <div className="empty">Немає записів</div>
                    : history.data.works?.map((w, i) => (
                      <div key={i} className="history-row">
                        <span className="history-date">{formatDate(w.date)}</span>
                        <span>{w.workName}{w.isManual && <span className="manual-tag"> (вручну)</span>}</span>
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