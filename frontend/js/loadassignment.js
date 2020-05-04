let user_json = JSON.parse(localStorage.getItem('user'));
document.getElementById("sidebar-title").innerText = "Classroom ".concat(user_json["classroom_id"]);
document.getElementById('progress-bar-student').style.width = "25%";
document.getElementById('username').innerText = "Hi, "+user_json["first_name"];
function getUrlVars() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}

let assignment_id = getUrlVars()["id"];
console.log(assignment_id);

fetch('http://167.99.53.134:8080/api/firestore/Assignments/'+assignment_id)
    .then((res) => {
        return res.json()
    })
    .then(async (data) => {
        const {title, description, problem, test, id} = data;
        console.log(data);
        document.getElementById("name").innerText = title;
        document.getElementById("description").innerText = description;
        let button = document.getElementById("download");
        button.setAttribute('href', problem);
        let testpath = document.getElementById("testpath");
        testpath.setAttribute('value', test);
        document.getElementById('progress-bar-student').style.width = "100%";
        document.getElementById('assignment-id').innerText = id;

        // Load problem into editor
        let problemText = await fetch(problem);
        var editor = ace.edit("editor");
        editor.setTheme("ace/theme/chrome");
        editor.getSession().setMode("ace/mode/kotlin");
        editor.getSession().setValue(await problemText.text());
        editor.setOption("maxlines", 25);
    });

// fetch(document.getElementById('download').getAttribute('href'))
//     .then((res) => {
//         return res.text();
//     }).then((data) => {
//     let editor = document.getElementById('editor');
//     editor.innerText = data;
// });