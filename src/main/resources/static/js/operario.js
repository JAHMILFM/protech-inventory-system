/**
 * operario.js — Lógica del Panel POS Pro-Tech
 * Módulos: Venta, Recepción de Mercancía, Consulta de Producto
 */

'use strict';

/* ═══════════════════════════════════════════════════
   ESTADO GLOBAL
═══════════════════════════════════════════════════ */
const state = {
    allProducts:     [],      // todos los productos del catálogo
    filteredProducts: [],     // productos filtrados en la vista
    cart:            [],      // ítems del carrito actual
    metodoPago:      'EFECTIVO',
    activeCategory:  'all',
    recepcionRows:   [],      // filas de la tabla de recepción
    consultaProduct: null,    // producto seleccionado en consulta
};

/* Iconos por categoría */
const CAT_ICONS = {
    'Herramientas': '🔧', 'Herramientas Manuales': '🔨',
    'Herramientas Eléctricas': '⚡', 'Electricidad': '💡',
    'Cables y Conductores': '🔌', 'Plomería': '🚿',
    'Tuberías PVC': '🪣', 'Pinturas': '🎨',
    'Construcción': '🏗️', 'Fijaciones': '🪛',
    'default': '📦'
};

function getCatIcon(catName) {
    return CAT_ICONS[catName] || CAT_ICONS['default'];
}

