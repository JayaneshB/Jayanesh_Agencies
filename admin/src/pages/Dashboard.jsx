import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowUpRight, Boxes, DollarSign, PackageCheck, ShoppingCart, ShieldAlert, Truck } from 'lucide-react';
import api from '../lib/api';
import Loader from '../components/Loader';
import { BarListCard, DonutCard, MetricCard, TrendChartCard } from '../components/DashboardVisuals';
import { getMockDashboardPayload, isMockModeEnabled } from '../lib/mockData';

function toIsoDate(value) {
  return new Date(value).toISOString().slice(0, 10);
}

function buildTrendPoints(orders) {
  const today = new Date();
  const days = Array.from({ length: 7 }, (_, index) => {
    const date = new Date(today);
    date.setDate(today.getDate() - (6 - index));
    return date;
  });

  const totals = new Map(days.map((date) => [toIsoDate(date), 0]));

  orders.forEach((order) => {
    const key = toIsoDate(order.createdAt);
    if (totals.has(key)) {
      totals.set(key, totals.get(key) + Number(order.totalAmount || 0));
    }
  });

  return days.map((date) => ({
    label: new Intl.DateTimeFormat('en-US', { weekday: 'short' }).format(date),
    value: totals.get(toIsoDate(date)) || 0,
  }));
}

function buildStatusItems(orders) {
  const palette = {
    PENDING_PAYMENT: '#d97706',
    CONFIRMED: '#0f766e',
    PROCESSING: '#0ea5e9',
    PACKED: '#7c3aed',
    SHIPPED: '#14b8a6',
    OUT_FOR_DELIVERY: '#f59e0b',
    DELIVERED: '#16a34a',
    CANCELLED: '#ef4444',
  };

  const counts = orders.reduce((acc, order) => {
    acc[order.status] = (acc[order.status] || 0) + 1;
    return acc;
  }, {});

  return Object.entries(counts)
    .map(([status, value]) => ({
      label: status.replace(/_/g, ' '),
      value,
      color: palette[status] || '#64748b',
    }))
    .sort((a, b) => b.value - a.value);
}

