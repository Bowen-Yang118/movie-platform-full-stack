function loadCart() {
    let cart = JSON.parse(sessionStorage.getItem("cart")) || {};
    let resultsDiv = jQuery("#cartTable");

    let totalPrice = 0;

    let tableHTML = ""
    Object.keys(cart).forEach(movieId => {
        let item = cart[movieId];
        let row = `<tr>

            <td>${item.title}</td>
            
            <!-- quantity change -->
            <td><button onclick="updateQuantity('${movieId}', -1)">-</button> ${item.quantity} 
                <button onclick="updateQuantity('${movieId}', 1)">+</button></td>
              
            <td>$${item.price.toFixed(2)}</td>
            <td>$${(item.price * item.quantity).toFixed(2)}</td>
            <!-- remove -->
            <td><button onclick="removeFromCart('${movieId}')">Delete</button></td>
            
        </tr>`;
        tableHTML += row;
        totalPrice += item.price * item.quantity;
    });
    resultsDiv.html(tableHTML);

    jQuery("#totalPrice").text(totalPrice.toFixed(2));
    sessionStorage.setItem("totalPrice", totalPrice.toFixed(2));
}

//add and decrease button functionality
function updateQuantity(movieId, change) {
    let cart = JSON.parse(sessionStorage.getItem("cart")) || {};
    if (!cart[movieId]) return;

    cart[movieId].quantity += change;
    if (cart[movieId].quantity <= 0) delete cart[movieId];

    sessionStorage.setItem("cart", JSON.stringify(cart));
    loadCart();
}

function removeFromCart(movieId) {
    let cart = JSON.parse(sessionStorage.getItem("cart")) || {};
    delete cart[movieId];

    sessionStorage.setItem("cart", JSON.stringify(cart));
    loadCart();
}

//process
document.addEventListener("DOMContentLoaded", loadCart);


