# Mevo

A simple Android app to show vehicles and parking area on the map using Mevo public API endpoint (https://developer.mevo.co.nz/docs/public/introduction).

Configure Mapbox secret token by adding below to gradle.properties file that could be found in Gradle user home folder (https://docs.gradle.org/current/userguide/directory_layout.html#dir:gradle_user_home for details):

          "MAPBOX_DOWNLOADS_TOKEN=YOUR_DOWNLOAD_TOKEN"
          
Android Studio - Build - Make project. Will need user location permission granted. 

Screenshots:
![Screenshot_20220306-204138](https://user-images.githubusercontent.com/90913093/156914039-393419f2-dc0e-4408-9314-dc8fe68e481d.png)
(user current location)
![Screenshot_20220306-204211](https://user-images.githubusercontent.com/90913093/156914034-787b0fef-f83e-49b2-a6d4-d2c38d0a9c03.png)
(show parking)
![Screenshot_20220306-204200](https://user-images.githubusercontent.com/90913093/156914038-68180a09-6457-4544-b823-56504b013df6.png)
(show Vehicles)
![Screenshot_20220306-204223](https://user-images.githubusercontent.com/90913093/156914028-9a0217c4-b402-449f-ba0b-c045c54adb40.png)
(show vehicles and parkings)
