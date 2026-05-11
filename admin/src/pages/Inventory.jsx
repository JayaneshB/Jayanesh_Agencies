import { useEffect, useState } from 'react';
import { AlertTriangle } from 'lucide-react';
import api from '../lib/api';
import Loader from '../components/Loader';
import { adjustMockInventory, getMockInventory, isMockModeEnabled } from '../lib/mockData';

export default function InventoryPage() {
  const [inventory, setInventory] = useState([]);
  const [showLow, setShowLow] = useState(false);
  const [adjusting, setAdjusting] = useState(null);
  const [qty, setQty] = useState('');
  const [reason, setReason] = useState('RESTOCK');
  const [note, setNote] = useState('');
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(true);

  const load = () => {
    if (isMockModeEnabled()) {
      getMockInventory({ lowOnly: showLow }).then((rows) => { setInventory(rows); setLoading(false); });
      return;
    }
    const url = showLow ? '/admin/inventory/low-stock' : '/admin/inventory';
    api.get(url).then(r => { setInventory(r.data); setLoading(false); });
  };

  useEffect(() => { load(); }, [showLow]);

  const handleAdjust = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      if (isMockModeEnabled()) {
        await adjustMockInventory(adjusting, parseInt(qty), reason, note || null);
        setAdjusting(null); setQty(''); setNote('');
        load();
        return;
      }
      await api.post(`/admin/inventory/${adjusting}/adjust`, {
        quantity: parseInt(qty),
        reason,
        note: note || null,
      });
      setAdjusting(null); setQty(''); setNote('');
      load();
    } catch (err) {
      alert(err.response?.data?.message || 'Adjust failed');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <Loader />;

  return (
    <div className="space-y-6">
      <div className="app-surface flex flex-wrap items-center justify-between gap-4 px-6 py-5">
        <div>
          <p className="text-sm font-medium uppercase tracking-[0.18em] text-emerald-700">Operations</p>
          <h1 className="mt-2 text-3xl font-semibold tracking-tight text-slate-900">Inventory</h1>
          <p className="mt-2 text-sm text-slate-500">Track stock balances and adjust quantities with the same streamlined panel style.</p>
        </div>
        <button
          onClick={() => setShowLow(!showLow)}
          className={`app-btn-secondary ${showLow ? 'border-red-200 bg-red-50 text-red-700 hover:bg-red-50' : ''}`}
        >
          <AlertTriangle size={16} /> {showLow ? 'Show All' : 'Low Stock Only'}
        </button>
      </div>

      <div className="app-table-card">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="app-table-head">
                <th className="px-5 py-3 font-medium">Product</th>
                <th className="px-5 py-3 font-medium text-center">Total</th>
                <th className="px-5 py-3 font-medium text-center">Reserved</th>
                <th className="px-5 py-3 font-medium text-center">Sold</th>
                <th className="px-5 py-3 font-medium text-center">Available</th>
                <th className="px-5 py-3 font-medium text-center">Reorder At</th>
                <th className="px-5 py-3 font-medium text-center">MOQ</th>
                <th className="px-5 py-3 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {inventory.map(inv => (
                <tr key={inv.id} className="app-table-row">
                  <td className="px-5 py-3 font-medium text-slate-900">{inv.productName}</td>
                  <td className="px-5 py-3 text-center">{inv.totalStock}</td>
                  <td className="px-5 py-3 text-center">{inv.reserved}</td>
                  <td className="px-5 py-3 text-center">{inv.sold}</td>
                  <td className="px-5 py-3 text-center">
                    <span className={inv.available <= inv.reorderThreshold ? 'text-red-600 font-semibold' : ''}>
                      {inv.available}
                    </span>
                  </td>
                  <td className="px-5 py-3 text-center">{inv.reorderThreshold}</td>
                  <td className="px-5 py-3 text-center">{inv.moq}</td>
                  <td className="px-5 py-3 text-right">
                    <button onClick={() => setAdjusting(inv.productId)}
                      className="rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-xs font-medium text-emerald-700 transition hover:bg-emerald-100 cursor-pointer">
                      Adjust
                    </button>
                  </td>
                </tr>
              ))}
              {inventory.length === 0 && (
                <tr><td colSpan={8} className="px-5 py-8 text-center text-slate-500">No inventory records.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {adjusting && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="app-surface w-full max-w-sm m-4 p-6">
            <h2 className="text-lg font-semibold mb-4">Adjust Stock</h2>
            <form onSubmit={handleAdjust} className="space-y-3">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Quantity (+/-)</label>
                <input type="number" value={qty} onChange={e => setQty(e.target.value)} required
                  className="input-field"
                  placeholder="e.g. 50 or -10" />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Reason</label>
                <select value={reason} onChange={e => setReason(e.target.value)}
                  className="input-field">
                  <option>RESTOCK</option>
                  <option>RETURN</option>
                  <option>DAMAGE</option>
                  <option>ADJUSTMENT</option>
                  <option>OTHER</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Note</label>
                <input value={note} onChange={e => setNote(e.target.value)}
                  className="input-field" />
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={() => setAdjusting(null)} className="app-btn-secondary px-4 py-2 text-sm">Cancel</button>
                <button type="submit" disabled={saving}
                  className="app-btn-primary px-4 py-2 text-sm disabled:opacity-50">
                  {saving ? 'Saving...' : 'Apply'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
