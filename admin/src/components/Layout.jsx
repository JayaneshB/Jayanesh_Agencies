import { Outlet, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Sidebar from './Sidebar';
import TopBar from './TopBar';

export default function Layout() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;

  return (
    <div className="app-shell flex min-h-screen gap-4 p-4 lg:gap-5 lg:p-5">
      <Sidebar />
      <main className="min-w-0 flex-1 overflow-hidden">
        <TopBar />
        <div className="space-y-6 pb-2">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
