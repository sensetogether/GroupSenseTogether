# BeTogether
Key Features 
---------------------
#BeTogether is an Android application, a prototype for research purpose to elaborate in-network collaborative crowdsensing.
It supports user context inference, context-aware collaboration, and sensing and logging, by leveraging embedded sensors on smartphones.

The context inference function distinguishes user activity, the physical environment, and device attributes.
It is designed to be personalized on each device, and therefore it requires feedback from the user to update the internal machine learning models.
The user can click on the feedback button once the inference result is incorrect.
It also infers the duration of user activity and the physical environment.

The sensing and logging function measures the environmental conditions and transmits observation to the cloud.
The sensing data can also be stored on the device as a local file or be sent via email.
Sensing data includes environmental measurement, location, and timestamp.

The service discovery function provides context match, intent value calculation, and Wi-Fi Direct service discovery, to exchange the context/intent information, negotiate the group owner, and allocate tasks among D2D neighbors.

Getting started 
------------------------
Step 1 - Download or clone the source code of #BeTogether.  
 
Step 2 - Download [Android Studio](https://developer.android.com/studio/)  
Start Android Studio and open the #BeTogether project by selecting the directory wherein is placed #BeTogether. 
All dependencies are located in the file "build.gradle".

Step 3 - Download the following java library:
* [Weka-Android](https://github.com/Yifan-DU/Weka-Android/blob/master/dist/weka-stable-3.8.1-SNAPSHOT.jar) (version 
3.8.1 is preferred) 

Place the jar file that just has been downloaded into the "app/libs" folder of the #BeTogether app directory, through Windows Explorer or Mac Finder.
Then open "Project" files view in android on the left side of Android Studio, find the "libs" folder, right-click the "weka-stable-3.8.1-SNAPSHOT.jar" file, and select "Add as Library".

Running on the mobile phone: 
------------------------------------------------
Note that the application starts with a permission checker and you need to grant these permission requests.

The application may also request to turn on some components in runtime, such as GPS, please follow the notification to turn on them.

License 
-------------
#BeTogether is available under the terms of the GPL License, which implies that application developers are free to use #BeTogether. 
It also means that developers are invited to contribute to improving #BeTogether as long as the original source code remains open.

Contributors
---------------------

* Yifan Du, designer & developer (90%)
* Françoise Sailhan, reviewer (5%)
* Valérie Issarny, reviewer (5%) 
