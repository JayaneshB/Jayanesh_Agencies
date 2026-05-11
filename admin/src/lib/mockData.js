const MOCK_MODE = import.meta.env.VITE_USE_MOCK_DATA === 'true';
const MOCK_MODE_STORAGE_KEY = 'admin.mockDataMode';

const clone = (value) => JSON.parse(JSON.stringify(value));
const wait = (ms = 180) => new Promise((resolve) => setTimeout(resolve, ms));

function readMockModeOverride() {
  if (typeof window === 'undefined') return null;

  const stored = window.localStorage.getItem(MOCK_MODE_STORAGE_KEY);
  if (stored === 'true') return true;
  if (stored === 'false') return false;
  return null;
}

function daysAgo(days, hour = 10, minute = 0) {
  const date = new Date();
  date.setUTCDate(date.getUTCDate() - days);
  date.setUTCHours(hour, minute, 0, 0);
  return date.toISOString();
}

function todayUtcKey() {
  return new Date().toISOString().slice(0, 10);
}

const seedCategories = [
  { id: 'cat-choco', name: 'Chocolates', description: 'Bars, spreads, and gift boxes', isActive: true },
  { id: 'cat-bakery', name: 'Bakery', description: 'Cookies, rusks, and biscuits', isActive: true },
  { id: 'cat-sweets', name: 'Sweets', description: 'Traditional and festive sweets', isActive: true },
  { id: 'cat-pack', name: 'Packaging', description: 'Boxes, wrappers, and gift packs', isActive: true },
];

const seedProducts = [
  {
    id: 'prod-choco-box',
    name: 'Assorted Chocolate Box',
    description: 'Premium mixed chocolate gift box',
    categoryId: 'cat-choco',
    hsnCode: '1806',
    taxRate: 12,
    isActive: true,
    stock: 64,
    reorderThreshold: 18,
    moq: 4,
    pricingTiers: [
      { minQty: 1, maxQty: 9, price: 350 },
      { minQty: 10, maxQty: 49, price: 320 },
      { minQty: 50, maxQty: null, price: 295 },
    ],
    imageUrls: ['/login-bg.jpg'],
  },
  {
    id: 'prod-dark-bites',
    name: 'Dark Chocolate Bites',
    description: 'Individually wrapped mini bites',
    categoryId: 'cat-choco',
    hsnCode: '1806',
    taxRate: 12,
    isActive: true,
    stock: 120,
    reorderThreshold: 30,
    moq: 12,
    pricingTiers: [
      { minQty: 1, maxQty: 24, price: 180 },
      { minQty: 25, maxQty: 99, price: 165 },
      { minQty: 100, maxQty: null, price: 149 },
    ],
    imageUrls: ['/login-bg.jpg'],
  },
  {
    id: 'prod-butter-cookies',
    name: 'Butter Cookies',
    description: 'Crispy wholesale cookie packs',
    categoryId: 'cat-bakery',
    hsnCode: '1905',
    taxRate: 5,
    isActive: true,
    stock: 88,
    reorderThreshold: 24,
    moq: 8,
    pricingTiers: [
      { minQty: 1, maxQty: 19, price: 120 },
      { minQty: 20, maxQty: 79, price: 110 },
      { minQty: 80, maxQty: null, price: 98 },
    ],
    imageUrls: ['/login-bg.jpg'],
  },
  {
    id: 'prod-kaju-katli',
    name: 'Kaju Katli',
    description: 'Festive sweet slab boxes',
    categoryId: 'cat-sweets',
    hsnCode: '1704',
    taxRate: 5,
    isActive: true,
    stock: 42,
    reorderThreshold: 12,
    moq: 2,
    pricingTiers: [
      { minQty: 1, maxQty: 9, price: 540 },
      { minQty: 10, maxQty: 39, price: 495 },
      { minQty: 40, maxQty: null, price: 450 },
    ],
    imageUrls: ['/login-bg.jpg'],
  },
  {
    id: 'prod-gift-box',
    name: 'Festival Gift Box',
    description: 'Luxury combo hamper for corporate orders',
    categoryId: 'cat-pack',
    hsnCode: '4819',
    taxRate: 18,
    isActive: true,
    stock: 26,
    reorderThreshold: 10,
    moq: 1,
    pricingTiers: [
      { minQty: 1, maxQty: 9, price: 620 },
      { minQty: 10, maxQty: 24, price: 575 },
      { minQty: 25, maxQty: null, price: 540 },
    ],
    imageUrls: ['/login-bg.jpg'],
  },
  {
    id: 'prod-rusk',
    name: 'Premium Rusk Pack',
    description: 'Tea-time premium rusk bundles',
    categoryId: 'cat-bakery',
    hsnCode: '1905',
    taxRate: 5,
    isActive: true,
    stock: 18,
    reorderThreshold: 20,
    moq: 6,
    pricingTiers: [
      { minQty: 1, maxQty: 19, price: 72 },
      { minQty: 20, maxQty: 99, price: 66 },
      { minQty: 100, maxQty: null, price: 60 },
    ],
    imageUrls: ['/login-bg.jpg'],
  },
  {
    id: 'prod-peanut-chikki',
    name: 'Peanut Chikki',
    description: 'Classic sweet snack packs',
    categoryId: 'cat-sweets',
    hsnCode: '1704',
    taxRate: 5,
    isActive: true,
    stock: 74,
    reorderThreshold: 18,
    moq: 10,
    pricingTiers: [
      { minQty: 1, maxQty: 24, price: 90 },
      { minQty: 25, maxQty: 99, price: 84 },
      { minQty: 100, maxQty: null, price: 78 },
    ],
    imageUrls: ['/login-bg.jpg'],
  },
];

