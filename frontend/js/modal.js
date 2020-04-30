// Please don't roast me for using js and jquery, modal documentation only uses jQuery :/
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

$('#modalConfirmation').on('show.bs.modal', function (event) {
    let button = $(event.relatedTarget);
    let id = button.data('id');
    let title = button.data('title');
    $('#confirmForm-assignment').val(id);
    $('#confirmForm-title').val(title);
});

$('#confirmation-form').on('submit', function (e) {
    e.preventDefault();
    $.ajax({
        url : 'http://localhost:8080/api/firestore/delete/Assignments/'+$("#confirmForm-assignment").val(),
        type: "POST",
        data: {

        },
        contentType: "application/json",
        success: function (data) {
            location.reload();
        },
        fail: function () {
            console.log("Could not delete assignment");
        }
    });
});