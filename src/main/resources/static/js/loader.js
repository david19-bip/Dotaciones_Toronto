document.addEventListener('DOMContentLoaded', function() {

    const overlay = document.getElementById('loader-overlay');

    // Siempre ocultar al cargar la página
    overlay.classList.remove('active');

    // Mostrar solo al enviar formularios
    document.querySelectorAll('form').forEach(function(form) {
        form.addEventListener('submit', function() {
            // Verificar que el formulario sea válido antes de mostrar
            if (form.checkValidity()) {
                overlay.classList.add('active');
                // Ocultar automáticamente después de 5 segundos por seguridad
                setTimeout(function() {
                    overlay.classList.remove('active');
                }, 5000);
            }
        });
    });

    // Mostrar al navegar entre páginas
    document.querySelectorAll('a[href]').forEach(function(link) {
        const href = link.getAttribute('href');
        if (href && !href.startsWith('#') && !href.startsWith('javascript')) {
            link.addEventListener('click', function() {
                overlay.classList.add('active');
                setTimeout(function() {
                    overlay.classList.remove('active');
                }, 5000);
            });
        }
    });

    // Ocultar cuando la página vuelve a mostrarse
    window.addEventListener('pageshow', function() {
        overlay.classList.remove('active');
    });

    // Ocultar cuando termina de cargar todo
    window.addEventListener('load', function() {
        overlay.classList.remove('active');
    });
});