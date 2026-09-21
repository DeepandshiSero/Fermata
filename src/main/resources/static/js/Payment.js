function selectMethod(method) {
    document.getElementById('method-card').classList.remove('selected');
    document.getElementById('method-cash').classList.remove('selected');
    document.getElementById(`method-${method}`).classList.add('selected');

    const cardDetails = document.getElementById('card-details');
    cardDetails.style.display = method === 'card' ? 'block' : 'none';
}

function placeOrder() {
    fetch('/order/place', {
        method: 'POST',
    })
        .then((res) => {
            if (res.ok) {
                alert('Order placed successfully!');
                window.location.href = '/';
            } else {
                res.text().then((message) => alert(message));
            }
        })
        .catch(() => alert('Something went wrong.'));
}