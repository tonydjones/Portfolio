# tonydjones

Once the code and LÖVE have been downloaded and installed, the actual change you’ll be making to the code base is small, but it will require you to understand what many of the pieces do, so be sure to watch each of the track’s lessons and read through the code so you have a firm understanding of how it works before diving in! In particular, take note of how paddle movement works, reading both the Paddle class as well as the code in main.lua that actually drives the movement, located in the update function (currently done using keyboard input for each). If our agent’s goal is just to deflect the ball back toward the player, what needs to drive its movement?

Your goal:

Implement an AI-controlled paddle (either the left or the right will do) such that it will try to deflect the ball at all times. Since the paddle can move on only one axis (the Y axis), you will need to determine how to keep the paddle moving in relation to the ball. Currently, each paddle has its own chunk of code where input is detected by the keyboard; this feels like an excellent place to put the code we need! Once either the left or right paddle (or both, if desired) try to deflect the paddle on their own, you’ve done it!
