// ===== SMART CAMPUS EMS - MAIN JS =====

document.addEventListener('DOMContentLoaded', function () {

    // Auto-dismiss flash messages after 5 seconds
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s ease';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });

    // Star Rating Interactive
    initStarRating();

    // Confirm delete / cancel dialogs — message depends on the button inside
    document.querySelectorAll('.confirm-delete').forEach(form => {
        form.addEventListener('submit', function (e) {
            const btn = form.querySelector('button[type="submit"]');
            const btnText = btn ? btn.textContent.trim() : '';
            let msg = '⚠️ Are you sure? This action cannot be undone.';
            if (btnText.includes('Cancel') || btnText.includes('🚫')) {
                msg = '🚫 Cancel this registration? The student will lose their seat.';
            } else if (btnText.includes('Delete') || btnText.includes('🗑️')) {
                msg = '🗑️ Delete permanently? This action cannot be undone.';
            }
            if (!confirm(msg)) {
                e.preventDefault();
            }
        });
    });

    // Confirm cancel registration
    document.querySelectorAll('.confirm-cancel').forEach(form => {
        form.addEventListener('submit', function (e) {
            if (!confirm('Are you sure you want to cancel your registration?')) {
                e.preventDefault();
            }
        });
    });

    // Capacity bar color
    document.querySelectorAll('.progress-bar').forEach(bar => {
        const pct = parseInt(bar.getAttribute('data-percent') || '0');
        bar.style.width = pct + '%';
        if (pct >= 90) bar.classList.add('high');
        else if (pct >= 60) bar.classList.add('medium');
        else bar.classList.add('low');
    });

    // Set min date for event date inputs to today
    const dateInputs = document.querySelectorAll('input[type="date"]');
    const today = new Date().toISOString().split('T')[0];
    dateInputs.forEach(input => {
        if (!input.value) input.setAttribute('min', today);
    });

    // Character counter for textarea
    document.querySelectorAll('textarea[maxlength]').forEach(ta => {
        const max = ta.getAttribute('maxlength');
        const counter = document.createElement('small');
        counter.className = 'text-muted';
        counter.style.display = 'block';
        counter.style.textAlign = 'right';
        counter.textContent = `0 / ${max}`;
        ta.parentNode.appendChild(counter);
        ta.addEventListener('input', () => {
            counter.textContent = `${ta.value.length} / ${max}`;
        });
    });
});

// Star Rating
function initStarRating() {
    const ratingInputs = document.querySelectorAll('.star-rating-group');
    ratingInputs.forEach(group => {
        const stars = group.querySelectorAll('.star');
        const hiddenInput = group.querySelector('input[type="hidden"]');

        stars.forEach((star, index) => {
            star.addEventListener('mouseover', () => highlightStars(stars, index));
            star.addEventListener('mouseleave', () => {
                const selected = hiddenInput ? parseInt(hiddenInput.value || '0') : 0;
                highlightStars(stars, selected - 1);
            });
            star.addEventListener('click', () => {
                if (hiddenInput) hiddenInput.value = index + 1;
                highlightStars(stars, index);
            });
        });
    });
}

function highlightStars(stars, upTo) {
    stars.forEach((star, i) => {
        star.classList.toggle('active', i <= upTo);
    });
}

// Search with Enter key
function searchOnEnter(event, formId) {
    if (event.key === 'Enter') {
        document.getElementById(formId).submit();
    }
}
