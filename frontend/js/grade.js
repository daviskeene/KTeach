document.querySelector('#upload').addEventListener('change', event => {
    handleImageUpload(event);
});

const handleImageUpload = event => {
    let user_json = JSON.parse(localStorage.getItem('user'));
    let student_id = user_json["id"];
    let testpath = document.getElementById("testpath").getAttribute("value");
    // Clear output
    document.getElementById('score').innerText = "";
    document.getElementById('autograder').innerHTML = "";

    const files = event.target.files;
    const formData = new FormData();
    formData.append('file', files[0]);
    formData.append('test', testpath);
    console.log(student_id);
    console.log(formData.get('file'));
    console.log(formData.get('test'));

    // Start spinner
    document.getElementById('upload-spinner').style.opacity = "100%";

    fetch('http://localhost:8080/api/upload/' + student_id, {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            document.getElementById('upload-spinner').style.opacity = "0%";
            document.getElementById('score').innerText += 'Score: ' + data.grade[0] + ' / ' + data.grade[1];
            data.cases.forEach((testcase) => {
                let result =
                    '<h4>'+testcase+'</h4>';
                document.getElementById('autograder').innerHTML += result;
            });
        })
};