/* ═══════════════════════════════════════════════════
   INICIALIZACIÓN
═══════════════════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', () => {
    // Fecha por defecto en recepción
    const today = new Date().toISOString().split('T')[0];
    const recFecha = document.getElementById('rec-fecha');
    if (recFecha) recFecha.value = today;

    // Avatar con inicial del usuario
    const userName = document.getElementById('userName');
    const avatar   = document.getElementById('userAvatar');
    if (userName && avatar) {
        const name = userName.textContent.trim();
        avatar.textContent = name.charAt(0).toUpperCase();
    }

    loadProducts();
    loadDashboardMetrics();

    // Refresca métricas cada 60 segundos
    setInterval(loadDashboardMetrics, 60000);
});

/* ═══════════════════════════════════════════════════
   CARGA DE DATOS
═══════════════════════════════════════════════════ */
async function loadProducts() {
    try {
        const res = await fetch('/api/productos');
        if (!res.ok) throw new Error('Error al cargar productos');
        state.allProducts = await res.json();
        state.filteredProducts = [...state.allProducts];
        renderCategoryPills();
        renderProductGrid(state.filteredProducts);
    } catch (err) {
        console.error(err);
        showToast('Error al cargar el catálogo de productos', 'error');
        document.getElementById('ventaProductsGrid').innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">❌</div>
                <p>No se pudo cargar el catálogo.<br>Verifica la conexión.</p>
            </div>`;
    }
}

async function loadDashboardMetrics() {
    try {
        const res = await fetch('/api/ventas/dashboard-operario');
        if (!res.ok) return;
        const data = await res.json();
        document.getElementById('m-ventas').textContent    = data.ventasHoy    ?? 0;
        document.getElementById('m-total').textContent     = 'S/ ' + parseFloat(data.totalRecaudado ?? 0).toFixed(2);
        document.getElementById('m-critico').textContent   = data.stockCritico ?? 0;
        document.getElementById('m-entradas').textContent  = data.entradasHoy  ?? 0;
    } catch (e) {
        // silencioso
    }
}

/* ═══════════════════════════════════════════════════
   TABS
═══════════════════════════════════════════════════ */
function switchTab(tab) {
    ['venta', 'recepcion', 'consulta'].forEach(t => {
        document.getElementById(`tab-${t}`).classList.toggle('active', t === tab);
        document.getElementById(`content-${t}`).classList.toggle('active', t === tab);
    });
}

/* ═══════════════════════════════════════════════════
   CATÁLOGO — PESTAÑA VENTA
═══════════════════════════════════════════════════ */
function renderCategoryPills() {
    const cats = [...new Set(
        state.allProducts
            .filter(p => p.categoria)
            .map(p => p.categoria.nombre)
    )].sort();

    const container = document.getElementById('catPills');
    // Mantener "Todos" y agregar categorías
    const existing = container.querySelector('[data-cat="all"]');
    container.innerHTML = '';
    if (existing) container.appendChild(existing);

    // Recrear el pill "Todos"
    const allPill = document.createElement('span');
    allPill.className = 'cat-pill' + (state.activeCategory === 'all' ? ' active' : '');
    allPill.dataset.cat = 'all';
    allPill.textContent = 'Todos';
    allPill.onclick = () => filterByCategory('all', allPill);
    container.appendChild(allPill);

    cats.forEach(cat => {
        const pill = document.createElement('span');
        pill.className = 'cat-pill' + (state.activeCategory === cat ? ' active' : '');
        pill.dataset.cat = cat;
        pill.textContent = getCatIcon(cat) + ' ' + cat;
        pill.onclick = () => filterByCategory(cat, pill);
        container.appendChild(pill);
    });
}

function filterByCategory(cat, el) {
    state.activeCategory = cat;
    document.querySelectorAll('.cat-pill').forEach(p => p.classList.remove('active'));
    el.classList.add('active');
    applyFilters();
}

function filterVentaProducts() {
    applyFilters();
}

function applyFilters() {
    const q   = (document.getElementById('searchVenta').value || '').toLowerCase().trim();
    const cat = state.activeCategory;

    state.filteredProducts = state.allProducts.filter(p => {
        const matchSearch = !q
            || (p.nombre  && p.nombre.toLowerCase().includes(q))
            || (p.sku     && p.sku.toLowerCase().includes(q))
            || (p.ean13   && p.ean13.includes(q))
            || (p.marca   && p.marca.toLowerCase().includes(q));
        const matchCat = cat === 'all'
            || (p.categoria && p.categoria.nombre === cat);
        return matchSearch && matchCat;
    });

    renderProductGrid(state.filteredProducts);
}

function renderProductGrid(products) {
    const grid = document.getElementById('ventaProductsGrid');
    grid.innerHTML = '';

    if (products.length === 0) {
        grid.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">🔍</div>
                <p>No se encontraron productos.</p>
            </div>`;
        return;
    }

    products.forEach(p => {
        const isCritico = p.stockActual !== null && p.stockMinimo !== null
            && p.stockActual <= p.stockMinimo;
        const isBajo = p.stockActual !== null && p.stockMinimo !== null
            && p.stockActual <= p.stockMinimo * 1.5 && !isCritico;
        const hasOferta = p.precioOferta && parseFloat(p.precioOferta) > 0;
        const precio    = hasOferta ? p.precioOferta : p.precioVenta;
        const icon      = p.categoria ? getCatIcon(p.categoria.nombre) : '📦';

        let stockBadge = '';
        if (isCritico) {
            stockBadge = `<span class="stock-badge critico">⚠️ Stock: ${p.stockActual}</span>`;
        } else if (isBajo) {
            stockBadge = `<span class="stock-badge bajo">🟡 Stock: ${p.stockActual}</span>`;
        } else {
            stockBadge = `<span class="stock-badge ok">✅ Stock: ${p.stockActual}</span>`;
        }

        const card = document.createElement('div');
        card.className = 'product-card' + (isCritico ? ' critico' : '');
        card.innerHTML = `
            <div class="product-card-icon">${icon}</div>
            <div class="product-card-name">${escHtml(p.nombre)}</div>
            <div class="product-card-sku">${escHtml(p.sku)}</div>
            ${hasOferta ? `<div class="product-card-price-original">S/ ${parseFloat(p.precioVenta).toFixed(2)}</div>` : ''}
            <div class="product-card-price${hasOferta ? ' oferta' : ''}">S/ ${parseFloat(precio).toFixed(2)}</div>
            ${stockBadge}`;

        card.onclick = () => addToCart(p);
        card.title   = `${p.nombre} — Clic para agregar al carrito`;
        grid.appendChild(card);
    });
}

