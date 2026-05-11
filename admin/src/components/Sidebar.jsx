import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Package, ShoppingCart, Layers, Warehouse, LogOut } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const links = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/products', label: 'Products', icon: Package },
  { to: '/orders', label: 'Orders', icon: ShoppingCart },
  { to: '/categories', label: 'Categories', icon: Layers },
  { to: '/inventory', label: 'Inventory', icon: Warehouse },
];

export default function Sidebar() {
  const { user, logout } = useAuth();

  return (
    <aside className="w-72 shrink-0 p-0">
      <div className="flex min-h-[calc(100vh-2rem)] flex-col rounded-[28px] border border-emerald-100 bg-white p-4 shadow-[0_10px_35px_rgba(15,23,42,0.05)] lg:min-h-[calc(100vh-2.5rem)]">
        <div className="rounded-[24px] border border-emerald-100 bg-gradient-to-br from-emerald-50 via-white to-teal-50 px-4 py-4 shadow-sm">
          <div className="flex items-center gap-3">
            <div className="grid h-11 w-11 place-items-center rounded-full bg-gradient-to-br from-emerald-700 to-teal-600 text-white shadow-sm">
              JA
            </div>
            <div>
              <h1 className="text-lg font-semibold tracking-tight text-slate-900">Jayanesh Agencies</h1>
              <p className="text-xs text-slate-500">Wholesale Admin</p>
            </div>
          </div>
        </div>

        <div className="mt-6 px-1">
          <p className="mb-3 text-[11px] font-semibold uppercase tracking-[0.18em] text-slate-400">Menu</p>
          <nav className="space-y-1">
            {links.map(({ to, label, icon: Icon }) => (
              <NavLink
                key={to}
                to={to}
                end={to === '/'}
                className={({ isActive }) =>
                  `flex items-center gap-3 rounded-full px-4 py-3 text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-emerald-50 text-emerald-800 ring-1 ring-emerald-100'
                      : 'text-slate-500 hover:bg-slate-50 hover:text-slate-900'
                  }`
                }
              >
                <span
                  className={`grid h-9 w-9 place-items-center rounded-full transition-colors ${
                    label === 'Dashboard'
                      ? 'bg-white text-emerald-700 ring-1 ring-emerald-100'
                      : 'bg-slate-100 text-slate-500'
                  }`}
                >
                  <Icon size={17} />
                </span>
                <span>{label}</span>
              </NavLink>
            ))}
          </nav>
        </div>

        <div className="mt-auto space-y-4 px-1 pb-1 pt-6">
          <div className="app-soft-surface p-4">
            <p className="text-xs font-semibold uppercase tracking-[0.16em] text-emerald-700">General</p>
            <div className="mt-3 space-y-2 text-sm text-slate-500">
              <div className="flex items-center justify-between rounded-2xl bg-white px-3 py-2">
                <span>Signed in as</span>
                <span className="font-medium text-slate-900">{user?.name || 'Admin'}</span>
              </div>
              <button
                onClick={logout}
                className="flex w-full items-center justify-center gap-2 rounded-full border border-emerald-200 bg-white px-4 py-2.5 text-sm font-medium text-emerald-800 transition hover:bg-emerald-50"
              >
                <LogOut size={16} /> Log out
              </button>
            </div>
          </div>

          <div className="overflow-hidden rounded-[24px] bg-gradient-to-br from-emerald-950 via-emerald-900 to-teal-950 p-4 text-white shadow-sm">
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="text-xs font-semibold uppercase tracking-[0.18em] text-emerald-200/80">Quick Tip</p>
                <h2 className="mt-2 text-lg font-semibold leading-tight">Track stock & orders in one glance</h2>
                <p className="mt-2 text-xs leading-5 text-emerald-100/80">Use the dashboard charts to catch low stock items before they affect sales.</p>
              </div>
              <div className="grid h-11 w-11 place-items-center rounded-full bg-white/10 text-lg">◌</div>
            </div>
          </div>
        </div>
      </div>
    </aside>
  );
}
