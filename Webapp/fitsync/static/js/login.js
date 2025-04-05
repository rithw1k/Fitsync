document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const loadingOverlay = document.getElementById('loadingOverlay');

    loginForm.addEventListener('submit', (e) => {
        e.preventDefault();
        loadingOverlay.style.display = 'flex';

        const formData = new FormData(loginForm);
        
        fetch(loginForm.action, {
            method: 'POST',
            body: formData,
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
        .then(response => response.json())
        .then(data => {
            loadingOverlay.style.display = 'none';
            if (data.success) {
                window.location.href = data.redirect_url;
            } else {
                showToast(data.error || 'Invalid credentials');
            }
        })
        .catch(error => {
            loadingOverlay.style.display = 'none';
            showToast('An error occurred. Please try again.');
            console.error('Error:', error);
        });
    });
});

function showToast(message) {
    const toast = document.getElementById('toast');
    toast.innerText = message;
    toast.style.display = 'block';
    setTimeout(() => {
        toast.style.display = 'none';
    }, 3000);
}