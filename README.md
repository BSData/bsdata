
## BSData Project ##

BattleScribeDataWeb is the web application that serves BattleScribe data files from the various data repositories on GitHub. It is a Java 8 web application designed to run on Google App Engine. It is built and deployed using Maven 3.5.

It consists of an Angular (JavaScript) front end and a RESTful (Jersey / JAX-RS) back end.

This guide is written from the perspective of a Netbeans IDE user running Windows, however you can use any IDE or toolset that supports Maven.

It is assumes that you have some experience with developing on Windows, or can translate the following to your operating system of choice. You should also have some knowledge of software development/programming using Java (or are willing to learn it!).


### Before You Start ###

1. Make sure you have a Google account.
2. Make sure you have a GitHub account and are a member of the BSData organisation (https://github.com/BSData).
3. Generate a GitHub security token to let the app access GitHub on your behalf.
    * Log in to GitHub and go to Settings -> Developer Settings -> Personal access tokens (https://github.com/settings/tokens).
    * Generate a new token with `public_repo` and `read:org` scopes. Take a note of the token.


### Download and Install Everything ###
(Note: On Windows, references to "Google Cloud Shell" below means the "Google Cloud Shell" command line launched from the Start Menu.)

1. Download and install the **Java SE 8 JDK** (http://www.oracle.com/technetwork/java/javase/downloads/index.html)
    * You may be able to use the Java 9 SDK - YMMV.
2. Download and install the **GitHub Desktop** app (https://desktop.github.com/).
    * Check out the bsdata project. (https://github.com/BSData/bsdata).
    * The root folder you check out into (containing `pom.xml`) will be referred to as the project folder.
3. Download and install the **Google Cloud SDK** (https://cloud.google.com/sdk).
    * Once installed, tick the boxes to `Start Cloud SDK Shell` and `Run gcloud init` (https://cloud.google.com/sdk/docs/).
4. **Initialise the Google Cloud SDK** (https://cloud.google.com/sdk/docs/quickstarts).
    * You will be presented with a command line after installation, or you can run `gcloud init` from the Google Cloud Shell.
    * Log in with your Google account.
    * Choose to create a new project (or select a previously created project). This will will be your own App Engine development environment that you can deploy and test on. Make a note of the name.
    * You do not need to configure Compute Engine, it can be skipped.
    * Make sure your App Engine project is alive and well in Google Cloud Console (https://console.cloud.google.com).
5. Install the **Google Cloud Java Components** (https://cloud.google.com/sdk/docs/managing-components).
    * From the Google Cloud Shell, run `gcloud components install app-engine-java`
6. Download and install **Netbeans Java EE** bundle (https://netbeans.org/downloads/).
    * The current version of Netbeans is 8.2, and by default it does not include Maven 3.5. Future versions of Netbeans may change this.
7. Download and unzip **Maven 3.5** (https://maven.apache.org/download.cgi).
    * On Windows, you can extract it to a folder in Program Files.
    * Make sure you end up with a Maven folder that contains `bin` and `boot`folders.
    * Your `.../Maven/bin` folder needs to be on your PATH environment variable (https://superuser.com/questions/949560/how-do-i-set-system-environment-variables-in-windows-10).

### Set up the Project in Netbeans ###

1. Launch Netbeans
    * Pro tip: If you prefer a dark colour scheme, go to Tools -> Plugins and install "Darcula LAF for Netbeans"
2. Set Netbeans to use Maven 3.5
    * Go to Tools -> Options, Select the Java section then the Maven tab.
    * Set Maven Home to the directory you unzipped Maven 3.5 into.
3. Open the BattleScribeData project you checked out from GitHub.
4. Create a file in the project directory called **`maven.properties`**
    * **This file should not be checked in to GitHub** (it's excluded via .gitignore). It contains Maven settings specific to individual developers.
    * Add the following line: 
        ```
        appengine.dev.project.name=YOUR_APP_ENGINE_DEV_PROJECT_NAME
        ```
        (Use your App Engine development project name created when setting up the Cloud SDK above).
5. Create a file in the `<project directory>/src/main/resources/common/java/` directory called **`github-user.properties`**.
    * **This file should not be checked in to GitHub** (it's excluded via .gitignore). It contains GitHub authentication settings that should not be public. If your GitHub authentication token is checked in to GitHub, it will be invalidated and you will need to generate a new one.
    * Add the following lines:
        ```
        ## GitHub User ##
        github.anon.username=GITHUB_USER_NAME
        github.anon.token=GITHUB_AUTHENTICATION_TOKEN
        github.anon.email=BSDataAnon@users.noreply.github.com
        ```
        (Use your own GitHub username and token).
6. Build the project
    * Select the `local` Maven profile from the "Project Configuration" dropdown at the top.
    * Right-click the project and select "Clean and Build".
    * Wait for Maven to download required dependencies (jar libraries) and build the project.
7. Run the project locally (`appengine:run` Maven goal).
    * Select the `local` Maven profile.
    * Right-click the project -> Run Maven -> appengine:run.
    * Go to http://localhost:8080 to see the app.
    * (The local server is provided as part of the Cloud SDK)
8. Deploy the project to your App Engine development environment (`appengine:deploy` Maven goal).
    * Select the `dev` Maven profile.
    * Right-click the project -> Run Maven -> appengine:deploy.
    * Go to http://YOUR_APP_ENGINE_DEV_PROJECT_NAME.appspot.com to see the app.
    * Take a look at the Google Cloud Console (https://console.cloud.google.com)


### A Quick Tour ###

* `pom.xml` in the project directory contains Maven configuration.
    * General app properties, such as name, version, Java version etc.
    * **Build profiles** which determine app configuration for specific environments.
        * `local` for running on the local server
        * `dev` for deploying on your App Engine development environment
        * `test` and `prod` are for the main BSData test and live App Engine environments. You will not be able to use these unless authorised.
    * Dependencies - the specific versions of libraries required by the app.
        * Maven will handle downloading and providing the libraries when building and deploying the app.
* `/src/main/resources/` folder contains configuration files for each Maven profile, plus common properties files used by all profiles.
    * `.../java/` files are general config used by the java app and are copied into the `WEB-INF/classes/` folder upon build/deploy.
    * `.../webapp/` files are used to configure the application server and are copied into the `WEB-INF` folder upon build/deploy.
* `/src/main/webapp/` folder contains the web front end
    * HTML and CSS
    * `.../app/` folder contains the Angular Javascript app
* `/src/main/java/` folder contains the back end Java app.
    * `rest` package contains RESTful web services (https://jersey.github.io/documentation/2.26/jaxrs-resources.html).
        * `BattleScribeDataRestConfig.java` configures the app and performs startup tasks.
    * `viewmodel` package contains model classes that are used to pass data between the web services and the Angular front end.
        * These objects are converted to/from JSON to be sent/recieved in web requests/responses.
    * `model` package contains model classes for BattleScribe XML data files and indexes (http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php).
    * `dao` package contains "Data Access Object" classes used used to read/write to data sources.
        * `GitHubDao.java` is used for communicating with GitHub.
    * `repository` package contains classes for creating BattleScribe repository indexes (`.bsi`) files.
