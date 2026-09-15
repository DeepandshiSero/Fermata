const imgs = document.querySelectorAll('.img-select a');
const imgBtns = [...imgs];
let imgId = 1;

imgBtns.forEach((imgItem) => {
  imgItem.addEventListener('click', (event) => {
    event.preventDefault();
    imgId = imgItem.dataset.id;
    slideImage();
  });
});

function slideImage() {
  const displayWidth = document.querySelector('.img-showcase img:first-child').clientWidth;
  document.querySelector('.img-showcase').style.transform = `translateX(${- (imgId - 1) * displayWidth}px)`;
}

window.addEventListener('resize', slideImage);

const addToCartBtn = document.querySelector('#add-to-cart-btn');
const qtyInput = document.querySelector('#qty-input');

addToCartBtn.addEventListener('click', () => {
  const params = new URLSearchParams({
    id: addToCartBtn.dataset.id,
    name: addToCartBtn.dataset.name,
    price: addToCartBtn.dataset.price,
    imageUrl: addToCartBtn.dataset.image,
    quantity: qtyInput.value,
  });

  fetch('/cart/add', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params.toString(),
  })
      .then((res) => {
        if (res.ok) {
          alert('Added to cart!');
        } else if (res.status === 401) {
          res.text().then((message) => alert(message));
        } else {
          alert('Something went wrong.');
        }
      })
      .catch(() => alert('Something went wrong.'));
});