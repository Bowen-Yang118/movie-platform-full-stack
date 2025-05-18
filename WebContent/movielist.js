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
    return results[2].replace(/\+/g, " ");
}

/**
 * Handles and formats search results from the API response
 * @param resultData jsonArray
 */
function handleSearchResults(resultData) {
    console.log("handleSearchResults: Populating search results");
    let resultsDiv = jQuery("#searchResults");
    resultsDiv.empty(); // Clear previous results

    if (resultData.length === 0) {
        resultsDiv.append("<p>No movies found.</p>");
        return;
    }

    let tableHTML = ""
    resultData.forEach(movie => {
        //sort the genre generated
        let genresHTML = movie.genres
            .sort((a, b) => a.name.localeCompare(b.name))
            .slice(0, 3)
            .map(g =>
            `<a href=movielist.html?genre=${g.name}"
                onclick="updateSessionAndNavigate('${g.name}')">${g.name}</a>`
            ).join(", ");




        console.log(genresHTML);
        let starsHTML = movie.stars
            .map(s => ({
                id: s.id,
                name: s.name,
                movie_count: parseInt(s.movie_count) || 0
            }))
            .sort((a, b) => b.movie_count !== a.movie_count
                ? b.movie_count - a.movie_count
                : a.name.localeCompare(b.name))
            .slice(0,3)
            .map(s => `<a href="single-star.html?id=${s.id}">${s.name}</a>`).join(", ");
        let titleHTML = `<a href="single-movie.html?id=${movie.id}"> ${movie.title} </a>`;


        tableHTML += `
            <tr>
                <td>${titleHTML}</td>
                <td>${movie.year}</td>
                <td>${movie.director}</td>
                <td>${genresHTML}</td>
                <td>${starsHTML}</td>
                <td>${movie.rating}</td>
                <td>
                <button class="add-to-cart" data-movie-id="${movie.id}" data-title="${movie.title}" data-price="${movie.price}">
                    Add
                </button>
                </td>
            </tr>`;
    });

    resultsDiv.append(tableHTML);
}

//genre navigate
function updateSessionAndNavigate(genre) {
    clearSession()
    sessionStorage.setItem("searchState", JSON.stringify({ currentPage: 1, genre }));
}

//clear search state--->do not modify cart or user
function clearSession() {
    sessionStorage.removeItem("searchState");
}




/**
 * get params from url and update searchstate in sessionStorage
 */
function getSearchParameters() {
    console.log("Extracted title:", getParameterByName("title"));
    let params = {
        title: getParameterByName('title') || "",
        year: getParameterByName('year') || "",
        director: getParameterByName('director') || "",
        star_name: getParameterByName('star_name') || "",
        genre: getParameterByName('genre') || "",
        sortBy: getParameterByName('sortBy') || "title",
        titleOrder: getParameterByName('titleOrder') || "asc",
        ratingOrder: getParameterByName('ratingOrder') || "asc",
        limit: parseInt(getParameterByName('limit')) || 10,
        currentPage: parseInt(getParameterByName('currentPage')) || 1
    };


    if (!sessionStorage.getItem("searchState")) {
        sessionStorage.setItem("searchState", JSON.stringify(params));
    } else {
        let searchState = JSON.parse(sessionStorage.getItem("searchState"));
        Object.assign(params, searchState);
    }

    return params;
}



/**
 * Ajax to fetch data from backend: GET
 */
function fetchMovies(page = 1) {
    let searchParams = getSearchParameters();

    // update cuurent page in session
    let searchState = JSON.parse(sessionStorage.getItem("searchState")) || {};
    searchState.currentPage = page;
    sessionStorage.setItem("searchState", JSON.stringify(searchState));
    let queryParams = `title=${searchParams.title}&year=${searchParams.year}&director=${searchParams.director}&genre=${searchParams.genre}&star_name=${searchParams.star_name}&sortBy=${searchParams.sortBy}&titleOrder=${searchParams.titleOrder}&ratingOrder=${searchParams.ratingOrder}&limit=${searchParams.limit}&page=${searchState.currentPage}`;

    console.log("API Request:", queryParams);
    $.ajax({
        url: "api/searchResults?" + queryParams,
        type: "GET",
        dataType: "json",
        success: function (data) {
            console.log("Received Data:", data);
            handleSearchResults(data);
            updatePaginationButtons(data.length);
        },
        error: function (xhr, status, error) {
            console.error("AJAX Error:", status, error);
            console.error("Server Response:", xhr.responseText);
        }
    });
}


/**
 * Prev/Next: Pagination Control
 */
function setupEventHandlers() {
    $("#prevPage").click(function () {
        let searchState = JSON.parse(sessionStorage.getItem("searchState")) || {};
        if (searchState.currentPage > 1) {
            searchState.currentPage -= 1;
            sessionStorage.setItem("searchState", JSON.stringify(searchState));
            fetchMovies(searchState.currentPage);
        }
    });

    $("#nextPage").click(function () {
        let searchState = JSON.parse(sessionStorage.getItem("searchState")) || {};
        searchState.currentPage += 1;
        sessionStorage.setItem("searchState", JSON.stringify(searchState));
        fetchMovies(searchState.currentPage);
    });

    $("#entriesPerPage").change(function () {
        let searchState = JSON.parse(sessionStorage.getItem("searchState")) || {};
        searchState.limit = parseInt($(this).val());
        searchState.currentPage = 1; // Always back to first page when change limit
        sessionStorage.setItem("searchState", JSON.stringify(searchState));
        fetchMovies(1);
    });

    $("#applySort").click(function () {
        applySort();
    });
}


/**
 * Pagination Boundry
 */
function updatePaginationButtons(length) {
    let searchState = JSON.parse(sessionStorage.getItem("searchState")) || {};
    $("#prevPage").prop("disabled", searchState.currentPage <= 1);
    $("#nextPage").prop("disabled", searchState.limit > length);
}


/**
 * Sort
 */
function applySort() {
    // Get existing parameters from the URL and session storage
    let searchState = getSearchParameters();

    // Update sorting-related param
    searchState.sortBy = $("#sortBy").val();
    searchState.titleOrder = $("#titleOrder").val();
    searchState.ratingOrder = $("#ratingOrder").val();
    searchState.limit = $("#entriesPerPage").val();
    searchState.currentPage = 1; // Reset to page 1 after sorting

    // save
    sessionStorage.setItem("searchState", JSON.stringify(searchState));

    // Build updated URL
    let params = new URLSearchParams();
    Object.keys(searchState).forEach(key => {
        if (searchState[key]) params.set(key, searchState[key]);
    });

    console.log("Updated search parameters:", params.toString());

    // Refresh
    window.location.href = "movielist.html?" + params.toString();
}


//Cart related: Update "cart" in session
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
 * Loading page
 */
$(document).ready(function () {
    let searchParams = getSearchParameters();

    // maintain sortBy`、`titleOrder`、`ratingOrder`、`limit` previous state
    $("#sortBy").val(searchParams.sortBy);
    $("#titleOrder").val(searchParams.titleOrder);
    $("#ratingOrder").val(searchParams.ratingOrder);
    $("#entriesPerPage").val(searchParams.limit);

    fetchMovies(searchParams.currentPage);
    setupEventHandlers();
});





