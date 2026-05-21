'use strict';

document.addEventListener('DOMContentLoaded', () => {
    loadMetricas();
    loadOrdenes();
    loadStockCritico();
    loadCatalogo();
});

function switchTab(tab) {
    document.getElementById('tab-dashboard').style.display = 'none';
    document.getElementById('tab-ordenes').style.display = 'none';
    document.getElementById('tab-catalogo').style.display = 'none';
    document.getElementById('tab-anaqueles').style.display = 'none';
    
    document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
    event.currentTarget.classList.add('active');

    document.getElementById(`tab-${tab}`).style.display = 'block';
}

async function loadMetricas() {
    try {
        const res = await fetch('/api/proveedor/metricas');
        const data = await res.json();
        document.getElementById('empresaName').textContent = data.empresa || 'Proveedor';
        document.getElementById('m-pendientes').textContent = data.pendientes;
        document.getElementById('m-transito').textContent = data.enTransito;
        document.getElementById('m-facturado').textContent = 'S/ ' + data.totalFacturado.toFixed(2);
    } catch (e) { console.error('Error cargando métricas', e); }
}

async function loadOrdenes() {
    try {
        const res = await fetch('/api/proveedor/ordenes');
        const ordenes = await res.json();
        
        const tbodyDashboard = document.getElementById('dashboard-ordenes');
        const tbodyTodas = document.getElementById('todas-ordenes');
        
        tbodyDashboard.innerHTML = '';
        tbodyTodas.innerHTML = '';

        ordenes.forEach((o, i) => {
            const fecha = new Date(o.createdAt).toLocaleDateString('es-PE');
            const productosResumen = o.detalles && o.detalles.length > 0
                ? o.detalles.map(d => `${d.productoNombre} (x${d.cantidad})`).join(', ')
                : 'Sin detalles';

            const rowStr = `
                <tr>
                    <td><strong>${o.numeroOrden}</strong></td>
                    <td>${fecha}</td>
                    <td>S/ ${o.total.toFixed(2)}</td>
                    <td><span class="badge ${o.estado}">${o.estado}</span></td>
                </tr>`;
            
            if (i < 5) tbodyDashboard.innerHTML += rowStr;

            tbodyTodas.innerHTML += `
                <tr>
                    <td><strong>${o.numeroOrden}</strong></td>
                    <td>${fecha}</td>
                    <td>${o.cantidadArticulos} und.</td>
                    <td>S/ ${o.total.toFixed(2)}</td>
                    <td><span class="badge ${o.estado}">${o.estado}</span></td>
                    <td>
                        <select class="status-select" onchange="updateEstado(${o.id}, this.value)">
                            <option value="PENDIENTE" ${o.estado==='PENDIENTE'?'selected':''}>Pendiente</option>
                            <option value="EN_TRANSITO" ${o.estado==='EN_TRANSITO'?'selected':''}>En Tránsito</option>
                            <option value="RECIBIDO" disabled ${o.estado==='RECIBIDO'?'selected':''}>Recibido por ferretería</option>
                        </select>
                    </td>
                </tr>
                <tr class="detalle-row">
                    <td colspan="6" style="padding:6px 20px; background:rgba(0,0,0,0.15); font-size:12px; color:var(--text-muted)">
                        📦 <strong>Productos:</strong> ${productosResumen}
                    </td>
                </tr>`;
        });
    } catch (e) { console.error(e); }
}

async function updateEstado(ordenId, nuevoEstado) {
    try {
        const res = await fetch(`/api/proveedor/ordenes/${ordenId}/estado`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ estado: nuevoEstado })
        });
        if (res.ok) {
            loadMetricas();
            loadOrdenes();
        } else {
            alert('Error actualizando estado');
        }
    } catch (e) { console.error(e); }
}

