/* ═══════════════════════════════════════════
   FERRETERÍA PRO-TECH :: Application Logic
   ═══════════════════════════════════════════ */

const API = {
    productos: '/api/productos',
    categorias: '/api/categorias',
    kardex: '/api/kardex',
    dashboard: '/api/dashboard'
};

let allProducts = [];
let allCategories = [];
let allKardex = [];
let currentEditId = null;

// ── INIT ────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    setupSidebar();
    setupNavigation();
    loadDashboard();
    loadCategories();
    loadProducts();
    loadKardex();
});

// ── SIDEBAR ─────────────────────────────────
function setupSidebar() {
    const toggle = document.getElementById('sidebarToggle');
    const sidebar = document.getElementById('sidebar');
    if (toggle) {
        toggle.addEventListener('click', () => sidebar.classList.toggle('collapsed'));
    }
}

// ── NAVIGATION ──────────────────────────────
function setupNavigation() {
    document.querySelectorAll('.nav-item[data-page]').forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const page = item.dataset.page;
            document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
            item.classList.add('active');
            document.querySelectorAll('.page-section').forEach(s => s.classList.remove('active'));
            const section = document.getElementById('page-' + page);
            if (section) section.classList.add('active');
            if (section) section.classList.add('active');
            setText('current-breadcrumb', item.querySelector('.label').textContent);
            if (page === 'dashboard') loadDashboard();
            if (page === 'productos') loadProducts();
            if (page === 'categorias') loadCategories();
            if (page === 'kardex') loadKardex();
            if (page === 'marcas') loadMarcas();
            if (page === 'proveedores') loadProveedores();
            if (page === 'anaquel') loadAnaquel();
            if (page === 'stock-critico') loadStockCritico();
            if (page === 'pedidos') loadPedidos();
            if (page === 'reportes') loadReportes();
            if (page === 'usuarios') loadUsuarios();
        });
    });
}

