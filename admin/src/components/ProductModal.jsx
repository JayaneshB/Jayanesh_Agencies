import { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import api from '../lib/api';

export default function ProductModal({ product, onClose, onSaved }) {
  const isEdit = !!product;
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState({
    name: '', description: '', categoryId: '', hsnCode: '', taxRate: '0',
    isActive: true, stock: '0', reorderThreshold: '0', moq: '1',
    pricingTiers: [{ minQty: '1', maxQty: '', price: '' }],
    imageUrls: [''],
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/admin/categories').then(r => setCategories(r.data));
    if (product) {
      setForm({
        name: product.name || '',
        description: product.description || '',
        categoryId: product.categoryId || '',
        hsnCode: product.hsnCode || '',
        taxRate: String(product.taxRate ?? '0'),
        isActive: product.isActive ?? true,
        stock: String(product.inventory?.totalStock ?? '0'),
        reorderThreshold: String(product.inventory?.reorderThreshold ?? '0'),
        moq: String(product.inventory?.moq ?? '1'),
        pricingTiers: product.pricingTiers?.length
          ? product.pricingTiers.map(t => ({ minQty: String(t.minQty), maxQty: t.maxQty != null ? String(t.maxQty) : '', price: String(t.price) }))
          : [{ minQty: '1', maxQty: '', price: '' }],
        imageUrls: product.images?.length ? product.images.map(i => i.url) : [''],
      });
    }
  }, [product]);

  const set = (field, value) => setForm(f => ({ ...f, [field]: value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const body = {
        name: form.name,
        description: form.description || null,
        categoryId: form.categoryId || null,
        hsnCode: form.hsnCode || null,
        taxRate: parseFloat(form.taxRate) || 0,
        isActive: form.isActive,
        stock: parseInt(form.stock) || 0,
        reorderThreshold: parseInt(form.reorderThreshold) || 0,
        moq: parseInt(form.moq) || 1,
        pricingTiers: form.pricingTiers.filter(t => t.price).map(t => ({
          minQty: parseInt(t.minQty) || 1,
          maxQty: t.maxQty ? parseInt(t.maxQty) : null,
          price: parseFloat(t.price),
        })),
        imageUrls: form.imageUrls.filter(Boolean),
      };
      if (isEdit) {
        await api.put(`/admin/products/${product.id}`, body);
      } else {
        await api.post('/admin/products', body);
      }
      onSaved();
    } catch (err) {
      setError(err.response?.data?.message || 'Save failed');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg max-h-[90vh] overflow-y-auto m-4">
        <div className="flex items-center justify-between px-6 py-4 border-b">
          <h2 className="text-lg font-semibold">{isEdit ? 'Edit Product' : 'New Product'}</h2>
          <button onClick={onClose} className="p-1 text-slate-400 hover:text-slate-600 cursor-pointer"><X size={20} /></button>
        </div>

        {error && <div className="mx-6 mt-4 bg-red-50 text-red-600 text-sm rounded-lg p-3">{error}</div>}

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <Field label="Name" required>
            <input value={form.name} onChange={e => set('name', e.target.value)} required className="input-field" />
          </Field>
          <Field label="Description">
            <textarea value={form.description} onChange={e => set('description', e.target.value)} rows={2} className="input-field" />
          </Field>
          <div className="grid grid-cols-2 gap-4">
            <Field label="Category">
              <select value={form.categoryId} onChange={e => set('categoryId', e.target.value)} className="input-field">
                <option value="">None</option>
                {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </Field>
            <Field label="Tax Rate (%)">
              <input type="number" step="0.01" value={form.taxRate} onChange={e => set('taxRate', e.target.value)} className="input-field" />
            </Field>
          </div>
          <div className="grid grid-cols-3 gap-4">
            <Field label="Stock">
              <input type="number" value={form.stock} onChange={e => set('stock', e.target.value)} className="input-field" />
            </Field>
            <Field label="Reorder At">
              <input type="number" value={form.reorderThreshold} onChange={e => set('reorderThreshold', e.target.value)} className="input-field" />
            </Field>
            <Field label="MOQ">
              <input type="number" value={form.moq} onChange={e => set('moq', e.target.value)} className="input-field" />
            </Field>
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Pricing Tiers</label>
            {form.pricingTiers.map((tier, i) => (
              <div key={i} className="flex gap-2 mb-2">
                <input placeholder="Min Qty" type="number" value={tier.minQty} onChange={e => {
                  const tiers = [...form.pricingTiers]; tiers[i].minQty = e.target.value; set('pricingTiers', tiers);
                }} className="input-field flex-1" />
                <input placeholder="Max Qty" type="number" value={tier.maxQty} onChange={e => {
                  const tiers = [...form.pricingTiers]; tiers[i].maxQty = e.target.value; set('pricingTiers', tiers);
                }} className="input-field flex-1" />
                <input placeholder="Price" type="number" step="0.01" value={tier.price} onChange={e => {
                  const tiers = [...form.pricingTiers]; tiers[i].price = e.target.value; set('pricingTiers', tiers);
                }} className="input-field flex-1" />
              </div>
            ))}
            <button type="button" onClick={() => set('pricingTiers', [...form.pricingTiers, { minQty: '', maxQty: '', price: '' }])}
              className="text-sm text-indigo-600 hover:underline cursor-pointer">+ Add tier</button>
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Image URLs</label>
            {form.imageUrls.map((url, i) => (
              <input key={i} placeholder="https://..." value={url} onChange={e => {
                const urls = [...form.imageUrls]; urls[i] = e.target.value; set('imageUrls', urls);
              }} className="input-field mb-2" />
            ))}
            <button type="button" onClick={() => set('imageUrls', [...form.imageUrls, ''])}
              className="text-sm text-indigo-600 hover:underline cursor-pointer">+ Add image</button>
          </div>

          <div className="flex items-center gap-2">
            <input type="checkbox" checked={form.isActive} onChange={e => set('isActive', e.target.checked)} id="active" />
            <label htmlFor="active" className="text-sm text-slate-700">Active</label>
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={onClose} className="px-4 py-2 text-sm border rounded-lg hover:bg-slate-50 cursor-pointer">Cancel</button>
            <button type="submit" disabled={saving}
              className="px-4 py-2 text-sm bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50 cursor-pointer">
              {saving ? 'Saving...' : isEdit ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function Field({ label, required, children }) {
  return (
    <div>
      <label className="block text-sm font-medium text-slate-700 mb-1">
        {label}{required && <span className="text-red-500 ml-0.5">*</span>}
      </label>
      {children}
    </div>
  );
}
