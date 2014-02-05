bsdata
======

*Configuring the project in Netbeans*
- Download and install Netbeans 7.3.x (Java EE version) from here: https://netbeans.org/downloads/7.3.1/
- Download and extract the App Engine Java SDK from here: https://developers.google.com/appengine/downloads#Google_App_Engine_SDK_for_Java.
- Download and extract the App Engine plugins for 7.3 from here: https://code.google.com/p/nb-gaelyk-plugin/downloads/list.
- In Netbeans, go to Tools -> Plugins -> Downloaded tab -> Add Plugins.
- Select all the .nbm files from the unzipped plugins folder and install them.
- Tools -> Servers -> Add Server. Choose Google App Engine then browse to the folder you extracted the SDK to.
- In Netbeans, create a new project (New Project -> Java Web -> Web Application). Name the project and choose where to create it. Check "Dedicated folder for libraries". Select App Engine as the server. No need to check any frameworks.
- Check out the project from GitHub into the newly created project folder.
- Once your project is created, you need to reference the required library jars. Right click the Libraries node -> Add Jar/Folder -> browse to the lib folder under the project folder -> Select all the .jar files -> Open
- Right-click the root project node -> Run!
