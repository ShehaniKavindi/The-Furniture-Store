document.addEventListener('DOMContentLoaded', function() {
    restoreSearchPreferences();
    loadCategories().then(() => searchProducts(1));

    document.getElementById('search-keyword').addEventListener('keydown', function(event) {
        if (event.key === 'Enter') searchProducts();
    });
});

async function loadCategories() {
    try {
        const response = await fetch('api/products/categories');
        const data = await response.json();
        const categoryGroup = document.getElementById('search-category');
        categoryGroup.innerHTML = '';
        if (!data.status || !data.data) return;

        const savedValues = (categoryGroup.dataset.savedValue || '').split(',').filter(Boolean);
        data.data.forEach(category => {
            const pill = document.createElement('button');
            pill.type = 'button'; pill.className = 'category-pill'; pill.textContent = category.name;
            pill.dataset.categoryId = category.id; pill.setAttribute('role', 'checkbox');
            const isSelected = savedValues.includes(String(category.id));
            pill.classList.toggle('is-selected', isSelected);
            pill.setAttribute('aria-checked', String(isSelected));
            pill.addEventListener('click', function() {
                const nowSelected = !pill.classList.contains('is-selected');
                pill.classList.toggle('is-selected', nowSelected);
                pill.setAttribute('aria-checked', String(nowSelected));
                searchProducts();
            });
            categoryGroup.appendChild(pill);
        });
    } catch (e) {
        showSearchMessage('Failed to load categories.', false);
    }
}

function toggleFilterPanel() {
    const panel = document.getElementById('search-filter-row');
    const toggleBtn = document.getElementById('search-filter-toggle');
    const isOpen = panel.classList.toggle('is-open');
    toggleBtn.setAttribute('aria-expanded', String(isOpen));
    toggleBtn.classList.toggle('is-active', isOpen);
}

function resetSearchFilters() {
    document.getElementById('search-keyword').value = '';
    document.getElementById('search-sort').value = 'recent';
    document.getElementById('search-min-price').value = '';
    document.getElementById('search-max-price').value = '';
    document.getElementById('search-in-stock').checked = false;
    document.querySelectorAll('#search-category .category-pill.is-selected').forEach(pill => {
        pill.classList.remove('is-selected');
        pill.setAttribute('aria-checked', 'false');
    });
    localStorage.removeItem('tfs-search-preferences');
    searchProducts(1);
}

function updateActiveFilterCount() {
    let count = 0;
    if (document.getElementById('search-min-price').value) count++;
    if (document.getElementById('search-max-price').value) count++;
    if (document.getElementById('search-in-stock').checked) count++;
    count += document.querySelectorAll('#search-category .category-pill.is-selected').length;
    const badge = document.getElementById('search-filter-count');
    if (count > 0) { badge.hidden = false; badge.textContent = String(count); }
    else badge.hidden = true;
}

async function searchProducts(page) {
    updateActiveFilterCount();
    const productContainer = document.getElementById('search-products');
    productContainer.innerHTML = '<div class="search-status">Searching products...</div>';
    const payload = {
        keyword: document.getElementById('search-keyword').value,
        sort: document.getElementById('search-sort').value,
        categoryIds: Array.from(document.querySelectorAll('#search-category .category-pill.is-selected')).map(pill => Number(pill.dataset.categoryId)),
        minPrice: Number(document.getElementById('search-min-price').value),
        maxPrice: Number(document.getElementById('search-max-price').value),
        inStockOnly: document.getElementById('search-in-stock').checked,
        page: page || 1,
        pageSize: 12
    };
    localStorage.setItem('tfs-search-preferences', JSON.stringify({
        keyword: payload.keyword, sort: payload.sort, categoryIds: payload.categoryIds,
        minPrice: payload.minPrice || '', maxPrice: payload.maxPrice || '', inStockOnly: payload.inStockOnly
    }));
    try {
        const response = await fetch('api/advanced-search/search-data', {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
        });
        renderProducts(await response.json());
    } catch (e) {
        productContainer.innerHTML = '<div class="search-status">Search failed. Please try again.</div>';
    }
}

function renderProducts(data) {
    const productContainer = document.getElementById('search-products');
    const resultCount = document.getElementById('search-result-count');
    if (!data.status) {
        productContainer.innerHTML = '<div class="search-status">' + (data.message || 'Failed to load products.') + '</div>';
        resultCount.textContent = '0 products';
        return;
    }
    const products = data.data || [];
    resultCount.textContent = (data.count || 0) + ((data.count || 0) === 1 ? ' product' : ' products');
    if (products.length === 0) {
        productContainer.innerHTML = '<div class="search-status">No products found.</div>';
        return;
    }
    productContainer.innerHTML = '';
    products.forEach(product => productContainer.appendChild(createSearchProductCard(product)));
    renderPagination(data.currentPage || 1, data.totalPages || 0);
}

function renderPagination(currentPage, totalPages) {
    const container = document.getElementById('search-pagination');
    container.innerHTML = '';
    if (totalPages <= 1) return;
    const addButton = (label, page, disabled) => {
        const button = document.createElement('button');
        button.type = 'button'; button.className = 'btn btn-outline-dark'; button.textContent = label; button.disabled = disabled;
        button.onclick = () => searchProducts(page); container.appendChild(button);
    };
    addButton('Previous', currentPage - 1, currentPage === 1);
    for (let page = Math.max(1, currentPage - 2); page <= Math.min(totalPages, currentPage + 2); page++) addButton(String(page), page, page === currentPage);
    addButton('Next', currentPage + 1, currentPage === totalPages);
}

function restoreSearchPreferences() {
    try {
        const preferences = JSON.parse(localStorage.getItem('tfs-search-preferences') || '{}');
        document.getElementById('search-keyword').value = preferences.keyword || '';
        document.getElementById('search-sort').value = preferences.sort || 'recent';
        document.getElementById('search-min-price').value = preferences.minPrice || '';
        document.getElementById('search-max-price').value = preferences.maxPrice || '';
        document.getElementById('search-in-stock').checked = Boolean(preferences.inStockOnly);
        document.getElementById('search-category').dataset.savedValue = (preferences.categoryIds || []).join(',');
    } catch (e) { localStorage.removeItem('tfs-search-preferences'); }
}

function createSearchProductCard(product) {
    const card = document.createElement('div');
    card.className = 'product-card search-product-card';
    card.onclick = function() { window.location.href = 'single-product-view.html?id=' + product.productId; };
    card.innerHTML =
        '<img src="' + product.image + '" alt="' + escapeHtml(product.title) + '">' +
        '<span class="search-product-category">' + escapeHtml(product.categoryName || 'Furniture') + '</span>' +
        '<h4>' + escapeHtml(product.title) + '</h4>' +
        '<p class="price">$' + Number(product.price).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + '</p>';
    return card;
}

function escapeHtml(value) {
    return String(value || '').replace(/[&<>"']/g, function(character) {
        return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[character];
    });
}

function showSearchMessage(message, status) {
    if (window.Notiflix) status ? Notiflix.Notify.success(message) : Notiflix.Notify.failure(message);
}
