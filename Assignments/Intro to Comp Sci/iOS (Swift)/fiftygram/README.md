# tonydjones

More Filters
We’ve added a few different filters together, but now try experimenting with your own! Add at least one new filter of your choosing to the app. Be creative!

Saving Photos
Our app can apply filters to photos, but it would be nice if we could save those photos so we could post them elsewhere!

First, some bookkeeping. iOS has a pretty strict permissions model, so your app will need to request permission to store a photo to the user’s device. To do so, open up the file called Info.plist. plist is short for “property list”, and you’ll see that this file is just that—a list of key/value pairs containing some information about your app. Click on the plus sign on the first row (next to Information Property List) to create a new row. In the key column for the new row, select Privacy - Photo Library Additions Usage Description, which should auto-complete for you. For value, use something like Fiftygram allows you to save photos to your library.. This tells iOS that your app needs Photo Library access, and it will prompt the user accordingly.

Next, add a new button to the storyboard that will save the photo when pressed. Then, wire it up to an @IBAction in your ViewController class. Finally, inside of that method, implement saving the photo. The UIImageWriteToSavedPhotosAlbum function will come in handy!
