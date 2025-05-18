/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    console.log(resultData);
    console.log("handleResult: populating single star");


    let movieTableBodyElement = jQuery("#movie_table_body");

    let rowHTML = "";
    rowHTML += "<tr>";
    rowHTML += "<th>" + resultData["name"] + "</th>";
    rowHTML += "<th>" + resultData["birthYear"] + "</th>";

    let movieHTML = resultData["movies"]
        .map(movie => `<a href="single-movie.html?id=${movie.movieId}">${movie.title})</a>`)
        .join("<br>");
    rowHTML += "<th>" + movieHTML + "</th>";

    rowHTML += "</tr>";
    movieTableBodyElement.append(rowHTML);

}
function clearSession() {
    sessionStorage.removeItem("searchState");
}

//session restore
document.addEventListener("DOMContentLoaded", function () {
    let backButton = document.getElementById("backToMovieList");

    if (backButton) {
        backButton.addEventListener("click", function () {
            let searchState = sessionStorage.getItem("searchState");

            if (searchState) {
                let state = JSON.parse(searchState);

                let queryParams = new URLSearchParams();
                if (state.title) queryParams.set("title", state.title);
                if (state.year) queryParams.set("year", state.year);
                if (state.director) queryParams.set("director", state.director);
                if (state.star_name) queryParams.set("star_name", state.star_name);
                if (state.genre) queryParams.set("genre", state.genre);
                if (state.sortBy) queryParams.set("sortBy", state.sortBy.trim());
                if (state.titleOrder) queryParams.set("titleOrder", state.titleOrder.trim());
                if (state.ratingOrder) queryParams.set("ratingOrder", state.ratingOrder.trim());
                if (state.limit) queryParams.set("limit", state.limit);


                if (state.currentPage) queryParams.set("currentPage", state.currentPage);

                console.log(queryParams.toString());

                window.location.href = `movielist.html?${queryParams.toString()}`;
            } else {
                window.location.href = "movielist.html";
            }
        });
    }
});

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let starId = getParameterByName('id');
console.log(starId);
// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});





