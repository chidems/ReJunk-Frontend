/* ReJunk — Client-side JavaScript */
document.addEventListener('DOMContentLoaded', function() {
    // Confirm dangerous actions
    document.querySelectorAll('[data-confirm]').forEach(function(btn) {
        btn.addEventListener('click', function(e) {
            if (!confirm(this.dataset.confirm)) e.preventDefault();
        });
    });

    // Image preview on upload
    document.querySelectorAll('.rj-upload-box input[type="file"]').forEach(function(input) {
        input.addEventListener('change', function() {
            var box = this.closest('.rj-upload-box');
            if (this.files && this.files[0]) {
                var reader = new FileReader();
                reader.onload = function(e) {
                    box.style.backgroundImage = 'url(' + e.target.result + ')';
                    box.style.backgroundSize = 'cover';
                    box.querySelector('i').style.display = 'none';
                };
                reader.readAsDataURL(this.files[0]);
            }
        });
    });

    //Password match validation
    var confirmPw = document.querySelector('input[name="confirmPassword"]');
    var password = document.querySelector('input[name="password"]');
    if (confirmPw && password) {
        confirmPw.addEventListener('input', function() {
            this.setCustomValidity(this.value !== password.value ? 'Passwords do not match' : '');
        });
    }

    //dismiss success alerts
    document.querySelectorAll('.alert-success').forEach(function(alert) {
        setTimeout(function() {
            alert.style.transition = 'opacity 0.5s';
            alert.style.opacity = '0';
            setTimeout(function() { alert.remove(); }, 500);
        }, 5000);
    });
});