const seedInventory = [
  { id: 'inv-1', productId: 'prod-choco-box', totalStock: 64, reserved: 7, sold: 15, available: 42, reorderThreshold: 18, moq: 4 },
  { id: 'inv-2', productId: 'prod-dark-bites', totalStock: 120, reserved: 12, sold: 46, available: 62, reorderThreshold: 30, moq: 12 },
  { id: 'inv-3', productId: 'prod-butter-cookies', totalStock: 88, reserved: 8, sold: 31, available: 49, reorderThreshold: 24, moq: 8 },
  { id: 'inv-4', productId: 'prod-kaju-katli', totalStock: 42, reserved: 4, sold: 18, available: 20, reorderThreshold: 12, moq: 2 },
  { id: 'inv-5', productId: 'prod-gift-box', totalStock: 26, reserved: 5, sold: 11, available: 10, reorderThreshold: 10, moq: 1 },
  { id: 'inv-6', productId: 'prod-rusk', totalStock: 18, reserved: 2, sold: 6, available: 10, reorderThreshold: 20, moq: 6 },
  { id: 'inv-7', productId: 'prod-peanut-chikki', totalStock: 74, reserved: 6, sold: 18, available: 50, reorderThreshold: 18, moq: 10 },
];

const seedOrders = [
  {
    id: 'ord-1007',
    customerName: 'Asha Traders',
    customerPhone: '9876543210',
    status: 'OUT_FOR_DELIVERY',
    paymentStatus: 'Paid',
    subtotal: 12800,
    taxAmount: 1536,
    deliveryFee: 300,
    totalAmount: 14636,
    createdAt: daysAgo(0, 8, 30),
    items: [
      { id: 'item-1', productName: 'Assorted Chocolate Box', quantity: 8, totalPrice: 2800 },
      { id: 'item-2', productName: 'Festival Gift Box', quantity: 12, totalPrice: 7440 },
    ],
    statusHistory: [
      { fromStatus: 'CONFIRMED', toStatus: 'PROCESSING', reason: '' },
      { fromStatus: 'PROCESSING', toStatus: 'PACKED', reason: '' },
      { fromStatus: 'PACKED', toStatus: 'SHIPPED', reason: '' },
      { fromStatus: 'SHIPPED', toStatus: 'OUT_FOR_DELIVERY', reason: '' },
    ],
  },
  {
    id: 'ord-1006',
    customerName: 'Bharat Retail',
    customerPhone: '9900112233',
    status: 'PROCESSING',
    paymentStatus: 'Paid',
    subtotal: 9400,
    taxAmount: 1128,
    deliveryFee: 250,
    totalAmount: 10778,
    createdAt: daysAgo(0, 11, 10),
    items: [{ id: 'item-3', productName: 'Dark Chocolate Bites', quantity: 40, totalPrice: 6600 }],
    statusHistory: [{ fromStatus: 'CONFIRMED', toStatus: 'PROCESSING', reason: '' }],
  },
  {
    id: 'ord-1005',
    customerName: 'City Wholesalers',
    customerPhone: '9988776655',
    status: 'CONFIRMED',
    paymentStatus: 'Pending',
    subtotal: 6100,
    taxAmount: 366,
    deliveryFee: 200,
    totalAmount: 6666,
    createdAt: daysAgo(1, 13, 0),
    items: [{ id: 'item-4', productName: 'Butter Cookies', quantity: 30, totalPrice: 3300 }],
    statusHistory: [{ fromStatus: 'PENDING_PAYMENT', toStatus: 'CONFIRMED', reason: '' }],
  },
  {
    id: 'ord-1004',
    customerName: 'Dhanalakshmi Mart',
    customerPhone: '9898989898',
    status: 'DELIVERED',
    paymentStatus: 'Paid',
    subtotal: 7600,
    taxAmount: 912,
    deliveryFee: 250,
    totalAmount: 8762,
    createdAt: daysAgo(1, 16, 20),
    items: [{ id: 'item-5', productName: 'Kaju Katli', quantity: 8, totalPrice: 4320 }],
    statusHistory: [{ fromStatus: 'SHIPPED', toStatus: 'DELIVERED', reason: '' }],
  },
  {
    id: 'ord-1003',
    customerName: 'Evergreen Stores',
    customerPhone: '9123456780',
    status: 'PACKED',
    paymentStatus: 'Paid',
    subtotal: 4800,
    taxAmount: 576,
    deliveryFee: 250,
    totalAmount: 5626,
    createdAt: daysAgo(2, 10, 15),
    items: [{ id: 'item-6', productName: 'Premium Rusk Pack', quantity: 50, totalPrice: 3300 }],
    statusHistory: [{ fromStatus: 'PROCESSING', toStatus: 'PACKED', reason: '' }],
  },
  {
    id: 'ord-1002',
    customerName: 'Fresh Mart',
    customerPhone: '9345678901',
    status: 'PENDING_PAYMENT',
    paymentStatus: 'Pending',
    subtotal: 3400,
    taxAmount: 204,
    deliveryFee: 150,
    totalAmount: 3754,
    createdAt: daysAgo(3, 9, 40),
    items: [{ id: 'item-7', productName: 'Peanut Chikki', quantity: 25, totalPrice: 2100 }],
    statusHistory: [],
  },
  {
    id: 'ord-1001',
    customerName: 'Greenfield Retail',
    customerPhone: '9000090000',
    status: 'CANCELLED',
    paymentStatus: 'Refunded',
    subtotal: 5300,
    taxAmount: 318,
    deliveryFee: 0,
    totalAmount: 5618,
    createdAt: daysAgo(4, 14, 55),
    items: [{ id: 'item-8', productName: 'Assorted Chocolate Box', quantity: 10, totalPrice: 3500 }],
    statusHistory: [{ fromStatus: 'CONFIRMED', toStatus: 'CANCELLED', reason: 'Customer requested cancellation' }],
  },
  {
    id: 'ord-1000',
    customerName: 'Heritage Foods',
    customerPhone: '9555001122',
    status: 'DELIVERED',
    paymentStatus: 'Paid',
    subtotal: 15400,
    taxAmount: 1848,
    deliveryFee: 300,
    totalAmount: 17548,
    createdAt: daysAgo(5, 12, 5),
    items: [{ id: 'item-9', productName: 'Festival Gift Box', quantity: 18, totalPrice: 11160 }],
    statusHistory: [{ fromStatus: 'PACKED', toStatus: 'SHIPPED', reason: '' }, { fromStatus: 'SHIPPED', toStatus: 'DELIVERED', reason: '' }],
  },
];

