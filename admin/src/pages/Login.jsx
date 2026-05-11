import { useState } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const { user, login } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  if (user) return <Navigate to="/" replace />;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await login(email, password);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid credentials');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center p-4">
      <div className="relative w-full max-w-6xl">
        <div className="absolute -inset-1 rounded-[34px] bg-gradient-to-r from-emerald-400 via-teal-500 to-emerald-700 opacity-15 blur-2xl" />

        <div className="relative overflow-hidden rounded-[34px] border border-emerald-100 bg-white shadow-[0_20px_60px_rgba(15,23,42,0.08)]">
          <div className="grid min-h-[680px] md:grid-cols-2">
            <div className="hidden flex-col justify-between bg-gradient-to-br from-emerald-900 via-emerald-800 to-teal-900 p-10 text-white md:flex">
              <div>
                <p className="text-xs font-semibold uppercase tracking-[0.22em] text-emerald-200/70">Jayanesh Agencies</p>
                <h2 className="mt-4 max-w-md text-4xl font-semibold leading-tight">
                  Manage your sweets & chocolate business with ease.
                </h2>
                <p className="mt-4 max-w-md text-sm leading-6 text-emerald-100/80">
                  Streamline orders, track inventory, and grow your wholesale business from one powerful dashboard.
                </p>
              </div>

              <div className="overflow-hidden rounded-[28px] border border-white/10 bg-white/5 p-3 shadow-[0_10px_30px_rgba(0,0,0,0.12)]">
                <img src="/login-bg.jpg" alt="Login illustration" className="h-full w-full rounded-[22px] object-cover" />
              </div>
            </div>

            <div className="flex items-center justify-center p-8 md:p-10">
              <div className="w-full max-w-md text-center">
                <div className="mb-8 flex items-center justify-center gap-3">
                  <div className="grid h-11 w-11 place-items-center rounded-full bg-emerald-100 text-emerald-700 shadow-sm">
                    🛒
                  </div>
                  <div className="text-left">
                    <p className="text-xs font-semibold uppercase tracking-[0.18em] text-emerald-600">Wholesale Admin</p>
                    <h1 className="text-lg font-semibold text-slate-900">Jayanesh Agencies</h1>
                  </div>
                </div>

                <h1 className="text-3xl font-semibold tracking-tight text-slate-900">Welcome Back</h1>
                <p className="mt-2 text-sm text-slate-500">Please login to your account</p>

                {error && (
                  <div className="mt-6 rounded-2xl border border-red-100 bg-red-50 px-4 py-3 text-left text-sm text-red-600">
                    {error}
                  </div>
                )}

                <form onSubmit={handleSubmit} className="mt-6 space-y-4 text-left">
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    placeholder="Email address"
                    className="input-field"
                  />

                  <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    placeholder="Password"
                    className="input-field"
                  />

                  <div className="pt-2">
                    <button
                      type="submit"
                      disabled={loading}
                      className="app-btn-primary w-full py-3.5 text-base"
                    >
                      {loading ? 'Signing in...' : 'Login'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}