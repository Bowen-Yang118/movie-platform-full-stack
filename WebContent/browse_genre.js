$(document).ready(function () {
    fetchGenres();
});

function fetchGenres() {
    $.ajax({
        url: "_browse_genre",
        method: "GET",
        success: function (response) {
            response = JSON.parse(response);
            console.log("Genres retrieved:", response);

            let genreList = $("#genreList");
            genreList.empty(); // Clear existing content

            response.forEach(genre => {
                let genreLink = `<a href="movielist.html?genre=${encodeURIComponent(genre.name)}" style="color: #007bff; font-weight: bold;">${genre.name}</a><br>`;
                genreList.append(genreLink);
            });
        },
        error: function (xhr) {
            console.error("Error retrieving genres:", xhr.responseText);
            $("#genreList").html("<p style='color: red;'>Error fetching genres.</p>");
        }
    });
}