// ── API FETCH HELPER ────────────────────────
async function apiFetch(url, options = {}) {
    try {
        const res = await fetch(url, {
            headers: { 'Content-Type': 'application/json', ...options.headers },
            ...options
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Error en la operación');
        return data;
    } catch (err) {
        showToast(err.message, 'error');
        throw err;
    }
}

// ── DASHBOARD ───────────────────────────────
async function loadDashboard() {
    try {
        const d = await apiFetch(API.dashboard);
        setText('metric-total', d.totalProductos);
        setText('metric-activos', d.productosActivos);
        setText('metric-critico', d.stockCritico);
        setText('metric-categorias', d.totalCategorias);
        setText('metric-usuarios', d.totalUsuarios);
        setText('metric-movimientos', d.movimientosHoy);
        setText('metric-valor-costo', 'S/ ' + numberFmt(d.valorInventario));
        setText('metric-valor-venta', 'S/ ' + numberFmt(d.valorInventarioVenta));
        setText('prod-total', d.totalProductos);
        setText('prod-critico', d.stockCritico);
        setText('prod-valor', 'S/ ' + numberFmt(d.valorInventario));
    } catch (e) { console.error('Dashboard error:', e); }
}

// ── PRODUCTS ────────────────────────────────
async function loadProducts() {
    showTableSkeleton('products-tbody', 8);
    try {
        allProducts = await apiFetch(API.productos);
        renderProducts(allProducts);
    } catch (e) { console.error(e); }
}

function renderProducts(products) {
    const tbody = document.getElementById('products-tbody');
    if (!products.length) {
        tbody.innerHTML = '<tr><td colspan="10" class="empty-state"><div class="icon">📦</div><h3>Sin productos</h3><p>Agrega tu primer producto</p></td></tr>';
        return;
    }
    tbody.innerHTML = products.map(p => `
        <tr>
            <td><span class="product-name">${esc(p.nombre)}</span><br><small style="color:var(--text-muted)">${esc(p.sku)}</small></td>
            <td>${esc(p.categoriaNombre || '—')}</td>
            <td>${esc(p.marca || '—')}</td>
            <td class="price">S/ ${numFmt2(p.precioCosto)}</td>
            <td class="price">S/ ${numFmt2(p.precioVenta)}</td>
            <td><span class="stock-badge badge ${p.stockCritico ? 'badge-danger' : 'badge-success'}">${p.stockActual}</span></td>
            <td>${p.stockMinimo}</td>
            <td>${esc(p.ubicacionCompleta || '—')}</td>
            <td>
                ${!p.activo ? '<span class="badge badge-secondary">Inactivo</span>' : 
                  (p.stockCritico ? '<span class="badge badge-danger">Stock Bajo</span>' : 
                  '<span class="badge badge-success">Activo</span>')}
            </td>
            <td class="table-actions">
                <button class="btn-icon edit" onclick="editProduct(${p.id})" title="Editar">✏️</button>
                <button class="btn-icon delete" onclick="deleteProduct(${p.id},'${esc(p.nombre)}')" title="Desactivar">🗑️</button>
            </td>
        </tr>
    `).join('');
}

function filterProducts() {
    const q = document.getElementById('searchProducts').value.toLowerCase();
    const filtered = allProducts.filter(p =>
        p.nombre.toLowerCase().includes(q) ||
        p.sku.toLowerCase().includes(q) ||
        (p.marca && p.marca.toLowerCase().includes(q))
    );
    renderProducts(filtered);
}

function openProductModal(title = 'Nuevo Producto') {
    currentEditId = null;
    document.getElementById('productModalTitle').textContent = title;
    document.getElementById('productForm').reset();
    populateCategorySelect('prodCategoria');
    openModal('productModal');
}

async function editProduct(id) {
    try {
        const p = await apiFetch(API.productos + '/' + id);
        currentEditId = id;
        document.getElementById('productModalTitle').textContent = 'Editar Producto';
        populateCategorySelect('prodCategoria', p.categoriaId);
        document.getElementById('prodSku').value = p.sku || '';
        document.getElementById('prodEan').value = p.ean13 || '';
        document.getElementById('prodNombre').value = p.nombre || '';
        document.getElementById('prodDescripcion').value = p.descripcion || '';
        document.getElementById('prodPrecioCosto').value = p.precioCosto || '';
        document.getElementById('prodPrecioVenta').value = p.precioVenta || '';
        document.getElementById('prodPrecioOferta').value = p.precioOferta || '';
        document.getElementById('prodStockActual').value = p.stockActual || 0;
        document.getElementById('prodStockMinimo').value = p.stockMinimo || 0;
        document.getElementById('prodMarca').value = p.marca || '';
        document.getElementById('prodModelo').value = p.modelo || '';
        document.getElementById('prodPasillo').value = p.ubicacionPasillo || '';
        document.getElementById('prodLado').value = p.ubicacionLado || '';
        document.getElementById('prodNivel').value = p.ubicacionNivel || '';
        openModal('productModal');
    } catch (e) { console.error(e); }
}

async function saveProduct() {
    const body = {
        sku: val('prodSku'),
        ean13: val('prodEan'),
        nombre: val('prodNombre'),
        descripcion: val('prodDescripcion'),
        precioCosto: parseFloat(val('prodPrecioCosto')),
        precioVenta: parseFloat(val('prodPrecioVenta')),
        precioOferta: val('prodPrecioOferta') ? parseFloat(val('prodPrecioOferta')) : null,
        stockActual: parseInt(val('prodStockActual')) || 0,
        stockMinimo: parseInt(val('prodStockMinimo')) || 0,
        stockReserva: 0,
        marca: val('prodMarca'),
        modelo: val('prodModelo'),
        ubicacionPasillo: val('prodPasillo'),
        ubicacionLado: val('prodLado'),
        ubicacionNivel: val('prodNivel'),
        unidadMedida: 'UND',
        activo: true,
        categoria: val('prodCategoria') ? { id: parseInt(val('prodCategoria')) } : null
    };
    if (!body.sku || !body.nombre || !body.precioCosto || !body.precioVenta) {
        showToast('Completa los campos obligatorios (SKU, Nombre, Precios)', 'warning');
        return;
    }
    try {
        if (currentEditId) {
            await apiFetch(API.productos + '/' + currentEditId, { method: 'PUT', body: JSON.stringify(body) });
            showToast('Producto actualizado correctamente', 'success');
        } else {
            await apiFetch(API.productos, { method: 'POST', body: JSON.stringify(body) });
            showToast('Producto creado correctamente', 'success');
        }
        closeModal('productModal');
        loadProducts();
        loadDashboard();
    } catch (e) { /* toast already shown */ }
}

async function deleteProduct(id, name) {
    if (!confirm(`¿Desactivar el producto "${name}"?`)) return;
    try {
        await apiFetch(API.productos + '/' + id, { method: 'DELETE' });
        showToast('Producto desactivado', 'success');
        loadProducts();
        loadDashboard();
    } catch (e) { /* toast shown */ }
}

// ── CATEGORIES ──────────────────────────────
async function loadCategories() {
    try {
        allCategories = await apiFetch(API.categorias);
        renderCategories(allCategories);
    } catch (e) { console.error(e); }
}

function renderCategories(cats) {
    const tbody = document.getElementById('categories-tbody');
    if (!cats.length) {
        tbody.innerHTML = '<tr><td colspan="8" class="empty-state"><div class="icon">📁</div><h3>Sin categorías</h3></td></tr>';
        return;
    }
    tbody.innerHTML = cats.map(c => `
        <tr>
            <td><div class="cat-hierarchy-${c.nivel}">${esc(c.nombre)}</div></td>
            <td><span class="icon" style="font-size:20px">${c.icono ? c.icono : '📁'}</span></td>
            <td><span class="badge badge-info">${esc(c.nombreNivel)}</span></td>
            <td>${esc(c.padreNombre || '—')}</td>
            <td>${c.cantidadSubcategorias || 0}</td>
            <td>${c.cantidadProductos}</td>
            <td><span class="badge ${c.activo ? 'badge-success' : 'badge-secondary'}">${c.activo ? 'Sí' : 'No'}</span></td>
            <td class="table-actions">
                <button class="btn-icon edit" onclick="editCategory(${c.id})" title="Editar">✏️</button>
                <button class="btn-icon delete" onclick="deleteCategory(${c.id},'${esc(c.nombre)}')" title="Eliminar">🗑️</button>
            </td>
        </tr>
    `).join('');
}

function openCategoryModal() {
    currentEditId = null;
    document.getElementById('catModalTitle').textContent = 'Nueva Categoría';
    document.getElementById('categoryForm').reset();
    populateParentCatSelect('catPadre');
    openModal('categoryModal');
}

async function editCategory(id) {
    try {
        const c = await apiFetch(API.categorias + '/' + id);
        currentEditId = id;
        document.getElementById('catModalTitle').textContent = 'Editar Categoría';
        document.getElementById('catNombre').value = c.nombre || '';
        document.getElementById('catDescripcion').value = c.descripcion || '';
        populateParentCatSelect('catPadre', c.padreId);
        openModal('categoryModal');
    } catch (e) { console.error(e); }
}

async function saveCategory() {
    const body = {
        nombre: val('catNombre'),
        descripcion: val('catDescripcion'),
        padre: val('catPadre') ? { id: parseInt(val('catPadre')) } : null
    };
    if (!body.nombre) { showToast('El nombre es obligatorio', 'warning'); return; }
    try {
        if (currentEditId) {
            await apiFetch(API.categorias + '/' + currentEditId, { method: 'PUT', body: JSON.stringify(body) });
            showToast('Categoría actualizada', 'success');
        } else {
            await apiFetch(API.categorias, { method: 'POST', body: JSON.stringify(body) });
            showToast('Categoría creada', 'success');
        }
        closeModal('categoryModal');
        loadCategories();
        loadDashboard();
    } catch (e) { /* toast shown */ }
}

async function deleteCategory(id, name) {
    if (!confirm(`¿Desactivar la categoría "${name}"?`)) return;
    try {
        await apiFetch(API.categorias + '/' + id, { method: 'DELETE' });
        showToast('Categoría desactivada', 'success');
        loadCategories();
        loadDashboard();
    } catch (e) { /* toast shown */ }
}

// ── KARDEX ──────────────────────────────────
async function loadKardex() {
    try {
        allKardex = await apiFetch(API.kardex);
        renderKardex(allKardex);
    } catch (e) { console.error(e); }
}

function renderKardex(items) {
    const tbody = document.getElementById('kardex-tbody');
    if (!items.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state"><div class="icon">📋</div><h3>Sin movimientos</h3></td></tr>';
        return;
    }
    tbody.innerHTML = items.map(k => {
        const isEntrada = k.tipoMovimiento === 'ENTRADA' || k.tipoMovimiento === 'AJUSTE_POSITIVO' || k.tipoMovimiento === 'DEVOLUCION';
        const badgeClass = isEntrada ? 'badge-success' : 'badge-danger';
        const fecha = k.fechaMovimiento ? new Date(k.fechaMovimiento).toLocaleString('es-PE') : '—';
        return `
        <tr>
            <td>${fecha}</td>
            <td><strong>${esc(k.productoNombre || '')}</strong><br><small style="color:var(--text-muted)">${esc(k.productoSku || '')}</small></td>
            <td><span class="badge ${badgeClass}">${esc(k.tipoMovimientoDescripcion)}</span></td>
            <td style="font-weight:700">${k.cantidad}</td>
            <td>${k.stockAnterior} → ${k.stockNuevo}</td>
            <td>${esc(k.motivo || '—')}</td>
            <td>${esc(k.proveedor || '—')}</td>
        </tr>`;
    }).join('');
}

function openKardexModal(tipo) {
    document.getElementById('kardexModalTitle').textContent = tipo === 'entrada' ? '📥 Registrar Entrada' : '📤 Registrar Salida';
    document.getElementById('kardexTipo').value = tipo;
    document.getElementById('kardexForm').reset();
    document.getElementById('kardexTipo').value = tipo;
    const provGroup = document.getElementById('kardexProveedorGroup');
    const precioGroup = document.getElementById('kardexPrecioGroup');
    if (tipo === 'entrada') {
        provGroup.style.display = '';
        precioGroup.style.display = '';
    } else {
        provGroup.style.display = 'none';
        precioGroup.style.display = 'none';
    }
    populateProductSelect('kardexProducto');
    openModal('kardexModal');
}

async function saveKardex() {
    const tipo = val('kardexTipo');
    const productoId = val('kardexProducto');
    const cantidad = val('kardexCantidad');
    if (!productoId || !cantidad) { showToast('Selecciona producto y cantidad', 'warning'); return; }
    try {
        if (tipo === 'entrada') {
            const precio = val('kardexPrecio') || '0';
            await apiFetch(API.kardex + '/entrada', {
                method: 'POST',
                body: JSON.stringify({
                    productoId: parseInt(productoId),
                    cantidad: parseInt(cantidad),
                    precioUnitario: parseFloat(precio),
                    proveedor: val('kardexProveedor'),
                    motivo: val('kardexMotivo'),
                    documentoReferencia: val('kardexDocumento')
                })
            });
            showToast('Entrada registrada correctamente', 'success');
        } else {
            await apiFetch(API.kardex + '/salida', {
                method: 'POST',
                body: JSON.stringify({
                    productoId: parseInt(productoId),
                    cantidad: parseInt(cantidad),
                    motivo: val('kardexMotivo'),
                    documentoReferencia: val('kardexDocumento')
                })
            });
            showToast('Salida registrada correctamente', 'success');
        }
        closeModal('kardexModal');
        loadKardex();
        loadProducts();
        loadDashboard();
    } catch (e) { /* toast shown */ }
}

// ── HELPERS ─────────────────────────────────
function val(id) { return document.getElementById(id)?.value?.trim() || ''; }
function setText(id, v) { const el = document.getElementById(id); if (el) el.textContent = v; }
function esc(s) { if (!s) return ''; const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }
function numberFmt(n) { return Number(n).toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }); }
function numFmt2(n) { return n != null ? Number(n).toFixed(2) : '0.00'; }

