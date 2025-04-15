document.addEventListener('DOMContentLoaded', function() {
    const loginButton = document.querySelector('.a3');

    if (loginButton && loginButton.getAttribute('href') === '/login') {
        loginButton.addEventListener('click', function(e) {
            e.preventDefault();

            const contentDiv = document.querySelector('.div9');

            contentDiv.style.transition = 'opacity 0.3s ease';
            contentDiv.style.opacity = '0';

            setTimeout(function() {
                window.location.href = '/login';
            }, 300);
        });
    }
});