let categories = clone(seedCategories);
let products = clone(seedProducts);
let inventory = clone(seedInventory);
let orders = clone(seedOrders);

function getCategoryName(categoryId) {
  return categories.find((category) => category.id === categoryId)?.name || '—';
}

function getProductName(productId) {
  return products.find((product) => product.id === productId)?.name || '—';
}

function decorateProduct(product) {
  const inventoryItem = inventory.find((item) => item.productId === product.id);
  return {
    ...clone(product),
    categoryName: getCategoryName(product.categoryId),
    inventory: inventoryItem
      ? {
          available: inventoryItem.available,
          totalStock: inventoryItem.totalStock,
          reorderThreshold: inventoryItem.reorderThreshold,
          moq: inventoryItem.moq,
        }
      : null,
    pricingTiers: clone(product.pricingTiers || []),
    images: (product.imageUrls || []).map((url) => ({ url })),
  };
}

function decorateOrder(order) {
  return clone(order);
}

function decorateInventoryItem(item) {
  return {
    ...clone(item),
    productName: getProductName(item.productId),
  };
}

function sortByCreatedAtDesc(left, right) {
  return new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime();
}

function isSameUtcDay(value, key = todayUtcKey()) {
  return new Date(value).toISOString().slice(0, 10) === key;
}