function openModal(id) { document.getElementById(id)?.classList.add('active'); }
function closeModal(id) { document.getElementById(id)?.classList.remove('active'); currentEditId = null; }

function showToast(msg, type = 'info') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = 'toast ' + type;
    toast.innerHTML = `<span>${type === 'success' ? '✅' : type === 'error' ? '❌' : type === 'warning' ? '⚠️' : 'ℹ️'}</span><span>${msg}</span><button class="toast-close" onclick="this.parentElement.remove()">✕</button>`;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
}

function showTableSkeleton(tbodyId, cols) {
    const tbody = document.getElementById(tbodyId);
    if (!tbody) return;
    let rows = '';
    for (let i = 0; i < 5; i++) {
        rows += '<tr>';
        for (let j = 0; j < cols; j++) rows += '<td><div class="skeleton skeleton-text" style="width:' + (60 + Math.random() * 40) + '%"></div></td>';
        rows += '</tr>';
    }
    tbody.innerHTML = rows;
}

function populateCategorySelect(selectId, selectedId) {
    const sel = document.getElementById(selectId);
    sel.innerHTML = '<option value="">Sin categoría</option>';
    allCategories.forEach(c => {
        sel.innerHTML += `<option value="${c.id}" ${c.id == selectedId ? 'selected' : ''}>${c.rutaCompleta || c.nombre}</option>`;
    });
}

function populateParentCatSelect(selectId, selectedId) {
    const sel = document.getElementById(selectId);
    sel.innerHTML = '<option value="">Sin padre (Departamento)</option>';
    allCategories.filter(c => c.nivel < 3).forEach(c => {
        sel.innerHTML += `<option value="${c.id}" ${c.id == selectedId ? 'selected' : ''}>${c.nombre} (${c.nombreNivel})</option>`;
    });
}

function populateProductSelect(selectId) {
    const sel = document.getElementById(selectId);
    sel.innerHTML = '<option value="">Seleccionar producto...</option>';
    allProducts.forEach(p => {
        sel.innerHTML += `<option value="${p.id}">${p.sku} - ${p.nombre} (Stock: ${p.stockActual})</option>`;
    });
}
