var csrfToken = null;

document.addEventListener('DOMContentLoaded', function() {
  if (document.querySelector("[name='csrfmiddlewaretoken']")) {
    csrfToken = document.querySelector("[name='csrfmiddlewaretoken']").value;
  }

});

function update_settings() {

  var data = {}

  data.name = document.querySelector('input[name="name"]').value
  data.patient = false;
  data.doctor = false;
  data.pharmacy = false;

  if (document.querySelector('input[name="patient"]').checked) {
      data.patient = true;
  }

  if (document.querySelector('input[name="doctor"]').checked) {
    data.doctor = true;
}

if (document.querySelector('input[name="pharmacy"]').checked) {
  data.pharmacy = true;
}

var json = JSON.stringify(data);

        fetch('/settings', {
          method: 'POST',
          body: json,
          headers: { "X-CSRFToken": csrfToken,
          "Content-Type": "application/json",
          Accept: 'application/json' }
      });
}

function reveal(div_id){
  var element = document.getElementById(div_id);
  element.style.display = 'block';
  element.nextElementSibling.style.display = 'block'
}

function hide(div_id){
  var element = document.getElementById(div_id);
  element.style.display = 'none';
  element.nextElementSibling.style.display = 'none'
}

function save(prescription_id){
  hide(`prescription_${prescription_id}_adjust`);

  var new_directions = document.getElementById(`directions_${prescription_id}`).value;

  fetch(`/save/${prescription_id}`, {
    method: 'POST',
    body: JSON.stringify(new_directions),
    headers: { "X-CSRFToken": csrfToken,
    "Content-Type": "application/json",
    Accept: 'application/json' }
});
  
}

function request_refill(prescription_id){
  hide(`prescription_${prescription_id}_refill`);

  data = {};
  data.pharmacy = document.getElementById(`pharmacies_${prescription_id}`).value;
  data.prescription = prescription_id;

  data.count = document.getElementById(`refill_${prescription_id}`).value;

  fetch(`/refill/${prescription_id}`, {
    method: 'POST',
    body: JSON.stringify(data),
    headers: { "X-CSRFToken": csrfToken,
    "Content-Type": "application/json",
    Accept: 'application/json' }
});
  
}

function request_adjusted_refill(request_id){
  hide(`request_${request_id}_details`);
  hide(`request_${request_id}`);

  data = {};
  data.pharmacy = document.getElementById(`pharmacies_${request_id}`).value;
  data.request = request_id;
  data.count = document.getElementById(`refill_${request_id}`).value;

  fetch(`/adjust_refill/${request_id}`, {
    method: 'POST',
    body: JSON.stringify(data),
    headers: { "X-CSRFToken": csrfToken,
    "Content-Type": "application/json",
    Accept: 'application/json' }
});
  
}

function request_default(request_id){
  hide(`request_${request_id}`);
  hide(`request_${request_id}_details`);

  fetch(`/default_refill/${request_id}`, {
    method: 'POST',
    body: null,
    headers: { "X-CSRFToken": csrfToken,
    "Content-Type": "application/json",
    Accept: 'application/json' }
});
}

function ask_dr_for_refill(prescription_id){
  hide(`prescription_${prescription_id}_refill`);

  data = {};
  data.pharmacy = document.getElementById(`pharmacies_${prescription_id}`).value;
  data.prescription = prescription_id;
  data.doctor = document.getElementById(`doctors_${prescription_id}`).value;

  data.count = document.getElementById(`refill_${prescription_id}`).value;

  fetch(`/ask_for_refill/${prescription_id}`, {
    method: 'POST',
    body: JSON.stringify(data),
    headers: { "X-CSRFToken": csrfToken,
    "Content-Type": "application/json",
    Accept: 'application/json' }
});
  
}

function discontinue(prescription_id){
  hide(`prescription_${prescription_id}`);
  hide(`prescription_${prescription_id}_history`);
  hide(`prescription_${prescription_id}_adjust`);
  hide(`prescription_${prescription_id}_refill`);

  fetch(`/discontinue/${prescription_id}`, {
    method: 'POST',
    body: null,
    headers: { "X-CSRFToken": csrfToken,
    "Content-Type": "application/json",
    Accept: 'application/json' }
});
}

function prescribe(div_id, username){

  var medication = document.getElementById("medication");
  var dose = document.getElementById("dose");
  var directions = document.getElementById("directions");
  var count = document.getElementById("count");
  var pharmacy = document.getElementById("pharmacies");

  data = {};
  data.medication = medication.value;
  data.dose = dose.value;
  data.directions = directions.value;
  data.count = count.value;
  data.pharmacy = pharmacy.value;

  hide(div_id);

  document.getElementById("confirmation_message").innerHTML = `New prescription for ${medication.value} ${dose.value} has been requested.`;

  medication.value = "";
  dose.value = "";
  directions.value = "";
  count.value = "";

  var json = JSON.stringify(data);

  fetch(`/prescribe/${username}`, {
    method: 'POST',
    body: json,
    headers: { "X-CSRFToken": csrfToken,
    "Content-Type": "application/json",
    Accept: 'application/json' }
});
}

function prepare_refill(request_id){
  fetch('/prepare_refill', {
    method: 'POST',
    body: JSON.stringify(request_id),
    headers: { "X-CSRFToken": csrfToken,
    "Content-Type": "application/json",
    Accept: 'application/json' }
});

  hide(`request_${request_id}`);
}

