##BSData Project##

####Contents####

* [Overview][]
* [Technical][]
  * [Configuring the project in Netbeans][]
* [Links][]


[Overview]: #overview
[Technical]: #technical
[Configuring the project in Netbeans]: #configuring-the-project-in-netbeans
[Links]: #links


##Overview##

__What's this?__

BSData organisation created this project. It's open source web app hiding GitHub repository of datafiles under nice GUI hood. Maintained by community, in no way endorsed by BattleScribe. If you want to develop the page, take a look at issues. We will happily collaborate with you. To start, [configure the project in Netbeans][Technical]

__I'd rather develop/maintain datafiles.__

Cool! We need you! Take a look at [Getting Started wiki][]

__Okay, nice project. Is it actually working?__ _I just want those files..._

Yeah! We have it hosted on AppSpot. Take a look: [BattleScribe Data on Appspot][]


##Technical##

####Configuring the project in Netbeans####

- Download and install Netbeans 7.3.x (Java EE version).
    * link: https://netbeans.org/downloads/7.3.1/
- Download and extract the App Engine Java SDK.
    * link: https://developers.google.com/appengine/downloads#Google_App_Engine_SDK_for_Java
- Download and extract the App Engine plugins for 7.3.
    * link: https://code.google.com/p/nb-gaelyk-plugin/downloads/list.
- In Netbeans, go to Tools -> Plugins -> Downloaded tab -> Add Plugins.
- Select all the .nbm files from the unzipped plugins folder and install them.
- Tools -> Servers -> Add Server. Choose Google App Engine, then browse to the folder you extracted the SDK to.
- In Netbeans, create a new project (New Project -> Java Web -> Web Application). Name the project and choose where to create it. Check "Dedicated folder for libraries". Select App Engine as the server. No need to check any frameworks.
- Check out the project from GitHub into the newly created project folder.
- Once your project is created, you need to reference the required library jars. Right click the Libraries node -> Add Jar/Folder -> browse to the lib folder under the project folder -> Select all the .jar files -> Open
- Right-click the root project node -> Run!



##Links##

* [BattleScribe homepage][]
* [BattleScribe Data on Appspot][]
* [Getting Started wiki][]


[BattleScribe homepage]: http://www.battlescribe.net/
[BattleScribe Data on Appspot]: http://battlescribedata.appspot.com/#/repos
[Getting Started wiki]: https://github.com/BSData/bsdata/wiki/Home-page
