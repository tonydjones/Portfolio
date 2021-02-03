# tonydjones

## Medication Management Website

The idea behind this website is giving the ability for patients, doctors, and pharmacies to easily communicate with each other and connect 
to each other in order to manage a patient's medications. A patient can easily tell their doctor they're in need of a refill. 
The doctor can check the medication history to see how many doses the patient has, and whether they've been taking it as prescribed. 
Pharmacies can let a patient know when a medication is ready for pickup easily. Et cetera. 

Check out the video demo: https://youtu.be/YS8NtGYLPvk

### Here's a quick overview of the various functions in the application:

-register as any combination of patient, doctor, and pharmacy, and to later adjust your role if necessary
-choose display name (Separate from username)

-as a patient:
	-choose which pharmacy you want to pick up medications from
	-request to connect to a doctor (or accept a doctor's connection request)
	-disconnect from doctors and pharmacies
	-search for doctors and pharmacies by name or username
	-confirm that they picked up a medication from a pharmacy (which will update the number of doses of a medication they have)
	-view their medications and details:
		-view the history of the medication: refills and when they used it
		-take medication (view instructions and choose how many doses you're taking)
		-See how many doses they have remaining (if applicable)
		-request a refill for a medication
			-choose pharmacy
			-choose doctor
			-specify number of doses
			-this request is sent to the doctor, who can choose to reject, confirm, or adjust the details of the request

-as a doctor:
	-request to connect to a patient (or accept a patient's connection request)
	-disconnect from patients
	-search for patients by name or username
	-view all current patients
	-view patient medications and details:
		-prescribe a new medication for the patient (immediately also sends request to a pharmacy)
		-view the history of the medication: refills and when they used it
		-See how many doses they have remaining (if applicable)
		-discontinue medication (client will not be able to request refills anymore)
		-adjust the prescription directions (when patient takes the medication they will see the new instructions)
		-request a refill for a medication
			-choose pharmacy
			-specify number of doses
			-this request is sent to the pharmacy, who can choose to reject or confirm the refill

-as a pharmacy:
	-confirm refill is ready for pickup
	-reject refill request

The application allows for "connections" but for the purpose of communication and keeping track of medications for ease of getting proper 
medical treatments, so I don't believe it counts as a "social network" which I think puts more emphasis on the connections and 
sharing "posts." It also is clearly distinct from e-commerce. The project utilizes Django for, well, everything. It can be run by using 
the command window, navigating to the project folder, and running the command "python manage.py runserver" The main files you would want 
to look at (if you had such a desire) would be "static" (which contains my main javascript file containing the vast majority of my JS, 
and a CSS file for styling a few website elements such as tables) and "templates" (which holds all of my html layouts). The main files in 
the main directory are views.py (for back-end processes), models.py (for defining objects), and, to a lesser extent, urls.py, to see how 
my front and back ends are communicating with each other.