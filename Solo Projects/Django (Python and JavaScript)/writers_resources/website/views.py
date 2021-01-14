from django.contrib.auth import authenticate, login, logout
from django.db import IntegrityError
from django.http import HttpResponse, HttpResponseRedirect, JsonResponse
from django.shortcuts import render
from django.urls import reverse
from datetime import datetime
import json
from jsonfield import JSONField


from .models import User, Title, Message

def get_time(request, username):
    user = User.objects.get(username=username)
    return JsonResponse(user.get_time(), safe=False)

def find_connections(request):
    user = request.user
    connections = user.connections.all()
    all_users = User.objects.all()
    connection_requests = user.messages.all()
    for connection in connection_requests:
        if connection.sender == user:
            all_users = all_users.exclude(username=connection.receiver.username)
        else:
            all_users = all_users.exclude(username=connection.sender.username)
    for connection in connections:
        all_users = all_users.exclude(username=connection.username)
    all_users = all_users.exclude(username=user.username)
    return render(request, "website/profile.html", {
                "users" : all_users,
                "pending_requests" : True
            })

def my_connections(request):
    user = request.user
    connections = user.connections.all()
    return render(request, "website/profile.html", {
                "users" : connections
            })

def my_connection_requests(request):
    user = request.user
    connection_requests = user.messages.filter(receiver=user)
    all_users = []
    sent_users = []
    sent_requests = user.messages.filter(sender=user)
    for connection in connection_requests:
        all_users.append(connection.sender)
    for connection in sent_requests:
        sent_users.append(connection.receiver)
    return render(request, "website/profile.html", {
                "users" : all_users,
                "find_connections" : True,
                "sent_users" : sent_users
            })
    

def get_works(request, username, job):
    user = User.objects.get(username=username)
    if job == "author":
        works = user.author_titles.all()
    if job == "editor":
        works = user.editor_titles.all()
    if job == "agent":
        works = user.agent_titles.all()
    if job == "publisher":
        works = user.publisher_titles.all()
    if job == "proofreader":
        works = user.proofreader_titles.all()

    titles = []
    for work in works:
        titles.append(work.title)
    return JsonResponse(titles, safe=False)

def get_connected(user, target):
    if not user.is_authenticated:
        return False
    connections = user.connections.all()
    if target in connections:
        return True
    else:
        return False

def get_requested(user, target):
    if not user.is_authenticated:
        return False
    requests = target.messages.all()
    for message in requests:
        if message.sender == user:
            return True
    return False

def get_received(user, target):
    if not user.is_authenticated:
        return False
    requests = user.messages.all()
    for message in requests:
        if message.sender == target:
            return True
    return False


