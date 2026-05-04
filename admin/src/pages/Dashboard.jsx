import { useEffect, useState } from 'react';
import { DollarSign, ShoppingCart, Truck, AlertTriangle } from 'lucide-react';
import api from '../lib/api';
import Loader from '../components/Loader';

function StatCard({ label, value, icon: Icon, color }) {
  return (
    <div className="bg-white rounded-xl border border-slate-200 p-5 flex items-center gap-4">
      <div className={`p-3 rounded-lg ${color}`}>
        <Icon size={22} className="text-white" />
      </div>
      <div>
        <p className="text-sm text-slate-500">{label}</p>
        <p className="text-2xl font-bold text-slate-900">{value}</p>
      </div>
    </div>
  );
}

const statusColors = {
  PENDING_PAYMENT: 'bg-yellow-100 text-yellow-800',
  CONFIRMED: 'bg-blue-100 text-blue-800',
  PROCESSING: 'bg-indigo-100 text-indigo-800',
  PACKED: 'bg-purple-100 text-purple-800',
  SHIPPED: 'bg-cyan-100 text-cyan-800',
  OUT_FOR_DELIVERY: 'bg-orange-100 text-orange-800',
  DELIVERED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-red-100 text-red-800',
};

export default function Dashboard() {
  const [summary, setSummary] = useState(null);
  const [recentOrders, setRecentOrders] = useState([]);

  useEffect(() => {
    api.get('/admin/dashboard/summary').then((r) => setSummary(r.data));
    api.get('/admin/dashboard/recent-orders').then((r) => setRecentOrders(r.data));
  }, []);

  if (!summary) return <Loader />;

  return (
    <div>
      <h1 className="text-2xl font-bold text-slate-900 mb-6">Dashboard</h1>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard label="Today's Revenue" value={`₹${summary.todayRevenue.toLocaleString()}`} icon={DollarSign} color="bg-emerald-500" />
        <StatCard label="Today's Orders" value={summary.todayOrderCount} icon={ShoppingCart} color="bg-blue-500" />
        <StatCard label="Pending Orders" value={summary.pendingOrders} icon={Truck} color="bg-amber-500" />
        <StatCard label="Low Stock SKUs" value={summary.lowStockSkuCount} icon={AlertTriangle} color="bg-red-500" />
      </div>

      <div className="bg-white rounded-xl border border-slate-200">
        <div className="px-5 py-4 border-b border-slate-200">
          <h2 className="text-lg font-semibold text-slate-900">Recent Orders</h2>
        </div>
        {recentOrders.length === 0 ? (
          <p className="p-5 text-sm text-slate-500">No orders yet.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-slate-50 text-left text-slate-600">
                  <th className="px-5 py-3 font-medium">Order ID</th>
                  <th className="px-5 py-3 font-medium">Customer</th>
                  <th className="px-5 py-3 font-medium">Status</th>
                  <th className="px-5 py-3 font-medium text-right">Amount</th>
                  <th className="px-5 py-3 font-medium">Date</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {recentOrders.map((o) => (
                  <tr key={o.id} className="hover:bg-slate-50">
                    <td className="px-5 py-3 font-mono text-xs">{o.id.slice(0, 8)}...</td>
                    <td className="px-5 py-3">{o.customerName || '—'}</td>
                    <td className="px-5 py-3">
                      <span className={`inline-block px-2 py-0.5 rounded-full text-xs font-medium ${statusColors[o.status] || 'bg-slate-100 text-slate-700'}`}>
                        {o.status.replace(/_/g, ' ')}
                      </span>
                    </td>
                    <td className="px-5 py-3 text-right">₹{o.totalAmount?.toLocaleString()}</td>
                    <td className="px-5 py-3 text-slate-500">{new Date(o.createdAt).toLocaleDateString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
