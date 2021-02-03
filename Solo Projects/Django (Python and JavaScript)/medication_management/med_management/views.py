from django.contrib.auth import authenticate, login, logout
from django.db import IntegrityError
from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import render
from django.urls import reverse
import json
from django.http import JsonResponse
from datetime import datetime

from .models import User, Prescription, Entry, Request, Med_Request

def meds(request, username):
    user = request.user
    target = User.objects.get(username=username)
    print(target.username)
    prescriptions = Prescription.objects.filter(patient=target)
    pharmacies = target.ph_connections.all()
    if not user.is_authenticated:
        return render(request, "med_management/meds.html", {
            "error" : "login"
        })
    elif not target.patient:
        return render(request, "med_management/meds.html", {
            "error" : "not_a_patient"
        })
    elif user.pharmacy:
        return render(request, "med_management/meds.html", {
            "error" : "pharmacy"
        })
    elif user.doctor and user.username != username:
        if user not in target.dr_connections.all():
            return render(request, "med_management/meds.html", {
                "error" : "not_my_patient"
            })
        return render(request, "med_management/meds.html", {
            "rel" : "dr-pt",
            "target" : target,
            "prescriptions" : prescriptions,
            "pharmacies" : pharmacies
        })
    elif user.patient:
        if user != target:
            return render(request, "med_management/meds.html", {
                "error" : "not_me"
            })
        doctors = target.dr_connections.all()
        return render(request, "med_management/meds.html", {
            "rel" : "pt-self",
            "target" : target,
            "prescriptions" : prescriptions,
            "doctors" : doctors,
            "pharmacies" : pharmacies
        })
    else:
        return render(request, "med_management/meds.html", {
            "error" : "permission"
        })

def get_dr_requests(request, username):
    dr_requests = Med_Request.objects.filter(receiver=User.objects.get(username=username), request_type="want_refill")
    results = dict()
    for med_request in dr_requests:
        pharmacies = med_request.target.ph_connections.all()
        pharms = []
        for pharm in pharmacies:
            pharms.append([pharm.username, pharm.name])
        results[med_request.id] = pharms
    return JsonResponse(results, safe=False)

def save(request, prescription):
    data = json.loads(request.body)
    script = Prescription.objects.get(id=prescription)
    script.directions = data
    script.save()
    return HttpResponse(status=204)

def get_history(request, username):
    patient = User.objects.get(username=username)
    prescriptions = Prescription.objects.filter(patient=patient)
    results = dict()
    for prescription in prescriptions:
        entries = prescription.entries.all()
        entries = entries.order_by("-timestamp").all()
        entry_list = []
        for entry in entries:
            entry_dict = dict()
            entry_dict["action"] = entry.action
            entry_dict["start"] = entry.start
            entry_dict["change"] = entry.change
            entry_dict["end"] = entry.end
            entry_dict["timestamp"] = entry.timestamp.strftime("%b %#d %Y, %#I:%M %p")
            entry_list.append(entry_dict)

        results[prescription.id] = entry_list

    return JsonResponse(results, safe=False)

def prescribe(request, username):
    user = request.user
    data = json.loads(request.body)
    other_user = User.objects.get(username=username)
    pharmacy = User.objects.get(username=data["pharmacy"])
    medication = data["medication"]
    dose = data["dose"]
    directions = data["directions"]
    count = data["count"]

    prescription = Prescription(medication=medication, dose=dose, directions=directions, doctor=user, patient=other_user)

    summary = other_user.name + " " + medication + " " + dose
    if count != "":
        summary += " " + count + " doses"
        count = int(count)
        prescription.count = 0
    else:
        count = None
        
    prescription.save()

    fill_request = Med_Request(sender=user, receiver=pharmacy, request_type="dr_ph_refill", amount=count, script=prescription, target=other_user, summary=summary)
    fill_request.save()
    return HttpResponse(status=204)

def prepare_refill(request):
    request_id = json.loads(request.body)
    the_request = Med_Request.objects.get(id=request_id)
    the_request.request_type = "ready_for_pickup"
    the_request.save()
    return HttpResponse(status=204)