/* ═══════════════════════════════════════════════════
   CARRITO
═══════════════════════════════════════════════════ */
function addToCart(product) {
    if (product.stockActual <= 0) {
        showToast(`⚠️ ${product.nombre} no tiene stock disponible`, 'warning');
        return;
    }

    const existing = state.cart.find(i => i.productoId === product.id);
    if (existing) {
        if (existing.cantidad >= product.stockActual) {
            showToast(`Stock máximo alcanzado (${product.stockActual} unid.)`, 'warning');
            return;
        }
        existing.cantidad++;
    } else {
        const precio = (product.precioOferta && parseFloat(product.precioOferta) > 0)
            ? parseFloat(product.precioOferta)
            : parseFloat(product.precioVenta);
        state.cart.push({
            productoId:    product.id,
            nombre:        product.nombre,
            sku:           product.sku,
            precioUnitario: precio,
            stockMax:      product.stockActual,
            cantidad:      1,
            icono:         product.categoria ? getCatIcon(product.categoria.nombre) : '📦'
        });
    }

    renderCart();
    showToast(`✅ ${product.nombre} agregado`, 'success');
}

function removeFromCart(index) {
    state.cart.splice(index, 1);
    renderCart();
}

function changeQty(index, delta) {
    const item = state.cart[index];
    const newQty = item.cantidad + delta;
    if (newQty <= 0) {
        removeFromCart(index);
        return;
    }
    if (newQty > item.stockMax) {
        showToast(`Stock máximo: ${item.stockMax} unid.`, 'warning');
        return;
    }
    item.cantidad = newQty;
    renderCart();
}

function renderCart() {
    const container = document.getElementById('cartItems');
    const empty     = document.getElementById('cartEmpty');
    const countEl   = document.getElementById('cartCount');
    const confirmBtn = document.getElementById('btnConfirmSale');

    if (state.cart.length === 0) {
        container.innerHTML = '';
        container.appendChild(empty);
        empty.style.display = 'flex';
        countEl.style.display = 'none';
        confirmBtn.disabled = true;
        updateTotals();
        return;
    }

    empty.style.display = 'none';
    countEl.style.display = 'inline';
    countEl.textContent = state.cart.reduce((s, i) => s + i.cantidad, 0);
    confirmBtn.disabled = false;

    // Reconstruir ítems
    const tempEmpty = empty.cloneNode(true);
    container.innerHTML = '';
    container.appendChild(tempEmpty);

    state.cart.forEach((item, idx) => {
        const subtotal = (item.precioUnitario * item.cantidad).toFixed(2);
        const div = document.createElement('div');
        div.className = 'cart-item';
        div.innerHTML = `
            <div class="ci-icon">${item.icono}</div>
            <div class="ci-info">
                <div class="ci-name" title="${escHtml(item.nombre)}">${escHtml(item.nombre)}</div>
                <div class="ci-price">S/ ${item.precioUnitario.toFixed(2)} × ${item.cantidad}</div>
            </div>
            <div class="ci-controls">
                <button class="qty-btn" onclick="changeQty(${idx}, -1)" aria-label="Quitar uno">−</button>
                <span class="qty-display">${item.cantidad}</span>
                <button class="qty-btn" onclick="changeQty(${idx}, 1)" aria-label="Agregar uno">+</button>
            </div>
            <div class="ci-subtotal">S/ ${subtotal}</div>
            <button class="btn-remove" onclick="removeFromCart(${idx})" aria-label="Eliminar">🗑</button>`;
        container.appendChild(div);
    });

    updateTotals();
    calcVuelto();
}

function updateTotals() {
    const subtotal = state.cart.reduce((s, i) => s + i.precioUnitario * i.cantidad, 0);
    const igv      = subtotal * 0.18;
    const total    = subtotal + igv;

    document.getElementById('subtotalDisplay').textContent = `S/ ${subtotal.toFixed(2)}`;
    document.getElementById('igvDisplay').textContent      = `S/ ${igv.toFixed(2)}`;
    document.getElementById('totalDisplay').textContent    = `S/ ${total.toFixed(2)}`;

    return total;
}

function calcVuelto() {
    const total    = updateTotals();
    const received = parseFloat(document.getElementById('cashReceived').value) || 0;
    const badge    = document.getElementById('vueltoBadge');
    const cashRow  = document.getElementById('cashRow');

    cashRow.style.display = state.metodoPago === 'EFECTIVO' ? 'flex' : 'none';

    if (state.metodoPago !== 'EFECTIVO') {
        badge.textContent = '';
        return;
    }

    if (received <= 0) {
        badge.textContent = 'Vuelto: —';
        badge.className   = 'vuelto-badge';
        return;
    }

    const vuelto = received - total;
    if (vuelto < 0) {
        badge.textContent = `Falta: S/ ${Math.abs(vuelto).toFixed(2)}`;
        badge.className   = 'vuelto-badge error';
    } else {
        badge.textContent = `Vuelto: S/ ${vuelto.toFixed(2)}`;
        badge.className   = 'vuelto-badge';
    }
}

