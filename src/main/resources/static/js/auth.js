document.addEventListener('DOMContentLoaded', function () {
    const toggleButtons = document.querySelectorAll('.toggle-password');

    toggleButtons.forEach(function (toggle) {
        function togglePasswordVisibility() {
            const passwordField = toggle.closest('.password-field');
            if (!passwordField) return;

            const input = passwordField.querySelector('input');
            if (!input) return;

            const isPassword = input.type === 'password';
            input.type = isPassword ? 'text' : 'password';

            if (isPassword) {
                toggle.classList.remove('fa-eye');
                toggle.classList.add('fa-eye-slash');
                toggle.setAttribute('title', 'Hide password');
                toggle.setAttribute('aria-label', 'Hide password');
            } else {
                toggle.classList.remove('fa-eye-slash');
                toggle.classList.add('fa-eye');
                toggle.setAttribute('title', 'Show password');
                toggle.setAttribute('aria-label', 'Show password');
            }
        }

        toggle.addEventListener('click', togglePasswordVisibility);

        toggle.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                togglePasswordVisibility();
            }
        });
    });
});
