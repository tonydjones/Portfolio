# tonydjones

Download this project’s distribution code.

To open the distribution code, extract the ZIP, open Android Studio, select “Import project”, and select the folder you extracted from the ZIP.

What To Do
Deleting Notes
Deleting Notes
So far, our Notes app can add and edit notes. Let’s add the ability for a user to delete a note when they no longer need it.

First, add a new method called delete to the NoteDao interface. You’ll probably want this method to take an id of the note to delete, and use a DELETE query.

Next, add a button to your layout for deleting notes. Exactly what the UI looks like is up to you! (If you’re feeling ambitious, you can try implementing a UI that allows a user to swipe on a note from the list to delete it, much like many email apps on Android.)

Finally, wire up that UI to a method that calls your new delete method on the NoteDao. Depending on your UI, you might find the finish method helpful—this method will dismiss the current activity and go back to the previous one.

To test your app, try creating a few notes and then deleting them, to make sure the right things get deleted!