function selectPago(metodo) {
    state.metodoPago = metodo;
    document.querySelectorAll('.pay-btn').forEach(b => b.classList.remove('active-pay'));
    const map = { EFECTIVO: 'pay-efectivo', YAPE: 'pay-yape', TARJETA: 'pay-tarjeta' };
    document.getElementById(map[metodo]).classList.add('active-pay');
    calcVuelto();
}

/* ═══════════════════════════════════════════════════
   CONFIRMAR VENTA
═══════════════════════════════════════════════════ */
async function confirmarVenta() {
    if (state.cart.length === 0) {
        showToast('El carrito está vacío', 'warning');
        return;
    }

    const total    = updateTotals();
    const received = parseFloat(document.getElementById('cashReceived').value) || 0;

    if (state.metodoPago === 'EFECTIVO' && received > 0 && received < total) {
        showToast('El monto recibido es menor al total de la venta', 'error');
        return;
    }

    const btn = document.getElementById('btnConfirmSale');
    btn.disabled   = true;
    btn.innerHTML  = '<div class="spinner"></div> Procesando...';

    const payload = {
        cliente:    document.getElementById('clienteName').value.trim() || 'Cliente General',
        metodoPago: state.metodoPago,
        items:      state.cart.map(i => ({
            productoId: i.productoId,
            cantidad:   i.cantidad
        }))
    };

    try {
        const res = await fetch('/api/ventas/confirmar', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify(payload)
        });

        const data = await res.json();

        if (!res.ok) {
            throw new Error(data.error || 'Error al procesar la venta');
        }

        // Mostrar ticket
        showTicket(data, total, received);
        loadDashboardMetrics();
        // Recargar stock en el catálogo
        loadProducts();

    } catch (err) {
        showToast('❌ ' + err.message, 'error');
    } finally {
        btn.disabled  = false;
        btn.innerHTML = '✅ Confirmar Venta';
    }
}

function showTicket(data, total, received) {
    const igv    = total * (18 / 118);   // IGV incluido en el total
    const vuelto = state.metodoPago === 'EFECTIVO' && received > 0
        ? Math.max(0, received - total) : null;

    document.getElementById('ticketId').textContent = `Folio: ${data.ventaId}`;

    const itemsHtml = state.cart.map(i => `
        <div class="ticket-row">
            <span class="label">${escHtml(i.nombre)} × ${i.cantidad}</span>
            <span class="value">S/ ${(i.precioUnitario * i.cantidad).toFixed(2)}</span>
        </div>`).join('');

    document.getElementById('ticketBody').innerHTML = `
        ${itemsHtml}
        <div class="ticket-row"><span class="label">IGV (18%)</span>
            <span class="value">S/ ${(total * 0.18 / 1.18).toFixed(2)}</span></div>
        <div class="ticket-row total">
            <span class="label">TOTAL</span>
            <span class="value">S/ ${total.toFixed(2)}</span>
        </div>
        <div class="ticket-row"><span class="label">Método de pago</span>
            <span class="value">${data.metodoPago}</span></div>
        <div class="ticket-row"><span class="label">Cliente</span>
            <span class="value">${escHtml(data.cliente)}</span></div>
        ${vuelto !== null ? `<div class="ticket-row">
            <span class="label">Vuelto</span>
            <span class="value" style="color:var(--success)">S/ ${vuelto.toFixed(2)}</span>
        </div>` : ''}`;

    document.getElementById('ticketModal').classList.add('open');
}

function cerrarTicket() {
    document.getElementById('ticketModal').classList.remove('open');
}

function nuevaVenta() {
    cerrarTicket();
    cancelarVenta();
}

function cancelarVenta() {
    state.cart = [];
    document.getElementById('clienteName').value  = '';
    document.getElementById('cashReceived').value = '';
    renderCart();
}

/* ═══════════════════════════════════════════════════
   RECEPCIÓN DE MERCANCÍA
═══════════════════════════════════════════════════ */
let recRowCount = 0;

