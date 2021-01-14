var csrfToken = null;

document.addEventListener('DOMContentLoaded', function() {
    if (document.querySelector("[name='csrfmiddlewaretoken']")) {
      csrfToken = document.querySelector("[name='csrfmiddlewaretoken']").value;
    }

    count_requests();
  });

  function accept_request(targetname, button_id){

    var button = document.getElementById(button_id)
        button.value = "Request Accepted";
        button.disabled = true;

    fetch(`/accept_request/${targetname}`)
    .catch(error => {
    console.log('Error:', error);
    });
  }

  function send_request_direct(target){
    var button = document.getElementById(target + "_button")
    button.value = "Request Sent";
    button.disabled = true;

    fetch(`/send_request/${target}`)
        .catch(error => {
        console.log('Error:', error);
        });
}
    
    function reveal(div_id) {
        if (document.querySelector(`#${div_id}`).style.display === 'block'){
            document.querySelector(`#${div_id}`).style.display = 'none';
        }
        else {
            document.querySelector(`#${div_id}`).style.display = 'block';
        }
    }

    function add_title(generate_div, title_name = "") {

        var div = document.querySelector(`#${generate_div}`);

        var form = document.createElement("li");
        form.innerHTML = title_name;

        div.appendChild(form);                
      }

    var counter = 0;

    function add(count_id, div_id, generate_div, title_name = "") {

        var div = document.querySelector(`#${generate_div}`);

        var form = document.createElement("input");
        form.className = `form-control; title_${generate_div}`;
        form.autofocus = true;
        form.id = `form_${generate_div}_${counter}`;
        form.type = "text";
        form.value = title_name;

        var button = document.createElement("input");
        button.className = "btn btn-primary";
        button.value = "Remove This Title";
        var count = counter;
        button.onclick = function() {
            remove_title(`${generate_div}`, `${count}`);
        };
        button.id = `remove_${generate_div}_${count}`;

        div.appendChild(form);
        div.appendChild(button);
        div.appendChild(document.createElement("br"));

        counter++;
                
      }

      function count_requests(){
        fetch(`/request_count`)
        .then(response => response.json())
        .then(data => {
            if (data !== false && data > 0){
                var div = document.getElementById(`request_count`);
                div.innerHTML = "Connection Requests: " + data;
            }
        })
        .catch(error => {
        console.log('Error:', error);
        });
}
      

      function remove_title(generate_div, count){

        var div = document.getElementById(`${generate_div}`);
        var form = document.getElementById(`form_${generate_div}_${count}`);
        var button = document.getElementById(`remove_${generate_div}_${count}`);

        div.removeChild(form);
        div.removeChild(button.nextSibling);
        div.removeChild(button);        
      }

      function update_profile() {

        var data = {}

        data.pen_name = document.querySelector('input[name="pen_name"]').value

        if (document.querySelector('input[name="author"]').checked) {
            data.author = true;
            if (document.querySelector('input[name="author_fantasy"]').checked) {
                data.author_fantasy = true;
            }
            else {
                data.author_fantasy = false;
            }
            if (document.querySelector('input[name="author_scifi"]').checked) {
                data.author_scifi = true;
            }
            else {
                data.author_scifi = false;
            }
            if (document.querySelector('input[name="author_historical"]').checked) {
                data.author_historical = true;
            }
            else {
                data.author_historical = false;
            }
            if (document.querySelector('input[name="author_bio"]').checked) {
                data.author_bio = true;
            }
            else {
                data.author_bio = false;
            }
            if (document.querySelector('input[name="author_realistic"]').checked) {
                data.author_realistic = true;
            }
            else {
                data.author_realistic = false;
            }
            if (document.querySelector('input[name="author_children"]').checked) {
                data.author_children = true;
            }
            else {
                data.author_children = false;
            }
            if (document.querySelector('input[name="author_young"]').checked) {
                data.author_young = true;
            }
            else {
                data.author_young = false;
            }
            if (document.querySelector('input[name="author_adult"]').checked) {
                data.author_adult = true;
            }
            else {
                data.author_adult = false;
            }
            if (document.querySelector('input[name="author_spiritual"]').checked) {
                data.author_spiritual = true;
            }
            else {
                data.author_spiritual = false;
            }
            if (document.querySelector('input[name="author_help"]').checked) {
                data.author_help = true;
            }
            else {
                data.author_help = false;
            }
            if (document.querySelector('input[name="author_short"]').checked) {
                data.author_short = true;
            }
            else {
                data.author_short = false;
            }
            if (document.querySelector('input[name="author_novel"]').checked) {
                data.author_novel = true;
            }
            else {
                data.author_novel = false;
            }
            if (document.querySelector('input[name="author_poetry"]').checked) {
                data.author_poetry = true;
            }
            else {
                data.author_poetry = false;
            }
            if (document.querySelector('input[name="author_comic"]').checked) {
                data.author_comic = true;
            }
            else {
                data.author_comic = false;
            }

            var titles = document.getElementsByClassName("title_published_titles");
            data.author_titles = [];
            var i;
            for (i = 0; i < titles.length; i++) {
                if (titles[i].value.length > 0){
                    data.author_titles.push(titles[i].value);
                }
                
            }
        }
        else {
            data.author = false;
            data.author_fantasy = false;
            data.author_scifi = false;
            data.author_historical = false;
            data.author_bio = false;
            data.author_realistic = false;
            data.author_children = false;
            data.author_young = false;
            data.author_adult = false;
            data.author_spiritual = false;
            data.author_help = false;
            data.author_short = false;
            data.author_novel = false;
            data.author_poetry = false;
            data.author_comic = false;
            data.author_titles = [];
        }

        if (document.querySelector('input[name="editor"]').checked) {
            data.editor = true;
            if (document.querySelector('input[name="editor_fantasy"]').checked) {
                data.editor_fantasy = true;
            }
            else {
                data.editor_fantasy = false;
            }
            if (document.querySelector('input[name="editor_scifi"]').checked) {
                data.editor_scifi = true;
            }
            else {
                data.editor_scifi = false;
            }
            if (document.querySelector('input[name="editor_historical"]').checked) {
                data.editor_historical = true;
            }
            else {
                data.editor_historical = false;
            }
            if (document.querySelector('input[name="editor_bio"]').checked) {
                data.editor_bio = true;
            }
            else {
                data.editor_bio = false;
            }
            if (document.querySelector('input[name="editor_realistic"]').checked) {
                data.editor_realistic = true;
            }
            else {
                data.editor_realistic = false;
            }
            if (document.querySelector('input[name="editor_children"]').checked) {
                data.editor_children = true;
            }
            else {
                data.editor_children = false;
            }
            if (document.querySelector('input[name="editor_young"]').checked) {
                data.editor_young = true;
            }
            else {
                data.editor_young = false;
            }
            if (document.querySelector('input[name="editor_adult"]').checked) {
                data.editor_adult = true;
            }
            else {
                data.editor_adult = false;
            }
            if (document.querySelector('input[name="editor_spiritual"]').checked) {
                data.editor_spiritual = true;
            }
            else {
                data.editor_spiritual = false;
            }
            if (document.querySelector('input[name="editor_help"]').checked) {
                data.editor_help = true;
            }
            else {
                data.editor_help = false;
            }
            if (document.querySelector('input[name="editor_short"]').checked) {
                data.editor_short = true;
            }
            else {
                data.editor_short = false;
            }
            if (document.querySelector('input[name="editor_novel"]').checked) {
                data.editor_novel = true;
            }
            else {
                data.editor_novel = false;
            }
            if (document.querySelector('input[name="editor_poetry"]').checked) {
                data.editor_poetry = true;
            }
            else {
                data.editor_poetry = false;
            }
            if (document.querySelector('input[name="editor_comic"]').checked) {
                data.editor_comic = true;
            }
            else {
                data.editor_comic = false;
            }
            var titles = document.getElementsByClassName("title_published_edited_titles");
            data.editor_titles = [];
            var i;
            for (i = 0; i < titles.length; i++) {
                if (titles[i].value.length > 0){
                    data.editor_titles.push(titles[i].value);
                }
                
            }
        }
        else {
            data.editor = false;
            data.editor_fantasy = false;
            data.editor_scifi = false;
            data.editor_historical = false;
            data.editor_bio = false;
            data.editor_realistic = false;
            data.editor_children = false;
            data.editor_young = false;
            data.editor_adult = false;
            data.editor_spiritual = false;
            data.editor_help = false;
            data.editor_short = false;
            data.editor_novel = false;
            data.editor_poetry = false;
            data.editor_comic = false;
            data.editor_titles = [];
        }

        if (document.querySelector('input[name="agent"]').checked) {
            data.agent = true;
            if (document.querySelector('input[name="agent_fantasy"]').checked) {
                data.agent_fantasy = true;
            }
            else {
                data.agent_fantasy = false;
            }
            if (document.querySelector('input[name="agent_scifi"]').checked) {
                data.agent_scifi = true;
            }
            else {
                data.agent_scifi = false;
            }
            if (document.querySelector('input[name="agent_historical"]').checked) {
                data.agent_historical = true;
            }
            else {
                data.agent_historical = false;
            }
            if (document.querySelector('input[name="agent_bio"]').checked) {
                data.agent_bio = true;
            }
            else {
                data.agent_bio = false;
            }
            if (document.querySelector('input[name="agent_realistic"]').checked) {
                data.agent_realistic = true;
            }
            else {
                data.agent_realistic = false;
            }
            if (document.querySelector('input[name="agent_children"]').checked) {
                data.agent_children = true;
            }
            else {
                data.agent_children = false;
            }
            if (document.querySelector('input[name="agent_young"]').checked) {
                data.agent_young = true;
            }
            else {
                data.agent_young = false;
            }
            if (document.querySelector('input[name="agent_adult"]').checked) {
                data.agent_adult = true;
            }
            else {
                data.agent_adult = false;
            }
            if (document.querySelector('input[name="agent_spiritual"]').checked) {
                data.agent_spiritual = true;
            }
            else {
                data.agent_spiritual = false;
            }
            if (document.querySelector('input[name="agent_help"]').checked) {
                data.agent_help = true;
            }
            else {
                data.agent_help = false;
            }
            if (document.querySelector('input[name="agent_short"]').checked) {
                data.agent_short = true;
            }
            else {
                data.agent_short = false;
            }
            if (document.querySelector('input[name="agent_novel"]').checked) {
                data.agent_novel = true;
            }
            else {
                data.agent_novel = false;
            }
            if (document.querySelector('input[name="agent_poetry"]').checked) {
                data.agent_poetry = true;
            }
            else {
                data.agent_poetry = false;
            }
            if (document.querySelector('input[name="agent_comic"]').checked) {
                data.agent_comic = true;
            }
            else {
                data.agent_comic = false;
            }
            var titles = document.getElementsByClassName("title_published_agent_titles");
            data.agent_titles = [];
            var i;
            for (i = 0; i < titles.length; i++) {
                if (titles[i].value.length > 0){
                    data.agent_titles.push(titles[i].value);
                }
                
            }
        }
        else {
            data.agent = false;
            data.agent_fantasy = false;
            data.agent_scifi = false;
            data.agent_historical = false;
            data.agent_bio = false;
            data.agent_realistic = false;
            data.agent_children = false;
            data.agent_young = false;
            data.agent_adult = false;
            data.agent_spiritual = false;
            data.agent_help = false;
            data.agent_short = false;
            data.agent_novel = false;
            data.agent_poetry = false;
            data.agent_comic = false;
            data.agent_titles = [];
        }

        if (document.querySelector('input[name="publisher"]').checked) {
            data.publisher = true;
            if (document.querySelector('input[name="publisher_fantasy"]').checked) {
                data.publisher_fantasy = true;
            }
            else {
                data.publisher_fantasy = false;
            }
            if (document.querySelector('input[name="publisher_scifi"]').checked) {
                data.publisher_scifi = true;
            }
            else {
                data.publisher_scifi = false;
            }
            if (document.querySelector('input[name="publisher_historical"]').checked) {
                data.publisher_historical = true;
            }
            else {
                data.publisher_historical = false;
            }
            if (document.querySelector('input[name="publisher_bio"]').checked) {
                data.publisher_bio = true;
            }
            else {
                data.publisher_bio = false;
            }
            if (document.querySelector('input[name="publisher_realistic"]').checked) {
                data.publisher_realistic = true;
            }
            else {
                data.publisher_realistic = false;
            }
            if (document.querySelector('input[name="publisher_children"]').checked) {
                data.publisher_children = true;
            }
            else {
                data.publisher_children = false;
            }
            if (document.querySelector('input[name="publisher_young"]').checked) {
                data.publisher_young = true;
            }
            else {
                data.publisher_young = false;
            }
            if (document.querySelector('input[name="publisher_adult"]').checked) {
                data.publisher_adult = true;
            }
            else {
                data.publisher_adult = false;
            }
            if (document.querySelector('input[name="publisher_spiritual"]').checked) {
                data.publisher_spiritual = true;
            }
            else {
                data.publisher_spiritual = false;
            }
            if (document.querySelector('input[name="publisher_help"]').checked) {
                data.publisher_help = true;
            }
            else {
                data.publisher_help = false;
            }
            if (document.querySelector('input[name="publisher_short"]').checked) {
                data.publisher_short = true;
            }
            else {
                data.publisher_short = false;
            }
            if (document.querySelector('input[name="publisher_novel"]').checked) {
                data.publisher_novel = true;
            }
            else {
                data.publisher_novel = false;
            }
            if (document.querySelector('input[name="publisher_poetry"]').checked) {
                data.publisher_poetry = true;
            }
            else {
                data.publisher_poetry = false;
            }
            if (document.querySelector('input[name="publisher_comic"]').checked) {
                data.publisher_comic = true;
            }
            else {
                data.publisher_comic = false;
            }
            var titles = document.getElementsByClassName("title_published_publisher_titles");
            data.publisher_titles = [];
            var i;
            for (i = 0; i < titles.length; i++) {
                if (titles[i].value.length > 0){
                    data.publisher_titles.push(titles[i].value);
                }
                
            }
        }
        else {
            data.publisher = false;
            data.publisher_fantasy = false;
            data.publisher_scifi = false;
            data.publisher_historical = false;
            data.publisher_bio = false;
            data.publisher_realistic = false;
            data.publisher_children = false;
            data.publisher_young = false;
            data.publisher_adult = false;
            data.publisher_spiritual = false;
            data.publisher_help = false;
            data.publisher_short = false;
            data.publisher_novel = false;
            data.publisher_poetry = false;
            data.publisher_comic = false;
            data.publisher_titles = [];
        }

        if (document.querySelector('input[name="proofreader"]').checked) {
            data.proofreader = true;
            var titles = document.getElementsByClassName("title_published_proofread_titles");
            data.proofreader_titles = [];
            var i;
            for (i = 0; i < titles.length; i++) {
                if (titles[i].value.length > 0){
                    data.proofreader_titles.push(titles[i].value);
                }
                
            }
        }
        else {
            data.proofreader = false;
            data.proofreader_titles = [];
        }

        data.summary = document.querySelector('textarea[name="summary"]').value;

        var json = JSON.stringify(data);

        fetch('/update', {
          method: 'POST',
          body: json,
          headers: { "X-CSRFToken": csrfToken,
          "Content-Type": "application/json",
          Accept: 'application/json' }
      });
      }