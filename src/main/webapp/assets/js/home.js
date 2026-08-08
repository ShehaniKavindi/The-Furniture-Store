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
