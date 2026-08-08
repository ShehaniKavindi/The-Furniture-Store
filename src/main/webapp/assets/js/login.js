async function login() {

    Notiflix.Loading.pulse("Wait...", {
        clickToClose: false,
        svgColor: '#0284c7'
    });

    let email = document.getElementById("email");
    let password = document.getElementById("password");

    const userLoginObj = {
        email: email.value,
        password: password.value,
        rememberMe: document.getElementById("rememberme").checked
    }

    try {
        const response = await fetch("api/users/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(userLoginObj)
        });

        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                Notiflix.Report.success(
                    'The Furniture Store',
                    data.message,
                    'Okay',
                    ()=> {
                        window.location = "home.html"
                    },
                );
            } else {
                Notiflix.Notify.failure(data.message);
            }
        }else{
            Notiflix.Notify.failure("Login failed! Please try again.");
        }
    }catch (e) {
        Notiflix.Notify.failure(e.message);
    }finally {
        Notiflix.Loading.remove(1000);
    }
}

async function forgotPassword() {
    const email = document.getElementById("email").value.trim();

    if (!email) {
        Notiflix.Notify.failure("email is required");
        return;
    }

    Notiflix.Loading.pulse("Sending...", {
        clickToClose: false,
        svgColor: '#0284c7'
    });

    try {
        const response = await fetch("api/users/forgot-password", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ email: email })
        });

        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                Notiflix.Notify.success(data.message);
            } else {
                Notiflix.Notify.failure(data.message);
            }
        } else {
            Notiflix.Notify.failure("Failed to send password email. Please try again.");
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    } finally {
        Notiflix.Loading.remove(1000);
    }
}
