/**
 * LIBRERÍA LUZ — SISTEMA DE CASA
 * Lógica interactiva del cliente, filtros en tiempo real, modales y administración
 */

document.addEventListener('DOMContentLoaded', () => {
  // Estado local
  const state = {
    products: [],
    modality: 'todo',
    category: 'todas',
    search: '',
    selectedProduct: null
  };

  // Elementos DOM principales
  const productsGrid = document.getElementById('productsGrid');
  const searchInput = document.getElementById('searchInput');
  const clearSearchBtn = document.getElementById('clearSearchBtn');
  const resultsCount = document.getElementById('resultsCount');
  const noResultsState = document.getElementById('noResultsState');
  const resetFiltersBtn = document.getElementById('resetFiltersBtn');
  const modalityFilters = document.getElementById('modalityFilters');
  const categoryTags = document.getElementById('categoryTags');
  const heroPromoCard = document.getElementById('heroPromoCard');

  // Modal de Detalle / Compra / Préstamo
  const productModalBackdrop = document.getElementById('productModalBackdrop');
  const btnCloseProductModal = document.getElementById('btnCloseProductModal');
  const modalCoverTag = document.getElementById('modalCategoryTag');
  const modalCoverTitle = document.getElementById('modalCoverTitle');
  const modalCoverAuthor = document.getElementById('modalCoverAuthor');
  const modalDetailCategory = document.getElementById('modalDetailCategory');
  const modalDetailTitle = document.getElementById('modalDetailTitle');
  const modalDetailAuthor = document.getElementById('modalDetailAuthor');
  const modalDetailPrice = document.getElementById('modalDetailPrice');
  const modalDetailStock = document.getElementById('modalDetailStock');
  const modalDetailDesc = document.getElementById('modalDetailDesc');
  const modalityOptionsGroup = document.getElementById('modalityOptionsGroup');
  const rentDaysGroup = document.getElementById('rentDaysGroup');
  const rentDaysSelect = document.getElementById('rentDaysSelect');
  const summaryLabel = document.getElementById('summaryLabel');
  const summaryTotalValue = document.getElementById('summaryTotalValue');
  const customerNameInput = document.getElementById('customerNameInput');
  const customerEmailInput = document.getElementById('customerEmailInput');
  const btnConfirmAction = document.getElementById('btnConfirmAction');

  // Admin Drawer
  const adminModalBackdrop = document.getElementById('adminModalBackdrop');
  const btnOpenAdmin = document.getElementById('btnOpenAdmin');
  const btnCloseAdmin = document.getElementById('btnCloseAdmin');
  const adminTabs = document.querySelectorAll('.admin-tab');
  const adminProductsTableBody = document.getElementById('adminProductsTableBody');
  const newProductForm = document.getElementById('newProductForm');
  const btnResetCatalog = document.getElementById('btnResetCatalog');
  const recentTxList = document.getElementById('recentTxList');

  // Stats Elements
  const statTotalItems = document.getElementById('statTotalItems');
  const statTotalUnits = document.getElementById('statTotalUnits');
  const statTotalLoans = document.getElementById('statTotalLoans');
  const statTotalSales = document.getElementById('statTotalSales');

  // Toast
  const toast = document.getElementById('toastNotification');

  // Formateador de moneda en pesos colombianos
  const formatCOP = (amount) => {
    return '$ ' + Math.round(amount).toLocaleString('es-CO');
  };

  // Función Toast
  const showToast = (message) => {
    toast.textContent = message;
    toast.classList.add('show');
    setTimeout(() => {
      toast.classList.remove('show');
    }, 3500);
  };

  // Cargar catálogo inicial desde el API
  const fetchProducts = async () => {
    try {
      const url = new URL('/api/products', window.location.origin);
      if (state.search) url.searchParams.append('search', state.search);
      if (state.modality && state.modality !== 'todo') url.searchParams.append('modality', state.modality);
      if (state.category && state.category !== 'todas') url.searchParams.append('category', state.category);

      const res = await fetch(url);
      const data = await res.json();
      state.products = data.products || [];
      renderProductsGrid(state.products);
    } catch (err) {
      console.error('Error cargando productos:', err);
    }
  };

  // Renderizar la grilla de productos
  const renderProductsGrid = (products) => {
    productsGrid.innerHTML = '';

    resultsCount.textContent = `Mostrando ${products.length} ejemplar${products.length === 1 ? '' : 'es'}`;

    if (products.length === 0) {
      noResultsState.classList.remove('hidden');
      return;
    }

    noResultsState.classList.add('hidden');

    products.forEach((p) => {
      const article = document.createElement('article');
      article.className = 'product-item';
      article.dataset.id = p.id;
      article.dataset.category = p.category.toLowerCase();

      const isOutOfStock = p.stock <= 0;
      const stockText = isOutOfStock
        ? '<span class="product-stock out-of-stock">Agotado</span>'
        : `<span class="product-stock">${p.stock} disponible${p.stock === 1 ? '' : 's'}</span>`;

      article.innerHTML = `
        <div class="book-cover" data-id="${p.id}">
          <span class="category-tag">${p.category.toUpperCase()}</span>
          <div class="cover-content">
            <h3 class="cover-title">${p.title}</h3>
          </div>
          <div class="cover-footer">
            <span class="cover-author">${p.author}</span>
          </div>
          <div class="cover-overlay">
            <span class="overlay-action">Ver detalle / Adquirir</span>
          </div>
        </div>
        <div class="product-meta">
          <h4 class="product-title" data-id="${p.id}">${p.title}</h4>
          <p class="product-author">${p.author}</p>
          <div class="product-footer">
            <span class="product-price">${formatCOP(p.price)}</span>
            ${stockText}
          </div>
        </div>
      `;

      // Eventos para abrir modal
      article.querySelector('.book-cover').addEventListener('click', () => openModal(p));
      article.querySelector('.product-title').addEventListener('click', () => openModal(p));

      productsGrid.appendChild(article);
    });
  };

  // Búsqueda en tiempo real con debounce
  let searchTimeout = null;
  searchInput.addEventListener('input', (e) => {
    const val = e.target.value.trim();
    state.search = val;
    clearSearchBtn.style.display = val ? 'block' : 'none';

    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
      fetchProducts();
    }, 200);
  });

  clearSearchBtn.addEventListener('click', () => {
    searchInput.value = '';
    state.search = '';
    clearSearchBtn.style.display = 'none';
    fetchProducts();
  });

  // Filtros por modalidad (Todo, Venta, Renta, Préstamo)
  modalityFilters.addEventListener('click', (e) => {
    if (e.target.classList.contains('pill-btn')) {
      modalityFilters.querySelectorAll('.pill-btn').forEach(btn => btn.classList.remove('active'));
      e.target.classList.add('active');
      state.modality = e.target.dataset.modality;
      fetchProducts();
    }
  });

  // Filtros por categoría desde "La Casa"
  categoryTags.addEventListener('click', (e) => {
    if (e.target.classList.contains('cat-pill')) {
      const cat = e.target.dataset.category;
      if (state.category === cat) {
        state.category = 'todas';
        e.target.classList.remove('active');
      } else {
        categoryTags.querySelectorAll('.cat-pill').forEach(btn => btn.classList.remove('active'));
        e.target.classList.add('active');
        state.category = cat;
      }
      // Scroll suave hacia la mesa
      document.getElementById('mesa').scrollIntoView({ behavior: 'smooth' });
      fetchProducts();
    }
  });

  // Botón reset filtros en estado vacío
  resetFiltersBtn.addEventListener('click', () => {
    state.search = '';
    state.modality = 'todo';
    state.category = 'todas';
    searchInput.value = '';
    clearSearchBtn.style.display = 'none';
    modalityFilters.querySelectorAll('.pill-btn').forEach(btn => btn.classList.remove('active'));
    modalityFilters.querySelector('[data-modality="todo"]').classList.add('active');
    categoryTags.querySelectorAll('.cat-pill').forEach(btn => btn.classList.remove('active'));
    fetchProducts();
  });

  // Promo card en Hero: abre el libro especial destacado
  heroPromoCard.addEventListener('click', () => {
    const featured = state.products.find(p => p.is_featured) || state.products[5] || state.products[0];
    if (featured) openModal(featured);
  });

  // ================= MODAL DETALLE & CHECKOUT =================
  const openModal = (product) => {
    state.selectedProduct = product;

    modalCoverTag.textContent = product.category.toUpperCase();
    modalCoverTitle.textContent = product.title;
    modalCoverAuthor.textContent = product.author;

    modalDetailCategory.textContent = product.category.toUpperCase();
    modalDetailTitle.textContent = product.title;
    modalDetailAuthor.textContent = `Por ${product.author}`;
    modalDetailPrice.textContent = formatCOP(product.price);
    modalDetailStock.textContent = `${product.stock} ejemplar${product.stock === 1 ? '' : 'es'} en anaquel`;
    modalDetailDesc.textContent = product.description || 'Una lectura selecta cuidada por el equipo de Librería Luz.';

    // Actualizar modalidades disponibles
    const hasVenta = product.modalities.includes('venta');
    const hasRenta = product.modalities.includes('renta');
    const hasPrestamo = product.modalities.includes('prestamo');

    const radioVenta = document.getElementById('radioVenta');
    const radioRenta = document.getElementById('radioRenta');
    const radioPrestamo = document.getElementById('radioPrestamo');

    radioVenta.style.display = hasVenta ? 'flex' : 'none';
    radioRenta.style.display = hasRenta ? 'flex' : 'none';
    radioPrestamo.style.display = hasPrestamo ? 'flex' : 'none';

    // Seleccionar la primera modalidad disponible por defecto
    if (hasVenta) {
      radioVenta.querySelector('input').checked = true;
    } else if (hasRenta) {
      radioRenta.querySelector('input').checked = true;
    } else if (hasPrestamo) {
      radioPrestamo.querySelector('input').checked = true;
    }

    updateRadioCardsVisual();
    updateModalSummary();

    // Botón deshabilitado si está agotado
    if (product.stock <= 0) {
      btnConfirmAction.disabled = true;
      btnConfirmAction.textContent = 'Agotado temporalmente';
      btnConfirmAction.style.opacity = '0.5';
      btnConfirmAction.style.cursor = 'not-allowed';
    } else {
      btnConfirmAction.disabled = false;
      btnConfirmAction.textContent = 'Confirmar adquisición';
      btnConfirmAction.style.opacity = '1';
      btnConfirmAction.style.cursor = 'pointer';
    }

    productModalBackdrop.classList.add('open');
  };

  const closeModal = () => {
    productModalBackdrop.classList.remove('open');
    state.selectedProduct = null;
  };

  btnCloseProductModal.addEventListener('click', closeModal);
  productModalBackdrop.addEventListener('click', (e) => {
    if (e.target === productModalBackdrop) closeModal();
  });

  // Cambiar selector de modalidad
  const updateRadioCardsVisual = () => {
    const radios = modalityOptionsGroup.querySelectorAll('.radio-card');
    radios.forEach(card => {
      const input = card.querySelector('input');
      if (input.checked) {
        card.classList.add('active');
      } else {
        card.classList.remove('active');
      }
    });
  };

  const updateModalSummary = () => {
    if (!state.selectedProduct) return;
    const selectedModality = document.querySelector('input[name="modalActionType"]:checked')?.value || 'venta';

    if (selectedModality === 'renta') {
      rentDaysGroup.classList.remove('hidden');
      const days = parseInt(rentDaysSelect.value, 10);
      const weeks = Math.max(1, Math.ceil(days / 7));
      const rentaTotal = Math.round(state.selectedProduct.price * 0.15 * weeks);
      summaryLabel.textContent = `Costo de renta (${days} días):`;
      summaryTotalValue.textContent = formatCOP(rentaTotal);
      btnConfirmAction.textContent = 'Confirmar Renta';
    } else if (selectedModality === 'prestamo') {
      rentDaysGroup.classList.add('hidden');
      summaryLabel.textContent = 'Préstamo de biblioteca:';
      summaryTotalValue.textContent = 'Gratis (Sistema de Casa)';
      btnConfirmAction.textContent = 'Registrar Préstamo';
    } else {
      rentDaysGroup.classList.add('hidden');
      summaryLabel.textContent = 'Total a pagar:';
      summaryTotalValue.textContent = formatCOP(state.selectedProduct.price);
      btnConfirmAction.textContent = 'Comprar Ejemplar';
    }
  };

  modalityOptionsGroup.addEventListener('change', () => {
    updateRadioCardsVisual();
    updateModalSummary();
  });

  rentDaysSelect.addEventListener('change', updateModalSummary);

  // Confirmar Acción (Comprar / Rentar / Prestar)
  btnConfirmAction.addEventListener('click', async () => {
    if (!state.selectedProduct) return;
    if (state.selectedProduct.stock <= 0) {
      alert('Este ejemplar se encuentra agotado.');
      return;
    }

    const customerName = customerNameInput.value.trim() || 'Lector de Casa';
    const customerEmail = customerEmailInput.value.trim();
    const actionType = document.querySelector('input[name="modalActionType"]:checked')?.value || 'venta';
    const days = parseInt(rentDaysSelect.value, 10) || 7;

    btnConfirmAction.disabled = true;
    btnConfirmAction.textContent = 'Procesando...';

    try {
      const res = await fetch('/api/checkout', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          productId: state.selectedProduct.id,
          type: actionType,
          quantity: 1,
          customerName,
          customerEmail,
          days
        })
      });

      const data = await res.json();

      if (res.ok && data.success) {
        // Actualizar stock en memoria local y UI
        state.selectedProduct.stock = data.newStock;
        closeModal();
        showToast(`✓ ${data.message} ¡Gracias, ${customerName}!`);
        fetchProducts(); // refrescar lista
        customerNameInput.value = '';
        customerEmailInput.value = '';
      } else {
        alert(data.error || 'Ocurrió un error al procesar la solicitud.');
      }
    } catch (err) {
      console.error(err);
      alert('Error de conexión con el servidor.');
    } finally {
      btnConfirmAction.disabled = false;
      btnConfirmAction.textContent = 'Confirmar adquisición';
    }
  });

  // ================= ADMIN DRAWER & METRICS =================
  const openAdminDrawer = async () => {
    adminModalBackdrop.classList.add('open');
    await loadAdminData();
  };

  const closeAdminDrawer = () => {
    adminModalBackdrop.classList.remove('open');
  };

  btnOpenAdmin.addEventListener('click', openAdminDrawer);
  btnCloseAdmin.addEventListener('click', closeAdminDrawer);
  adminModalBackdrop.addEventListener('click', (e) => {
    if (e.target === adminModalBackdrop) closeAdminDrawer();
  });

  // Tabs en Admin
  adminTabs.forEach(tab => {
    tab.addEventListener('click', () => {
      adminTabs.forEach(t => t.classList.remove('active'));
      tab.classList.add('active');

      const target = tab.dataset.tab;
      document.querySelectorAll('.admin-tab-content').forEach(content => {
        content.classList.remove('active');
      });

      if (target === 'inventario') document.getElementById('tabInventario').classList.add('active');
      if (target === 'nuevo') document.getElementById('tabNuevo').classList.add('active');
      if (target === 'historial') document.getElementById('tabHistorial').classList.add('active');
    });
  });

  const loadAdminData = async () => {
    try {
      const [statsRes, productsRes] = await Promise.all([
        fetch('/api/admin/stats'),
        fetch('/api/products?modality=todo')
      ]);

      const stats = await statsRes.json();
      const productsData = await productsRes.json();

      // Métricas
      statTotalItems.textContent = stats.totalItems;
      statTotalUnits.textContent = stats.totalUnits;
      statTotalLoans.textContent = stats.totalLoans;
      statTotalSales.textContent = formatCOP(stats.totalSales);

      // Tabla de inventario
      renderAdminProductsTable(productsData.products);

      // Historial
      renderAdminHistory(stats.recentTransactions);
    } catch (err) {
      console.error('Error al cargar datos de admin:', err);
    }
  };

  const renderAdminProductsTable = (products) => {
    adminProductsTableBody.innerHTML = '';
    products.forEach(p => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td><strong>${p.title}</strong><br><small style="color:var(--text-secondary)">${p.author}</small></td>
        <td>${p.category}</td>
        <td>${formatCOP(p.price)}</td>
        <td>
          <div class="stock-control">
            <button class="stock-btn" onclick="window.updateStock(${p.id}, ${p.stock - 1})">-</button>
            <span style="min-width:24px;text-align:center">${p.stock}</span>
            <button class="stock-btn" onclick="window.updateStock(${p.id}, ${p.stock + 1})">+</button>
          </div>
        </td>
        <td>
          <span style="font-size:0.75rem; color:${p.stock > 0 ? '#385723' : '#a83a32'}">
            ${p.stock > 0 ? 'En anaquel' : 'Agotado'}
          </span>
        </td>
      `;
      adminProductsTableBody.appendChild(tr);
    });
  };

  // Función global para actualizar stock desde tabla admin
  window.updateStock = async (productId, newStock) => {
    if (newStock < 0) return;
    try {
      const res = await fetch(`/api/admin/products/${productId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ stock: newStock })
      });
      if (res.ok) {
        showToast('Stock actualizado');
        await loadAdminData();
        await fetchProducts();
      }
    } catch (err) {
      console.error(err);
    }
  };

  const renderAdminHistory = (txList) => {
    if (!txList || txList.length === 0) {
      recentTxList.innerHTML = '<p class="text-muted">Aún no hay transacciones registradas.</p>';
      return;
    }

    recentTxList.innerHTML = '';
    txList.forEach(tx => {
      const div = document.createElement('div');
      div.className = 'recent-tx-item';
      div.innerHTML = `
        <div>
          <span class="tx-type-tag tx-type-${tx.type}">${tx.type}</span>
          <strong style="margin-left:6px">${tx.product_title}</strong>
          <div style="font-size:0.75rem;color:var(--text-secondary);margin-top:4px">
            Lector: ${tx.customer_name} • ${tx.created_at ? tx.created_at.substring(0, 16) : ''}
          </div>
        </div>
        <div style="text-align:right">
          <strong>${tx.type === 'prestamo' ? 'Préstamo' : formatCOP(tx.total_amount)}</strong>
        </div>
      `;
      recentTxList.appendChild(div);
    });
  };

  // Formulario nuevo ejemplar
  newProductForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const title = document.getElementById('newTitle').value.trim();
    const author = document.getElementById('newAuthor').value.trim();
    const category = document.getElementById('newCategory').value;
    const price = parseFloat(document.getElementById('newPrice').value) || 0;
    const stock = parseInt(document.getElementById('newStock').value, 10) || 0;
    const description = document.getElementById('newDescription').value.trim();

    const modalities = [];
    if (document.getElementById('modVenta').checked) modalities.push('venta');
    if (document.getElementById('modRenta').checked) modalities.push('renta');
    if (document.getElementById('modPrestamo').checked) modalities.push('prestamo');

    try {
      const res = await fetch('/api/admin/products', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title,
          author,
          category,
          price,
          stock,
          modalities: modalities.join(','),
          description
        })
      });

      if (res.ok) {
        showToast('✓ Ejemplar añadido al catálogo con éxito');
        newProductForm.reset();
        // Cambiar a tab inventario
        document.querySelector('.admin-tab[data-tab="inventario"]').click();
        await loadAdminData();
        await fetchProducts();
      }
    } catch (err) {
      console.error(err);
      alert('Error guardando el nuevo ejemplar.');
    }
  });

  // Restaurar inventario original
  btnResetCatalog.addEventListener('click', async () => {
    if (!confirm('¿Deseas restaurar el catálogo al estado original del diseño? Se restablecerán todos los productos y stocks.')) {
      return;
    }

    try {
      const res = await fetch('/api/admin/reset', { method: 'POST' });
      if (res.ok) {
        showToast('Catálogo restaurado a los 12 títulos originales.');
        await loadAdminData();
        await fetchProducts();
      }
    } catch (err) {
      console.error(err);
    }
  });

  // Inicializar productos al cargar la página
  fetchProducts();
});
