import { useState } from 'react';
import { X } from 'lucide-react';
import api from '../lib/api';
import { isMockModeEnabled, updateMockOrderStatus } from '../lib/mockData';

const nextStatuses = {
  PENDING_PAYMENT: ['CONFIRMED', 'CANCELLED'],
  CONFIRMED: ['PROCESSING', 'CANCELLED'],
  PROCESSING: ['PACKED', 'CANCELLED'],
  PACKED: ['SHIPPED', 'CANCELLED'],
  SHIPPED: ['OUT_FOR_DELIVERY', 'CANCELLED'],
  OUT_FOR_DELIVERY: ['DELIVERED', 'CANCELLED'],
};

export default function OrderDetailModal({ order, onClose, onUpdated }) {
  const [newStatus, setNewStatus] = useState('');
  const [reason, setReason] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const allowed = nextStatuses[order.status] || [];

  const handleUpdate = async () => {
    if (!newStatus) return;
    setSaving(true);
    setError('');
    try {
      if (isMockModeEnabled()) {
        await updateMockOrderStatus(order.id, newStatus, reason || null);
        onUpdated();
        return;
      }
      await api.patch(`/admin/orders/${order.id}/status`, { status: newStatus, reason: reason || null });
      onUpdated();
    } catch (err) {
      setError(err.response?.data?.message || 'Update failed');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="app-surface w-full max-w-lg max-h-[90vh] overflow-y-auto m-4">
        <div className="app-card-head">
          <h2 className="text-lg font-semibold">Order Details</h2>
          <button onClick={onClose} className="p-1 text-slate-400 hover:text-emerald-600 cursor-pointer"><X size={20} /></button>
        </div>

        <div className="p-6 space-y-4">
          <div className="grid grid-cols-2 gap-3 text-sm">
            <div><span className="text-slate-500">Order ID:</span><br /><span className="font-mono text-xs">{order.id}</span></div>
            <div><span className="text-slate-500">Customer:</span><br />{order.customerName || '—'}</div>
            <div><span className="text-slate-500">Phone:</span><br />{order.customerPhone || '—'}</div>
            <div><span className="text-slate-500">Status:</span><br /><span className="font-medium">{order.status.replace(/_/g, ' ')}</span></div>
            <div><span className="text-slate-500">Subtotal:</span><br />₹{order.subtotal?.toLocaleString()}</div>
            <div><span className="text-slate-500">Tax:</span><br />₹{order.taxAmount?.toLocaleString()}</div>
            <div><span className="text-slate-500">Delivery:</span><br />₹{order.deliveryFee?.toLocaleString()}</div>
            <div><span className="text-slate-500">Total:</span><br /><span className="font-bold">₹{order.totalAmount?.toLocaleString()}</span></div>
          </div>

          {order.items?.length > 0 && (
            <div>
              <h3 className="text-sm font-semibold text-slate-700 mb-2">Items</h3>
              <div className="border rounded-lg divide-y text-sm">
                {order.items.map(item => (
                  <div key={item.id} className="px-3 py-2 flex justify-between">
                    <span>{item.productName} × {item.quantity}</span>
                    <span>₹{item.totalPrice?.toLocaleString()}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {order.statusHistory?.length > 0 && (
            <div>
              <h3 className="text-sm font-semibold text-slate-700 mb-2">Status History</h3>
              <div className="space-y-1 text-xs text-slate-500">
                {order.statusHistory.map((h, i) => (
                  <div key={i}>{h.fromStatus?.replace(/_/g, ' ') || '—'} → {h.toStatus.replace(/_/g, ' ')} {h.reason ? `(${h.reason})` : ''}</div>
                ))}
              </div>
            </div>
          )}

          {allowed.length > 0 && (
            <div className="border-t pt-4">
              <h3 className="text-sm font-semibold text-slate-700 mb-2">Update Status</h3>
              {error && <div className="mb-2 rounded-2xl border border-red-100 bg-red-50 p-2 text-sm text-red-600">{error}</div>}
              <div className="flex gap-2 mb-2">
                {allowed.map(s => (
                  <button key={s} onClick={() => setNewStatus(s)}
                    className={`px-3 py-1.5 text-xs rounded-full border cursor-pointer ${newStatus === s ? 'bg-emerald-600 text-white border-emerald-600' : 'hover:bg-slate-50'}`}>
                    {s.replace(/_/g, ' ')}
                  </button>
                ))}
              </div>
              {newStatus === 'CANCELLED' && (
                <input placeholder="Reason for cancellation" value={reason} onChange={e => setReason(e.target.value)}
                  className="input-field mb-2" />
              )}
              <button onClick={handleUpdate} disabled={!newStatus || saving}
                className="app-btn-primary px-4 py-2 text-sm disabled:opacity-50">
                {saving ? 'Updating...' : 'Confirm'}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
