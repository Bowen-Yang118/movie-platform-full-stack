/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


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

    console.log("handleResult: populating star info from resultData");
    console.log(resultData);
    let MovieInfoElement = jQuery("#movie_info");
    // title;
    // year;
    // director
    // rating.
    // all of the genres;
    // all of the stars (hyperlinked);

    // append two html <p> created to the h3 body, which will refresh the page
    MovieInfoElement.append("<p>Title: " + resultData["title"] + "</p>");


    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    let rowHTML = "";
    rowHTML += "<tr>";
    rowHTML += "<th>" + resultData["year"] + "</th>";
    rowHTML += "<th>" + resultData["director"] + "</th>";

    //genre
    let genreHTML = resultData["genres"].map(g =>
        `<a href="movielist.html?genre=${g.name}" 
        onclick="updateSessionAndNavigate('${g.name}')">${g.name}</a>`
    ).join("<br>");
    rowHTML += "<th>" + genreHTML + "</th>";

    rowHTML += "<th>" + resultData["rating"] + "</th>";

    //stars
    let starsHTML = resultData["stars"]
        .map(star => `<a href="single-star.html?id=${star.id}">${star.name}</a>`)
        .join("<br>");
    rowHTML += "<th>" + starsHTML + "</th>";

    //add to cart
    rowHTML += `<th>
    <button class="add-to-cart" data-movie-id="${resultData["id"]}" data-title="${resultData["title"]}"
            data-price="${resultData["price"]}">
        Add
    </button>
    </th>`;


    rowHTML += "</tr>";
    movieTableBodyElement.append(rowHTML);

}

function updateSessionAndNavigate(genre) {
    clearSession();
    sessionStorage.setItem("searchState", JSON.stringify({currentPage: 1, genre}));
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
                Object.keys(state).forEach(key => {
                    if (state[key]) queryParams.set(key, state[key]); // Preserve ALL filters
                });

                console.log("Returning to Movie List with params:", queryParams.toString());

                window.location.href = `movielist.html?${queryParams.toString()}`;
            } else {
                window.location.href = "movielist.html";
            }
        });
    }
});


//add to cart
document.addEventListener("DOMContentLoaded", function () {
    document.body.addEventListener("click", function (event) {
        if (event.target.classList.contains("add-to-cart")) {
            let movieId = event.target.getAttribute("data-movie-id");
            let title = event.target.getAttribute("data-title");
            let price = parseFloat(event.target.getAttribute("data-price"));

            let cart = JSON.parse(sessionStorage.getItem("cart")) || {};

            if (cart[movieId]) {
                cart[movieId].quantity += 1;
            } else {
                cart[movieId] = { title, quantity: 1, price };
            }

            sessionStorage.setItem("cart", JSON.stringify(cart));
            console.log(cart);
            alert("Added to cart successfully!");
        }
    });
});

/**
 * Once this .js is loaded, following scripts will be executed by the browser: Loading
 */

// Get id from URL
let movieId = getParameterByName('id');
console.log(movieId);
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/single-movie?id=" + movieId,
    success: (resultData) => handleResult(resultData)
});



