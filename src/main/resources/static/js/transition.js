document.addEventListener('DOMContentLoaded', function() {
    // Enhanced transition from home to login
    const loginButton = document.querySelector('.a3');
    if (loginButton && loginButton.getAttribute('href') === '/login') {
        loginButton.addEventListener('click', function(e) {
            e.preventDefault();

            // Fade out content with smoother transition
            const contentDiv = document.querySelector('.div9');
            contentDiv.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
            contentDiv.style.opacity = '0';
            contentDiv.style.transform = 'translateY(-20px)';

            // Navigate after transition completes
            setTimeout(function() {
                window.location.href = '/login';
            }, 400);
        });
    }

    // Make "Minder" navigation go to memories page
    const minderElements = document.querySelectorAll('.minder');
    minderElements.forEach(function(element) {
        const parentSummary = element.closest('.summary');
        if (parentSummary) {
            parentSummary.addEventListener('click', function(e) {
                e.preventDefault();
                window.location.href = '/memories';
            });
        }
    });
});