function addRecepcionRow() {
    const idx = recRowCount++;
    const tbody = document.getElementById('recepcionTbody');

    // Opciones de productos
    const options = state.allProducts
        .map(p => `<option value="${p.id}" data-stock="${p.stockActual}"
                            data-costo="${p.precioCosto ?? 0}">
                     ${escHtml(p.nombre)} (${p.sku}) — Stock: ${p.stockActual}
                   </option>`)
        .join('');

    const tr = document.createElement('tr');
    tr.id = `rec-row-${idx}`;
    tr.innerHTML = `
        <td>
            <select class="form-control" id="rec-prod-${idx}"
                    onchange="updateRecRowPreview(${idx})" style="font-size:11px;">
                <option value="">— Seleccionar producto —</option>
                ${options}
            </select>
        </td>
        <td>
            <input type="number" id="rec-cant-${idx}" min="1" value="1"
                   placeholder="0"
                   oninput="updateRecRowPreview(${idx}); updateRecepcionSummary()">
        </td>
        <td>
            <input type="number" id="rec-costo-${idx}" min="0.01" step="0.01"
                   placeholder="0.00"
                   oninput="updateRecepcionSummary()">
        </td>
        <td>
            <div class="stock-preview" id="rec-preview-${idx}">
                <span>—</span>
            </div>
        </td>
        <td>
            <button class="btn-del-row" onclick="removeRecepcionRow(${idx})">🗑</button>
        </td>`;

    tbody.appendChild(tr);
    updateRecepcionSummary();
}

function removeRecepcionRow(idx) {
    const row = document.getElementById(`rec-row-${idx}`);
    if (row) row.remove();
    updateRecepcionSummary();
}

function updateRecRowPreview(idx) {
    const sel     = document.getElementById(`rec-prod-${idx}`);
    const cantEl  = document.getElementById(`rec-cant-${idx}`);
    const costoEl = document.getElementById(`rec-costo-${idx}`);
    const preview = document.getElementById(`rec-preview-${idx}`);

    if (!sel || !sel.value) {
        preview.innerHTML = '<span>—</span>';
        return;
    }

    const opt        = sel.selectedOptions[0];
    const stockAct   = parseInt(opt.dataset.stock) || 0;
    const costoSug   = parseFloat(opt.dataset.costo) || 0;
    const cant       = parseInt(cantEl?.value) || 0;
    const stockNuevo = stockAct + cant;

    if (costoEl && !costoEl.value) costoEl.value = costoSug.toFixed(2);

    preview.innerHTML = `
        <span>${stockAct}</span>
        <span class="arrow">→</span>
        <span class="new-stock">${stockNuevo}</span>`;

    updateRecepcionSummary();
}

function updateRecepcionSummary() {
    let totalProds = 0, totalUnits = 0, totalCosto = 0;

    document.querySelectorAll('[id^="rec-row-"]').forEach(row => {
        const idx    = row.id.split('-')[2];
        const sel    = document.getElementById(`rec-prod-${idx}`);
        const cant   = parseInt(document.getElementById(`rec-cant-${idx}`)?.value) || 0;
        const costo  = parseFloat(document.getElementById(`rec-costo-${idx}`)?.value) || 0;

        if (sel?.value) {
            totalProds++;
            totalUnits += cant;
            totalCosto += cant * costo;
        }
    });

    document.getElementById('rec-sum-prods').textContent = totalProds;
    document.getElementById('rec-sum-units').textContent = totalUnits;
    document.getElementById('rec-sum-costo').textContent = `S/ ${totalCosto.toFixed(2)}`;
}