def profile_settings_edit(request):
    if request.method == "POST":
        user = request.user
        user.timestamp = datetime.now()
        data = json.loads(request.body)
        user.pen_name = data["pen_name"]
        user.summary = data["summary"]

        user.proofreader = data["proofreader"]
        titles = Title.objects.filter(proofreader=user)
        for title in titles:
            title.delete()
        titles = data["proofreader_titles"]
        for title in titles:
            new_title = Title(title=title, owner=user)
            new_title.save()
            user.proofreader_titles.add(new_title)
        user.proofreader_count = len(titles)

        user.author = data["author"]
        user.author_fantasy = data["author_fantasy"]
        user.author_scifi = data["author_scifi"]
        user.author_historical = data["author_historical"]
        user.author_bio = data["author_bio"]
        user.author_realistic = data["author_realistic"]
        user.author_children = data["author_children"]
        user.author_young = data["author_young"]
        user.author_adult = data["author_adult"]
        user.author_spiritual = data["author_spiritual"]
        user.author_help = data["author_help"]
        user.author_short = data["author_short"]
        user.author_novel = data["author_novel"]
        user.author_poetry = data["author_poetry"]
        user.author_comic = data["author_comic"]
        titles = Title.objects.filter(author=user)
        for title in titles:
            title.delete()
        titles = data["author_titles"]
        for title in titles:
            new_title = Title(title=title, owner=user)
            new_title.save()
            user.author_titles.add(new_title)
        user.author_count = len(titles)

        user.editor = data["editor"]
        user.editor_fantasy = data["editor_fantasy"]
        user.editor_scifi = data["editor_scifi"]
        user.editor_historical = data["editor_historical"]
        user.editor_bio = data["editor_bio"]
        user.editor_realistic = data["editor_realistic"]
        user.editor_children = data["editor_children"]
        user.editor_young = data["editor_young"]
        user.editor_adult = data["editor_adult"]
        user.editor_spiritual = data["editor_spiritual"]
        user.editor_help = data["editor_help"]
        user.editor_short = data["editor_short"]
        user.editor_novel = data["editor_novel"]
        user.editor_poetry = data["editor_poetry"]
        user.editor_comic = data["editor_comic"]
        titles = Title.objects.filter(editor=user)
        for title in titles:
            title.delete()
        titles = data["editor_titles"]
        for title in titles:
            new_title = Title(title=title, owner=user)
            new_title.save()
            user.editor_titles.add(new_title)
        user.editor_count = len(titles)

        user.agent = data["agent"]
        user.agent_fantasy = data["agent_fantasy"]
        user.agent_scifi = data["agent_scifi"]
        user.agent_historical = data["agent_historical"]
        user.agent_bio = data["agent_bio"]
        user.agent_realistic = data["agent_realistic"]
        user.agent_children = data["agent_children"]
        user.agent_young = data["agent_young"]
        user.agent_adult = data["agent_adult"]
        user.agent_spiritual = data["agent_spiritual"]
        user.agent_help = data["agent_help"]
        user.agent_short = data["agent_short"]
        user.agent_novel = data["agent_novel"]
        user.agent_poetry = data["agent_poetry"]
        user.agent_comic = data["agent_comic"]
        titles = Title.objects.filter(agent=user)
        for title in titles:
            title.delete()
        titles = data["agent_titles"]
        for title in titles:
            new_title = Title(title=title, owner=user)
            new_title.save()
            user.agent_titles.add(new_title)
        user.agent_count = len(titles)

        user.publisher = data["publisher"]
        user.publisher_fantasy = data["publisher_fantasy"]
        user.publisher_scifi = data["publisher_scifi"]
        user.publisher_historical = data["publisher_historical"]
        user.publisher_bio = data["publisher_bio"]
        user.publisher_realistic = data["publisher_realistic"]
        user.publisher_children = data["publisher_children"]
        user.publisher_young = data["publisher_young"]
        user.publisher_adult = data["publisher_adult"]
        user.publisher_spiritual = data["publisher_spiritual"]
        user.publisher_help = data["publisher_help"]
        user.publisher_short = data["publisher_short"]
        user.publisher_novel = data["publisher_novel"]
        user.publisher_poetry = data["publisher_poetry"]
        user.publisher_comic = data["publisher_comic"]
        titles = Title.objects.filter(publisher=user)
        for title in titles:
            title.delete()
        titles = data["publisher_titles"]
        for title in titles:
            new_title = Title(title=title, owner=user)
            new_title.save()
            user.publisher_titles.add(new_title)
        user.publisher_count = len(titles)
        user.timestamp = datetime.now()
        user.save()
        return HttpResponse(status=204)

def profile(request, username):
    user = request.user
    target = User.objects.get(username=username)
    connected = get_connected(user, target)
    requested = get_requested(user, target)
    received = get_received(user, target)
    return render(request, "website/profile_settings.html", {
                "target" : target,
                "connected" : connected,
                "requested" : requested,
                "received" : received
            })

def send_request(request, targetname):
    user = request.user
    target = User.objects.get(username=targetname)
    request = Message(sender=user, receiver=target, body="")
    request.save()
    target.messages.add(request)
    target.save()
    user.messages.add(request)
    user.save()
    return HttpResponse(status=204)

