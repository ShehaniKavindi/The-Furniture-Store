class HeaderContent extends HTMLElement{
    connectedCallback(){
        this.innerHTML = `
         <!-- NAV -->
        <nav class="navbar navbar-expand-lg">
            <div class="container flex-column">
                <div class="w-100 d-flex align-items-center justify-content-between py-2">
                    <div class="logo-box"></div>
                    <h4 class="navbar-brand-header">THE FURNITURE STORE</h4>
                    <div class="d-flex gap-4">
                        <div class="icon-box"><i class="bi bi-person"></i></div>
                        <div class="icon-box"><i class="bi bi-cart2"></i></div>
                        <div class="icon-box"><i class="bi bi-heart"></i></div>
                        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navMenu">
                            <span class="navbar-toggler-icon"></span>
                        </button>
                    </div>
                </div>
                

                
                <div class="w-100 collapse navbar-collapse" id="navMenu">
                    <ul class="navbar-nav">
                        <li class="nav-item"><a class="nav-link" href="#">Home</a></li>
                        <li class="nav-item"><a class="nav-link" href="shop.html">Shop</a></li>
                        <li class="nav-item"><a class="nav-link" href="#">Signature Sofas</a></li>
                        <li class="nav-item"><a class="nav-link" href="#">Gather & Dine</a></li>
                        <li class="nav-item"><a class="nav-link" href="#">Study & Office</a></li>
                        <li class="nav-item"><a class="nav-link" href="#">About</a></li>
                        <li class="nav-item"><a class="nav-link" href="#">Contact</a></li>
                    </ul>
                </div>
            </div>
        </nav>
        `;
    }
}
customElements.define("header-content",HeaderContent);