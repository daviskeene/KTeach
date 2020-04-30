// Load classes into the sidebar
let user_json = JSON.parse(localStorage.getItem('user'));
let courses = user_json["courses"];
function getUrlVars() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}
// Reset dynamic elements on page
document.getElementById('about-content').style.display = "none";
document.getElementById('banner-text').style.display = "none";
document.getElementById('content').innerHTML = "";
let progress = document.getElementById('progress-bar');
courses.forEach((course) => {
    // Get the name of the course
    fetch('http://localhost:8080/api/firestore/Classrooms/'+course)
        .then((response) => {
            progress.style.width = "50%";
            return response.json()
        })
        .then((data) => {
            const {assignments, id, name, students, teacher} = data;
            let res = '<a href="?classroom='+id+'"class="list-group-item list-group-item-action bg-light">'+name+'</a>';
            document.getElementById('classes-nav').innerHTML += res;
            // Check active classroom
            if (getUrlVars().hasOwnProperty('classroom')) {
                if (getUrlVars()["classroom"] === id) {
                    // Load the page content
                    document.getElementById("banner-text").innerText = "Assignments for "+name;
                    document.getElementById('banner-text').style.display = "block";
                    assignments.forEach((assignment) => {
                        const {title, description, problem, test, id} = assignment;
                        let result =
                            '<h1>' + title + '</h1>'+
                            '<p>' + description + '</p>' +
                            '<a class="btn btn-primary" type="button" href="" id="username" data-toggle="modal" data-target="#modalAssignmentForm" data-title="'+title+'" data-desc="'+description+'"' +
                            ' data-problem="'+problem+'" data-test="'+test+'" data-id="'+id+'">Edit Assignment</a>'+
                            '<a class="btn btn-danger mb-0 mx-2" style="color: white" type="button" data-toggle="modal" data-target="#modalConfirmation" data-id="'+id+'" data-title="'+title+'">Delete Assignment</a>'+
                            '<hr>';
                        document.getElementById('content').innerHTML += result;
                    });
                    document.getElementById('add-assignment-div').style.display = "block";
                    progress.style.width = "100%";
                }
            } else {
                progress.style.width = "100%";
                document.getElementById('about-content').style.display = "inline";
                let json = JSON.parse(localStorage.getItem('user'));
                document.getElementById("banner-text").innerText += " " + json["first_name"]+"!";
                document.getElementById('banner-text').style.display = "block";
            }
        })
});