function reject_general(request_id){
  fetch(`/reject_general`, {
    method: 'POST',
    body: JSON.stringify(request_id),
    headers: { "X-CSRFToken": csrfToken,
    "Content-Type": "application/json",
    Accept: 'application/json' }
});
hide(`request_${request_id}`);
hide(`request_${request_id}_details`);
}

function take(prescription_id){
  hide(`prescription_${prescription_id}_take`);

  count = document.getElementById(`take_${prescription_id}`).value;

  fetch(`/take/${prescription_id}`, {
    method: 'POST',
    body: JSON.stringify(count),
    headers: { "X-CSRFToken": csrfToken,
    "Content-Type": "application/json",
    Accept: 'application/json' }})
    .then(response => response.json())
            .then(result => {
              var count = result[0]
                if (count != "None"){
                  var remaining = document.getElementById(`remaining_${prescription_id}`);
                  remaining.innerHTML = count + " doses remaining";
                  if (count == 0){
                    var take_button = document.getElementById(`take_${prescription.id}_button`);
                    take_button.style.display = 'none';
                  }
                }

                var key = result[2];

                var table = document.getElementById(`history_${key}`);
                var element = document.createElement("tr");
                    var entry = result[1]

                    var date = document.createElement("td");
                    var action = document.createElement("td");
                    var start = document.createElement("td");
                    var change = document.createElement("td");
                    var end = document.createElement("td");

                    date.innerHTML = entry["timestamp"];
                    action.innerHTML = entry["action"];
                    start.innerHTML = entry["start"];
                    change.innerHTML = entry["change"];
                    end.innerHTML = entry["end"];

                    var headers = document.getElementById('headers');
                    console.log(headers);
                    console.log(headers.parentElement.nextElementSibling);

                    if (headers.parentElement.nextElementSibling == null){
                      table.appendChild(element);
                    }
                    else {
                      table.insertBefore(element, headers.parentElement.nextElementSibling);
                    }

                    
                    element.appendChild(date);
                    element.appendChild(action);
                    element.appendChild(start);
                    element.appendChild(change);
                    element.appendChild(end);
          });
}

function confirm_refill(request_id){
  fetch('/confirm_refill', {
    method: 'POST',
    body: JSON.stringify(request_id),
    headers: { "X-CSRFToken": csrfToken,
    "Content-Type": "application/json",
    Accept: 'application/json' }
});

  hide(`request_${request_id}`);
}

function connect(type, username){
  var data = {};
  data.type = type;
  data.username = username;

  var json = JSON.stringify(data);

        fetch('/connect', {
          method: 'POST',
          body: json,
          headers: { "X-CSRFToken": csrfToken,
          "Content-Type": "application/json",
          Accept: 'application/json' }
      });


      var row = document.getElementById(`${username}`);
      row.parentElement.removeChild(row.nextElementSibling);
      row.parentElement.removeChild(row);
}

function reject(type, username){
  var data = {};
  data.type = type;
  data.username = username;

  var json = JSON.stringify(data);

        fetch('/reject', {
          method: 'POST',
          body: json,
          headers: { "X-CSRFToken": csrfToken,
          "Content-Type": "application/json",
          Accept: 'application/json' }
      });

      var row = document.getElementById(`${username}`);
      row.parentElement.removeChild(row.nextElementSibling);
      row.parentElement.removeChild(row);
}

function disconnect(type, username){
  var data = {};
  data.type = type;
  data.username = username;

  var json = JSON.stringify(data);

        fetch('/disconnect', {
          method: 'POST',
          body: json,
          headers: { "X-CSRFToken": csrfToken,
          "Content-Type": "application/json",
          Accept: 'application/json' }
      });

      var row = document.getElementById(`${username}`);
      row.parentElement.removeChild(row.nextElementSibling);
      row.parentElement.removeChild(row);
}

function request(type, username){
  var data = {};
  data.type = type;
  data.username = username;

  var json = JSON.stringify(data);

        fetch('/request', {
          method: 'POST',
          body: json,
          headers: { "X-CSRFToken": csrfToken,
          "Content-Type": "application/json",
          Accept: 'application/json' }
      });

      var row = document.getElementById(`${username}`);
      row.parentElement.removeChild(row.nextElementSibling);
      row.parentElement.removeChild(row);
}

function update_search(type){
  var text = document.getElementById("search_terms").value;

  if (text === ""){
    var result_div = document.querySelector(`#results`);
    var all_results = result_div.querySelectorAll(".row");

    for (i = 0; i < all_results.length; i++){
      all_results[i].style.display = '';
      all_results[i].nextElementSibling.style.display = '';
              
    }
    return
  }

  fetch(`/search/${type}/${text}`)
        .then(response => response.json())
        .then(results => {
            var result_div = document.querySelector(`#results`);
            var all_results = result_div.querySelectorAll(".row");

            for (i = 0; i < all_results.length; i++){
              if (results.includes(all_results[i].id)){
                all_results[i].style.display = '';
                all_results[i].nextElementSibling.style.display = '';
              }
              else {
                all_results[i].style.display = 'none';
                all_results[i].nextElementSibling.style.display = 'none';
              }
            }
      });
}