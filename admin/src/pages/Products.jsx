import { useEffect, useState, useCallback } from 'react';
import { Plus, Search, Pencil, Trash2 } from 'lucide-react';
import api from '../lib/api';
import Loader from '../components/Loader';
import ProductModal from '../components/ProductModal';

export default function Products() {
  const [products, setProducts] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState('');
  const [editing, setEditing] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    const params = { page, size: 15 };
    if (search) params.search = search;
    const { data } = await api.get('/admin/products', { params });
    setProducts(data.content);
    setTotalPages(data.totalPages);
    setLoading(false);
  }, [page, search]);

  useEffect(() => { load(); }, [load]);

  if (loading) return <Loader />;

  const handleDelete = async (id) => {
    if (!confirm('Deactivate this product?')) return;
    await api.delete(`/admin/products/${id}`);
    load();
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-slate-900">Products</h1>
        <button
          onClick={() => { setEditing(null); setShowModal(true); }}
          className="flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 transition-colors cursor-pointer"
        >
          <Plus size={16} /> Add Product
        </button>
      </div>

      <div className="relative mb-4">
        <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
        <input
          type="text"
          placeholder="Search products..."
          value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(0); }}
          className="w-full pl-10 pr-4 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
      </div>

      <div className="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-slate-50 text-left text-slate-600">
                <th className="px-5 py-3 font-medium">Name</th>
                <th className="px-5 py-3 font-medium">Category</th>
                <th className="px-5 py-3 font-medium text-right">Price (min)</th>
                <th className="px-5 py-3 font-medium text-center">Stock</th>
                <th className="px-5 py-3 font-medium text-center">Active</th>
                <th className="px-5 py-3 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {products.map((p) => (
                <tr key={p.id} className="hover:bg-slate-50">
                  <td className="px-5 py-3 font-medium text-slate-900">{p.name}</td>
                  <td className="px-5 py-3 text-slate-500">{p.categoryName || '—'}</td>
                  <td className="px-5 py-3 text-right">
                    ₹{p.pricingTiers?.[0]?.price?.toLocaleString() || '—'}
                  </td>
                  <td className="px-5 py-3 text-center">
                    <span className={p.inventory?.available <= (p.inventory?.reorderThreshold || 0) ? 'text-red-600 font-semibold' : ''}>
                      {p.inventory?.available ?? '—'}
                    </span>
                  </td>
                  <td className="px-5 py-3 text-center">
                    <span className={`inline-block w-2.5 h-2.5 rounded-full ${p.isActive ? 'bg-green-500' : 'bg-slate-300'}`} />
                  </td>
                  <td className="px-5 py-3 text-right space-x-1">
                    <button onClick={() => { setEditing(p); setShowModal(true); }} className="p-1.5 text-slate-400 hover:text-indigo-600 cursor-pointer"><Pencil size={15} /></button>
                    <button onClick={() => handleDelete(p.id)} className="p-1.5 text-slate-400 hover:text-red-600 cursor-pointer"><Trash2 size={15} /></button>
                  </td>
                </tr>
              ))}
              {products.length === 0 && (
                <tr><td colSpan={6} className="px-5 py-8 text-center text-slate-500">No products found.</td></tr>
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

      {showModal && (
        <ProductModal
          product={editing}
          onClose={() => setShowModal(false)}
          onSaved={() => { setShowModal(false); load(); }}
        />
      )}
    </div>
  );
}