function calculateDashboardSummary() {
  const todayKey = todayUtcKey();
  const todaysOrders = orders.filter((order) => isSameUtcDay(order.createdAt, todayKey) && order.status !== 'CANCELLED');
  const todayRevenue = todaysOrders.reduce((sum, order) => sum + Number(order.totalAmount || 0), 0);

  return {
    todayRevenue,
    todayOrderCount: todaysOrders.length,
    pendingOrders: orders.filter((order) => ['CONFIRMED', 'PROCESSING'].includes(order.status)).length,
    outForDeliveryCount: orders.filter((order) => order.status === 'OUT_FOR_DELIVERY').length,
    lowStockSkuCount: inventory.filter((item) => item.available <= item.reorderThreshold).length,
  };
}

export function isMockModeEnabled() {
  const override = readMockModeOverride();
  return override ?? MOCK_MODE;
}

export function setMockModeEnabled(enabled) {
  if (typeof window === 'undefined') return;
  window.localStorage.setItem(MOCK_MODE_STORAGE_KEY, enabled ? 'true' : 'false');
}

export function clearMockModeOverride() {
  if (typeof window === 'undefined') return;
  window.localStorage.removeItem(MOCK_MODE_STORAGE_KEY);
}

export async function getMockDashboardPayload() {
  await wait();
  return {
    summary: calculateDashboardSummary(),
    recentOrders: clone(orders).sort(sortByCreatedAtDesc).slice(0, 20).map(decorateOrder),
    inventory: clone(inventory).map(decorateInventoryItem),
    categories: clone(categories),
    productsTotal: products.length,
  };
}

export async function getMockProductsPage({ page = 0, size = 15, search = '' } = {}) {
  await wait();
  const query = search.trim().toLowerCase();
  const filtered = products
    .filter((product) => {
      if (!query) return true;
      return [product.name, product.description, getCategoryName(product.categoryId), product.hsnCode]
        .filter(Boolean)
        .some((field) => String(field).toLowerCase().includes(query));
    })
    .map(decorateProduct)
    .sort((a, b) => a.name.localeCompare(b.name));

  const totalElements = filtered.length;
  const totalPages = Math.max(1, Math.ceil(totalElements / size));
  const start = page * size;
  const content = filtered.slice(start, start + size);

  return { content, totalPages, totalElements, number: page };
}

export async function getMockCategories() {
  await wait();
  return clone(categories);
}

export async function saveMockCategory(id, payload) {
  await wait();
  if (id) {
    categories = categories.map((category) =>
      category.id === id
        ? { ...category, name: payload.name, description: payload.description ?? null }
        : category
    );
  } else {
    categories = [
      ...categories,
      {
        id: `cat-${Date.now()}`,
        name: payload.name,
        description: payload.description ?? null,
        isActive: true,
      },
    ];
  }
  return clone(categories);
}

export async function deactivateMockCategory(id) {
  await wait();
  categories = categories.map((category) =>
    category.id === id ? { ...category, isActive: false } : category
  );
}

