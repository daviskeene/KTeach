const userAction = () => {
    fetch('http://localhost:8080/api/firestore/Students/test')
        .then(response => {
            return response.json()
        })
        .then(data => {
            document.getElementById('result').innerText = JSON.stringify(data);
        });
};