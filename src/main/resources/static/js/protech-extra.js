/* ═══════════════════════════════════════════
   FERRETERÍA PRO-TECH :: Extra Modules Logic
   ═══════════════════════════════════════════ */

// ── MARCAS ──────────────────────────────────
function loadMarcas() {
    const tbody = document.getElementById('marcas-tbody');
    if (!tbody) return;
    const marcaMap = {};
    allProducts.forEach(p => {
        const m = p.marca || 'Sin marca';
        if (!marcaMap[m]) marcaMap[m] = { count: 0, categorias: new Set() };
        marcaMap[m].count++;
        if (p.categoriaNombre) marcaMap[m].categorias.add(p.categoriaNombre);
    });
    const marcas = Object.entries(marcaMap).sort((a, b) => b[1].count - a[1].count);
    if (!marcas.length) {
        tbody.innerHTML = '<tr><td colspan="4" class="empty-state"><div class="icon">🏷️</div><h3>Sin marcas</h3></td></tr>';
        return;
    }
    tbody.innerHTML = marcas.map(([marca, data]) => `
        <tr>
            <td><strong>${esc(marca)}</strong></td>
            <td><span class="badge badge-info">${data.count} productos</span></td>
            <td>${[...data.categorias].join(', ') || '—'}</td>
            <td><span class="badge badge-success">Activa</span></td>
        </tr>
    `).join('');
}

// ── PROVEEDORES ─────────────────────────────
function loadProveedores() {
    const tbody = document.getElementById('proveedores-tbody');
    if (!tbody) return;
    const provMap = {};
    allKardex.filter(k => k.proveedor).forEach(k => {
        const p = k.proveedor;
        if (!provMap[p]) provMap[p] = { count: 0, lastDate: k.fechaMovimiento };
        provMap[p].count++;
        if (k.fechaMovimiento > provMap[p].lastDate) provMap[p].lastDate = k.fechaMovimiento;
    });
    const provs = Object.entries(provMap).sort((a, b) => b[1].count - a[1].count);
    if (!provs.length) {
        tbody.innerHTML = '<tr><td colspan="4" class="empty-state"><div class="icon">🚚</div><h3>Sin proveedores registrados en Kardex</h3><p>Los proveedores aparecen al registrar entradas en el Kardex</p></td></tr>';
        return;
    }
    tbody.innerHTML = provs.map(([prov, data]) => {
        const fecha = data.lastDate ? new Date(data.lastDate).toLocaleDateString('es-PE') : '—';
        return `<tr>
            <td><strong>${esc(prov)}</strong></td>
            <td><span class="badge badge-info">${data.count} entradas</span></td>
            <td>${fecha}</td>
            <td><span class="badge badge-success">Activo</span></td>
        </tr>`;
    }).join('');
}

// ── STOCK CRÍTICO ────────────────────────────
function loadStockCritico() {
    const tbody = document.getElementById('stock-critico-tbody');
    if (!tbody) return;
    const criticos = allProducts.filter(p => p.stockCritico && p.activo);
    if (!criticos.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state"><div class="icon">✅</div><h3>¡Sin alertas!</h3><p>Todos los productos tienen stock suficiente</p></td></tr>';
        return;
    }
    tbody.innerHTML = criticos.map(p => {
        const deficit = (p.stockMinimo || 0) - (p.stockActual || 0);
        return `<tr>
            <td><strong>${esc(p.nombre)}</strong></td>
            <td><small>${esc(p.sku)}</small></td>
            <td>${esc(p.categoriaNombre || '—')}</td>
            <td><span class="badge badge-danger">${p.stockActual}</span></td>
            <td>${p.stockMinimo}</td>
            <td><span class="badge badge-warning">+${deficit} unidades</span></td>
            <td><button class="btn btn-primary btn-sm" onclick="openKardexModal('entrada')">📥 Reponer</button></td>
        </tr>`;
    }).join('');
    const badge = document.querySelector('[data-page="stock-critico"] .sidebar-badge');
    if (badge) badge.textContent = criticos.length;
}

// ── ANAQUEL 2D ──────────────────────────────────
window.clickEspacio = function(id, f, c, estado, producto) {
    console.log('───────────────────────────────────────');
    console.log('📦 DETALLES DEL ESPACIO SELECCIONADO');
    console.log('ID: ' + id);
    console.log('Nivel: ' + f);
    console.log('Sección: ' + c);
    console.log('Estado: ' + estado);
    console.log('Producto: ' + producto);
    console.log('───────────────────────────────────────');
    showToast(`Espacio: ${id} <br> ${estado} - ${producto}`, estado === 'ALERTA_STOCK' ? 'warning' : (estado === 'LLENO' ? 'info' : 'success'));
};

