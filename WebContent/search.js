/*
 * CS 122B Project 4.
 * Implements full-text search and autocomplete with caching using sessionStorage
 */

// /**
//  * Handles searching movies when the user submits a query.
//  * Supports full-text search and redirects if an autocomplete suggestion is selected.
//  */
// function searchMovies() {
//     let title = document.getElementById("title").value;
//     let year = document.getElementById("year").value;
//     let director = document.getElementById("director").value;
//     let star = document.getElementById("star_name").value;
//
//     let params = new URLSearchParams();
//     if (title) params.append("title", title);
//     if (year) params.append("year", year);
//     if (director) params.append("director", director);
//     if (star) params.append("star_name", star);
//
//     // window.location.href = "movielist.html?" + params.toString();
//     if (!selectedMovieID) {
//         // Perform normal search if no autocomplete suggestion was selected
//         console.log("Performing Full-Text Search with parameters: " + params.toString());
//         window.location.href = "movielist.html?" + params.toString();
//     } else {
//         console.log("Redirecting to Movie Page: ID " + selectedMovieID);
//         window.location.href = "single-movie.html?id=" + selectedMovieID;
//     }
//
// }

/*
 * This function is called by the library when it needs to lookup a query.
 *
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
// function handleLookup(query, doneCallback) {
//     console.log("autocomplete initiated with query:", query);

//     // Do not search for queries less than 3 characters
//     if (query.length < 3) {
//         doneCallback({ suggestions: [] });
//         return;
//     }

//     // TODO: if you want to check past query results first, you can do it here
//     // Check SessionStorage cache first
//     let cachedResults = sessionStorage.getItem(query);
//     if (cachedResults) {
//         console.log("Using cached results for query:", query);
//         doneCallback({ suggestions: JSON.parse(cachedResults) });
//         return;
//     }

//     console.log("sending AJAX request to backend Java Servlet");
//     // sending the HTTP GET request to the Java Servlet endpoint 'api/autocomplete'
//     // with the query data
//     jQuery.ajax({
//         "method": "GET",
//         // generate the request url from the query.
//         // encode the query string to avoid errors caused by special characters
//         "url": "api/autocomplete?query=" + encodeURIComponent(query),
//         "success": function(data) {
//             // pass the data, query, and doneCallback function into the success handler
//             handleLookupAjaxSuccess(data, query, doneCallback);
//         },
//         "error": function(errorData) {
//             console.log("lookup ajax error");
//             console.log(errorData);
//         }
//     })
// }


// /*
//  * This function is used to handle the ajax success callback function.
//  * It is called by our own code upon the success of the AJAX request
//  *
//  * data is the JSON data string you get from your Java Servlet
//  *
//  */
// function handleLookupAjaxSuccess(data, query, doneCallback) {
//     console.log("lookup ajax successful. Query:", query);
//     console.log(typeof data);
//     // parse the string into JSON
//     var jsonData = JSON.parse(data);
//     console.log("Received suggestions:", jsonData);

//     // TODO: if you want to cache the result into a global variable you can do it here
//     // Cache the result in SessionStorage
//     sessionStorage.setItem(query, JSON.stringify(jsonData));

//     // call the callback function provided by the autocomplete library
//     // add "{suggestions: jsonData}" to satisfy the library response format according to
//     //   the "Response Format" section in documentation
//     doneCallback( { suggestions: jsonData } );
// }


// /*
//  * This function is the select suggestion handler function.
//  * When a suggestion is selected, this function is called by the library.
//  *
//  * You can redirect to the page you want using the suggestion data.
//  */
// function handleSelectSuggestion(suggestion) {
//     // TODO: jump to the specific result page based on the selected suggestion
//     console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movieID"]);

//     // Redirect to the single movie page
//     window.location.href = "single-movie.html?id=" + suggestion["data"]["movieID"];
// }

// /*
//  * This statement binds the autocomplete library with the input box element and
//  *   sets necessary parameters of the library.
//  *
//  * The library documentation can be find here:
//  *   https://github.com/devbridge/jQuery-Autocomplete
//  *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
//  *
//  */
// // $('#title') is to find element by the ID "autocomplete"
// $('#title').autocomplete({
//     // documentation of the lookup function can be found under the "Custom lookup function" section
//     lookup: function (query, doneCallback) {
//         handleLookup(query, doneCallback);
//     },
//     onSelect: function(suggestion) {
//         handleSelectSuggestion(suggestion);
//     },
//     // set delay time
//     deferRequestBy: 300,
//     // there are some other parameters that you might want to use to satisfy all the requirements
//     // TODO: add other parameters, such as minimum characters
//     minChars: 3 // Only search if at least 3 characters are typed
// });

