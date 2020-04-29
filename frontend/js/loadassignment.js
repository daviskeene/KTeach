let user_json = JSON.parse(localStorage.getItem('user'));
document.getElementById("sidebar-title").innerText = "Classroom ".concat(user_json["classroom_id"]);
function getUrlVars() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}

let assignment_id = getUrlVars()["id"];
console.log(assignment_id);

fetch('http://localhost:8080/api/firestore/Assignments/'+assignment_id)
    .then((res) => {
        return res.json()
    })
    .then((data) => {
        const {title, description, problem, test, id} = data;
        console.log(data);
        document.getElementById("name").innerText = title;
        document.getElementById("description").innerText = description;
        let button = document.getElementById("download");
        button.setAttribute('href', problem);
        let testpath = document.getElementById("testpath");
        testpath.setAttribute('value', test);
    });