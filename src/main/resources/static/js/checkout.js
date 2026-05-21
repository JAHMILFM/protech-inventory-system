document.addEventListener('DOMContentLoaded', () => {
    const cart = JSON.parse(localStorage.getItem('protech_cart')) || [];
    
    // Elements
    const summaryItems = document.getElementById('summaryItems');
    const summarySubtotal = document.getElementById('summarySubtotal');
    const summaryTotal = document.getElementById('summaryTotal');
    const btnPay = document.getElementById('btnPay');
    const payError = document.getElementById('payError');

    // Form Elements
    const txtDireccion = document.getElementById('direccion');
    const ccNumber = document.getElementById('ccNumber');
    const ccName = document.getElementById('ccName');
    const ccExp = document.getElementById('ccExp');
    const ccCvv = document.getElementById('ccCvv');
    const luhnError = document.getElementById('luhnError');

    // UI Elements
    const ccCard = document.getElementById('ccCard');
    const ccNumDisplay = document.getElementById('ccNumDisplay');
    const ccNameDisplay = document.getElementById('ccNameDisplay');
    const ccExpDisplay = document.getElementById('ccExpDisplay');
    const ccCvvDisplay = document.getElementById('ccCvvDisplay');

    // Initial check
    if (cart.length === 0) {
        window.location.href = '/tienda';
        return;
    }

    renderSummary();
    setupCardInteractions();
    checkFormValidity();

    // ---- TIMER LOGIC ----
    let timerInterval = null;
    function startTimerIfNeeded() {
        let expireTime = localStorage.getItem('protech_cart_expire');
        if (!expireTime) {
            expireTime = Date.now() + 15 * 60 * 1000;
            localStorage.setItem('protech_cart_expire', expireTime);
        }
        
        if (timerInterval) clearInterval(timerInterval);
        
        timerInterval = setInterval(() => {
            const now = Date.now();
            const diff = parseInt(expireTime) - now;
            
            if (diff <= 0) {
                clearInterval(timerInterval);
                document.getElementById('countdownClock').innerText = "00:00";
                showErrorModal();
                return;
            }
            
            const minutes = Math.floor(diff / 60000);
            const seconds = Math.floor((diff % 60000) / 1000);
            const clockEl = document.getElementById('countdownClock');
            if (clockEl) {
                clockEl.innerText = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
            }
        }, 1000);
    }
    
    startTimerIfNeeded();

    function showErrorModal() {
        document.getElementById('expiredModal').classList.add('active');
        localStorage.removeItem('protech_cart');
        localStorage.removeItem('protech_cart_expire');
        btnPay.disabled = true;
    }

    // ---- SUMMARY RENDER ----
    function renderSummary() {
        summaryItems.innerHTML = '';
        let sub = 0;
        cart.forEach(item => {
            const row = document.createElement('div');
            row.className = 's-item';
            const itemSub = item.precio * item.cantidad;
            sub += itemSub;
            row.innerHTML = `<span>${item.cantidad}x ${item.nombre}</span> <span style="font-weight:600">S/ ${itemSub.toFixed(2)}</span>`;
            summaryItems.appendChild(row);
        });

        const total = sub + 15; // 15.00 delivery
        summarySubtotal.innerText = `S/ ${sub.toFixed(2)}`;
        summaryTotal.innerText = `S/ ${total.toFixed(2)}`;
    }

    // ---- CARD UX & VALIDATION ----
    function setupCardInteractions() {
        // Number Formatting & Luhn
        ccNumber.addEventListener('input', (e) => {
            let val = e.target.value.replace(/\D/g, '');
            let formatted = val.match(/.{1,4}/g)?.join(' ') || '';
            e.target.value = formatted;
            
            // UI Update
            ccNumDisplay.innerText = formatted || '#### #### #### ####';

            if (val.length >= 13) {
                if (luhnCheck(val)) {
                    ccNumber.classList.add('valid');
                    ccNumber.classList.remove('invalid');
                    luhnError.style.display = 'none';
                } else {
                    ccNumber.classList.add('invalid');
                    ccNumber.classList.remove('valid');
                    luhnError.style.display = 'block';
                }
            } else {
                ccNumber.classList.remove('valid', 'invalid');
                luhnError.style.display = 'none';
            }
            checkFormValidity();
        });

        // Name
        ccName.addEventListener('input', (e) => {
            e.target.value = e.target.value.toUpperCase();
            ccNameDisplay.innerText = e.target.value || 'NOMBRE DEL TITULAR';
            checkFormValidity();
        });

        // Expiration (MM/YY)
        ccExp.addEventListener('input', (e) => {
            let val = e.target.value.replace(/\D/g, '');
            if (val.length >= 2) {
                val = val.substring(0,2) + '/' + val.substring(2,4);
            }
            e.target.value = val;
            ccExpDisplay.innerText = val || 'MM/YY';
            checkFormValidity();
        });

        // CVV Flip Animation
        ccCvv.addEventListener('focus', () => ccCard.classList.add('flipped'));
        ccCvv.addEventListener('blur', () => ccCard.classList.remove('flipped'));
        ccCvv.addEventListener('input', (e) => {
            e.target.value = e.target.value.replace(/\D/g, '');
            ccCvvDisplay.innerText = '*'.repeat(e.target.value.length) || '***';
            checkFormValidity();
        });

        // Address
        txtDireccion.addEventListener('input', checkFormValidity);
    }

    // Algoritmo de Luhn para validación real de tarjetas
    function luhnCheck(num) {
        let arr = (num + '')
          .split('')
          .reverse()
          .map(x => parseInt(x));
        let lastDigit = arr.splice(0, 1)[0];
        let sum = arr.reduce((acc, val, i) => (i % 2 !== 0 ? acc + val : acc + ((val * 2) % 9) || 9), 0);
        sum += lastDigit;
        return sum % 10 === 0;
    }

    function checkFormValidity() {
        const numVal = ccNumber.value.replace(/\D/g, '');
        const isValid = txtDireccion.value.trim().length > 5 &&
                        luhnCheck(numVal) &&
                        ccName.value.trim().length > 3 &&
                        ccExp.value.length === 5 &&
                        ccCvv.value.length >= 3;
        
        btnPay.disabled = !isValid;
        if (isValid) {
            btnPay.innerText = 'PAGAR AHORA';
        } else {
            btnPay.innerText = 'COMPLETA LOS DATOS';
        }
    }

    // ---- CHECKOUT SUBMISSION ----
    btnPay.addEventListener('click', async () => {
        btnPay.disabled = true;
        btnPay.innerText = 'PROCESANDO PAGO...';
        payError.style.display = 'none';

        const payload = {
            items: cart.map(i => ({ productoId: i.productoId, cantidad: i.cantidad })),
            direccionEnvio: txtDireccion.value.trim(),
            metodoPago: "TARJETA_CREDITO_TERMINACION_" + ccNumber.value.slice(-4)
        };

        try {
            const res = await fetch('/api/tienda/checkout', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const data = await res.json();

            if (res.ok && data.id) { // Success returns Pedido object
                localStorage.removeItem('protech_cart');
                localStorage.removeItem('protech_cart_expire');
                document.getElementById('stickyTimerBar').classList.remove('active');
                
                // Efecto de éxito
                document.querySelector('.checkout-main').innerHTML = `
                    <div style="text-align:center; padding: 4rem 2rem; background:white; border-radius:8px; box-shadow:0 1px 2px rgba(0,0,0,0.05)">
                        <h1 style="font-size:4rem; margin-bottom:1rem">🎉</h1>
                        <h2 style="color:var(--success); margin-bottom:1rem">¡Pago Aprobado!</h2>
                        <p style="color:var(--text-muted); margin-bottom:2rem">Tu pedido <strong>${data.numeroPedido}</strong> ha sido confirmado y está en preparación.</p>
                        <a href="/tienda" class="btn-pay" style="text-decoration:none; display:inline-block; width:auto; padding: 1rem 2rem">VOLVER A LA TIENDA</a>
                    </div>
                `;
                document.querySelector('aside').style.display = 'none';
            } else {
                if (res.status === 401) {
                    window.location.href = '/login';
                } else {
                    if (data.error && (data.error.includes('JPA') || data.error.includes('Optimistic') || data.error.includes('Locking'))) {
                        showErrorModal();
                    } else {
                        payError.innerText = data.error || 'Ocurrió un error al procesar el pago.';
                        payError.style.display = 'block';
                        btnPay.disabled = false;
                        btnPay.innerText = 'PAGAR AHORA';
                    }
                }
            }
        } catch (e) {
            payError.innerText = 'Error de conexión con la pasarela.';
            payError.style.display = 'block';
            btnPay.disabled = false;
            btnPay.innerText = 'PAGAR AHORA';
        }
    });
});
