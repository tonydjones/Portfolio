
from django.urls import path

from . import views

urlpatterns = [
    path("", views.index, name="index"),
    path("login", views.login_view, name="login"),
    path("logout", views.logout_view, name="logout"),
    path("register", views.register, name="register"),
    path("update", views.profile_settings_edit, name="profile_settings_edit"),
    path("request_count", views.get_request_count, name="get_request_count"),
    path("find_connections", views.find_connections, name="find_connections"),
    path("my_connections", views.my_connections, name="my_connections"),
    path("my_connection_requests", views.my_connection_requests, name="my_connection_requests"),
    path("get_works/<str:username>/<str:job>", views.get_works, name="get_works"),
    path("send_request/<str:targetname>", views.send_request, name="send_request"),
    path("accept_request/<str:targetname>", views.accept_request, name="accept_request"),
    path("profile/<str:username>", views.profile, name="profile"),
    path("get_time/<str:username>", views.get_time, name="get_time")
]
