document.querySelectorAll('form[data-confirm]').forEach(form => {
    form.addEventListener('submit', event => {
        if (!window.confirm(form.dataset.confirm)) event.preventDefault();
    });
});
const note = document.querySelector('#note');
const counter = document.querySelector('#note-counter');
if (note && counter) {
    const updateCounter = () => {
        counter.textContent = `${note.value.length} / 2 000`;
    };
    note.addEventListener('input', updateCounter);
    updateCounter();
}
