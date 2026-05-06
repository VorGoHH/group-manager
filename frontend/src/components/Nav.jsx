import { NavLink } from 'react-router-dom'

export default function Nav() {
  return (
    <nav className="nav">
      <div className="nav-brand">241 н.г.</div>
      <div className="nav-links">
        <NavLink to="/" end className={({isActive}) => 'nav-link' + (isActive ? ' active' : '')}>Розхід</NavLink>
        <NavLink to="/duties" className={({isActive}) => 'nav-link' + (isActive ? ' active' : '')}>Наряди</NavLink>
        <NavLink to="/cleaning" className={({isActive}) => 'nav-link' + (isActive ? ' active' : '')}>Прибирання</NavLink>
        <NavLink to="/stats" className={({isActive}) => 'nav-link' + (isActive ? ' active' : '')}>Статистика</NavLink>
        <NavLink to="/work" className={({isActive}) => 'nav-link' + (isActive ? ' active' : '')}>Роботи</NavLink>
      </div>
    </nav>
  )
}