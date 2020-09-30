from django.urls import path

from . import views

urlpatterns = [
    path("", views.index, name="index"),
    path("encyclopedia/", views.index),
    path("wiki/", views.index),
    path("wiki/<str:title>", views.page, name="page"),
    path("search/", views.search, name="search"),
    path("new/", views.new, name="new"),
    path("edit/", views.edit, name="edit"),
    path("random/", views.choose, name="random")
]
