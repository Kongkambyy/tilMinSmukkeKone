document.addEventListener('DOMContentLoaded', function() {
    // Elements
    const uploadBtn = document.getElementById('upload-btn');
    const modal = document.getElementById('upload-modal');
    const closeBtn = document.querySelector('.close-btn');
    const memoryForm = document.getElementById('memory-form');
    const memoryFilter = document.getElementById('memory-filter');
    const memoriesGrid = document.getElementById('memories-grid');
    const imageInput = document.getElementById('memory-images');
    const previewContainer = document.getElementById('preview-container');

    uploadBtn.addEventListener('click', function() {
        modal.style.display = 'block';
    });

    closeBtn.addEventListener('click', function() {
        modal.style.display = 'none';
        resetForm();
    });

    window.addEventListener('click', function(event) {
        if (event.target == modal) {
            modal.style.display = 'none';
            resetForm();
        }
    });

    memoryFilter.addEventListener('change', function() {
        const filterValue = this.value;
        filterMemories(filterValue);
    });

    memoryForm.addEventListener('submit', function(e) {
        e.preventDefault();

        createMemoryCard({
            title: document.getElementById('memory-title').value,
            description: document.getElementById('memory-description').value,
            date: document.getElementById('memory-date').value,
            type: document.getElementById('memory-type-select').value,
            imageSrc: '/images/placeholders/memory1.jpg'
        });

        modal.style.display = 'none';
        resetForm();
    });

    imageInput.addEventListener('change', handleImagePreview);

    function filterMemories(type) {
        const memoryCards = memoriesGrid.querySelectorAll('.memory-card');

        memoryCards.forEach(card => {
            if (type === 'all' || card.dataset.type === type) {
                card.style.display = 'block';
            } else {
                card.style.display = 'none';
            }
        });
    }

    function createMemoryCard(memoryData) {
        const formattedDate = formatDate(memoryData.date);

        const typeDisplay = getMemoryTypeDisplay(memoryData.type);

        const card = document.createElement('div');
        card.className = 'memory-card';
        card.dataset.type = memoryData.type;

        card.innerHTML = `
            <div class="memory-image-container">
                <img src="${memoryData.imageSrc}" alt="${memoryData.title}" class="memory-image">
            </div>
            <div class="memory-info">
                <h3>${memoryData.title}</h3>
                <p class="memory-date">${formattedDate}</p>
                <span class="memory-type">${typeDisplay}</span>
            </div>
        `;

        memoriesGrid.insertBefore(card, memoriesGrid.firstChild);
    }

    function resetForm() {
        memoryForm.reset();
        previewContainer.innerHTML = '';
    }

    function handleImagePreview(e) {
        previewContainer.innerHTML = '';

        const files = e.target.files;

        for (let i = 0; i < files.length; i++) {
            const file = files[i];

            if (!file.type.match('image.*')) {
                continue;
            }

            const reader = new FileReader();

            reader.onload = (function(file) {
                return function(e) {
                    const previewItem = document.createElement('div');
                    previewItem.className = 'preview-item';

                    previewItem.innerHTML = `
                        <img class="preview-image" src="${e.target.result}" alt="${file.name}">
                        <div class="remove-preview" data-filename="${file.name}">×</div>
                    `;

                    previewContainer.appendChild(previewItem);

                    previewItem.querySelector('.remove-preview').addEventListener('click', function() {
                        previewItem.remove();
                    });
                };
            })(file);

            reader.readAsDataURL(file);
        }
    }

    function formatDate(dateString) {
        const months = ['januar', 'februar', 'marts', 'april', 'maj', 'juni',
            'juli', 'august', 'september', 'oktober', 'november', 'december'];

        const date = new Date(dateString);
        return `${date.getDate()}. ${months[date.getMonth()]} ${date.getFullYear()}`;
    }

    function getMemoryTypeDisplay(type) {
        const typeMap = {
            'PHOTO': 'Billede',
            'VIDEO': 'Video',
            'TEXT': 'Tekst',
            'MILESTONE': 'Milepæl',
            'TRIP': 'Rejse',
            'DATE': 'Date',
            'GIFT': 'Gave'
        };

        return typeMap[type] || type;
    }
});