function loadAnaquel() {
    const container = document.getElementById('anaquel-grid');
    if (!container) return;
    
    // Agrupar por pasillo
    const pasilloMap = {};
    allProducts.forEach(p => {
        if (!p.ubicacionPasillo) return;
        const key = `Pasillo ${p.ubicacionPasillo}`;
        if (!pasilloMap[key]) pasilloMap[key] = [];
        pasilloMap[key].push(p);
    });

    const pasillos = Object.entries(pasilloMap).sort((a, b) => a[0].localeCompare(b[0]));
    if (!pasillos.length) {
        container.innerHTML = '<div class="empty-state"><div class="icon">🗄️</div><h3>Sin ubicaciones asignadas</h3><p>Asigna pasillo, lado y nivel a los productos</p></div>';
        return;
    }

    const FILAS = 4;
    const COLUMNAS = 5;
    let html = '';

    pasillos.forEach(([pasillo, prods]) => {
        // Inicializar matriz 2D vacía
        const matriz = Array.from({ length: FILAS }, () => Array(COLUMNAS).fill(null));

        prods.forEach(p => {
            let f = parseInt(p.ubicacionNivel) || 1;
            let c = parseInt(p.ubicacionLado) || 1;
            if (isNaN(c) && typeof p.ubicacionLado === 'string') {
                const charCode = p.ubicacionLado.toUpperCase().charCodeAt(0);
                if (charCode >= 65 && charCode <= 90) c = charCode - 64; // A=1, B=2...
            }
            f = Math.max(1, Math.min(f, FILAS));
            c = Math.max(1, Math.min(c, COLUMNAS));
            matriz[f - 1][c - 1] = p;
        });

        html += `<div class="anaquel-pasillo" style="margin-bottom: 24px; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
            <h3 class="anaquel-title" style="margin:0; padding: 16px; background:#1a1f2e; color:#fff; border-bottom:1px solid rgba(255,255,255,0.1);">🗄️ ${esc(pasillo)}</h3>
            <div style="display:grid; grid-template-columns: repeat(${COLUMNAS}, 1fr); gap: 10px; padding: 20px; background: #0D1B2A;">`;

        for (let f = 0; f < FILAS; f++) {
            for (let c = 0; c < COLUMNAS; c++) {
                const p = matriz[f][c];
                const id = `${pasillo}-N${f+1}-S${c+1}`;
                
                let estado = 'DISPONIBLE';
                let colorBg = '#CCFF00';
                let colorText = '#0D1B2A';
                let border = '2px solid rgba(204,255,0,0.5)';
                let texto = 'LIBRE';
                let productoStr = 'Ninguno';

                if (p) {
                    if (p.stockCritico || p.stockActual <= p.stockMinimo) {
                        estado = 'ALERTA_STOCK';
                        colorBg = '#FF9F1C';
                        colorText = '#fff';
                        border = '2px solid rgba(255,255,255,0.2)';
                    } else {
                        estado = 'LLENO';
                        colorBg = '#457B9D';
                        colorText = '#fff';
                        border = '2px solid rgba(255,255,255,0.2)';
                    }
                    texto = p.nombre.length > 20 ? p.nombre.substring(0, 18) + '…' : p.nombre;
                    productoStr = p.nombre;
                } else {
                    colorBg = '#1a1f2e';
                    colorText = '#ffffff44';
                    border = '2px dashed rgba(255,255,255,0.1)';
                }

                const safeStr = productoStr.replace(/'/g, "\\'").replace(/"/g, '&quot;');
                html += `
                <div onclick="clickEspacio('${id}', ${f+1}, ${c+1}, '${estado}', '${safeStr}')" 
                     style="background:${colorBg}; border:${border}; color:${colorText}; height:110px; border-radius:8px; cursor:pointer; 
                            display:flex; align-items:center; justify-content:center; text-align:center; position:relative; 
                            transition: transform 0.2s, filter 0.2s; font-size:13px; font-weight:600; padding:12px; box-shadow: 0 4px 6px rgba(0,0,0,0.2);"
                     onmouseover="this.style.filter='brightness(1.1)'; this.style.transform='translateY(-2px)'" 
                     onmouseout="this.style.filter='brightness(1)'; this.style.transform='translateY(0)'">
                    ${esc(texto)}
                    <span style="position:absolute; bottom:6px; right:8px; font-size:10px; opacity:0.6;">${f+1}-${c+1}</span>
                </div>`;
            }
        }
        
        html += `</div></div>`;
    });

    container.innerHTML = html;
}

