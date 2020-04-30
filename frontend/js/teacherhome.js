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
document.getElementById('content').innerHTML = "";
courses.forEach((course) => {
    // Get the name of the course
    fetch('http://localhost:8080/api/firestore/Classrooms/'+course)
        .then((response) => {
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
                    assignments.forEach((assignment) => {
                        const {title, description, problem, test, id} = assignment;
                        let result =
                            '<h1>' + title + '</h1>'+
                            '<p>' + description + '</p>' +
                            '<a class="btn btn-primary" type="button" href="" id="username" data-toggle="modal" data-target="#modalAssignmentForm" data-title="'+title+'" data-desc="'+description+'"' +
                            ' data-problem="'+problem+'" data-test="'+test+'" data-id="'+id+'">Edit Assignment</a>'+
                            '<hr>';
                        document.getElementById('content').innerHTML += result;
                    })

                }
            } else {
                let json = JSON.parse(localStorage.getItem('user'));
                document.getElementById("banner-text").innerText = "Hello, "+json["first_name"]+"! Click on a class to the left to view, edit and add assignments.";
            }
        })
});

$('#modalAssignmentForm').on('show.bs.modal', function (event) {
    let button = $(event.relatedTarget);
    let title = button.data('title');
    let desc = button.data('desc');
    let problem = button.data('problem');
    let test = button.data('test');
    let id = button.data('id');

    let modal = $(this);
    $('#defaultForm-title').val(title);
    $('#defaultForm-description').val(desc);
    $('#defaultForm-problem').val(problem);
    $('#defaultForm-test').val(test);
    $('#defaultForm-id').val(id);
});

$('#modal-form').on('submit', function (e) {
    e.preventDefault();
    if ($("#defaultForm-id").val() !== "") {
        let data = {
            "id" : $("#defaultForm-id").val(),
            "title" : $("#defaultForm-title").val(),
            "description" : $("#defaultForm-description").val(),
            "problem" : $("#defaultForm-problem").val(),
            "tests" : $("#defaultForm-test").val(),
        };
        $.ajax({
            url : 'http://localhost:8080/api/firestore/update/Assignments/'+$("#defaultForm-id").val(),
            type: "POST",
            data: JSON.stringify(data),
            contentType: "application/json",
            success: function (data) {
                location.reload();
            },
            fail: function (data) {
                console.log(data);
            }
        });
    } else {
        let classroom_id = getUrlVars()["classroom"];
        let data = {
            "classroom_id" : classroom_id,
            "title" : $("#defaultForm-title").val(),
            "description" : $("#defaultForm-description").val(),
            "problem" : $("#defaultForm-problem").val(),
            "tests" : $("#defaultForm-test").val(),
        };
        $.ajax({
            url : 'http://localhost:8080/api/firestore/add/assignment/',
            type: "POST",
            data: JSON.stringify(data),
            contentType: "application/json",
            success: function (data) {
                location.reload();
            },
            fail: function (data) {
                console.log(data);
            }
        });
    }
});