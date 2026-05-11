import { useEffect, useState } from 'react';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import api from '../lib/api';
import Loader from '../components/Loader';
import { deactivateMockCategory, getMockCategories, isMockModeEnabled, saveMockCategory } from '../lib/mockData';

export default function Categories() {
  const [categories, setCategories] = useState([]);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [editing, setEditing] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const load = async () => {
    if (isMockModeEnabled()) {
      setCategories(await getMockCategories());
      setLoading(false);
      return;
    }
    const r = await api.get('/admin/categories');
    setCategories(r.data);
    setLoading(false);
  };
  useEffect(() => { load(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      if (editing) {
        if (isMockModeEnabled()) {
          await saveMockCategory(editing, { name, description: description || null });
          setName(''); setDescription(''); setEditing(null);
          load();
          return;
        }
        await api.put(`/admin/categories/${editing}`, { name, description: description || null });
      } else {
        if (isMockModeEnabled()) {
          await saveMockCategory(null, { name, description: description || null });
          setName(''); setDescription(''); setEditing(null);
          load();
          return;
        }
        await api.post('/admin/categories', { name, description: description || null });
      }
      setName(''); setDescription(''); setEditing(null);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Save failed');
    }
  };

  const startEdit = (cat) => {
    setEditing(cat.id);
    setName(cat.name);
    setDescription(cat.description || '');
  };

  const handleDelete = async (id) => {
    if (!confirm('Deactivate this category?')) return;
    if (isMockModeEnabled()) {
      await deactivateMockCategory(id);
      load();
      return;
    }
    await api.delete(`/admin/categories/${id}`);
    load();
  };

  if (loading) return <Loader />;

  return (
    <div className="space-y-6">
      <div className="app-surface px-6 py-5">
        <p className="text-sm font-medium uppercase tracking-[0.18em] text-emerald-700">Catalog</p>
        <h1 className="mt-2 text-3xl font-semibold tracking-tight text-slate-900">Categories</h1>
        <p className="mt-2 text-sm text-slate-500">Manage the category groups that organize your product catalog.</p>
      </div>

      <div className="app-surface p-5">
        <form onSubmit={handleSubmit} className="flex gap-3 items-end">
          <div className="flex-1">
            <label className="block text-sm font-medium text-slate-700 mb-1">Name</label>
            <input value={name} onChange={e => setName(e.target.value)} required
              className="input-field" />
          </div>
          <div className="flex-1">
            <label className="block text-sm font-medium text-slate-700 mb-1">Description</label>
            <input value={description} onChange={e => setDescription(e.target.value)}
              className="input-field" />
          </div>
          <button type="submit"
            className="app-btn-primary">
            <Plus size={16} /> {editing ? 'Update' : 'Add'}
          </button>
          {editing && (
            <button type="button" onClick={() => { setEditing(null); setName(''); setDescription(''); }}
              className="app-btn-secondary">Cancel</button>
          )}
        </form>
        {error && <div className="mt-3 rounded-2xl border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-600">{error}</div>}
      </div>

      <div className="app-table-card">
        <table className="w-full text-sm">
          <thead>
            <tr className="app-table-head">
              <th className="px-5 py-3 font-medium">Name</th>
              <th className="px-5 py-3 font-medium">Description</th>
              <th className="px-5 py-3 font-medium text-center">Active</th>
              <th className="px-5 py-3 font-medium text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {categories.map(c => (
              <tr key={c.id} className="app-table-row">
                <td className="px-5 py-3 font-medium text-slate-900">{c.name}</td>
                <td className="px-5 py-3 text-slate-500">{c.description || '—'}</td>
                <td className="px-5 py-3 text-center">
                  <span className={`inline-block w-2.5 h-2.5 rounded-full ${c.isActive ? 'bg-green-500' : 'bg-slate-300'}`} />
                </td>
                <td className="px-5 py-3 text-right space-x-1">
                  <button onClick={() => startEdit(c)} className="p-1.5 text-slate-400 hover:text-emerald-600 cursor-pointer"><Pencil size={15} /></button>
                  <button onClick={() => handleDelete(c.id)} className="p-1.5 text-slate-400 hover:text-red-600 cursor-pointer"><Trash2 size={15} /></button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
