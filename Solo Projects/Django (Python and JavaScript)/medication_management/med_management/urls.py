
from django.urls import path

from . import views

urlpatterns = [
    path("", views.index, name="index"),
    path("logout", views.logout_view, name="logout"),
    path("register", views.register, name="register"),
    path("settings", views.settings, name="settings"),
    path("find_doc", views.find_doc, name="find_doc"),
    path("find_pharm", views.find_pharm, name="find_pharm"),
    path("pt_connections", views.pt_connections, name="pt_connections"),
    path("find_pt", views.find_pt, name="find_pt"),
    path("dr_connections", views.dr_connections, name="dr_connections"),
    path("refills", views.refills, name="refills"),
    path("connect", views.connect, name="connect"),
    path("disconnect", views.disconnect, name="disconnect"),
    path("request", views.request, name="request"),
    path("reject", views.reject, name="reject"),
    path("reject_general", views.reject_general, name="reject_general"),
    path("prepare_refill", views.prepare_refill, name="prepare_refill"),
    path("confirm_refill", views.confirm_refill, name="confirm_refill"),
    path("refill/<int:prescription>", views.refill, name="refill"),
    path("default_refill/<int:request_id>", views.default_refill, name="refill"),
    path("adjust_refill/<int:request_id>", views.adjust_refill, name="adjust_refill"),
    path("ask_for_refill/<int:prescription>", views.ask_for_refill, name="ask_for_refill"),
    path("take/<int:prescription_id>", views.take, name="take"),
    path("get_count/<int:prescription_id>", views.get_count, name="get_count"),
    path("get_history/<str:username>", views.get_history, name="get_history"),
    path("save/<int:prescription>", views.save, name="save"),
    path("discontinue/<int:prescription>", views.discontinue, name="discontinue"),
    path("search/<str:type>/<str:text>", views.search, name="search"),
    path("prescribe/<str:username>", views.prescribe, name="prescribe"),
    path("get_dr_requests/<str:username>", views.get_dr_requests, name="get_dr_requests"),
    path("patient/<str:username>", views.meds, name="meds"),
]
