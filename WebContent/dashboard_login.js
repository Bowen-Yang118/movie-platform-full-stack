
    $(document).ready(function() {
        let DashboardLoginForm = $("#DashboardLoginForm");
        DashboardLoginForm.submit(submitDashboardLoginForm);
    });


function handleDashboardLoginResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle dashboard login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success") {
        window.location.replace("dashboard.html");
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#dashboard-error-message").text(resultDataJson["message"]);
    }
}

function submitDashboardLoginForm(formSubmitEvent) {
    console.log("submit dashboard login form");
    formSubmitEvent.preventDefault();
    let requestData = {
        email: $("#email").val(),
        password: $("#password").val()
    };
    console.log(requestData.email);
    console.log(requestData.password);

    $.ajax(
        "_dashboard/login", {
            method: "POST",
            data: requestData,
            success: handleDashboardLoginResult
        }
    );
}