export async function getMockInventory({ lowOnly = false } = {}) {
  await wait();
  const list = clone(inventory).map(decorateInventoryItem);
  const filtered = lowOnly ? list.filter((item) => item.available <= item.reorderThreshold) : list;
  return filtered.sort((a, b) => a.productName.localeCompare(b.productName));
}

export async function adjustMockInventory(productId, quantity, reason, note) {
  await wait();
  inventory = inventory.map((item) => {
    if (item.productId !== productId) return item;
    const available = Math.max(0, item.available + quantity);
    const totalStock = Math.max(0, item.totalStock + quantity);
    return {
      ...item,
      available,
      totalStock,
      reserved: Math.max(0, item.reserved),
      sold: Math.max(0, item.sold - quantity),
      lastAdjustment: { quantity, reason, note, at: new Date().toISOString() },
    };
  });
}

export async function saveMockProduct(id, payload) {
  await wait();
  if (id) {
    products = products.map((product) =>
      product.id === id
        ? {
            ...product,
            name: payload.name,
            description: payload.description ?? null,
            categoryId: payload.categoryId ?? null,
            hsnCode: payload.hsnCode ?? null,
            taxRate: Number(payload.taxRate || 0),
            isActive: payload.isActive !== false,
            stock: Number(payload.stock || 0),
            reorderThreshold: Number(payload.reorderThreshold || 0),
            moq: Number(payload.moq || 1),
            pricingTiers: clone(payload.pricingTiers || []),
            imageUrls: clone(payload.imageUrls || []),
          }
        : product
    );
  } else {
    const newId = `prod-${Date.now()}`;
    products = [
      ...products,
      {
        id: newId,
        name: payload.name,
        description: payload.description ?? null,
        categoryId: payload.categoryId ?? null,
        hsnCode: payload.hsnCode ?? null,
        taxRate: Number(payload.taxRate || 0),
        isActive: payload.isActive !== false,
        stock: Number(payload.stock || 0),
        reorderThreshold: Number(payload.reorderThreshold || 0),
        moq: Number(payload.moq || 1),
        pricingTiers: clone(payload.pricingTiers || []),
        imageUrls: clone(payload.imageUrls || []),
      },
    ];
    inventory = [
      ...inventory,
      {
        id: `inv-${newId}`,
        productId: newId,
        totalStock: Number(payload.stock || 0),
        reserved: 0,
        sold: 0,
        available: Number(payload.stock || 0),
        reorderThreshold: Number(payload.reorderThreshold || 0),
        moq: Number(payload.moq || 1),
      },
    ];
  }

  const updated = products.find((product) => product.id === id) || products[products.length - 1];
  inventory = inventory.map((item) => {
    const product = products.find((candidate) => candidate.id === item.productId);
    if (!product) return item;
    if (product.id !== (id || updated.id)) return item;
    return {
      ...item,
      totalStock: Number(payload.stock || 0),
      available: Number(payload.stock || 0),
      reorderThreshold: Number(payload.reorderThreshold || 0),
      moq: Number(payload.moq || 1),
    };
  });

  return clone(updated);
}

export async function deleteMockProduct(id) {
  await wait();
  products = products.map((product) =>
    product.id === id ? { ...product, isActive: false } : product
  );
}

export async function getMockOrders({ page = 0, size = 15, status = '' } = {}) {
  await wait();
  let filtered = clone(orders).sort(sortByCreatedAtDesc);
  if (status) {
    filtered = filtered.filter((order) => order.status === status);
  }
  const totalElements = filtered.length;
  const totalPages = Math.max(1, Math.ceil(totalElements / size));
  const start = page * size;
  return {
    content: filtered.slice(start, start + size).map(decorateOrder),
    totalPages,
    totalElements,
    number: page,
  };
}

export async function updateMockOrderStatus(orderId, status, reason = null) {
  await wait();
  orders = orders.map((order) => {
    if (order.id !== orderId) return order;
    const historyEntry = {
      fromStatus: order.status,
      toStatus: status,
      reason: reason || null,
      at: new Date().toISOString(),
    };
    return {
      ...order,
      status,
      statusHistory: [...(order.statusHistory || []), historyEntry],
    };
  });
}

export { clone as cloneMockValue };
