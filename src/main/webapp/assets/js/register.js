async function register() {
    Notiflix.Loading.dots("Loading...", {
        clickToClose: false,
        svgColor: "#f5006a"
    });

    const fname = document.getElementById("fname").value;
    const lname = document.getElementById("lname").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    const userData = {
        fname: fname,
        lname: lname,
        email: email,
        password: password
    };

    try {
        const response = await fetch('api/users', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        });

        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                Notiflix.Report.success(
                    "The Furniture Store",
                    data.message,
                    "OK",
                    () => {
                        window.location = "verify-account.html?email=" + email;
                    }
                );
            } else {
                Notiflix.Notify.failure(data.message,{
                    position: 'center-top'
                });
            }
        } else {
            Notiflix.Notify.failure("Registering failed. Please try again.", {
                position: 'center-top'
            });
        }
    } catch (e) {
        Notiflix.Notify.failure("An error occurred. Please try again.", {
            position: 'center-top'
        });
    } finally {
        Notiflix.Loading.remove(1000);
    }

}