// ── PEDIDOS ───────────────────────────
async function loadPedidos() {
    const tbody = document.getElementById('pedidos-tbody');
    if (!tbody) return;
    try {
        const pedidos = await apiFetch('/api/pedidos');
        if (!pedidos || !pedidos.length) {
            tbody.innerHTML = '<tr><td colspan="7" class="empty-state"><div class="icon">🛒</div><h3>Sin pedidos</h3></td></tr>';
            return;
        }
        const estadoBadge = { 'Pendiente': 'badge-warning', 'Completado': 'badge-success', 'Cancelado': 'badge-danger' };
        
        let pendientes = 0, completados = 0, cancelados = 0;
        const hoyStr = new Date().toLocaleDateString('es-PE');

        tbody.innerHTML = pedidos.map(p => {
            if (p.estado === 'Pendiente') pendientes++;
            if (p.estado === 'Completado') completados++;
            if (p.estado === 'Cancelado') cancelados++;
            return `
            <tr>
                <td><strong>${esc(p.numeroPedido)}</strong></td>
                <td>${esc(p.cliente)}</td>
                <td>${p.fecha || '—'}</td>
                <td>${p.cantidadProductos} productos</td>
                <td class="price">S/ ${numberFmt(p.total)}</td>
                <td><span class="badge ${estadoBadge[p.estado] || 'badge-secondary'}">${p.estado}</span></td>
                <td><button class="btn-icon" title="Ver detalle">👁️</button></td>
            </tr>`;
        }).join('');
        
        setText('ped-pendientes', pendientes);
        setText('ped-completados', completados);
        setText('ped-cancelados', cancelados);
        setText('ped-total-dia', pedidos.length);
        
    } catch(e) { console.error(e); }
}

// ── REPORTES ─────────────────────────────────
function loadReportes() {
    const tbody = document.getElementById('reportes-tbody');
    if (!tbody || !allCategories.length) return;
    tbody.innerHTML = allCategories.map(c => {
        const prods = allProducts.filter(p => p.categoriaId === c.id);
        const stockTotal = prods.reduce((s, p) => s + (p.stockActual || 0), 0);
        const valorCosto = prods.reduce((s, p) => s + ((Number(p.precioCosto) || 0) * (p.stockActual || 0)), 0);
        const valorVenta = prods.reduce((s, p) => s + ((Number(p.precioVenta) || 0) * (p.stockActual || 0)), 0);
        return `<tr>
            <td><span class="cat-hierarchy-${c.nivel}">${esc(c.icono || '')} ${esc(c.nombre)}</span></td>
            <td><span class="badge badge-info">${esc(c.nombreNivel)}</span></td>
            <td>${c.cantidadProductos}</td>
            <td>${stockTotal}</td>
            <td class="price">S/ ${numberFmt(valorCosto)}</td>
            <td class="price">S/ ${numberFmt(valorVenta)}</td>
        </tr>`;
    }).join('');
}

// ── USUARIOS ──────────────────────────
async function loadUsuarios() {
    const tbody = document.getElementById('usuarios-tbody');
    if (!tbody) return;
    try {
        const users = await apiFetch('/api/usuarios');
        if (!users || !users.length) return;
        const rolBadge = { 'ADMIN': 'badge-danger', 'OPERARIO': 'badge-info', 'PROVEEDOR': 'badge-warning' };
        tbody.innerHTML = users.map(u => `
            <tr>
                <td><strong>${esc(u.username)}</strong></td>
                <td>${esc(u.nombre)}</td>
                <td>${esc(u.email)}</td>
                <td><span class="badge ${rolBadge[u.rol] || 'badge-secondary'}">${u.rol}</span></td>
                <td><span class="badge ${u.activo ? 'badge-success' : 'badge-secondary'}">${u.activo ? 'Activo' : 'Inactivo'}</span></td>
            </tr>
        `).join('');
    } catch(e) { console.error(e); }
}