def accept_request(request, targetname):
    user = request.user
    target = User.objects.get(username=targetname)
    request = Message.objects.get(sender=target, receiver=user)
    user.connections.add(target)
    target.connections.add(user)
    user.connections_count = user.connections_count + 1
    target.connections_count = target.connections_count + 1
    user.save()
    target.save()
    request.delete()
    return HttpResponse(status=204)
    

def get_request_count(request):
    if not request.user.is_authenticated:
        return JsonResponse(False, safe=False)
    return JsonResponse(len(request.user.messages.filter(receiver=request.user)), safe=False)
    
def index(request):
    authors = User.objects.filter(author=True)
    editors = User.objects.filter(editor=True)
    agents = User.objects.filter(agent=True)
    publishers = User.objects.filter(publisher=True)
    proofreaders = User.objects.filter(proofreader=True)

    if len(authors) > 0:
        authors = authors.order_by("-timestamp")
        new_author = authors[0]
        authors = authors.order_by("-connections_count")
        pop_author = authors[0]
        authors = authors.order_by("-author_count")
        pro_author = authors[0]
    else:
        new_author = None
        pop_author = None
        pro_author = None

    if len(editors) > 0:
        editors = editors.order_by("-timestamp")
        new_editor = editors[0]
        editors = editors.order_by("-connections_count")
        pop_editor = editors[0]
        editors = editors.order_by("-editor_count")
        pro_editor = editors[0]
    else:
        new_editor = None
        pop_editor = None
        pro_editor = None

    if len(agents) > 0:
        agents = agents.order_by("-timestamp")
        new_agent = agents[0]
        agents = agents.order_by("-connections_count")
        pop_agent = agents[0]
        agents = agents.order_by("-agent_count")
        pro_agent = agents[0]
    else:
        new_agent = None
        pop_agent = None
        pro_agent = None
    
    if len(publishers) > 0:
        publishers = publishers.order_by("-timestamp")
        new_publisher = publishers[0]
        publishers = publishers.order_by("-connections_count")
        pop_publisher = publishers[0]
        publishers = publishers.order_by("-publisher_count")
        pro_publisher = publishers[0]
    else:
        new_publisher = None
        pop_publisher = None
        pro_publisher = None

    if len(proofreaders) > 0:
        proofreaders = proofreaders.order_by("-timestamp")
        new_proofreader = proofreaders[0]
        proofreaders = proofreaders.order_by("-connections_count")
        pop_proofreader = proofreaders[0]
        proofreaders = proofreaders.order_by("-proofreader_count")
        pro_proofreader = proofreaders[0]
    else:
        new_proofreader = None
        pop_proofreader = None
        pro_proofreader = None

    return render(request, "website/index.html", {
        "new_author" : new_author,
        "new_editor" : new_editor,
        "new_agent" : new_agent,
        "new_publisher" : new_publisher,
        "new_proofreader" : new_proofreader,
        "pop_author" : pop_author,
        "pop_editor" : pop_editor,
        "pop_agent" : pop_agent,
        "pop_publisher" : pop_publisher,
        "pop_proofreader" : pop_proofreader,
        "pro_author" : pro_author,
        "pro_editor" : pro_editor,
        "pro_agent" : pro_agent,
        "pro_publisher" : pro_publisher,
        "pro_proofreader" : pro_proofreader
    })

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
            return render(request, "website/login.html", {
                "message": "Invalid username and/or password."
            })
    else:
        return render(request, "website/login.html")


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
            return render(request, "website/register.html", {
                "message": "Passwords must match."
            })

        # Attempt to create new user
        try:
            user = User.objects.create_user(username, email, password)
            user.timestamp = datetime.now()
            user.pen_name = user.username
            user.summary = ""
            user.save()
        except IntegrityError:
            return render(request, "website/register.html", {
                "message": "Username already taken."
            })
        login(request, user)
        return HttpResponseRedirect(reverse("profile", kwargs={'username': user.username}))
    else:
        return render(request, "website/register.html")
