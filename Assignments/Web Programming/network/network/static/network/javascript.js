var page = 1;
var public_posts = new Array();
var csrfToken = null;


document.addEventListener('DOMContentLoaded', function() {
  if (document.querySelector("[name='csrfmiddlewaretoken']")) {
    csrfToken = document.querySelector("[name='csrfmiddlewaretoken']").value
  }
    page = 1;
    public_posts = new Array();
    document.querySelector('#page').innerHTML = `Page ${page}`;
    load_posts();
});
  
  function generate_div(post, liked) {

  
    document.querySelector('#posts').innerHTML += 
      `<div style="border: thin solid black" id="email${post.id}">
      <a href="profile/${post.owner}"> <h5> ${post.owner} </h5> </a>
      <h6 id="body${post.id}"> ${post.body} </h6>
      <form method="POST">
      <textarea class="form-control" style="display : none;" id="edit_post${post.id}">${post.body}</textarea>
      <p> ${post.timestamp} </p>
      <p id="likes${post.id}"> ${post.likes} Likes</p>
      <button type="button" class="btn btn-sm btn-outline-primary" id="like${post.id}" style="display : none;" onclick="like(${post.id})">Like</button>
      <button type="button" class="btn btn-sm btn-outline-primary" id="edit${post.id}" style="display : none;" onclick="edit(${post.id})">Edit</button>
      <button type="button" class="btn btn-sm btn-outline-primary" id="save${post.id}" style="display : none;" onclick="save(${post.id})">Save</button>
      </form>
      </div>`;

    if (document.querySelector(`#username`)) {
        if (post.owner == document.querySelector(`#username`).value) {
            document.querySelector(`#edit${post.id}`).style.display = 'block';
        }
        document.querySelector(`#like${post.id}`).style.display = 'block';
        if (liked) {
            document.querySelector(`#like${post.id}`).innerHTML = 'Unlike';
        }
    }
  }

  function like(post_id) {
    fetch(`/like/${post_id}`)
        .then(response => response.json())
        .then(likes => {
            document.querySelector(`#likes${post_id}`).innerHTML = `${likes} Likes`;
            if (document.querySelector(`#like${post_id}`).innerHTML === 'Unlike') {
                document.querySelector(`#like${post_id}`).innerHTML = 'Like'
            }
            else{
                document.querySelector(`#like${post_id}`).innerHTML = 'Unlike'
            }
      });
  }

  function edit(post_id) {
    document.querySelector(`#edit${post_id}`).style.display = 'none';
    document.querySelector(`#save${post_id}`).style.display = 'block';
    document.querySelector(`#edit_post${post_id}`).style.display = 'block';
    document.querySelector(`#edit_post${post_id}`).required = true;
    document.querySelector(`#body${post_id}`).style.display = 'none';
  }

  function save(post_id) {
    document.querySelector(`#body${post_id}`).innerHTML = document.querySelector(`#edit_post${post_id}`).value;
    document.querySelector(`#edit_post${post_id}`).innerHTML = document.querySelector(`#body${post_id}`).value;
    fetch(`/posts/${post_id}`, {
      method: 'PUT',
      body: JSON.stringify({
          body: document.querySelector(`#edit_post${post_id}`).value
      }),
      headers: { "X-CSRFToken": csrfToken,
      "Content-Type": "application/json",
      Accept: 'application/json' }
  });
    
    document.querySelector(`#edit${post_id}`).style.display = 'block';
    document.querySelector(`#save${post_id}`).style.display = 'none';
    document.querySelector(`#edit_post${post_id}`).style.display = 'none';
    document.querySelector(`#edit_post${post_id}`).required = false;
    document.querySelector(`#body${post_id}`).style.display = 'block';
  }
  
  function next() {

    document.querySelector('#posts').innerHTML = '';
    document.querySelector('#previous').disabled = false;

    var start = page * 10;

    var finish = start + 10;

    page++;

                if (finish >= public_posts.length) {
                    finish = public_posts.length;
                    document.querySelector('#next').disabled = true;
                }
                for (i = start; i < finish; i++) {
                generate_div(public_posts[i]);
                }
                document.querySelector('#page').innerHTML = `Page ${page}`;
  }

  function previous() {

    document.querySelector('#posts').innerHTML = '';
    document.querySelector('#next').disabled = false;

    var start = (page - 2) * 10;

    var finish = start + 9;

    page--;

                if (page === 1) {
                    document.querySelector('#previous').disabled = true;
                }
                for (i = start; i < finish; i++) {
                generate_div(public_posts[i]);
                }
                document.querySelector('#page').innerHTML = `Page ${page}`;
  }

  function follow() {
      if (user_following === true) {
        document.querySelector('#follow').innerHTML = 'Follow';
        followers--;
        document.querySelector('#followers').innerHTML = `${username} has ${followers} followers`;
        user_following = false;
        fetch(`/unfollow/${username}`)
        .catch(error => {
          console.log('Error:', error);
      });
      }
      else {
        document.querySelector('#follow').innerHTML = 'Unfollow';
        followers++;
        document.querySelector('#followers').innerHTML = `${username} has ${followers} followers`;
        user_following = true;
        fetch(`/follow/${username}`)
        .catch(error => {
          console.log('Error:', error);
      });
      }
  }