document.addEventListener('DOMContentLoaded', function() {

  // Use buttons to toggle between views
  document.querySelector('#inbox').addEventListener('click', () => load_mailbox('inbox'));
  document.querySelector('#sent').addEventListener('click', () => load_mailbox('sent'));
  document.querySelector('#archived').addEventListener('click', () => load_mailbox('archive'));
  document.querySelector('#compose').addEventListener('click', compose_email);
  document.querySelector('#compose-form').addEventListener('submit', send_email);

  // By default, load the inbox
  load_mailbox('inbox');
});

function compose_email() {

  // Show compose view and hide other views
  document.querySelector('#emails-view').style.display = 'none';
  document.querySelector('#compose-view').style.display = 'block';

  // Clear out composition fields
  document.querySelector('#compose-recipients').value = '';
  document.querySelector('#compose-subject').value = '';
  document.querySelector('#compose-body').value = '';
}

function load_mailbox(mailbox) {
  
  // Show the mailbox and hide other views
  document.querySelector('#emails-view').style.display = 'block';
  document.querySelector('#compose-view').style.display = 'none';

  // Show the mailbox name
  document.querySelector('#emails-view').innerHTML = `<h3>${mailbox.charAt(0).toUpperCase() + mailbox.slice(1)}</h3>`;

  fetch(`/emails/${mailbox}`)
  .then(response => response.json())
  .then(emails => {
    // Print emails
    console.log(emails);
    emails.forEach(generate_div);
    emails.forEach(generate_listeners);
})
  .catch(error => {
    console.log('Error:', error);
});
}

function generate_div(email) {
  var user = document.querySelector('#user').value;

  if (email.read === true) {
    var color = 'gray';
  }
  else {
    var color = 'white';
  }

  if (email.sender === user) {
    var sender = email.recipients;
  }
  else {
    var sender = email.sender;
  }

  document.querySelector('#emails-view').innerHTML += 
    `<div style="border: thin solid black; background-color: ${color}" id="email${email.id}">
    <h4> ${sender} </h4>
    <h5> ${email.subject} </h5>
    <h6> ${email.timestamp} </h6>
    </div>`;
}

function generate_listeners(email) {
  document.querySelector(`#email${email.id}`).addEventListener('click', () => show_email(email.id));
  console.log(email);
}

function archive_email(id) {
  fetch(`/emails/${id}`)
  .then(response => response.json())
  .then(email => {
    // Print email
    console.log(email);
    if (email.archived === true) {
      fetch(`/emails/${id}`, {
        method: 'PUT',
        body: JSON.stringify({
            archived: false
        })
      });
    }
    else {
      fetch(`/emails/${id}`, {
        method: 'PUT',
        body: JSON.stringify({
            archived: true
        })
      });
    }

    load_mailbox('inbox');

});
}

function show_email(id) {
  fetch(`/emails/${id}`)
  .then(response => response.json())
  .then(email => {
    // Print email
    console.log(email);
    fetch(`/emails/${id}`, {
      method: 'PUT',
      body: JSON.stringify({
          read: true
      })
    });
    var recipients = '';
    email.recipients.forEach(function(recipient) {
      console.log(recipient);
      recipients += recipient + '; ';
    });
    if (email.archived === true) {
      var archive = 'Unarchive';
    }
    else {
      var archive = 'Archive';
    }
    document.querySelector('#emails-view').innerHTML = 
    `<div>
    <h3> ${email.subject} </h3>
    <h4> From: ${email.sender} </h4>
    <h5> To: ${recipients} </h5>
    <h6> ${email.timestamp} </h6>`

    var user = document.querySelector('#user').value;

    if (email.sender === user) {
      document.querySelector('#emails-view').innerHTML += 
      `<button class="btn btn-sm btn-outline-primary" id="reply${email.id}">Reply</button>
      <p> ${email.body} </p>
      </div>`;
    }
    else {
      document.querySelector('#emails-view').innerHTML += 
      `<button class="btn btn-sm btn-outline-primary" id="reply${email.id}">Reply</button>
      <button class="btn btn-sm btn-outline-primary" id="archive${email.id}">${archive}</button>
      <p> ${email.body} </p>
      </div>`;
      document.querySelector(`#archive${email.id}`).addEventListener('click', () => archive_email(email.id));
    }

    document.querySelector(`#reply${email.id}`).addEventListener('click', () => reply_email(email.id));

});
}

function reply_email(id) {

  fetch(`/emails/${id}`)
  .then(response => response.json())
  .then(email => {
    // Print email
    console.log(email);

    // Show compose view and hide other views
    document.querySelector('#emails-view').style.display = 'none';
    document.querySelector('#compose-view').style.display = 'block';

    // Clear out composition fields
    document.querySelector('#compose-recipients').value = email.sender;
    if (email.subject.substring(0,4) === 'Re: ') {
      document.querySelector('#compose-subject').value = email.subject;
    }
    else {
      document.querySelector('#compose-subject').value = 'Re: ' + email.subject;
    }
  
    document.querySelector('#compose-body').value = `\n\nOn ${email.timestamp} ${email.sender} wrote: \n\n${email.body}`;

  });

}

function send_email(event) {

  fetch('/emails', {
    method: 'POST',
    body: JSON.stringify({
        recipients: document.querySelector('#compose-recipients').value,
        subject: document.querySelector('#compose-subject').value,
        body: document.querySelector('#compose-body').value
    })
  })
  .then(response => response.json())
  .then(result => {
      // Print result
      console.log(result);
  });

  load_mailbox('sent');
  event.returnValue = false;

  return false;

}