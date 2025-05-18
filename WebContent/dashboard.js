
$(document).ready(function () {
    // addStarForm.submit(submitAddStarForm);
    // addMovieForm.submit(submitAddMovieForm);
    // $("#addStarForm").on("submit", submitAddStarForm);
    // $("#addMovieForm").on("submit", submitAddMovieForm);
    // $("#getMetadata").click(retrieveDatabaseMetadata);
    $("#getMetadata").click(function () {
        retrieveDatabaseMetadata();
        $("#hideMetadata").show();  // Show the hide button
    });

    $("#hideMetadata").click(function () {
        $("#metadata").empty();  // Clear the metadata content
        $("#hideMetadata").hide();  // Hide the button
    });

    $("#logoutButton").click(function () {
        logout();
    });
});
function logout() {
    window.location.replace("dashboard_login.html");
}

function showTemporaryMessage(selector, message, color, duration = 1000) {
    $(selector).text(message).css("color", color).show();

    // Hide the message after the specified duration
    setTimeout(function() {
        $(selector).fadeOut("slow", function() {
            $(this).text("").show(); // Clear text and reset visibility
        });
    }, duration);
}

function submitAddStarForm() {
    console.log("submit AddStar form");
    // formSubmitEvent.preventDefault();
    let requestData = {
        action: "add_star",
        name: $("#star_name").val(),
        birth_year: $("#star_birth_year").val() || null
    };
    $.ajax({
        url: "_dashboard",
        method: "POST",
        data: requestData,
        success: function (response) {
            response = JSON.parse(response);
            console.log("Star added successfully:", response);
            // $("#add-star-message").text(response.message).css("color", "green");
            showTemporaryMessage("#add-star-message", response.message, "green");
            // addStarForm.trigger("reset");
            $("#addStarForm")[0].reset()
        },
        error: function (xhr) {
            console.error("Error:", xhr.responseText);
            showTemporaryMessage("#add-star-message", "Error: " + xhr.responseText, "red");
            // $("#add-star-message").text("Error: " + xhr.responseText).css("color", "red");
        }
    });
}

function submitAddMovieForm() {
    console.log("submit AddMovie form");
    // formSubmitEvent.preventDefault();
    let requestData = {
        action: "add_movie",
        title: $("#title").val(),
        year: $("#year").val(),
        director: $("#director").val(),
        star: $("#star").val(),
        genre: $("#genre").val()
    };
    $.ajax({
        url: "_dashboard",
        method: "POST",
        data: requestData,
        success: function (response) {
            response = JSON.parse(response);
            console.log("Movie added successfully:", response);
            // $("#add-movie-message").text(response.message).css("color", "green");
            showTemporaryMessage("#add-movie-message", response.message, "green");
            // addMovieForm.trigger("reset");
            $("#addMovieForm")[0].reset();
        },
        error: function (xhr) {
            console.error("Error:", xhr.responseText);
            // $("#add-movie-message").text("Error: " + xhr.responseText).css("color", "red");
            showTemporaryMessage("#add-movie-message", "Error: " + xhr.responseText, "red");
        }
    });
}

function retrieveDatabaseMetadata() {
    console.log("Fetching Database Metadata...");
    $.ajax({
        url: "_dashboard",
        method: "POST",
        data: { action: "get_metadata" },
        success: function (response) {
            console.log("Metadata retrieved:", response);
            if (typeof response === "string") {
                try {
                    response = JSON.parse(response);
                } catch(e) {
                    console.error("Error parsing JSON response:", e);
                    $("#metadata").text("Error parsing JSON response").css("color", "red");
                    return;
                }
            }

            if (!response.metadata || !Array.isArray(response.metadata)) {
                console.error("Error: metadata is undefined or not an array!");
                $("#metadata").text("Error: metadata not found in response").css("color", "red");
                return;
            }
            let metadataDiv = $("#metadata");
            metadataDiv.empty();
            // Create a container for the grid layout
            let gridContainer = $('<div class="metadata-grid"></div>');

            response.metadata.forEach(table => {
                let tableBlock = `
                    <div class="metadata-table">
                        <h3>${table.table_name}</h3>
                        <ul>
                    `;
                // let tableInfo = `<h3>${table.table_name}</h3><ul>`;
                if(table.columns && Array.isArray(table.columns)) {
                    table.columns.forEach(col => {
                        tableBlock += `<li>${col.column_name} (${col.data_type})</li>`;
                    });
                } else {
                    tableBlock += "<li>No column data available</li>";
                }
                tableBlock += "</ul></div>";
                gridContainer.append(tableBlock);
            });
            metadataDiv.html(gridContainer);
        },
        error: function (xhr) {
            console.error("Error retrieving metadata:", xhr.responseText);
            $("#metadata").text("Error: " + xhr.responseText).css("color", "red");
        }
    });
}