export default function Dashboard() {
  const navigate = useNavigate();
  const [summary, setSummary] = useState(null);
  const [recentOrders, setRecentOrders] = useState([]);
  const [inventory, setInventory] = useState([]);
  const [categories, setCategories] = useState([]);
  const [productsTotal, setProductsTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let mounted = true;

    async function loadDashboard() {
      try {
        if (isMockModeEnabled()) {
          const mock = await getMockDashboardPayload();
          if (!mounted) return;
          setSummary(mock.summary);
          setRecentOrders(mock.recentOrders);
          setInventory(mock.inventory);
          setCategories(mock.categories);
          setProductsTotal(mock.productsTotal);
          return;
        }

        const [summaryRes, recentOrdersRes, inventoryRes, categoriesRes, productsRes] = await Promise.all([
          api.get('/admin/dashboard/summary'),
          api.get('/admin/dashboard/recent-orders'),
          api.get('/admin/inventory'),
          api.get('/admin/categories'),
          api.get('/admin/products', { params: { page: 0, size: 1 } }),
        ]);

        if (!mounted) return;

        setSummary(summaryRes.data);
        setRecentOrders(recentOrdersRes.data);
        setInventory(inventoryRes.data);
        setCategories(categoriesRes.data);
        setProductsTotal(productsRes.data?.totalElements || 0);
      } catch (err) {
        if (!mounted) return;
        setError(err.response?.data?.message || 'Unable to load dashboard data');
      } finally {
        if (mounted) setLoading(false);
      }
    }

    loadDashboard();
    return () => {
      mounted = false;
    };
  }, []);

  const metrics = useMemo(() => {
    if (!summary) return [];

    const activeCategories = categories.filter((category) => category.isActive !== false).length;
    const lowStockItems = inventory.filter((item) => item.available <= item.reorderThreshold);
    const inventoryUnits = inventory.reduce((acc, item) => acc + Number(item.available || 0), 0);

    return [
      {
        title: "Today's Revenue",
        value: `₹${Number(summary.todayRevenue || 0).toLocaleString()}`,
        hint: 'Net sales recorded from confirmed orders',
        icon: DollarSign,
        accent: 'emerald',
        change: `${summary.todayOrderCount} orders today`,
      },
      {
        title: "Today's Orders",
        value: summary.todayOrderCount,
        hint: 'Orders created in the current UTC day',
        icon: ShoppingCart,
        accent: 'teal',
        change: `${summary.pendingOrders} pending`,
      },
      {
        title: 'Products',
        value: productsTotal,
        hint: `${activeCategories} active categories`,
        icon: Boxes,
        accent: 'slate',
        change: `${inventory.length} inventory records`,
      },
      {
        title: 'Low Stock Items',
        value: summary.lowStockSkuCount,
        hint: `${lowStockItems.length} items currently flagged`,
        icon: ShieldAlert,
        accent: 'red',
        change: `${inventoryUnits.toLocaleString()} units available`,
      },
      {
        title: 'Out for Delivery',
        value: summary.outForDeliveryCount,
        hint: 'Orders active in the delivery queue',
        icon: Truck,
        accent: 'amber',
        change: 'Needs daily follow-up',
      },
      {
        title: 'Categories',
        value: categories.length,
        hint: 'Operational product groups',
        icon: PackageCheck,
        accent: 'emerald',
        change: `${activeCategories} active`,
      },
    ];
  }, [summary, categories, inventory, productsTotal]);

  const trendPoints = useMemo(() => buildTrendPoints(recentOrders), [recentOrders]);
  const statusItems = useMemo(() => buildStatusItems(recentOrders), [recentOrders]);
  const lowStockItems = useMemo(
    () =>
      inventory
        .filter((item) => item.available <= item.reorderThreshold)
        .sort((a, b) => a.available - b.available)
        .slice(0, 6)
        .map((item) => ({
          label: item.productName,
          value: Number(item.available || 0),
          valueLabel: `${item.available} left`,
          meta: `Reorder at ${item.reorderThreshold} • MOQ ${item.moq}`,
        })),
    [inventory]
  );

  const orderRows = recentOrders.slice(0, 8);

  if (loading) return <Loader />;

  if (error) {
    return (
      <div className="app-surface p-6">
        <h1 className="app-heading">Dashboard</h1>
        <p className="mt-2 text-sm text-red-600">{error}</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <section className="app-surface flex flex-wrap items-center justify-between gap-4 px-6 py-5">
        <div>
          <p className="text-sm font-medium uppercase tracking-[0.18em] text-emerald-700">Dashboard</p>
          <h1 className="mt-2 text-3xl font-semibold tracking-tight text-slate-900">Operations at a glance</h1>
          <p className="mt-2 text-sm text-slate-500">Monitor orders, stock, and product health with the same green theme across the admin panel.</p>
        </div>

        <div className="flex flex-wrap gap-3">
          <button onClick={() => navigate('/products')} className="app-btn-primary">
            <ArrowUpRight size={16} /> Add Product
          </button>
          <button onClick={() => navigate('/inventory')} className="app-btn-secondary">Import Data</button>
        </div>
      </section>

      <section className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
        {metrics.map((metric) => (
          <MetricCard key={metric.title} {...metric} />
        ))}
      </section>

      <section className="grid grid-cols-1 gap-6 xl:grid-cols-[1.35fr_0.95fr]">
        <TrendChartCard
          title="Revenue and order activity"
          subtitle="Based on the latest seven days of recent orders"
          points={trendPoints}
          footerNote="This chart is calculated from the most recent orders returned by the dashboard API."
        />

        <DonutCard
          title="Recent order mix"
          subtitle="Status distribution from recent orders"
          items={statusItems}
          centerValue={recentOrders.length}
          centerLabel="Recent Orders"
        />
      </section>

      <section className="grid grid-cols-1 gap-6 xl:grid-cols-[1.15fr_0.85fr]">
        <div className="app-surface overflow-hidden">
          <div className="app-card-head">
            <div>
              <h2 className="app-card-title">Recent Orders</h2>
              <p className="text-xs text-slate-500">Latest transactions surfaced by the backend</p>
            </div>
          </div>

          {orderRows.length === 0 ? (
            <p className="p-5 text-sm text-slate-500">No orders yet.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="app-table-head">
                    <th className="px-5 py-3 font-medium">Order ID</th>
                    <th className="px-5 py-3 font-medium">Customer</th>
                    <th className="px-5 py-3 font-medium">Status</th>
                    <th className="px-5 py-3 font-medium text-right">Amount</th>
                    <th className="px-5 py-3 font-medium">Date</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {orderRows.map((order) => (
                    <tr key={order.id} className="app-table-row">
                      <td className="px-5 py-3 font-mono text-xs text-slate-600">{order.id.slice(0, 8)}...</td>
                      <td className="px-5 py-3 text-slate-700">{order.customerName || '—'}</td>
                      <td className="px-5 py-3">
                        <span className={`inline-block rounded-full px-2.5 py-1 text-xs font-medium ${
                          order.status === 'DELIVERED'
                            ? 'bg-emerald-50 text-emerald-700'
                            : order.status === 'CANCELLED'
                              ? 'bg-red-50 text-red-700'
                              : order.status === 'OUT_FOR_DELIVERY'
                                ? 'bg-amber-50 text-amber-700'
                                : 'bg-slate-100 text-slate-700'
                        }`}>
                          {order.status.replace(/_/g, ' ')}
                        </span>
                      </td>
                      <td className="px-5 py-3 text-right font-medium text-slate-800">₹{Number(order.totalAmount || 0).toLocaleString()}</td>
                      <td className="px-5 py-3 text-slate-500">{new Date(order.createdAt).toLocaleDateString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <BarListCard
          title="Inventory watchlist"
          subtitle="Products at or below their reorder threshold"
          items={lowStockItems}
          emptyLabel="No stock alerts right now."
        />
      </section>
    </div>
  );
}
