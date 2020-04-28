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

const registerForm = document.getElementById("register-form");
registerForm.addEventListener('submit', function (e) {
    e.preventDefault();

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
            localStorage.setItem('user', JSON.stringify(text));
        })
        .then(function (error) {
            console.error(error);
            window.location.replace("studenthome.html");
        });
});

const loginForm = document.getElementById("login-form");
loginForm.addEventListener('submit', function (e) {
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
    console.log(json);

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
            localStorage.setItem('user', JSON.stringify(text));
            console.log(text);
        })
        .then(function (error) {
            console.error(error);
            window.location.replace("studenthome.html");
        });
});
