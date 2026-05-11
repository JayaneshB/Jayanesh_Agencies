import { useState } from 'react';
import { Bell, CheckCircle2, Mail, Search, LogOut } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { isMockModeEnabled, setMockModeEnabled } from '../lib/mockData';

function getInitials(name) {
  if (!name) return 'A';
  return name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase();
}

export default function TopBar() {
  const { user, logout } = useAuth();
  const [mockMode, setMockMode] = useState(isMockModeEnabled());

  const handleMockToggle = () => {
    const nextValue = !mockMode;
    setMockMode(nextValue);
    setMockModeEnabled(nextValue);
    window.location.reload();
  };

  return (
    <header className="app-surface mb-6 flex flex-wrap items-center justify-between gap-4 px-5 py-4">
      <div className="relative min-w-[260px] flex-1 max-w-xl">
        <Search className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
        <input
          type="search"
          placeholder="Search products, orders, inventory..."
          className="input-field !rounded-full !bg-slate-50 !pl-11 !pr-20"
        />
        <span className="absolute right-3 top-1/2 -translate-y-1/2 rounded-full bg-white px-3 py-1 text-[11px] font-semibold text-slate-400 shadow-sm">
          ⌘ F
        </span>
      </div>

      <div className="flex items-center gap-3">
        <button
          onClick={handleMockToggle}
          className={`flex items-center gap-2 rounded-full border px-3 py-2 text-sm font-medium transition ${
            mockMode
              ? 'border-emerald-200 bg-emerald-50 text-emerald-700 hover:bg-emerald-100'
              : 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50'
          }`}
          title="Toggle temporary mock data"
        >
          {mockMode ? <CheckCircle2 size={16} /> : <span className="h-2.5 w-2.5 rounded-full bg-slate-400" />}
          {mockMode ? 'Mock Data On' : 'Mock Data Off'}
        </button>

        <button className="grid h-11 w-11 place-items-center rounded-full border border-emerald-100 bg-white text-slate-500 transition hover:bg-emerald-50 hover:text-emerald-700">
          <Mail size={18} />
        </button>
        <button className="grid h-11 w-11 place-items-center rounded-full border border-emerald-100 bg-white text-slate-500 transition hover:bg-emerald-50 hover:text-emerald-700">
          <Bell size={18} />
        </button>

        <div className="flex items-center gap-3 rounded-full border border-emerald-100 bg-white px-3 py-2 shadow-sm">
          <div className="grid h-11 w-11 place-items-center rounded-full bg-gradient-to-br from-emerald-600 to-teal-600 text-sm font-semibold text-white">
            {getInitials(user?.name)}
          </div>
          <div className="leading-tight">
            <p className="text-sm font-semibold text-slate-900">{user?.name || 'Admin'}</p>
            <p className="text-xs text-slate-500">{user?.role || 'ADMIN'}</p>
          </div>
          <button
            onClick={logout}
            className="ml-1 grid h-10 w-10 place-items-center rounded-full text-slate-500 transition hover:bg-emerald-50 hover:text-emerald-700"
            aria-label="Sign out"
          >
            <LogOut size={16} />
          </button>
        </div>
      </div>
    </header>
  );
}
