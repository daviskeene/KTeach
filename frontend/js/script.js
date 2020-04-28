let basePath = 'http://localhost:8080/'

const userAction = () => {
    fetch(basePath + 'api/firestore/Students/test')
        .then(response => {
            return response.json()
        })
        .then(data => {
            document.getElementById('result').innerText = JSON.stringify(data);
        });
};

// Make a post request to firebase and make a user based on entered fields... let's see how this goes.
async function addStudentFromLogin() {
    let url = basePath + 'api/firestore/add/student/';
    let fname = document.getElementById("fname").value;
    let lname = document.getElementById("lname").value;
    let classroom_id = document.getElementById("classroom_id").value;
    let pwd = document.getElementById("pwd").value;

    const response = await fetch(url, {
        method: 'POST',
        mode: 'cors',
        cache: 'no-cache',
        credentials: 'same-origin',
        headers: {
            'Content-Type' : 'application/json'
        },
        redirect: 'follow',
        body:
    })

};