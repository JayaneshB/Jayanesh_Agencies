import { useEffect, useState, useCallback } from 'react';
import { Eye } from 'lucide-react';
import api from '../lib/api';
import Loader from '../components/Loader';
import OrderDetailModal from '../components/OrderDetailModal';

const statuses = [
  '', 'PENDING_PAYMENT', 'CONFIRMED', 'PROCESSING', 'PACKED', 'SHIPPED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED',
];

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

export default function Orders() {
  const [orders, setOrders] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');
  const [selected, setSelected] = useState(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    const params = { page, size: 15, sort: 'createdAt,desc' };
    if (statusFilter) params.status = statusFilter;
    const { data } = await api.get('/admin/orders', { params });
    setOrders(data.content);
    setTotalPages(data.totalPages);
    setLoading(false);
  }, [page, statusFilter]);

  useEffect(() => { load(); }, [load]);

  if (loading) return <Loader />;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-slate-900">Orders</h1>
        <select
          value={statusFilter}
          onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
          className="px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
        >
          {statuses.map(s => <option key={s} value={s}>{s ? s.replace(/_/g, ' ') : 'All Statuses'}</option>)}
        </select>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-slate-50 text-left text-slate-600">
                <th className="px-5 py-3 font-medium">Order ID</th>
                <th className="px-5 py-3 font-medium">Customer</th>
                <th className="px-5 py-3 font-medium">Status</th>
                <th className="px-5 py-3 font-medium">Payment</th>
                <th className="px-5 py-3 font-medium text-right">Total</th>
                <th className="px-5 py-3 font-medium">Date</th>
                <th className="px-5 py-3 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {orders.map(o => (
                <tr key={o.id} className="hover:bg-slate-50">
                  <td className="px-5 py-3 font-mono text-xs">{o.id.slice(0, 8)}...</td>
                  <td className="px-5 py-3">{o.customerName || '—'}</td>
                  <td className="px-5 py-3">
                    <span className={`inline-block px-2 py-0.5 rounded-full text-xs font-medium ${statusColors[o.status] || ''}`}>
                      {o.status.replace(/_/g, ' ')}
                    </span>
                  </td>
                  <td className="px-5 py-3 text-slate-500">{o.paymentStatus}</td>
                  <td className="px-5 py-3 text-right">₹{o.totalAmount?.toLocaleString()}</td>
                  <td className="px-5 py-3 text-slate-500">{new Date(o.createdAt).toLocaleDateString()}</td>
                  <td className="px-5 py-3 text-right">
                    <button onClick={() => setSelected(o)} className="p-1.5 text-slate-400 hover:text-indigo-600 cursor-pointer"><Eye size={15} /></button>
                  </td>
                </tr>
              ))}
              {orders.length === 0 && (
                <tr><td colSpan={7} className="px-5 py-8 text-center text-slate-500">No orders found.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 mt-4">
          <button disabled={page === 0} onClick={() => setPage(p => p - 1)} className="px-3 py-1.5 border rounded-lg text-sm disabled:opacity-40 cursor-pointer">Prev</button>
          <span className="text-sm text-slate-500">Page {page + 1} of {totalPages}</span>
          <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)} className="px-3 py-1.5 border rounded-lg text-sm disabled:opacity-40 cursor-pointer">Next</button>
        </div>
      )}

      {selected && (
        <OrderDetailModal
          order={selected}
          onClose={() => setSelected(null)}
          onUpdated={() => { setSelected(null); load(); }}
        />
      )}
    </div>
  );
}
