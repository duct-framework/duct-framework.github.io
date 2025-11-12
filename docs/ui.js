document.addEventListener('DOMContentLoaded', () => {
    const header = document.querySelector('#header');
    const icon   = document.createElement('i');
    icon.className = 'menu-icon';
    header.prepend(icon);

    icon.addEventListener('click', () => {
      header.classList.toggle('is-open');
    });

    header.addEventListener('click', (event) => {
      if (event.target.closest('a')) {
        header.classList.remove('is-open');
      }
    });
});
