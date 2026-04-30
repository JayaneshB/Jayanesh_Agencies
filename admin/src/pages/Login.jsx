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
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-[#e0e7ff] via-[#eef2ff] to-[#c7d2fe] p-4">
      <div className="relative w-full max-w-5xl">
        
        {/* Glow effect */}
        <div className="absolute -inset-1 bg-gradient-to-r from-blue-400 to-indigo-500 rounded-3xl blur-2xl opacity-20"></div>

        {/* Card */}
        <div className="relative bg-white rounded-3xl shadow-xl flex overflow-hidden">
          
          {/* LEFT PANEL */}
          <div className="hidden md:flex w-1/2 bg-gradient-to-br from-blue-600 to-indigo-600 text-white p-10 flex-col justify-between">
            <div>
              <h2 className="text-3xl font-bold leading-snug mb-4">
                Manage your sweets & chocolate business with ease.
              </h2>
              <p className="text-sm text-blue-100">
                Streamline orders, track inventory, and grow your wholesale business from one powerful dashboard.
              </p>
            </div>

            <div className="mt-6 w-full h-80 rounded-2xl overflow-hidden bg-white/10 flex items-center justify-center">
              <img
                src="/login-bg.jpg"
                alt="Login illustration"
                className="w-full h-full object-cover"
              />
            </div>
          </div>

          {/* RIGHT PANEL */}
          <div className="w-full md:w-1/2 p-8 flex flex-col justify-center items-center">
            <div className="w-full max-w-sm text-center">
              
              {/* Logo */}
              <div className="flex items-center justify-center gap-2 mb-6">
                <div className="w-10 h-10 flex items-center justify-center bg-blue-100 text-white rounded-full">
                  🛒
                </div>
                <span className="font-semibold text-lg">
                  Jayanesh Agencies
                </span>
              </div>

              <h1 className="text-2xl font-bold text-slate-900">
                Welcome Back
              </h1>

              <p className="text-sm text-slate-500 mb-6">
                Please login to your account
              </p>

              {error && (
                <div className="bg-red-50 text-red-600 text-sm rounded-lg p-3 mb-4 text-left">
                  {error}
                </div>
              )}

              <form onSubmit={handleSubmit} className="space-y-4 text-left">
                
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  placeholder="Email address"
                  className="w-full px-4 py-3 rounded-xl bg-slate-100 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />

                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  placeholder="Password"
                  className="w-full px-4 py-3 rounded-xl bg-slate-100 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />

                <div className="pt-2">
                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full py-3 bg-gradient-to-r from-blue-500 to-indigo-600 text-white rounded-xl font-medium hover:opacity-90 transition shadow-md hover:shadow-lg disabled:opacity-50"
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
  );
}