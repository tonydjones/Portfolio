from django.contrib.auth import authenticate, login, logout
from django.db import IntegrityError
from django.http import HttpResponse, HttpResponseRedirect, Http404
from django.shortcuts import render
from django.urls import reverse

from .models import User, Item, Bid, Comment


def index(request):
    #Item.objects.all().delete()
    items = Item.objects.filter(active=True)
    for item in items:
        update(request, item, request.user)
    return render(request, "auctions/index.html", {
        "items" : items
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
            return render(request, "auctions/login.html", {
                "message": "Invalid username and/or password."
            })
    else:
        return render(request, "auctions/login.html")


def logout_view(request):
    logout(request)
    return HttpResponseRedirect(reverse("index"))


def register(request):
    if request.method == "POST":
        username = request.POST["username"]

        # Ensure password matches confirmation
        password = request.POST["password"]
        confirmation = request.POST["confirmation"]
        if password != confirmation:
            return render(request, "auctions/register.html", {
                "message": "Passwords must match."
            })

        # Attempt to create new user
        try:
            user = User.objects.create_user(username, password)
            user.save()
        except IntegrityError:
            return render(request, "auctions/register.html", {
                "message": "Username already taken."
            })
        login(request, user)
        return HttpResponseRedirect(reverse("index"))
    else:
        return render(request, "auctions/register.html")

def create(request):
    if request.method == "POST":
        title = request.POST["title"]
        price = request.POST["price"]
        description = request.POST["description"]
        image = request.POST["image"]
        category = request.POST["category"]
        owner = request.user
        if image == '':
            image = None

        if category == '':
            category = None

        # create new item posting
        item = Item(title=title, price=price, description=description, image=image, category=category, current_price=price, owner = owner)
        item.save()
        item_id = item.id
        return HttpResponseRedirect(reverse("item", args=(item_id,)))
    else:
        return render(request, "auctions/create.html")


def update(request, item, user):

    if request.POST.get("toggle") == "remove":
        user.watching.remove(item)
    elif request.POST.get("toggle") == "add":
        user.watching.add(item)

    if request.POST.get("bid"):
        bid = Bid(owner=user, item=item, value=request.POST["bid"])
        bid.save()

    if request.POST.get("close"):
        item.active = False
        item.save()

    if request.POST.get("comment"):
        comment = Comment(commenter=user, item=item, text=request.POST["comment"])
        comment.save()

    price = item.price
    buyer = item.buyer
    bids = item.bids.all()
    for bid in bids:
        if bid.value > price:
            price = bid.value
            buyer = bid.owner

    item.current_price = price
    item.buyer = buyer
    item.save()

        


def listing(request, item_id):

    try:
        item = Item.objects.get(id=item_id)
        user = request.user
    except Item.DoesNotExist:
        raise Http404("Item not found.")


    update(request, item, user)


    return render(request, "auctions/item.html", {
        "item" : item,
        "title": item.title,
        "image": item.image,
        "description": item.description,
        "price" : item.current_price,
        "watching" : user.watching.all(),
        "min_bid" : float(item.current_price) + 0.01,
        "comments" : item.comments.all()
    })

def watchlist(request):
    items = request.user.watching.all()
    for item in items:
        update(request, item, request.user)
    return render(request, "auctions/watchlist.html", {
        "user" : request.user,
        "items" : items
    })

def categories(request):
    items = Item.objects.filter(active=True)
    categories = set()
    for item in items:
        if item.category:
            categories.add(item.category)
    return render(request, "auctions/categories.html", {
        "categories" : categories
    })

def category(request, category_id):
    items = Item.objects.filter(active=True, category=category_id)
    categories = set()
    for item in items:
        update(request, item, request.user)
    return render(request, "auctions/index.html", {
        "items" : items,
        "category": category_id
    })
