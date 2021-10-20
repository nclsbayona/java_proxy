addEventListener('DOMContentLoaded', onloadCb);

// This callback will be executed when the page initially loads.
// See line 1 to know why
function onloadCb() {
    // Grab all forms which have a method attribute
    const forms = document.querySelectorAll('form[method]');

    // Display info of any submitted form
    forms.forEach(function (form) {
        form.addEventListener('submit', displayInfo);
    });
};

function displayInfo(event) {
    // Prevent the page from redirecting to the action attribute.
    event.preventDefault();

    // Get the form data
    const formData = new FormData(event.target);

    logAsTitle('Data:');
    for (let [key, val] of formData.entries()) {
        console.log(`${key} \t ${val || '<Empty>'}`);
    }

    logAsTitle('Form attributes');
    const neededAttributes = ['action', 'method', 'enctype'];
    for (let attr of neededAttributes) {
        console.log(`${attr} - ${event.target[[attr]] || '<empty>'}`);
    }
}

function logAsTitle(str) {
    console.log('\n' + str);
    console.log('='.repeat(str.length));
}