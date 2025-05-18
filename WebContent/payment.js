

document.addEventListener("DOMContentLoaded", function () {
    let p = sessionStorage.getItem("totalPrice") || "0.00";
    document.getElementById("totalAmount").innerText = `$${p}`;
});


$("#paymentForm").submit(function (event) {
    event.preventDefault(); // ajax but not auto form

    let orderData = {
        firstName: $("#firstName").val(),
        lastName: $("#lastName").val(),
        creditCard: $("#creditCard").val(),
        expiry: $("#expiry").val(),
        cart: JSON.parse(sessionStorage.getItem("cart")) || {}
    };
    //use POST
    $.ajax({
        url: "api/placeOrder",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify(orderData),
        dataType: "json",
        success: function (data) {
            if (data.success) {
                sessionStorage.removeItem("cart");
                sessionStorage.removeItem("totalPrice");
                alert("Order placed successfully!");
                window.location.href = "confirmation.html"; // 跳转到确认页面
            } else {
                alert("Payment failed: " + data.message);
            }
        },
        error: function (xhr, status, error) {
            console.error("AJAX Error:", status, error);
            alert("Error placing order. Please try again.");
        }
    });
});