def default_refill(request, request_id):
    the_request = Med_Request.objects.get(id=request_id)
    the_request.receiver = the_request.pharmacy
    prescription = the_request.script

    summary = the_request.target.name + " " + prescription.medication + " " + prescription.dose
    if the_request.amount != None:
        summary += " " + str(the_request.amount) + " doses"

    the_request.summary = summary
    the_request.request_type = "dr_ph_refill"

    the_request.save()
    return HttpResponse(status=204)

def adjust_refill(request, request_id):
    the_request = Med_Request.objects.get(id=request_id)
    prescription = the_request.script

    data = json.loads(request.body)
    pharmacy = User.objects.get(username=data["pharmacy"])
    count = data["count"]

    the_request.receiver = pharmacy

    summary = the_request.target.name + " " + prescription.medication + " " + prescription.dose
    if count != "":
        summary += " " + count + " doses"
        the_request.amount = int(count)
    else:
        the_request.amount = None

    the_request.summary = summary
    the_request.request_type = "dr_ph_refill"

    the_request.save()
    return HttpResponse(status=204)

def take(request, prescription_id):
    count = json.loads(request.body)
    prescription = Prescription.objects.get(id=prescription_id)
    if count != "":
        count = -1 * int(count)
        entry = Entry(action="Took Meds", start=prescription.count, change=count, timestamp=datetime.now())
        prescription.count = prescription.count + count
        entry.end = prescription.count
    else:
        entry = Entry(action="Took Meds", timestamp=datetime.now())

    entry.save()
    prescription.entries.add(entry)
    prescription.save()

    entry_dict = dict()
    entry_dict["action"] = entry.action
    entry_dict["start"] = entry.start
    entry_dict["change"] = entry.change
    entry_dict["end"] = entry.end
    entry_dict["timestamp"] = entry.timestamp.strftime("%b %#d %Y, %#I:%M %p")

    return JsonResponse([prescription.count, entry_dict, prescription.id], safe=False)

def get_count(request, prescription_id):
    prescription = Prescription.objects.get(id=prescription_id)
    count = prescription.count
    return JsonResponse(count, safe=False)
        

def search(request, type, text):

    in_requests = []
    out_requests = []

    if type == "dr":
        in_requests = Request.objects.filter(receiver=request.user, request_type="pt_connect")
        out_requests = Request.objects.filter(sender=request.user, request_type="dr_connect")
        connected = request.user.dr_connections.all()
        all = User.objects.filter(doctor=True)

    elif type == "pt":
        in_requests = Request.objects.filter(receiver=request.user, request_type="dr_connect")
        out_requests = Request.objects.filter(sender=request.user, request_type="pt_connect")
        connected = request.user.pt_connections.all()
        all = User.objects.filter(patient=True)

    else:
        connected = request.user.ph_connections.all()
        all = User.objects.filter(pharmacy=True)

    requesting = []
    for pending in in_requests:
        requesting.append(pending.sender)
    for pending in out_requests:
        requesting.append(pending.sender)
    for pending in connected:
        requesting.append(pending.sender)

    for dr in requesting:
        all = all.exclude(username=dr.username)

    all = all.exclude(username=request.user.username)

    results = []

    for subject in all:
        if text.lower() in subject.username.lower() or text.lower() in subject.name.lower():
            results.append(subject.username)

    return JsonResponse(results, safe=False)

def find_doc(request):
    pending_requests = Request.objects.filter(receiver=request.user, request_type="pt_connect")
    requesting_drs = []
    for pending in pending_requests:
        requesting_drs.append(pending.sender)

    all_drs = User.objects.filter(doctor=True)
    my_drs = request.user.dr_connections.all()

    sent_requests = Request.objects.filter(sender=request.user, request_type="dr_connect")
    for dr in sent_requests:
        all_drs = all_drs.exclude(username=dr.receiver.username)

    for dr in requesting_drs:
        all_drs = all_drs.exclude(username=dr.username)

    for dr in my_drs:
        all_drs = all_drs.exclude(username=dr.username)

    all_drs = all_drs.exclude(username=request.user.username)

    return render(request, "med_management/find.html", {
        "type" : "dr",
        "requests" : requesting_drs,
        "all" : all_drs
    })

