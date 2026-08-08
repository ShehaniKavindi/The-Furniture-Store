const emailInput = document.getElementById("email");
emailInput.value = new URLSearchParams(window.location.search).get("email") || "";

async function resetPassword() {
    const payload = {
        email: emailInput.value.trim(),
        verificationCode: document.getElementById("code").value.trim(),
        password: document.getElementById("password").value
    };

    try {
        const response = await fetch("api/users/reset-password", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(payload)
        });
        const data = await response.json();
        if (data.status) {
            Notiflix.Notify.success(data.message);
            setTimeout(() => window.location.assign("login.html"), 1200);
        } else {
            Notiflix.Notify.failure(data.message);
        }
    } catch (error) {
        Notiflix.Notify.failure("Password reset failed. Please try again.");
    }
}
