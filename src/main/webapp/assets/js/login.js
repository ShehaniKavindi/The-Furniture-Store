async function login() {

    Notiflix.Loading.pulse("Wait...", {
        clickToClose: false,
        svgColor: '#0284c7'
    });

    let email = document.getElementById("email");
    let password = document.getElementById("password");

    const userLoginObj = {
        email: email.value,
        password: password.value
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