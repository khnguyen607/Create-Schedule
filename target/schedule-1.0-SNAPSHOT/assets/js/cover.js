const dropArea = document.querySelector(".drag-image"),
    dragText = dropArea.querySelector("h6"),
    button = dropArea.querySelector("button"),
    input = dropArea.querySelector("input");
let file;
var dataSchedules;
button.onclick = () => {
    input.click();
}

input.addEventListener("change", function () {

    file = this.files[0];
    dropArea.classList.add("active");
    viewfile();
});

dropArea.addEventListener("dragover", (event) => {
    event.preventDefault();
    dropArea.classList.add("active");
    dragText.textContent = "Release to Upload File";
});


dropArea.addEventListener("dragleave", () => {
    dropArea.classList.remove("active");
    dragText.textContent = "Drag & Drop to Upload File (.xlsx)";
});

dropArea.addEventListener("drop", (event) => {
    event.preventDefault();

    file = event.dataTransfer.files[0];
    viewfile();
});

function viewfile() {
    let fileType = file.type;
    console.log(fileType);
    let validExtensions = ["application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"];
    if (validExtensions.includes(fileType)) {
        const reader = new FileReader();

        reader.onload = function (event) {
            const data = new Uint8Array(event.target.result);
            const workbook = XLSX.read(data, { type: 'array' });
            const firstSheetName = workbook.SheetNames[0];
            const worksheet = workbook.Sheets[firstSheetName];
            const jsonData = XLSX.utils.sheet_to_json(worksheet);
            console.log(jsonData); // In dữ liệu JSON vào console (hoặc xử lý dữ liệu ở đây)
            SendData(jsonData);
        };

        reader.onerror = function (event) {
            console.error('File could not be read! Code ' + event.target.error.code);
        };

        reader.readAsArrayBuffer(file);
        dragText.textContent = "[" + file.name + "]"
    } else {
        alert("This is not an Excel File!");
        dropArea.classList.remove("active");
        dragText.textContent = "Drag & Drop to Upload File (.xlsx)";
    }
}

function SendData(jsonData) {
    // Convert JSON to string and append to FormData
    const formData = new URLSearchParams();
    formData.append('jsonData', JSON.stringify(jsonData));
    // Send data to server
    fetch('http://localhost:8080/first/test', {
        method: 'POST',
        body: formData,
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
        .then(response => response.json())
        .then(data => {
            // console.log('Server response:', data);
            dataSchedules = data.sort((a, b) => {
                return b.length - a.length
            })
            // set giá trị max course 
            document.querySelector("#maxCourses").value = dataSchedules[0].length
            document.querySelector("#maxCourses").max = dataSchedules[0].length;
            document.querySelector("#maxCourses").min = 0;

            // tạo checkbox các môn học
            var courses = document.querySelector(".form-filter .row.mt-3 .col")
            courses.innerHTML = ""
            jsonData.forEach((item, index) => {
                let course = document.createElement("div")
                course.classList.add("form-check");
                course.innerHTML = `
                    <input class="form-check-input" type="checkbox" value="${item.name}" id="courseCheckBox-${index}">
                    <label class="form-check-label" for="courseCheckBox-${index}">${item.name}</label>
                `
                courses.appendChild(course)
            })

            filterSchedule()
        })
        .catch(error => {
            console.error('Error:', error);
        });
}

function viewSchedule(jsonData) {

    document.querySelector(".tableTime").classList.remove("d-none")
    document.querySelectorAll(".tableTime thead th")[0].textContent = "Total: " + jsonData.length + " schedule"

    var dataBody = document.querySelector(".tableTime .dataBody")
    dataBody.innerHTML = ""
    jsonData.forEach((item, index) => {
        var tr = document.createElement("tr")
        tr.setAttribute("data-bs-toggle", "modal");
        tr.setAttribute("data-bs-target", "#exampleModal");
        tr.setAttribute("data-schedule", JSON.stringify(item));
        tr.innerHTML = `                          
                        <th scope="row">${index + 1}</th>
                        <td>${item.length}</td>
                    `
        dataBody.appendChild(tr)
    });

    // Sự kiện click vào 1 hàng 
    dataBody.querySelectorAll("tr").forEach(function (row, index) {
        row.addEventListener("click", function () {
            // Reset lịch
            let schedule = document.querySelector("#exampleModal .content")
            schedule.querySelectorAll(".shift").forEach(item => {
                item.innerHTML = ""
            })
            // console.log("Bạn đã click vào hàng số: " + (index + 1));
            let dataSchedule = JSON.parse(row.getAttribute("data-schedule"))
            dataSchedule.forEach(item => {
                schedule.querySelector(".shift.shift-" + (item.shift - 4)).innerHTML = `
                    <div class="accent-orange-gradient">
                        ${item.name}
                    </div>
                `
            })
        });
    });
}

function filterSchedule() {
    function isArraySubset(a, b) {
        // Kiểm tra độ dài của mảng a và b
        if (a.length > b.length) {
            return false; // Nếu độ dài của mảng a lớn hơn mảng b, mảng a không thể là mảng con của mảng b
        }

        // Sử dụng phương thức every() để kiểm tra xem mỗi phần tử của mảng a có tồn tại trong mảng b hay không
        return a.every(item => b.includes(item));
    }
    let maxCourse = parseInt(document.querySelector("#maxCourses").value);
    const checkedInputs = document.querySelectorAll('input[type="checkbox"]:checked');
    const checkedValues = Array.from(checkedInputs).map(input => input.value);
    const set = new Set(checkedValues);
    console.log(checkedValues);

    var filterData = dataSchedules.filter(item => {
        var toltal = 0;
        for (const i of item) {
            if (set.has(i.name)) {
                toltal++
            }
        }
        if (toltal<checkedValues.length) return false
        return item.length <= maxCourse;
    });

    viewSchedule(filterData)
}