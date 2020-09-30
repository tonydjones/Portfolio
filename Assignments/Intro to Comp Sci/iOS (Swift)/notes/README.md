# tonydjones

Deleting Notes
So far, our Notes app can add and edit notes. Let’s add the ability for a user to delete a note when they no longer need it.

First, implement a new method called delete inside of the NotesManager class. This method should use a DELETE query on your SQLite database in order to delete a specific note.

Next, add a button to the notes storyboard that we’ll use for deleting notes. Exactly what the UI looks like is up to you! (If you’re feeling ambitious, you can try implementing a UI that allows a user to swipe on a note from the list to delete it, much like many email apps on iOS.)

Finally, wire up your UI to an @IBAction that calls your new delete method. Depending on your UI, you might find the popViewController(animated: Bool) method on navigationController useful—this method will pop you from your notes view back to your list view.
