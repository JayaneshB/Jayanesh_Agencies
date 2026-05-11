import { useEffect, useState, useCallback } from 'react';
import { Eye } from 'lucide-react';
import api from '../lib/api';
import Loader from '../components/Loader';
import OrderDetailModal from '../components/OrderDetailModal';
import { getMockOrders, isMockModeEnabled } from '../lib/mockData';

const statuses = [
  '', 'PENDING_PAYMENT', 'CONFIRMED', 'PROCESSING', 'PACKED', 'SHIPPED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED',
];

const statusColors = {
  PENDING_PAYMENT: 'bg-yellow-100 text-yellow-800',
  CONFIRMED: 'bg-emerald-100 text-emerald-800',
  PROCESSING: 'bg-teal-100 text-teal-800',
  PACKED: 'bg-emerald-50 text-emerald-800',
  SHIPPED: 'bg-teal-50 text-teal-800',
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
    try {
      if (isMockModeEnabled()) {
        const data = await getMockOrders({ page, size: 15, status: statusFilter });
        setOrders(data.content);
        setTotalPages(data.totalPages);
        return;
      }

      const params = { page, size: 15, sort: 'createdAt,desc' };
      if (statusFilter) params.status = statusFilter;
      const { data } = await api.get('/admin/orders', { params });
      setOrders(data.content);
      setTotalPages(data.totalPages);
    } finally {
      setLoading(false);
    }
  }, [page, statusFilter]);

  useEffect(() => { load(); }, [load]);

  if (loading) return <Loader />;

  return (
    <div className="space-y-6">
      <div className="app-surface flex flex-wrap items-center justify-between gap-4 px-6 py-5">
        <div>
          <p className="text-sm font-medium uppercase tracking-[0.18em] text-emerald-700">Sales</p>
          <h1 className="mt-2 text-3xl font-semibold tracking-tight text-slate-900">Orders</h1>
          <p className="mt-2 text-sm text-slate-500">Review, filter, and manage customer orders in a consistent green interface.</p>
        </div>
        <select
          value={statusFilter}
          onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
          className="input-field w-full max-w-xs"
        >
          {statuses.map(s => <option key={s} value={s}>{s ? s.replace(/_/g, ' ') : 'All Statuses'}</option>)}
        </select>
      </div>

      <div className="app-table-card">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="app-table-head">
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
                <tr key={o.id} className="app-table-row">
                  <td className="px-5 py-3 font-mono text-xs text-slate-600">{o.id.slice(0, 8)}...</td>
                  <td className="px-5 py-3 text-slate-700">{o.customerName || '—'}</td>
                  <td className="px-5 py-3">
                    <span className={`inline-block px-2.5 py-1 rounded-full text-xs font-medium ${statusColors[o.status] || ''}`}>
                      {o.status.replace(/_/g, ' ')}
                    </span>
                  </td>
                  <td className="px-5 py-3 text-slate-500">{o.paymentStatus}</td>
                  <td className="px-5 py-3 text-right font-medium text-slate-800">₹{o.totalAmount?.toLocaleString()}</td>
                  <td className="px-5 py-3 text-slate-500">{new Date(o.createdAt).toLocaleDateString()}</td>
                  <td className="px-5 py-3 text-right">
                    <button onClick={() => setSelected(o)} className="p-1.5 text-slate-400 hover:text-emerald-600 cursor-pointer"><Eye size={15} /></button>
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
          <button disabled={page === 0} onClick={() => setPage(p => p - 1)} className="app-btn-secondary px-4 py-2 text-sm disabled:opacity-40">Prev</button>
          <span className="text-sm text-slate-500">Page {page + 1} of {totalPages}</span>
          <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)} className="app-btn-secondary px-4 py-2 text-sm disabled:opacity-40">Next</button>
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
