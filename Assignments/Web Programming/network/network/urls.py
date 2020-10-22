
from django.urls import path

from . import views

urlpatterns = [
    path("", views.index, name="index"),
    path("login", views.login_view, name="login"),
    path("logout", views.logout_view, name="logout"),
    path("register", views.register, name="register"),
    path("posts/all", views.all_posts, name="all"),
    path("profile/<str:username>", views.profile, name="profile"),
    path("follow/<str:username>", views.follow),
    path("unfollow/<str:username>", views.unfollow),
    path("posts/following", views.following_posts),
    path("posts/<int:post_id>", views.update_post),
    path("posts/<str:username>", views.user_posts),
    path("like/<int:post_id>", views.like_post),
    path("following", views.following, name="following")
]