def find_pharm(request):
    all_ph = User.objects.filter(pharmacy=True)

    my_ph = request.user.ph_connections.all()

    for ph in my_ph:
        all_ph = all_ph.exclude(username=ph.username)

    all_ph = all_ph.exclude(username=request.user.username)

    return render(request, "med_management/find.html", {
        "type" : "pharm",
        "all" : all_ph
    })

def find_pt(request):
    pending_requests = Request.objects.filter(receiver=request.user, request_type="dr_connect")
    requesting_pts = []
    for pending in pending_requests:
        requesting_pts.append(pending.sender)

    all_pts = User.objects.filter(patient=True)
    my_pts = request.user.pt_connections.all()
    
    sent_requests = Request.objects.filter(sender=request.user, request_type="pt_connect")
    for pt in sent_requests:
        all_pts = all_pts.exclude(username=pt.receiver.username)

    for pt in requesting_pts:
        all_pts = all_pts.exclude(username=pt.username)

    for pt in my_pts:
        all_pts = all_pts.exclude(username=pt.username)

    all_pts = all_pts.exclude(username=request.user.username)

    return render(request, "med_management/find.html", {
        "type" : "pt",
        "requests" : requesting_pts,
        "all" : all_pts
    })

def refills(request):
    requests = Med_Request.objects.filter(receiver=request.user, request_type="dr_ph_refill")

    return render(request, "med_management/refills.html", {
        "type" : "refills",
        "requests" : requests
    })

def refill(request, prescription):
    user = request.user
    data = json.loads(request.body)
    pharmacy = User.objects.get(username=data["pharmacy"])
    count = data["count"]
    script = Prescription.objects.get(id=prescription)
    medication = script.medication
    dose = script.dose
    other_user = script.patient

    summary = other_user.name + " " + medication + " " + dose
    if count != "":
        summary += " " + count + " doses"
        count = int(count)
    else:
        count = None

    fill_request = Med_Request(sender=user, receiver=pharmacy, request_type="dr_ph_refill", amount=count, script=script, target=other_user, summary=summary)
    fill_request.save()
    return HttpResponse(status=204)

def ask_for_refill(request, prescription):
    user = request.user
    data = json.loads(request.body)
    pharmacy = User.objects.get(username=data["pharmacy"])
    count = data["count"]
    script = Prescription.objects.get(id=prescription)
    medication = script.medication
    dose = script.dose
    other_user = user
    receiver = User.objects.get(username=data["doctor"])

    summary = other_user.name + " " + medication + " " + dose
    if count != "":
        summary += " " + count + " doses"
        count = int(count)
    else:
        count = None

    summary += " at " + pharmacy.name

    fill_request = Med_Request(sender=user, receiver=receiver, pharmacy=pharmacy, request_type="want_refill", amount=count, script=script, target=other_user, summary=summary)
    fill_request.save()
    return HttpResponse(status=204)

def discontinue(request, prescription):
    script = Prescription.objects.get(id=prescription)

    med_requests = Med_Request.objects.filter(script=script)
    for med_request in med_requests:
        med_request.delete()

    script.delete()
    return HttpResponse(status=204)

def connect(request):
    if request.method == "POST":
        user = request.user
        data = json.loads(request.body)
        other_user = User.objects.get(username=data["username"])
        if data["type"] == 'dr':
            user.dr_connections.add(other_user)
            other_user.pt_connections.add(user)
            Request.objects.get(sender=other_user, receiver=user, request_type="pt_connect").delete()
        elif data["type"] == 'pt':
            user.pt_connections.add(other_user)
            other_user.dr_connections.add(user)
            Request.objects.get(sender=other_user, receiver=user, request_type="dr_connect").delete()
        elif data["type"] == 'ph':
            user.ph_connections.add(other_user)

    return HttpResponse(status=204)

