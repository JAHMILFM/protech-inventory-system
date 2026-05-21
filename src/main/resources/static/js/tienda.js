document.addEventListener('DOMContentLoaded', () => {
    let allProducts = [];
    let cart = JSON.parse(localStorage.getItem('protech_cart')) || [];

    const grid = document.getElementById('productsGrid');
    const navCategoryList = document.getElementById('navCategoryList');
    const sidebarCategoryList = document.getElementById('sidebarCategoryList');
    const searchInput = document.getElementById('searchInput');
    const categoryTitle = document.getElementById('categoryTitle');

    // Cart Elements
    const cartBtn = document.getElementById('openCartBtn');
    const closeCartBtn = document.getElementById('closeCartBtn');
    const cartOverlay = document.getElementById('cartOverlay');
    const cartDrawer = document.getElementById('cartDrawer');
    const cartBadge = document.getElementById('cartBadge');
    const cartItemsContainer = document.getElementById('cartItemsContainer');
    const cartTotal = document.getElementById('cartTotal');
    const checkoutBtn = document.getElementById('checkoutBtn');

    // Mapa de imágenes stock para dar look profesional
    const stockImages = {
        "martillo": "https://images.unsplash.com/photo-1586864387967-d02ef85d93e8?w=400&q=80",
        "destornillador": "https://images.unsplash.com/photo-1508898578281-774ac4893c0c?w=400&q=80",
        "taladro": "https://images.unsplash.com/photo-1504148455328-c376907d081c?w=400&q=80",
        "amoladora": "https://images.unsplash.com/photo-1572981779307-38b8cabb2407?w=400&q=80",
        "cable": "https://images.unsplash.com/photo-1558522195-e1201b090344?w=400&q=80",
        "tubo": "https://images.unsplash.com/photo-1521575107034-e0fa0c594c88?w=400&q=80",
        "llave": "https://images.unsplash.com/photo-1530124566582-a618bc2615dc?w=400&q=80",
        "sierra": "https://images.unsplash.com/photo-1504148455328-c376907d081c?w=400&q=80",
        "cinta": "https://images.unsplash.com/photo-1589939705384-5185137a7f0f?w=400&q=80",
        "disco": "https://images.unsplash.com/photo-1572981779307-38b8cabb2407?w=400&q=80",
        "nivel": "https://images.unsplash.com/photo-1586864387967-d02ef85d93e8?w=400&q=80",
        "default": "https://images.unsplash.com/photo-1530124566582-a618bc2615dc?w=400&q=80"
    };

    function getImageUrl(nombre, urlBd) {
        if (urlBd) return urlBd;
        const key = Object.keys(stockImages).find(k => nombre.toLowerCase().includes(k));
        return key ? stockImages[key] : stockImages.default;
    }

    let allCategories = [];

    // Inicializar
    init();

    async function init() {
        updateCartBadge();
        await loadCategorias();
        await loadProductos();
        setupEventListeners();
    }

    async function loadCategorias() {
        try {
            const res = await fetch('/api/tienda/categorias');
            allCategories = await res.json();
            
            allCategories.filter(c => c.nivel === 1).forEach(cat => {
                // Header Nav
                const navLi = document.createElement('li');
                navLi.innerText = cat.nombre;
                navLi.dataset.id = cat.id;
                navLi.addEventListener('click', () => filterByCategory(cat.id, cat.nombre, navLi));
                navCategoryList.appendChild(navLi);

                // Sidebar
                const sideLi = document.createElement('li');
                sideLi.innerHTML = `<label style="cursor:pointer; display:flex; gap:0.5rem"><input type="radio" name="catFilter" value="${cat.id}"> ${cat.nombre}</label>`;
                sideLi.querySelector('input').addEventListener('change', () => filterByCategory(cat.id, cat.nombre, navLi));
                sidebarCategoryList.appendChild(sideLi);
            });
        } catch (e) { console.error('Error cargando categorias', e); }
    }

    async function loadProductos() {
        try {
            const res = await fetch('/api/tienda/productos');
            allProducts = await res.json();
            renderProductos(allProducts);
        } catch (e) {
            grid.innerHTML = '<p style="color:var(--danger)">Error al cargar el catálogo.</p>';
        }
    }

    function renderProductos(productos) {
        grid.innerHTML = '';
        if (productos.length === 0) {
            grid.innerHTML = '<p style="color:var(--text-muted); grid-column: 1/-1;">No se encontraron productos en esta categoría.</p>';
            return;
        }

        productos.forEach(p => {
            const div = document.createElement('div');
            div.className = 'product-card';
            
            const imageUrl = getImageUrl(p.nombre, p.imagenUrl);
            
            // Format price: split into integer and decimals
            const priceVal = parseFloat(p.precioVenta).toFixed(2);
            const [integerPart, decimalPart] = priceVal.split('.');

            // Calculate old price (fake discount for UI realism)
            const oldPrice = (p.precioVenta * 1.2).toFixed(2);

            // Alerta de Stock (Grid)
            let stockHtml = '';
            if (p.stockActual > 0 && p.stockActual <= 5) {
                stockHtml = `<div class="badge badge-warning" style="position:absolute; top:16px; right:16px; z-index:2;">¡Solo ${p.stockActual}!</div>`;
            } else if (p.stockActual === 0) {
                stockHtml = `<div class="badge badge-danger" style="position:absolute; top:16px; right:16px; z-index:2;">AGOTADO</div>`;
            }

            div.innerHTML = `
                ${stockHtml}
                <div class="card-img-wrap" onclick="openPDP(${p.id})">
                    <img src="${imageUrl}" alt="${p.nombre}" loading="lazy">
                </div>
                <div class="card-brand">${p.marca || 'GENÉRICO'}</div>
                <div class="card-title" title="${p.nombre}" onclick="openPDP(${p.id})">${p.nombre}</div>
                
                <div class="card-price-row">
                    <div class="price-current">S/ ${integerPart}.${decimalPart}</div>
                    <div class="price-old">S/ ${oldPrice}</div>
                </div>
                
                <button class="add-to-cart-btn" onclick="event.stopPropagation(); addToCart(${p.id})">
                    Añadir al Carrito
                </button>
            `;
            grid.appendChild(div);
        });
    }

    // --- PDP MODAL LOGIC ---
    const pdpOverlay = document.getElementById('pdpOverlay');
    const pdpModal = document.getElementById('pdpModal');
    const pdpCloseBtn = document.getElementById('pdpCloseBtn');
    
    pdpCloseBtn.addEventListener('click', closePDP);
    pdpOverlay.addEventListener('click', (e) => {
        if (e.target === pdpOverlay) closePDP();
    });

    window.openPDP = function(id) {
        const p = allProducts.find(x => x.id === id);
        if(!p) return;
        
        document.getElementById('pdpImage').src = getImageUrl(p.nombre, p.imagenUrl);
        document.getElementById('pdpBrand').innerText = p.marca || 'GENÉRICO';
        document.getElementById('pdpTitle').innerText = p.nombre;
        document.getElementById('pdpSku').innerText = 'SKU: ' + (p.sku || 'N/A');
        
        const priceVal = parseFloat(p.precioVenta).toFixed(2);
        const oldPrice = (p.precioVenta * 1.2).toFixed(2);
        document.getElementById('pdpCurrentPrice').innerText = priceVal;
        document.getElementById('pdpOldPrice').innerText = 'S/ ' + oldPrice;
        document.getElementById('pdpDescription').innerText = p.descripcion || 'Sin descripción disponible.';
        
        // Stock Alert in PDP
        const alertContainer = document.getElementById('pdpStockAlertContainer');
        if (p.stockActual > 0 && p.stockActual <= 5) {
            alertContainer.innerHTML = `
            <div class="stock-alert">
                <span class="pulse-dot"></span>
                <span class="stock-text">¡Apresúrate! Solo quedan <strong class="stock-count">${p.stockActual}</strong> u.</span>
            </div>`;
        } else if (p.stockActual === 0) {
            alertContainer.innerHTML = `<div class="stock-alert" style="background:var(--danger-bg); color:var(--danger); border-color:#fecaca;">AGOTADO</div>`;
        } else {
            alertContainer.innerHTML = '';
        }

        const addBtn = document.getElementById('pdpAddToCartBtn');
        addBtn.onclick = () => { addToCart(p.id); closePDP(); };
        addBtn.disabled = p.stockActual === 0;
        addBtn.innerText = p.stockActual === 0 ? 'AGOTADO' : 'Añadir al Carrito';

        pdpOverlay.classList.add('active');
        document.body.style.overflow = 'hidden';
    };

    function closePDP() {
        pdpOverlay.classList.remove('active');
        document.body.style.overflow = '';
    }

    function filterByCategory(catId, catName, navElement) {
        // Update UI Active states
        if (navElement) {
            navCategoryList.querySelectorAll('li').forEach(el => el.classList.remove('active'));
            navElement.classList.add('active');
        }
        
        categoryTitle.innerText = catName;

        if (catId === 'all') {
            // Reset sidebar radios
            sidebarCategoryList.querySelectorAll('input').forEach(r => r.checked = false);
            renderProductos(allProducts);
        } else {
            // Update sidebar radio
            const radio = sidebarCategoryList.querySelector(`input[value="${catId}"]`);
            if (radio) radio.checked = true;
            
            // Build an array of valid category IDs (the parent itself, and all its children)
            const targetId = parseInt(catId);
            const validIds = [targetId];
            allCategories.forEach(c => {
                if (c.parentId === targetId) {
                    validIds.push(c.id);
                }
            });
            
            const filtrados = allProducts.filter(p => validIds.includes(p.categoriaId));
            renderProductos(filtrados);
        }
    }

    searchInput.addEventListener('input', (e) => {
        const text = e.target.value.toLowerCase();
        const filtrados = allProducts.filter(p => 
            p.nombre.toLowerCase().includes(text) || 
            (p.marca && p.marca.toLowerCase().includes(text)) ||
            (p.sku && p.sku.toLowerCase().includes(text))
        );
        categoryTitle.innerText = text ? `Resultados para "${text}"` : 'Todos los Productos';
        renderProductos(filtrados);
    });

    // --- CARRITO ---

    function setupEventListeners() {
        cartBtn.addEventListener('click', openCart);
        closeCartBtn.addEventListener('click', closeCart);
        cartOverlay.addEventListener('click', closeCart);
        
        navCategoryList.querySelector('li[data-id="all"]').addEventListener('click', function() {
            filterByCategory('all', 'Todos los Productos', this);
        });

        checkoutBtn.addEventListener('click', processCheckout);
    }

    window.addToCart = function(productId) {
        const product = allProducts.find(p => p.id === productId);
        if (!product) return;

        const existingItem = cart.find(item => item.productoId === productId);
        
        if (existingItem) {
            if (existingItem.cantidad < product.stockActual) {
                existingItem.cantidad++;
            } else {
                showToast(`Solo quedan ${product.stockActual} u.`, true);
                return;
            }
        } else {
            cart.push({
                productoId: product.id,
                nombre: product.nombre,
                precio: product.precioVenta,
                cantidad: 1,
                maxStock: product.stockActual,
                imagen: getImageUrl(product.nombre, product.imagenUrl)
            });
        }

        saveCart();
        showToast(`Agregado al carro`, false);
        
        if (cartDrawer.classList.contains('active')) {
            renderCart();
        }
    };

    window.updateQty = function(productId, delta) {
        const item = cart.find(i => i.productoId === productId);
        if (!item) return;

        item.cantidad += delta;
        
        if (item.cantidad <= 0) {
            cart = cart.filter(i => i.productoId !== productId);
        } else if (item.cantidad > item.maxStock) {
            item.cantidad = item.maxStock;
            showToast('Límite de stock', true);
        }

        saveCart();
        renderCart();
    };

    function saveCart() {
        localStorage.setItem('protech_cart', JSON.stringify(cart));
        updateCartBadge();
    }

    function updateCartBadge() {
        const totalItems = cart.reduce((sum, item) => sum + item.cantidad, 0);
        cartBadge.innerText = totalItems;
        if (totalItems > 0) {
            cartBadge.style.transform = 'scale(1.2)';
            setTimeout(() => cartBadge.style.transform = 'scale(1)', 200);
        }
    }
    
    // Timer logica movida a checkout.js exclusivamente

    function openCart() {
        renderCart();
        cartOverlay.classList.add('active');
        cartDrawer.classList.add('active');
    }

    function closeCart() {
        cartOverlay.classList.remove('active');
        cartDrawer.classList.remove('active');
    }

    function renderCart() {
        cartItemsContainer.innerHTML = '';
        let total = 0;

        if (cart.length === 0) {
            cartItemsContainer.innerHTML = '<div style="text-align:center; margin-top:2rem"><p style="color:var(--text-muted);margin-bottom:1rem">El carro está vacío</p><button class="checkout-btn" onclick="document.getElementById(\'closeCartBtn\').click()" style="background:var(--brand-dark)">Seguir Comprando</button></div>';
            cartTotal.innerText = 'S/ 0.00';
            checkoutBtn.style.display = 'none';
            return;
        }

        checkoutBtn.style.display = 'block';
        checkoutBtn.disabled = false;
        checkoutBtn.style.opacity = '1';

        cart.forEach(item => {
            const subtotal = item.precio * item.cantidad;
            total += subtotal;

            const div = document.createElement('div');
            div.className = 'cart-item';
            div.innerHTML = `
                <img src="${item.imagen}" class="cart-item-img">
                <div class="cart-item-info">
                    <div class="cart-item-title">${item.nombre}</div>
                    <div class="cart-item-price">S/ ${item.precio.toFixed(2)}</div>
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-top:0.5rem">
                        <div class="cart-item-controls">
                            <button class="qty-btn" onclick="updateQty(${item.productoId}, -1)">-</button>
                            <span>${item.cantidad}</span>
                            <button class="qty-btn" onclick="updateQty(${item.productoId}, 1)">+</button>
                        </div>
                        <div style="font-weight:700; color:var(--brand-dark)">S/ ${subtotal.toFixed(2)}</div>
                    </div>
                </div>
            `;
            cartItemsContainer.appendChild(div);
        });

        cartTotal.innerText = `S/ ${total.toFixed(2)}`;
    }

    async function processCheckout() {
        if (cart.length === 0) return;

        checkoutBtn.innerText = 'PROCESANDO...';
        checkoutBtn.disabled = true;

        try {
            // Verificar si hay sesión intentando un endpoint protegido o simplemente redirigiendo
            // Spring Security se encarga de interceptar y enviar al /login si no hay sesión
            window.location.href = '/tienda/checkout';
        } catch (e) {
            showToast('Error de conexión', true);
            checkoutBtn.innerText = 'IR A PAGAR';
            checkoutBtn.disabled = false;
        }
    }

    function showToast(msg, isError = false) {
        const toast = document.getElementById('toast');
        const icon = document.getElementById('toastIcon');
        const text = document.getElementById('toastMessage');
        
        text.innerText = msg;
        icon.innerText = isError ? '❌' : '✅';
        toast.style.borderLeftColor = isError ? 'var(--danger)' : 'var(--success)';
        
        toast.classList.add('show');
        setTimeout(() => toast.classList.remove('show'), 3000);
    }
});