async function confirmarRecepcion() {
    const rows = document.querySelectorAll('[id^="rec-row-"]');
    if (rows.length === 0) {
        showToast('Agrega al menos un producto', 'warning');
        return;
    }

    const proveedor  = document.getElementById('rec-proveedor').value.trim();
    const documento  = document.getElementById('rec-documento').value.trim();

    const items = [];
    let hasError = false;

    rows.forEach(row => {
        const idx    = row.id.split('-')[2];
        const sel    = document.getElementById(`rec-prod-${idx}`);
        const cant   = parseInt(document.getElementById(`rec-cant-${idx}`)?.value) || 0;
        const costo  = parseFloat(document.getElementById(`rec-costo-${idx}`)?.value) || 0;

        if (!sel?.value) return;
        if (cant <= 0) { showToast('La cantidad debe ser mayor a 0', 'error'); hasError = true; return; }
        if (costo <= 0) { showToast('El precio de costo debe ser mayor a 0', 'error'); hasError = true; return; }

        items.push({
            productoId:         parseInt(sel.value),
            cantidad:           cant,
            precioUnitario:     costo,
            documentoReferencia: documento || null,
            proveedor:          proveedor  || null,
            motivo:             'Recepción de mercancía'
        });
    });

    if (hasError || items.length === 0) return;

    const btn = document.getElementById('btnConfirmarRecepcion');
    btn.disabled  = true;
    btn.innerHTML = '<div class="spinner"></div> Procesando...';

    let errores = 0;
    for (const item of items) {
        try {
            const res = await fetch('/api/kardex/entrada', {
                method:  'POST',
                headers: { 'Content-Type': 'application/json' },
                body:    JSON.stringify(item)
            });
            if (!res.ok) {
                const err = await res.json();
                showToast(`Error: ${err.error}`, 'error');
                errores++;
            }
        } catch (e) {
            showToast('Error de red al registrar entrada', 'error');
            errores++;
        }
    }

    btn.disabled  = false;
    btn.innerHTML = '📥 Confirmar Recepción';

    if (errores === 0) {
        showToast(`✅ ${items.length} producto(s) recepcionado(s) correctamente`, 'success');
        // Limpiar formulario
        document.getElementById('rec-proveedor').value = '';
        document.getElementById('rec-documento').value = '';
        document.getElementById('recepcionTbody').innerHTML = '';
        recRowCount = 0;
        updateRecepcionSummary();
        // Recargar catálogo con stock actualizado
        loadProducts();
        loadDashboardMetrics();
    } else {
        showToast(`Se procesaron con ${errores} error(es)`, 'warning');
    }
}

/* ═══════════════════════════════════════════════════
   CONSULTA DE PRODUCTO
═══════════════════════════════════════════════════ */
let consultaDebounce = null;

function filterConsultaProducts() {
    clearTimeout(consultaDebounce);
    consultaDebounce = setTimeout(() => {
        const q = document.getElementById('searchConsulta').value.trim().toLowerCase();
        if (!q) {
            hideConsultaResults();
            return;
        }
        const matches = state.allProducts.filter(p =>
            (p.nombre && p.nombre.toLowerCase().includes(q))
            || (p.sku   && p.sku.toLowerCase().includes(q))
            || (p.ean13 && p.ean13.includes(q))
        ).slice(0, 8);
        renderConsultaDropdown(matches);
    }, 200);
}

function renderConsultaDropdown(products) {
    const container = document.getElementById('consultaQuickResults');
    if (products.length === 0) {
        container.style.display = 'none';
        return;
    }

    container.style.display = 'block';
    container.innerHTML = `
        <div style="background:var(--bg-card);border:1px solid var(--border);
                    border-radius:var(--radius-sm);overflow:hidden;
                    box-shadow:var(--shadow);">
        ${products.map(p => `
            <div onclick="showProductDetail(${p.id})"
                 style="padding:10px 12px;cursor:pointer;display:flex;align-items:center;gap:10px;
                        border-bottom:1px solid var(--border);font-size:12px;
                        transition:var(--transition);"
                 onmouseover="this.style.background='var(--bg-hover)'"
                 onmouseout="this.style.background=''"
            >
                <span style="font-size:18px;">${getCatIcon(p.categoria?.nombre)}</span>
                <div>
                    <div style="font-weight:600;color:var(--text-primary);">${escHtml(p.nombre)}</div>
                    <div style="color:var(--text-muted);">${p.sku} · S/ ${parseFloat(p.precioVenta).toFixed(2)} · Stock: ${p.stockActual}</div>
                </div>
            </div>`).join('')}
        </div>`;
}

function hideConsultaResults() {
    const el = document.getElementById('consultaQuickResults');
    if (el) el.style.display = 'none';
}

