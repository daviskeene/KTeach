$(function() {
    $('#login-form-link').click(function(e) {
        $("#login-form").delay(100).fadeIn(100);
        $("#register-form").fadeOut(100);
        $('#register-form-link').removeClass('active');
        $(this).addClass('active');
        e.preventDefault();
    });
    $('#register-form-link').click(function(e) {
        $("#register-form").delay(100).fadeIn(100);
        $("#login-form").fadeOut(100);
        $('#login-form-link').removeClass('active');
        $(this).addClass('active');
        e.preventDefault();
    });

});

function getUrlVars() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}

if (getUrlVars().hasOwnProperty("classroom")) {
    document.getElementById('login-form').style.display = "none";
    document.getElementById('register-form').style.display = "block";
    document.getElementById('classroom_id').setAttribute('value', getUrlVars()["classroom"]);
}

const registerForm = document.getElementById("register-form");
registerForm.addEventListener('submit', function (e) {
    e.preventDefault();

    document.getElementById('alert').style.opacity = 0;
    document.getElementById('alert').innerText = "";

    const registerFormData = new FormData(this);
    var object = {};
    registerFormData.forEach(function (value, key) {
        object[key] = value;
    });
    var json = JSON.stringify(object);

    fetch('http://localhost:8080/api/firestore/add/student/', {
        method: 'POST',
        headers: {
            'Content-Type' : 'application/json'
        },
        redirect: 'follow',
        body: json
    })
        .then(function (response) {
            return response.json();
        })
        .then(function (text) {
            if (text === null) {
                document.getElementById('alert').style.opacity = 100;
                document.getElementById('alert').innerText = "Invalid classroom code!";
                throw new Error("Invalid classroom code!");
            } else {
                localStorage.setItem('user', JSON.stringify(text));
            }
        })
        .then(function (error) {
            console.error(error);
            window.location.replace("studenthome.html");
        });
});

const loginForm = document.getElementById("login-form");
loginForm.addEventListener('submit', function (e) {
    // Clear alert
    document.getElementById('alert').style.opacity = 0;
    document.getElementById('alert').innerText = "";
    e.preventDefault();

    const loginFormData = new FormData(this);
    var object = {};
    loginFormData.forEach(function (value, key) {
        if (key === 'email_login') {
            object['email'] = value;
        }
        if (key === 'password_login') {
            object['password'] = value;
        }
    });
    let json = JSON.stringify(object);

    fetch('http://localhost:8080/api/login/', {
        method: 'POST',
        headers: {
            'Content-Type' : 'application/json'
        },
        body: json
    })
        .then(function (response) {
            // Store returned object to local storage
            return response.json();
        })
        .then(function (text) {
            if (text === null) {
                document.getElementById('alert').style.opacity = 100;
                document.getElementById('alert').innerText = "Invalid Login Credentials!";
                throw new Error("Invalid login attempt!");
            } else {
                localStorage.setItem('user', JSON.stringify(text));
                console.log(text);
                window.location.replace("studenthome.html")
            }
        })
        .then(function (error) {
            console.error(error);
        });
});