// ── EXPORTAR REPORTE (CSV) ───────────────────
function exportarReporte(tipo) {
    let datos, headers, nombre;
    if (tipo === 'inventario') {
        headers = ['SKU', 'Nombre', 'Marca', 'Categoría', 'P. Costo', 'P. Venta', 'Stock', 'Mínimo', 'Ubicación', 'Estado'];
        datos = allProducts.map(p => [p.sku, p.nombre, p.marca || '', p.categoriaNombre || '', p.precioCosto, p.precioVenta, p.stockActual, p.stockMinimo, p.ubicacionCompleta || '', p.activo ? 'Activo' : 'Inactivo']);
        nombre = 'inventario_protech';
    } else if (tipo === 'movimientos') {
        headers = ['Fecha', 'Producto', 'SKU', 'Tipo', 'Cantidad', 'Stock Ant.', 'Stock Nuevo', 'Motivo', 'Proveedor'];
        datos = allKardex.map(k => [k.fechaMovimiento ? new Date(k.fechaMovimiento).toLocaleString('es-PE') : '', k.productoNombre, k.productoSku, k.tipoMovimientoDescripcion, k.cantidad, k.stockAnterior, k.stockNuevo, k.motivo || '', k.proveedor || '']);
        nombre = 'kardex_protech';
    } else if (tipo === 'stock-critico') {
        headers = ['SKU', 'Nombre', 'Categoría', 'Stock Actual', 'Stock Mínimo', 'Déficit'];
        datos = allProducts.filter(p => p.stockCritico).map(p => [p.sku, p.nombre, p.categoriaNombre || '', p.stockActual, p.stockMinimo, (p.stockMinimo || 0) - (p.stockActual || 0)]);
        nombre = 'stock_critico_protech';
    } else {
        showToast('Generando reporte de valoración...', 'info'); return;
    }
    const csv = [headers, ...datos].map(r => r.map(v => `"${String(v).replace(/"/g, '""')}"`).join(',')).join('\n');
    const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `${nombre}_${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    showToast(`Reporte "${tipo}" descargado exitosamente ✅`, 'success');
}

// ── VOICE BOT LOGIC ──────────────────────────
let recognition;
let isRecording = false;

function initVoiceBot() {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (SpeechRecognition) {
        recognition = new SpeechRecognition();
        recognition.lang = 'es-PE';
        recognition.interimResults = false;
        recognition.maxAlternatives = 1;

        recognition.onstart = function() {
            isRecording = true;
            document.getElementById('btnMicGlobal').classList.add('escuchando');
            document.getElementById('micStatusText').textContent = "Escuchando...";
            window.speechSynthesis.cancel();
        };

        recognition.onresult = function(event) {
            const transcripcion = event.results[0][0].transcript;
            addChatMessage(transcripcion, 'user');
            enviarConsultaBot(transcripcion);
        };

        recognition.onspeechend = function() {
            recognition.stop();
        };

        recognition.onend = function() {
            isRecording = false;
            document.getElementById('btnMicGlobal').classList.remove('escuchando');
            if (document.getElementById('micStatusText').textContent === "Escuchando...") {
                document.getElementById('micStatusText').textContent = "Inactivo";
            }
        };

        recognition.onerror = function(event) {
            document.getElementById('micStatusText').textContent = "Error: " + event.error;
        };
    } else {
        document.getElementById('micStatusText').textContent = "Voz no soportada";
    }
}

function toggleVoiceBot() {
    const win = document.getElementById('voiceBotWindow');
    win.classList.toggle('active');
    if (!recognition) initVoiceBot();
}

function toggleMic() {
    if (!recognition) return;
    if (isRecording) {
        recognition.stop();
    } else {
        recognition.start();
    }
}

function addChatMessage(text, sender) {
    const body = document.getElementById('voiceBotBody');
    const div = document.createElement('div');
    div.className = sender === 'user' ? 'user-msg' : 'bot-msg';
    div.textContent = text;
    body.appendChild(div);
    body.scrollTop = body.scrollHeight;
}

async function enviarConsultaBot(mensaje) {
    document.getElementById('micStatusText').textContent = "Analizando...";
    
    try {
        const response = await fetch('/api/analista/conversar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ mensaje: mensaje })
        });
        const data = await response.json();
        
        addChatMessage(data.respuestaTexto, 'bot');
        document.getElementById('micStatusText').textContent = "Inactivo";
        hablarBot(data.respuestaTexto);
    } catch (e) {
        addChatMessage("Error al contactar con el servidor.", 'bot');
        document.getElementById('micStatusText').textContent = "Error";
    }
}

function hablarBot(texto) {
    if (!window.speechSynthesis) return;
    const utterance = new SpeechSynthesisUtterance(texto);
    utterance.lang = 'es-PE';
    window.speechSynthesis.speak(utterance);
}