def disconnect(request):
    if request.method == "POST":
        user = request.user
        data = json.loads(request.body)
        other_user = User.objects.get(username=data["username"])
        if data["type"] == 'dr':
            user.dr_connections.remove(other_user)
            other_user.pt_connections.remove(user)
        elif data["type"] == 'pt':
            user.pt_connections.remove(other_user)
            other_user.dr_connections.remove(user)
        elif data["type"] == 'ph':
            user.ph_connections.remove(other_user)

    return HttpResponse(status=204)
        
def request(request):
    if request.method == "POST":
        data = json.loads(request.body)
        other_user = User.objects.get(username=data["username"])
        new_request = Request(sender=request.user, receiver=other_user, request_type=data["type"])
        new_request.save()
    return HttpResponse(status=204)

def reject_general(request):
    if request.method == "POST":
        request_id = json.loads(request.body)
        the_request = Med_Request.objects.get(id=request_id)
        the_request.delete()

    return HttpResponse(status=204)        

def confirm_refill(request):
    request_id = json.loads(request.body)
    the_request = Med_Request.objects.get(id=request_id)
    prescription = the_request.script

    if the_request.amount != None:
        new_entry = Entry(action="refill", timestamp=datetime.now(), start=prescription.count, change=the_request.amount, end=prescription.count+the_request.amount)
        prescription.count = prescription.count + the_request.amount
        new_entry.save()
        prescription.entries.add(new_entry)
        prescription.save()
    else:
        new_entry = Entry(action="refill", timestamp=datetime.now(), start=prescription.count, change=the_request.amount, end=prescription.count)
        new_entry.save()
        prescription.entries.add(new_entry)
        prescription.save()

    the_request.delete()
    return HttpResponse(status=204)

def reject(request):
    if request.method == "POST":
        user = request.user
        data = json.loads(request.body)
        other_user = User.objects.get(username=data["username"])
        if data["type"] == 'dr':
            Request.objects.get(sender=other_user, receiver=user, request_type="pt_connect").delete()
        elif data["type"] == 'pt':
            Request.objects.get(sender=other_user, receiver=user, request_type="dr_connect").delete()

    return HttpResponse(status=204)

def pt_connections(request):
    ph = request.user.ph_connections.all()
    dr = request.user.dr_connections.all()

    return render(request, "med_management/connections.html", {
        "type" : "pt",
        "dr" : dr,
        "ph" : ph
    })

def dr_connections(request):
    pt = request.user.pt_connections.all()

    return render(request, "med_management/connections.html", {
        "type" : "dr",
        "pt" : pt
    })

def settings(request):
    if request.method == "POST":
        user = request.user
        data = json.loads(request.body)

        user.name = data["name"]

        user.patient = data["patient"]
        user.doctor = data["doctor"]
        user.pharmacy = data["pharmacy"]

        user.save()
        return HttpResponse(status=204)
    return render(request, "med_management/settings.html")

def index(request):
    if request.user.is_authenticated:
        requests = Med_Request.objects.filter(target=request.user, request_type="ready_for_pickup")
        dr_requests = Med_Request.objects.filter(receiver=request.user, request_type="want_refill")
        ph_requests = Med_Request.objects.filter(receiver=request.user, request_type="dr_ph_refill")
        
        return render(request, "med_management/index.html", {
            "requests" : requests,
            "dr_requests" : dr_requests,
            "ph_requests" : ph_requests
        })

    elif request.method == "POST":

        # Attempt to sign user in
        username = request.POST["username"]
        password = request.POST["password"]
        user = authenticate(request, username=username, password=password)

        # Check if authentication successful
        if user is not None:
            login(request, user)
            return HttpResponseRedirect(reverse("index"))
        else:
            return render(request, "med_management/login.html", {
                "message": "Invalid username and/or password."
            })
    else:
        return render(request, "med_management/login.html")


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
            return render(request, "med_management/register.html", {
                "message": "Passwords must match."
            })

        # Attempt to create new user
        try:
            user = User.objects.create_user(username, email, password)
            user.name = user.username
            user.save()
        except IntegrityError:
            return render(request, "med_management/register.html", {
                "message": "Username already taken."
            })
        login(request, user)
        return HttpResponseRedirect(reverse("settings"))
    else:
        return render(request, "med_management/register.html")
