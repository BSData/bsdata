
## BSData Project ##

#### Contents ####

* [Overview][]
* [Technical][]
  * [Configuring the project in Netbeans][]
* [Links][]


[Overview]: #overview
[Technical]: #technical
[Configuring the project in Netbeans]: #configuring-the-project-in-netbeans
[Links]: #links


## Overview ##


## Technical ##

BattleScribeData is a Java 8 web application designed to run on Google App Engine. It is built and deployed using Maven 3.5.

This guide is written from the perspective of a Netbeans IDE user running Windows, however you can use any IDE or toolset that supports Maven.

It is assumed that you have some experience with the Java programming language (or are willing to learn it!).


#### Before You Start ####

1. Make sure you have a GitHub account and are a member of the BSData organisation (https://github.com/BSData).
2. Make sure you have a Google account.
3. On Windows, note that references to "Google Cloud Shell" below means the "Google Cloud Shell" command line launched from the Start Menu.


#### Download and Install Everything ####

1. Download and install the **Java SE 8 JDK** (http://www.oracle.com/technetwork/java/javase/downloads/index.html)
    * You may be able to use the Java 9 SDK - YMMV.
2. Download and install the GitHub desktop app (https://desktop.github.com/).
    * Check out the bsdata project. (https://github.com/BSData/bsdata).
    * The root folder you check out into (containing `pom.xml`) will be referred to as the project folder.
3. Download and install the Google Cloud SDK (https://cloud.google.com/sdk).
    * Once installed, tick the boxes to `Start Cloud SDK Shell` and `Run gcloud init` (https://cloud.google.com/sdk/docs/).
4. Set up Cloud SDK (https://cloud.google.com/sdk/docs/quickstarts).
    * You will be presented with a command line after installation, or you can run `gcloud init` from the Google Cloud Shell.
    * Log in with your Google account.
    * Choose to create a new project (or select a previously created project). This will create be own personal App Engine environment that you can deploy to and test on. Make a note of the name!
    * You do not need to configure Compute Engine, it can be skipped.
    * You can view and manage your App Engine environment in the Google Cloud Console (https://console.cloud.google.com).
5. Install the Google Cloud Java Components (https://cloud.google.com/sdk/docs/managing-components).
    * From the Google Cloud Shell, run `gcloud components install app-engine-java`
6. Download and install Netbeans Java EE bundle (https://netbeans.org/downloads/).
    * The current version of Netbeans is 8.2, and by default it **does use include Maven 3.5**. Future versions of Netbeans may change this.
7. Download and unzip Maven 3.5 (https://maven.apache.org/download.cgi).
    * On Windows, you can extract it to a folder in Program Files.


#### Set up the Project in Netbeans ####

1. Launch Netbeans
2. Set Netbeans to use Maven 3.5
    * Go to Tools -> Options, Select the Java section then the Maven tab.
    * Set Maven Home to the directory you unzipped Maven 3.5 into.
3. Open the BattleScribeData project you checked out from GitHub.
4. Create a file in the project directory called `maven.properties`
    * This file **should not be checked in to GitHub** (it's excluded via .gitignore). It contains Maven settings specific to individual developers.
    * Add the following line: 
        ```
        personal.appengine.project.name=YOUR_PERSONAL_APP_ENGINE_PROJECT_NAME
        ```
        (use your personal App Engine project name created when setting up the Cloud SDK above).
5. Create a file in the `<project directory>/src/main/resources/java/common/` directory called `github-user.properties`.
    * This file **should not be checked in to GitHub** (it's excluded via .gitignore). It contains GitHub authentication settings that should not be public.
    * **No, REALLY don't check this in** - if the GitHub authentication token is checked in to GitHub, it will be invalidated. This means the app will no longer have access to GitHub and will no longer work. The token will need to be regenerated!
    * Add the following lines
        ```
        ## GitHub User ##
        github.anon.username=GITHUB_USER_NAME
        github.anon.token=GITHUB_AUTHENTICATION_TOKEN
        github.anon.email=BSDataAnon@users.noreply.github.com
        ```

## Links ##

* [BattleScribe homepage][]
* [BattleScribe Data on Appspot][]
* [Getting Started wiki][]


[BattleScribe homepage]: http://www.battlescribe.net/
[BattleScribe Data on Appspot]: http://battlescribedata.appspot.com/#/repos
[Getting Started wiki]: https://github.com/BSData/bsdata/wiki/Home#getting-started
