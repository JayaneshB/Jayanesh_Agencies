import { useEffect, useState } from 'react';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import api from '../lib/api';
import Loader from '../components/Loader';

export default function Categories() {
  const [categories, setCategories] = useState([]);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [editing, setEditing] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const load = () => api.get('/admin/categories').then(r => { setCategories(r.data); setLoading(false); });
  useEffect(() => { load(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      if (editing) {
        await api.put(`/admin/categories/${editing}`, { name, description: description || null });
      } else {
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
    await api.delete(`/admin/categories/${id}`);
    load();
  };

  if (loading) return <Loader />;

  return (
    <div>
      <h1 className="text-2xl font-bold text-slate-900 mb-6">Categories</h1>

      <div className="bg-white rounded-xl border border-slate-200 p-5 mb-6">
        <form onSubmit={handleSubmit} className="flex gap-3 items-end">
          <div className="flex-1">
            <label className="block text-sm font-medium text-slate-700 mb-1">Name</label>
            <input value={name} onChange={e => setName(e.target.value)} required
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          </div>
          <div className="flex-1">
            <label className="block text-sm font-medium text-slate-700 mb-1">Description</label>
            <input value={description} onChange={e => setDescription(e.target.value)}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          </div>
          <button type="submit"
            className="flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 cursor-pointer">
            <Plus size={16} /> {editing ? 'Update' : 'Add'}
          </button>
          {editing && (
            <button type="button" onClick={() => { setEditing(null); setName(''); setDescription(''); }}
              className="px-4 py-2 text-sm border rounded-lg hover:bg-slate-50 cursor-pointer">Cancel</button>
          )}
        </form>
        {error && <div className="mt-3 bg-red-50 text-red-600 text-sm rounded-lg p-3">{error}</div>}
      </div>

      <div className="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-slate-50 text-left text-slate-600">
              <th className="px-5 py-3 font-medium">Name</th>
              <th className="px-5 py-3 font-medium">Description</th>
              <th className="px-5 py-3 font-medium text-center">Active</th>
              <th className="px-5 py-3 font-medium text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {categories.map(c => (
              <tr key={c.id} className="hover:bg-slate-50">
                <td className="px-5 py-3 font-medium text-slate-900">{c.name}</td>
                <td className="px-5 py-3 text-slate-500">{c.description || '—'}</td>
                <td className="px-5 py-3 text-center">
                  <span className={`inline-block w-2.5 h-2.5 rounded-full ${c.isActive ? 'bg-green-500' : 'bg-slate-300'}`} />
                </td>
                <td className="px-5 py-3 text-right space-x-1">
                  <button onClick={() => startEdit(c)} className="p-1.5 text-slate-400 hover:text-indigo-600 cursor-pointer"><Pencil size={15} /></button>
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