async function showProductDetail(productId) {
    hideConsultaResults();
    document.getElementById('searchConsulta').value = '';

    const product = state.allProducts.find(p => p.id === productId);
    if (!product) return;

    state.consultaProduct = product;

    const isCritico = product.stockActual <= product.stockMinimo;
    const ubicacion = [
        product.ubicacionPasillo ? 'P' + product.ubicacionPasillo : null,
        product.ubicacionLado    ? product.ubicacionLado : null,
        product.ubicacionNivel   ? 'N' + product.ubicacionNivel : null,
    ].filter(Boolean).join('-') || 'Sin asignar';

    const precioVenta = parseFloat(product.precioVenta);
    const precioIgv   = (precioVenta * 1.18).toFixed(2);
    const icon        = getCatIcon(product.categoria?.nombre);

    document.getElementById('pdIcon').textContent     = icon;
    document.getElementById('pdName').textContent     = product.nombre;
    document.getElementById('pdSku').textContent      = product.sku;
    document.getElementById('pdPrecio').textContent   = `S/ ${precioVenta.toFixed(2)}`;
    document.getElementById('pdPrecioIgv').textContent = `S/ ${precioIgv}`;
    document.getElementById('pdStock').textContent    = `${product.stockActual} unid.`;
    document.getElementById('pdStock').className      = `detail-item-value ${isCritico ? 'stock-bad' : 'stock-ok'}`;
    document.getElementById('pdUbicacion').textContent = ubicacion;
    document.getElementById('pdMarca').textContent    = [product.marca, product.modelo].filter(Boolean).join(' · ') || '—';
    document.getElementById('pdCategoria').textContent = product.categoria?.nombre || '—';

    // Ocultar placeholder, mostrar card
    document.getElementById('consultaPlaceholder').style.display = 'none';
    document.getElementById('productDetailCard').classList.add('visible');

    // Cargar movimientos recientes
    loadRecentMoves(productId);
}

async function loadRecentMoves(productId) {
    const container = document.getElementById('pdMovimientos');
    container.innerHTML = '<div style="color:var(--text-muted);font-size:12px;">Cargando...</div>';

    try {
        const res = await fetch(`/api/kardex/producto/${productId}`);
        if (!res.ok) throw new Error();
        const moves = await res.json();

        if (moves.length === 0) {
            container.innerHTML = '<div style="color:var(--text-muted);font-size:12px;padding:8px 0;">Sin movimientos registrados</div>';
            return;
        }

        const last5 = moves.slice(0, 5);
        container.innerHTML = last5.map(m => {
            const tipo    = m.tipoMovimiento?.toLowerCase().replace('_', '') || 'ajuste';
            const clase   = tipo.startsWith('entrada') ? 'entrada'
                          : tipo.startsWith('salida')  ? 'salida' : 'ajuste';
            const label   = tipo.startsWith('entrada') ? 'ENTRADA'
                          : tipo.startsWith('salida')  ? 'SALIDA' : 'AJUSTE';
            const fecha   = m.fechaMovimiento
                ? new Date(m.fechaMovimiento).toLocaleDateString('es-PE', { day:'2-digit', month:'short' })
                : '—';
            return `
                <div class="move-row">
                    <span class="move-badge ${clase}">${label}</span>
                    <span class="move-qty">${m.cantidad} uds.</span>
                    <span style="color:var(--text-muted);font-size:11px;">${escHtml(m.motivo || '—')}</span>
                    <span class="move-date">${fecha}</span>
                </div>`;
        }).join('');

    } catch (e) {
        container.innerHTML = '<div style="color:var(--text-muted);font-size:12px;">No se pudieron cargar los movimientos</div>';
    }
}

function addDetailProductToCart() {
    if (!state.consultaProduct) return;
    addToCart(state.consultaProduct);
    switchTab('venta');
}

/* ═══════════════════════════════════════════════════
   ESCANEO (simulado — Web API Barcode Detection)
═══════════════════════════════════════════════════ */
function activarEscaneo() {
    showToast('💡 Escribe el código EAN-13 en el buscador o conecta un lector USB', 'info');
    document.getElementById('searchVenta').focus();
}

/* ═══════════════════════════════════════════════════
   UTILIDADES
═══════════════════════════════════════════════════ */
function escHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function showToast(msg, type = 'info') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = msg;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 3500);
}

// Cerrar dropdown de consulta al hacer clic fuera
document.addEventListener('click', e => {
    if (!e.target.closest('#content-consulta')) hideConsultaResults();
});
