// Read email from URL query param
const urlParams = new URLSearchParams(window.location.search);
const email = urlParams.get('email');

if (!email) {
    window.location.href = 'login.html';
}

document.getElementById('display-email').textContent = email;

async function verifyAccount() {
    Notiflix.Loading.pulse("Verifying...", {
        clickToClose: false,
        svgColor: '#0284c7'
    });

    const verificationCode = document.getElementById('verification-code').value;

    const data = {
        email: email,
        verificationCode: verificationCode
    };

    try {
        const response = await fetch('api/users/verify', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if (response.ok) {
            const result = await response.json();
            if (result.status) {
                Notiflix.Report.success(
                    'The Furniture Store',
                    result.message,
                    'OK',
                    () => {
                        window.location.href = 'login.html';
                    }
                );
            } else {
                Notiflix.Notify.failure(result.message, { position: 'center-top' });
            }
        } else {
            Notiflix.Notify.failure('Verification failed. Please try again.', { position: 'center-top' });
        }
    } catch (e) {
        Notiflix.Notify.failure('An error occurred. Please try again.', { position: 'center-top' });
    } finally {
        Notiflix.Loading.remove(1000);
    }
}