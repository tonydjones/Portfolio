from django.contrib.auth.models import AbstractUser
from django.db import models


class User(AbstractUser):
    name = models.CharField(blank=True, null=True, default=None, max_length=100)
    patient = models.BooleanField(default=False, blank=False, null=False)
    doctor = models.BooleanField(default=False, blank=False, null=False)
    pharmacy = models.BooleanField(default=False, blank=False, null=False)
    dr_connections = models.ManyToManyField("User", related_name="dr")
    pt_connections = models.ManyToManyField("User", related_name="pt")
    ph_connections = models.ManyToManyField("User", related_name="ph")
    prescriptions = models.ManyToManyField("Prescription", related_name="script_owner")


class Prescription(models.Model):
    medication = models.CharField(blank=True, null=True, default=None, max_length=100)
    dose = models.CharField(blank=True, null=True, default=None, max_length=100)
    directions = models.CharField(blank=True, null=True, default=None, max_length=100)
    count = models.IntegerField(default=None, null=True, blank=True)
    doctor = models.ForeignKey("User", on_delete=models.CASCADE, related_name="dr_prescriptions")
    patient = models.ForeignKey("User", on_delete=models.CASCADE, related_name="pt_prescriptions")
    entries = models.ManyToManyField("Entry")

class Request(models.Model):
    sender = models.ForeignKey("User", on_delete=models.CASCADE, related_name="sent_requests")
    receiver = models.ForeignKey("User", on_delete=models.CASCADE, related_name="received_requests")
    request_type = models.CharField(blank=True, null=True, default=None, max_length=100)
    
class Med_Request(Request):
    amount = models.IntegerField(blank=True, null=True, default=None)
    script = models.ForeignKey("Prescription", on_delete=models.CASCADE, related_name="fill_requests")
    target = models.ForeignKey("User", on_delete=models.CASCADE, related_name="med_requests")
    summary = models.CharField(blank=True, null=True, default=None, max_length=255)
    pharmacy = models.ForeignKey("User", null=True, default=None, on_delete=models.CASCADE, related_name="future_med_requests")

class Entry(models.Model):
    action = models.CharField(blank=True, null=True, default=None, max_length=100)
    start = models.IntegerField(default=None, null=True, blank=True)
    change = models.IntegerField(default=None, null=True, blank=True)
    end = models.IntegerField(default=None, null=True, blank=True)
    timestamp = models.DateTimeField(blank=True)