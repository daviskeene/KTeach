let user_json = JSON.parse(localStorage.getItem('user'));
if (user_json === null) {
    window.location.replace("authentication.html");
    throw new Error("Incorrect login attempt!");
}
document.getElementById("username").innerText = "Hi, ".concat(user_json["first_name"]);
// Get assignments from currently logged in user
fetch('http://localhost:8080/api/assignments/'+user_json["id"])
    .then((res) => {
        return res.json()
    })
    .then((data) => {
        data.forEach((assignment) =>  {
            const {title, description, problem, test, id} = assignment;
            let ref = "assignment.html?id=".concat(id);
            let result =
                '<h1>' + title + '</h1>'+
                '<p>' + description + '</p>' +
                '<a class="btn btn-primary" href='+ref+ ' id="username">View Assignment</a>'+
                '<hr>';
            document.getElementById('assignments').innerHTML += result;
        })
    });