async function loadStockCritico() {
    try {
        const res = await fetch('/api/proveedor/stock-critico');
        const productos = await res.json();
        const container = document.getElementById('critico-list');
        
        if(productos.length === 0) {
            container.innerHTML = '<div style="color:var(--text-muted);font-size:13px">No hay stock crítico.</div>';
            return;
        }

        container.innerHTML = productos.map(p => `
            <div class="critico-item">
                <div>
                    <div class="name">${p.nombre}</div>
                    <div class="stock">Stock actual: <strong>${p.stockActual}</strong> (Mín: ${p.stockMinimo})</div>
                </div>
            </div>
        `).join('');
    } catch (e) { console.error(e); }
}

async function loadCatalogo() {
    try {
        const res = await fetch('/api/proveedor/productos');
        const productos = await res.json();
        const tbody = document.getElementById('catalogo-list');
        
        tbody.innerHTML = productos.map(p => {
            const pasillo = p.ubicacionPasillo ? 'P' + p.ubicacionPasillo : '';
            const lado = p.ubicacionLado ? '-' + p.ubicacionLado : '';
            const nivel = p.ubicacionNivel ? '-N' + p.ubicacionNivel : '';
            const ubicacion = (pasillo || lado || nivel) ? (pasillo + lado + nivel) : 'Sin asignar';

            return `
            <tr>
                <td>${p.sku}</td>
                <td><strong>${p.nombre}</strong></td>
                <td style="color:var(--accent);font-weight:700">S/ ${(p.precioCosto || 0).toFixed(2)}</td>
                <td>${p.stockActual}</td>
                <td><span class="badge" style="background:var(--bg-panel); color:var(--text-primary); border: 1px solid var(--border)">${ubicacion}</span></td>
            </tr>
            `;
        }).join('');

        renderAnaqueles(productos);

    } catch (e) { console.error(e); }
}

function renderAnaqueles(productos) {
    const container = document.getElementById('anaqueles-container');
    
    // Filtrar productos que no tienen ubicación asignada
    const ubicados = productos.filter(p => p.ubicacionPasillo);
    if(ubicados.length === 0) {
        container.innerHTML = '<p style="color:var(--text-muted)">No hay productos con ubicación en anaquel asignada.</p>';
        return;
    }

    // Agrupar jerárquicamente: Pasillo -> Lado -> Nivel
    const agrupado = {};
    ubicados.forEach(p => {
        const pas = p.ubicacionPasillo;
        const lad = p.ubicacionLado || 'Único';
        const niv = p.ubicacionNivel || '1';
        
        if(!agrupado[pas]) agrupado[pas] = {};
        if(!agrupado[pas][lad]) agrupado[pas][lad] = {};
        if(!agrupado[pas][lad][niv]) agrupado[pas][lad][niv] = [];
        
        agrupado[pas][lad][niv].push(p);
    });

    let html = '';
    // Ordenar pasillos
    Object.keys(agrupado).sort().forEach(pasillo => {
        html += `<div class="pasillo-block">
                    <div class="pasillo-header">Pasillo ${pasillo}</div>`;
        
        Object.keys(agrupado[pasillo]).sort().forEach(lado => {
            html += `<div class="lado-block">
                        <div class="lado-title">Lado: ${lado}</div>
                        <div class="niveles-container">`;
            
            // Ordenar niveles numéricamente si es posible
            Object.keys(agrupado[pasillo][lado])
                  .sort((a,b) => parseInt(a) - parseInt(b))
                  .forEach(nivel => {
                html += `<div class="nivel-row">
                            <div class="nivel-label">Nivel ${nivel}</div>
                            <div class="nivel-productos">`;
                
                agrupado[pasillo][lado][nivel].forEach(p => {
                    const statusClass = (p.stockActual <= p.stockMinimo) ? 'critico' : 'ok';
                    html += `
                        <div class="producto-caja ${statusClass}" title="${p.nombre}">
                            <div>
                                <div class="caja-sku">${p.sku}</div>
                                <div class="caja-nombre">${p.nombre}</div>
                            </div>
                            <div class="caja-stock">${p.stockActual} und.</div>
                        </div>
                    `;
                });
                
                html += `</div></div>`; // Fin nivel-productos, nivel-row
            });
            html += `</div></div>`; // Fin niveles-container, lado-block
        });
        html += `</div>`; // Fin pasillo-block
    });

    container.innerHTML = html;
}
