async function adminLogin() {
    Notiflix.Loading.pulse("Wait...", {
        clickToClose: false,
        svgColor: '#0284c7'
    });

    let email = document.getElementById("email");
    let password = document.getElementById("password");

    const adminLoginObj = {
        email: email.value,
        password: password.value
    };

    try {
        const response = await fetch("api/admins/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(adminLoginObj)
        });

        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                Notiflix.Report.success(
                    'The Furniture Store',
                    data.message,
                    'Okay',
                    () => {
                        window.location = "admin.html";
                    }
                );
            } else {
                Notiflix.Notify.failure(data.message);
            }
        } else {
            Notiflix.Notify.failure("Admin login failed! Please try again.");
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    } finally {
        Notiflix.Loading.remove(1000);
    }
}
