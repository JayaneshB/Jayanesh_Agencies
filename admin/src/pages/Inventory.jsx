import { useEffect, useState } from 'react';
import { AlertTriangle } from 'lucide-react';
import api from '../lib/api';
import Loader from '../components/Loader';

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
    const url = showLow ? '/admin/inventory/low-stock' : '/admin/inventory';
    api.get(url).then(r => { setInventory(r.data); setLoading(false); });
  };

  useEffect(() => { load(); }, [showLow]);

  const handleAdjust = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
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
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-slate-900">Inventory</h1>
        <button
          onClick={() => setShowLow(!showLow)}
          className={`flex items-center gap-2 px-4 py-2 text-sm font-medium rounded-lg border cursor-pointer ${showLow ? 'bg-red-50 border-red-200 text-red-700' : 'hover:bg-slate-50'}`}
        >
          <AlertTriangle size={16} /> {showLow ? 'Show All' : 'Low Stock Only'}
        </button>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-slate-50 text-left text-slate-600">
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
                <tr key={inv.id} className="hover:bg-slate-50">
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
                      className="px-3 py-1 text-xs bg-indigo-50 text-indigo-700 rounded-lg hover:bg-indigo-100 cursor-pointer">
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
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-sm m-4 p-6">
            <h2 className="text-lg font-semibold mb-4">Adjust Stock</h2>
            <form onSubmit={handleAdjust} className="space-y-3">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Quantity (+/-)</label>
                <input type="number" value={qty} onChange={e => setQty(e.target.value)} required
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  placeholder="e.g. 50 or -10" />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Reason</label>
                <select value={reason} onChange={e => setReason(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
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
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={() => setAdjusting(null)} className="px-4 py-2 text-sm border rounded-lg hover:bg-slate-50 cursor-pointer">Cancel</button>
                <button type="submit" disabled={saving}
                  className="px-4 py-2 text-sm bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50 cursor-pointer">
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
