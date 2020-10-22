# tonydjones

This is an app I designed for kicks and giggles. I've always loved rhythm games, but I never liked how you're restricted to only using certain songs. I've always 
wanted to use my own. So I made this app. It allows you to pick a song from your files (as long as you have mp3 files in either your downloads or music folders)
and then you can choose how many columns you want (3-6). Then as the song plays, the user can "tap along" (wink wink) with the music.
The app records the taps as integers in a linked list, recording the button pressed, the time it was pressed, and time it was released.
The list is saved as a gson string, and saved in the app files. Then to play the tap, you can choose from a list of the taps you've made,
and the app will produce the rhythm game according to how the user originally tapped along. Well technically according to the gson string, 
which the app translates into a sequence of buttons and particular times they need to appear and move. You get points for every tap you hit in the 
right time, and as your combo builds you gain more points for each tap. Your combo is broken when you miss a tap or tap at the wrong time. 
At the end of the song, if you see your score, and if it's the new high score for that song, it is saved. There's also a calibration function 
to make the taps better line up with the rhythm of the songs as originally input. Lots of other fun details too, like making a distinction 
between short taps and long taps, and the screen flashing red or green for missed or hit prompts respectively.

https://www.youtube.com/watch?v=2-SIcQ_5GZI
