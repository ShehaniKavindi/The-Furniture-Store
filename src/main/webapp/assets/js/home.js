document.addEventListener('DOMContentLoaded', () => {
    const countdown = document.getElementById('promotion-countdown');
    if (!countdown) return;

    const offerEnd = new Date();
    offerEnd.setHours(offerEnd.getHours() + 24);

    let timerId;
    const updateCountdown = () => {
        const remaining = Math.max(0, offerEnd.getTime() - Date.now());
        const hours = Math.floor(remaining / 3_600_000);
        const minutes = Math.floor((remaining % 3_600_000) / 60_000);
        const seconds = Math.floor((remaining % 60_000) / 1_000);
        countdown.textContent = remaining > 0
            ? `Free-delivery promotion ends in ${hours}h ${minutes}m ${seconds}s`
            : 'The free-delivery promotion has ended.';
        if (remaining === 0) clearInterval(timerId);
    };

    updateCountdown();
    timerId = setInterval(updateCountdown, 1000);
});

document.addEventListener('DOMContentLoaded', loadNewArrivals);

async function loadNewArrivals() {
    const productContainer = document.getElementById('new-arrival-products');
    if (!productContainer) return;

    const arrivalCategories = new Set([
        'study & office',
        'wardrobes',
        'chairs & stools'
    ]);

    try {
        const response = await fetch('api/products');
        if (!response.ok) throw new Error('Unable to load products.');

        const result = await response.json();
        const products = Array.isArray(result.data) ? result.data : [];
        const newArrivals = products
            .filter(product => arrivalCategories.has(String(product.categoryName || '').trim().toLowerCase()))
            .sort((first, second) => Number(second.id) - Number(first.id))
            .slice(0, 3);

        productContainer.replaceChildren();

        if (!result.status || newArrivals.length === 0) {
            const message = document.createElement('p');
            message.textContent = 'New arrivals will be available soon.';
            productContainer.append(message);
            return;
        }

        newArrivals.forEach(product => productContainer.append(createNewArrivalCard(product)));
    } catch (error) {
        console.error('Failed to load new arrivals.', error);
        productContainer.replaceChildren();
        const message = document.createElement('p');
        message.textContent = 'Unable to load new arrivals. Please try again later.';
        productContainer.append(message);
    }
}

function createNewArrivalCard(product) {
    const card = document.createElement('a');
    card.className = 'product-card';
    card.href = 'single-product-view.html?id=' + encodeURIComponent(product.id);
    card.style.textDecoration = 'none';
    card.style.color = 'inherit';

    const image = document.createElement('img');
    image.src = product.imagePaths && product.imagePaths.length > 0
        ? product.imagePaths[0]
        : 'assets/images/product-01.jpg';
    image.alt = product.title || 'New arrival';

    const title = document.createElement('h4');
    title.textContent = product.title || 'New arrival';

    const price = document.createElement('p');
    price.className = 'price';
    price.textContent = '$' + Number(product.price || 0).toLocaleString(undefined, {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });

    card.append(image, title, price);
    return card;
}
