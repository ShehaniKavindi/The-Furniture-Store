class FooterContent extends HTMLElement {
    connectedCallback() {
        this.innerHTML = `
            <footer class="footer">
                <div class="footer__inner">
             
                    <!-- Top Row -->
                    <div class="footer__top">
             
                        <!-- Brand Column -->
                        <div class="footer__brand">
                            <div class="footer__logo">The Furniture Store</div>
                            <p class="footer__tagline">
                                Thoughtful design for everyday living. From cozy sofas to elegant
                                statement pieces, we create spaces that feel warm, inviting, and totally you.
                            </p>
                            <div class="footer__socials">
                                <a href="#" class="footer__social-link" aria-label="Instagram">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="2" width="20" height="20" rx="5"/><circle cx="12" cy="12" r="4"/><circle cx="17.5" cy="6.5" r="0.5" fill="currentColor" stroke="none"/></svg>
                                </a>
                                <a href="#" class="footer__social-link" aria-label="Pinterest">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2C6.477 2 2 6.477 2 12c0 4.236 2.636 7.855 6.356 9.312-.088-.791-.167-2.005.035-2.868.181-.78 1.172-4.97 1.172-4.97s-.299-.598-.299-1.482c0-1.388.806-2.428 1.808-2.428.853 0 1.267.64 1.267 1.408 0 .858-.546 2.14-.828 3.33-.236.995.499 1.806 1.476 1.806 1.772 0 3.138-1.867 3.138-4.563 0-2.387-1.716-4.057-4.164-4.057-2.837 0-4.5 2.127-4.5 4.327 0 .857.33 1.775.741 2.276a.3.3 0 0 1 .069.286c-.076.313-.244.995-.277 1.134-.044.183-.145.222-.334.134C6.4 15.9 5.5 14.07 5.5 12.57c0-2.993 2.174-5.74 6.271-5.74 3.292 0 5.852 2.346 5.852 5.48 0 3.27-2.062 5.902-4.923 5.902-.962 0-1.867-.5-2.176-1.088l-.593 2.207c-.213.823-.79 1.854-1.176 2.482.888.275 1.83.423 2.805.423 5.523 0 10-4.477 10-10S17.523 2 12 2z"/></svg>
                                </a>
                                <a href="#" class="footer__social-link" aria-label="Facebook">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z"/></svg>
                                </a>
                                <a href="#" class="footer__social-link" aria-label="TikTok">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M9 12a4 4 0 1 0 4 4V4a5 5 0 0 0 5 5"/></svg>
                                </a>
                            </div>
                        </div>
             
                        <!-- Shop Column -->
                        <div class="footer__col">
                            <h3 class="footer__col-title">Shop</h3>
                            <ul class="footer__links">
                                <li><a href="#">Signature Sofas</a></li>
                                <li><a href="#">Gather &amp; Dine</a></li>
                                <li><a href="#">Study &amp; Office</a></li>
                                <li><a href="#">New Arrivals</a></li>
                                <li><a href="#">Sale</a></li>
                            </ul>
                        </div>
             
                        <!-- Help Column -->
                        <div class="footer__col">
                            <h3 class="footer__col-title">Help</h3>
                            <ul class="footer__links">
                                <li><a href="#">Delivery &amp; Returns</a></li>
                                <li><a href="#">Care Guide</a></li>
                                <li><a href="#">Track My Order</a></li>
                                <li><a href="#">FAQ</a></li>
                                <li><a href="#">Contact Us</a></li>
                            </ul>
                        </div>
             
                        <!-- Newsletter Column -->
                        <div class="footer__col footer__col--newsletter">
                            <h3 class="footer__col-title">Stay in Touch</h3>
                            <p class="footer__newsletter-desc">
                                New arrivals, design inspiration, and exclusive offers — straight to your inbox.
                            </p>
                            <div class="footer__newsletter-form">
                                <input type="email" placeholder="Your email address" aria-label="Email address" />
                                <button type="button" aria-label="Subscribe">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg>
                                </button>
                            </div>
                        </div>
             
                    </div>
             
                    <!-- Divider -->
                    <div class="footer__divider"></div>
             
                    <!-- Bottom Row -->
                    <div class="footer__bottom">
                        <span class="footer__copy">&copy; 2026 The Furniture Store. All rights reserved.</span>
                        <nav class="footer__legal" aria-label="Legal links">
                            <a href="#">Privacy Policy</a>
                            <a href="#">Terms of Use</a>
                            <a href="#">Cookie Settings</a>
                        </nav>
                    </div>
             
                </div>
            </footer>
        `;

        this.loadFooterCategoryLinks();
    }

    // async loadFooterCategoryLinks() {
    //     try {
    //         const response = await fetch('api/products/categories');
    //         const data = await response.json();
    //
    //         if (!data.status || !data.data) {
    //             return;
    //         }
    //
    //         this.querySelectorAll('.footer-category-link').forEach(link => {
    //             const categoryName = link.dataset.categoryName;
    //             const category = data.data.find(item =>
    //                 String(item.name || '').toLowerCase() === categoryName.toLowerCase()
    //             );
    //
    //             if (category) {
    //                 link.href = 'category-view.html?categoryId=' + category.id
    //                     + '&categoryName=' + encodeURIComponent(category.name);
    //             }
    //         });
    //     } catch (e) {
    //         console.error('Failed to load footer category links.', e);
    //     }
    // }
}

customElements.define('footer-content', FooterContent);
