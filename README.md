# Mevo

A simple Android app to show vehicles and parking area on the map using Mevo public API endpoint.

1. Configure Mapbox secret token by adding below to gradle.properties file that could be found in Gradle user home folder (https://docs.gradle.org/current/userguide/directory_layout.html#dir:gradle_user_home for details):

          "MAPBOX_DOWNLOADS_TOKEN=sk.eyJ1IjoiamFta3dvayIsImEiOiJja3ptYnBiMmY1NTU3MnJwcnAyNXhldWdiIn0.kazYZYgldjGL2qLMd1W-VA"

2. Add dependencies under module level build.gradle:

       //Retrofit and converter
          implementation 'com.squareup.retrofit2:retrofit:2.9.0'
          implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
          implementation 'com.squareup.okhttp3:okhttp:4.9.0'
          //Mapbox
          implementation ('com.mapbox.mapboxsdk:mapbox-android-sdk:9.7.0'){
              exclude group: 'group_name', module: 'module_name'
          }