// /*
//  * do normal full text search if no suggestion is selected
//  */
// function handleNormalSearch(query) {
//     console.log("doing normal search with query: " + query);
//     // TODO: you should do normal search here
//     // searchMovies()
//     // let title = document.getElementById("title").value;
//     let title = query;
//     let year = document.getElementById("year").value;
//     let director = document.getElementById("director").value;
//     let star = document.getElementById("star_name").value;

//     let params = new URLSearchParams();
//     if (title) params.append("title", title);
//     if (year) params.append("year", year);
//     if (director) params.append("director", director);
//     if (star) params.append("star_name", star);

//     // Perform normal search if no autocomplete suggestion was selected
//     console.log("Performing Full-Text Search with parameters: " + params.toString());
//     window.location.href = "movielist.html?" + params.toString();
// }

// // bind pressing enter key to a handler function
// $('#title').keypress(function(event) {
//     // keyCode 13 is the enter key
//     if (event.keyCode == 13) {
//         // pass the value of the input box to the handler function
//         handleNormalSearch($('#title').val());
//     }
// })

// /*
//  * Bind search button click to handleNormalSearch().
//  */
// $('#searchButton').click(function () {
//     handleNormalSearch($('#title').val());
// });

// 处理查询并返回建议项
function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated with query:", query);

    // Do not search for queries less than 3 characters
    if (query.length < 3) {
        doneCallback([]);
        return;
    }

    // Check SessionStorage cache first
    let cachedResults = sessionStorage.getItem(query);
    if (cachedResults !== null) {
        console.log("Using cached results for query:", query);
        // console.log("type of cached results:"+typeof cachedResults);
        let parsed_cachedResults = JSON.parse(cachedResults);
        console.log("Cached results:", parsed_cachedResults);
        doneCallback(parsed_cachedResults);
        return;
    }

    console.log("sending AJAX request to backend Java Servlet");
    // Sending the HTTP GET request to the Java Servlet endpoint 'api/autocomplete'
    jQuery.ajax({
        method: "GET",
        url: "api/autocomplete?query=" + encodeURIComponent(query),
        success: function(data) {
            // Process AJAX success
            handleLookupAjaxSuccess(data, query, doneCallback);
        },
        error: function(errorData) {
            console.log("lookup ajax error");
            console.log(errorData);
        }
    });
}

// AJAX 成功回调
function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful. Query:", query);
    // console.log(typeof data);
    var jsonData = JSON.parse(data);
    console.log("Received suggestions:", jsonData);

    // Cache the result in SessionStorage
    sessionStorage.setItem(query, JSON.stringify(jsonData));

    // Call the callback function provided by the autocomplete library
    doneCallback(jsonData);
}

// 处理选中的建议项
function handleSelectSuggestion(suggestion) {
    console.log("You selected " + suggestion.value + " with ID " + suggestion.data.movieID);

    // Redirect to the single movie page
    window.location.href = "single-movie.html?id=" + suggestion.data.movieID;
}

// 使用 jQuery UI 的 autocomplete 组件
$('#title').autocomplete({
    source: function(request, response) {
        // Use handleLookup to fetch the data
        handleLookup(request.term, function(data) {
            response(data); // pass the data to jQuery UI autocomplete
        });
    },
    delay: 300, // 设置延迟时间
    minLength: 3, // 至少输入 3 个字符才触发搜索

    select: function(event, ui) {
        // 当选择某个建议时触发
        handleSelectSuggestion(ui.item);
    }
});

// 普通的全文搜索
function handleNormalSearch(query) {
    console.log("Doing normal search with query: " + query);

    let title = query;
    let year = document.getElementById("year").value;
    let director = document.getElementById("director").value;
    let star = document.getElementById("star_name").value;

    let params = new URLSearchParams();
    if (title) params.append("title", title);
    if (year) params.append("year", year);
    if (director) params.append("director", director);
    if (star) params.append("star_name", star);

    // Perform normal search if no autocomplete suggestion was selected
    console.log("Performing Full-Text Search with parameters: " + params.toString());
    window.location.href = "movielist.html?" + params.toString();
}

// 监听回车键触发搜索
$('#title').keypress(function(event) {
    if (event.keyCode === 13) {  // Enter key
        handleNormalSearch($('#title').val());
    }
});

// 绑定搜索按钮点击事件
$('#searchButton').click(function () {
    handleNormalSearch($('#title').val());
});
