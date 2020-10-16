from django.contrib.auth import authenticate, login, logout
from django.db import IntegrityError
from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import render
from django.urls import reverse
import json
from django.http import JsonResponse
from datetime import datetime

from .models import User, Post


def index(request):
    if request.method == "POST":
        body = request.POST["new_post"]
        owner = request.user
        timestamp = datetime.now()
        post = Post(body=body, owner=owner, timestamp=timestamp)
        post.save()
        owner.my_posts.add(post)
        followers = owner.followers.all()
        for follower in followers:
            follower.following_posts.add(post)
            follower.save()
        owner.save()
        return HttpResponseRedirect(reverse("index"))
    return render(request, "network/index.html")

def all_posts(request):
    posts = Post.objects.all()
    posts = posts.order_by("-timestamp").all()
    liked = list()
    for x in range(len(posts)):
        if request.user.is_authenticated and posts[x] in request.user.liked_posts.all():
            liked.append(True)
        else:
            liked.append(False)

    return JsonResponse([[post.serialize() for post in posts], liked], safe=False)

def profile(request, username):
    if request.method == "POST":
        body = request.POST["new_post"]
        owner = request.user
        post = Post(body=body, owner=owner)
        post.save()
        owner.my_posts.add(post)
        followers = owner.followers.all()
        for follower in followers:
            follower.following_posts.add(post)
            follower.save()
        owner.save()
        return HttpResponseRedirect(reverse('profile', kwargs={'username': username}))
    user = User.objects.get(username=username)
    followers = user.followers.all().count()
    following = user.following.all().count()
    if user in request.user.following.all():
        user_following = True
    else:
        user_following = False
    return render(request, "network/profile.html", {
        "profile" : user,
        "followers" : followers,
        "following" : following,
        "user_following" : user_following
    })

def following(request):
    return render(request, "network/following.html")

def update_post(request, post_id):
    post = Post.objects.get(id=post_id)
    data = json.loads(request.body)
    post.body = data["body"]
    post.save()
    return HttpResponse(status=204)

def like_post(request, post_id):
    post = Post.objects.get(id=post_id)
    if (post in request.user.liked_posts.all()):
        request.user.liked_posts.remove(post)
        post.likes -= 1
    else:
        request.user.liked_posts.add(post)
        post.likes += 1
    request.user.save()
    post.save()
    return JsonResponse(post.likes, safe=False)

def following_posts(request):
    posts = request.user.following_posts.all()
    posts = posts.order_by("-timestamp").all()
    liked = list()
    for x in range(len(posts)):
        if request.user.is_authenticated and posts[x] in request.user.liked_posts.all():
            liked.append(True)
        else:
            liked.append(False)

    return JsonResponse([[post.serialize() for post in posts], liked], safe=False)

def follow(request, username):
    user = User.objects.get(username=username)
    request.user.following.add(user)
    for post in user.my_posts.all(): 
        request.user.following_posts.add(post)
    return HttpResponse(status=204)

def unfollow(request, username):
    user = User.objects.get(username=username)
    request.user.following.remove(user)
    for post in user.my_posts.all(): 
        request.user.following_posts.remove(post)
    return HttpResponse(status=204)

def user_posts(request, username):
    user = User.objects.get(username=username)
    posts = user.my_posts.all()
    posts = posts.order_by("-timestamp").all()
    liked = list()
    for x in range(len(posts)):
        if request.user.is_authenticated and posts[x] in request.user.liked_posts.all():
            liked.append(True)
        else:
            liked.append(False)

    print(user.username)
    print(posts)
    print(liked)


    return JsonResponse([[post.serialize() for post in posts], liked], safe=False)

def login_view(request):
    if request.method == "POST":

        # Attempt to sign user in
        username = request.POST["username"]
        password = request.POST["password"]
        user = authenticate(request, username=username, password=password)

        # Check if authentication successful
        if user is not None:
            login(request, user)
            return HttpResponseRedirect(reverse("index"))
        else:
            return render(request, "network/login.html", {
                "message": "Invalid username and/or password."
            })
    else:
        return render(request, "network/login.html")


def logout_view(request):
    logout(request)
    return HttpResponseRedirect(reverse("index"))


def register(request):
    if request.method == "POST":
        username = request.POST["username"]
        email = request.POST["email"]

        # Ensure password matches confirmation
        password = request.POST["password"]
        confirmation = request.POST["confirmation"]
        if password != confirmation:
            return render(request, "network/register.html", {
                "message": "Passwords must match."
            })

        # Attempt to create new user
        try:
            user = User.objects.create_user(username, email, password)
            user.save()
        except IntegrityError:
            return render(request, "network/register.html", {
                "message": "Username already taken."
            })
        login(request, user)
        return HttpResponseRedirect(reverse("index"))
    else:
        return render(request, "network/register.html")
