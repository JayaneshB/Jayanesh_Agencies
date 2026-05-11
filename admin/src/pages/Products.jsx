import { useEffect, useState, useCallback } from 'react';
import { Plus, Search, Pencil, Trash2 } from 'lucide-react';
import api from '../lib/api';
import Loader from '../components/Loader';
import ProductModal from '../components/ProductModal';
import { getMockProductsPage, isMockModeEnabled, deleteMockProduct } from '../lib/mockData';

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
    try {
      if (isMockModeEnabled()) {
        const data = await getMockProductsPage({ page, size: 15, search });
        setProducts(data.content);
        setTotalPages(data.totalPages);
        return;
      }

      const params = { page, size: 15 };
      if (search) params.search = search;
      const { data } = await api.get('/admin/products', { params });
      setProducts(data.content);
      setTotalPages(data.totalPages);
    } finally {
      setLoading(false);
    }
  }, [page, search]);

  useEffect(() => { load(); }, [load]);

  if (loading) return <Loader />;

  const handleDelete = async (id) => {
    if (!confirm('Deactivate this product?')) return;
    if (isMockModeEnabled()) {
      await deleteMockProduct(id);
      load();
      return;
    }
    await api.delete(`/admin/products/${id}`);
    load();
  };

  return (
    <div className="space-y-6">
      <div className="app-surface flex flex-wrap items-center justify-between gap-4 px-6 py-5">
        <div>
          <p className="text-sm font-medium uppercase tracking-[0.18em] text-emerald-700">Catalog</p>
          <h1 className="mt-2 text-3xl font-semibold tracking-tight text-slate-900">Products</h1>
          <p className="mt-2 text-sm text-slate-500">Add, edit, and review product inventory with the same design language across the panel.</p>
        </div>
        <button
          onClick={() => { setEditing(null); setShowModal(true); }}
          className="app-btn-primary"
        >
          <Plus size={16} /> Add Product
        </button>
      </div>

      <div className="relative">
        <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
        <input
          type="text"
          placeholder="Search products..."
          value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(0); }}
          className="input-field !rounded-full !pl-10"
        />
      </div>

      <div className="app-table-card">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="app-table-head">
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
                <tr key={p.id} className="app-table-row">
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
                    <button onClick={() => { setEditing(p); setShowModal(true); }} className="p-1.5 text-slate-400 hover:text-emerald-600 cursor-pointer"><Pencil size={15} /></button>
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
          <button disabled={page === 0} onClick={() => setPage(p => p - 1)} className="app-btn-secondary px-4 py-2 text-sm disabled:opacity-40">Prev</button>
          <span className="text-sm text-slate-500">Page {page + 1} of {totalPages}</span>
          <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)} className="app-btn-secondary px-4 py-2 text-sm disabled:opacity-40">Next</button>
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
