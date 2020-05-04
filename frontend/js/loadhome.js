let user_json = JSON.parse(localStorage.getItem('user'));
if (user_json === null) {
    window.location.replace("authentication.html");
    throw new Error("Incorrect login attempt!");
}
document.getElementById("username").innerText = "Hi, ".concat(user_json["first_name"]);
document.getElementById('progress-bar-student').style.width = "50%";
// Get assignments from currently logged in user
fetch('http://167.99.53.134:8080/api/assignments/'+user_json["id"])
    .then((res) => {
        return res.json()
    })
    .then((data) => {
        let courseGrade = [0, 0];
        data.forEach((assignment) =>  {
            const {title, description, problem, test, id, deadline, score} = assignment;
            courseGrade[0] += score[0];
            courseGrade[1] += score[1];
            let ref = "assignment.html?id=".concat(id);
            let result =
                '<h1>' + title + '</h1>'+
                '<p>' + description + '</p>' +
                '<p class="my-2">Score: '+Number((score[0] / score[1])*100).toFixed(2)+'</p>'+
                '<p class="text-muted">Deadline: '+deadline+'</p>'+
                '<a class="btn btn-primary" href='+ref+ ' id="username">View Assignment</a>'+
                '<hr>';
            document.getElementById('assignments').innerHTML += result;
        });
        document.getElementById('progress-bar-student').style.width = "100%";
        document.getElementById('course-grade').innerText += 'Homework Average: ' + Number((courseGrade[0] / courseGrade[1])*100).toFixed(2);
    });