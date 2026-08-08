class HeaderContent extends HTMLElement{
    connectedCallback(){
        this.innerHTML = `
         <!-- NAV -->
        <nav class="navbar navbar-expand-lg">
            <div class="container flex-column">
                <div class="w-100 d-flex align-items-center justify-content-between py-2">
                    <div class="logo-box"></div>
                    <h4 class="navbar-brand-header">THE FURNITURE STORE</h4>
                    <div class="header-actions">
                        <a class="header-action-btn" href="search.html" aria-label="Search products" title="Search">
                            <svg viewBox="0 0 24 24" aria-hidden="true">
                                <circle cx="11" cy="11" r="7"></circle>
                                <path d="M20 20l-4.2-4.2"></path>
                            </svg>
                        </a>
                        <a class="header-action-btn" href="my-profile.html" aria-label="Open profile" title="Profile">
                            <svg viewBox="0 0 24 24" aria-hidden="true">
                                <path d="M20 21a8 8 0 0 0-16 0"></path>
                                <circle cx="12" cy="7" r="4"></circle>
                            </svg>
                        </a>
                        <a class="header-action-btn" href="wishlist.html" aria-label="Open wishlist" title="Wishlist">♡</a>
                        <a class="header-action-btn header-cart-btn" href="cart.html" aria-label="Open cart" title="Cart">
                            <svg viewBox="0 0 24 24" aria-hidden="true">
                                <circle cx="9" cy="20" r="1.5"></circle>
                                <circle cx="18" cy="20" r="1.5"></circle>
                                <path d="M2 3h3l2.2 11.2a2 2 0 0 0 2 1.6h7.7a2 2 0 0 0 2-1.5L20.5 7H6"></path>
                            </svg>
                        </a>
                        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navMenu">
                            <span class="navbar-toggler-icon"></span>
                        </button>
                    </div>
                </div>
                

                
                <div class="w-100 collapse navbar-collapse" id="navMenu">
                    <ul class="navbar-nav">
                        <li class="nav-item"><a class="nav-link" href="home.html">Home</a></li>
                        <li class="nav-item"><a class="nav-link" href="shop.html">Shop</a></li>
                        <li class="nav-item"><a class="nav-link header-category-link" href="shop.html" data-category-name="Signature Sofas">Signature Sofas</a></li>
                        <li class="nav-item"><a class="nav-link header-category-link" href="shop.html" data-category-name="Gather & Dine">Gather & Dine</a></li>
                        <li class="nav-item"><a class="nav-link header-category-link" href="shop.html" data-category-name="Study & Office">Study & Office</a></li>
                        <li class="nav-item"><a class="nav-link" href="about.html">About</a></li>
                        <li class="nav-item"><a class="nav-link" href="contact.html">Contact</a></li>
                    </ul>
                </div>
            </div>
        </nav>
        `;

        this.loadHeaderCategoryLinks();
    }

    async loadHeaderCategoryLinks() {
        try {
            const response = await fetch('api/products/categories');
            const data = await response.json();

            if (!data.status || !data.data) {
                return;
            }

            const categories = data.data;
            this.querySelectorAll('.header-category-link').forEach(link => {
                const categoryName = link.dataset.categoryName;
                const category = categories.find(item =>
                    String(item.name || '').toLowerCase() === categoryName.toLowerCase()
                );

                if (category) {
                    link.href = 'category-view.html?categoryId=' + category.id
                        + '&categoryName=' + encodeURIComponent(category.name);
                }
            });
        } catch (e) {
            console.error('Failed to load header category links.', e);
        }
    }
}
customElements.define("header-content",HeaderContent);
