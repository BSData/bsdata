import React, {useState, useEffect} from 'react';
import logo from './logo.svg';
import axios from 'axios'
import './App.css';
import './main.css'

function App() {
  const repos = require('./dummy.json')
  const [state, setState] = useState({repos:[]});
  // useEffect(() => {
  //    axios.get('http://battlescribedata.appspot.com/repos',  { crossDomain: true })
  //     .then(res => {
  //       const repos = JSON.stringify(res.data)
  //       this.setState({ repos:repos });
  //     }).catch(err => {
  //       console.log( err )
  //     })
  // }, []);

  let ribbon = require("./img/ribbons/right-cerulean.png" )
  let logo = require("./img/bsdata_logo.png")
  console.log('repos',repos)
  return (
    <div className="App">
    <div id="wrapper">
        <div id="header">
            <div class="center">
                <a href="/#/repos">
                    <img class="logo" src={logo} alt="BattleScribe data file hosting platform" />
                </a>

                <div class="pageTitle">
                    <ul>
                        <li>
                            <div class="outerContainer">
                                <div class="innerContainer">
                                    <div class="element">
                                        <h1>BattleScribe Data Files</h1>
                                        <h2>Crowd-sourced data repositories for BattleScribe</h2>
                                    </div>
                                </div>
                            </div>
                        </li>
                    </ul>
                </div>
            </div>

            <a href="https://github.com/BSData">

                <img class="ribbon" src={ribbon}
                     onmouseover="this.src = 'app/img/ribbons/right-red.png';"
                     onmouseout="this.src = 'app/img/ribbons/right-cerulean.png';"
                     alt="Fork me on GitHub" />
            </a>
        </div>

        <div id="body">
            <div class="center">
                <div class="bodyText">
                    <ul>
                        <li>
                            <span class="bold">Found a bug or problem?</span> Go to a repository's details to report
                            a bug or submit a file update.
                        </li>
                        <li>
                            <span class="bold">Stay up to date</span> with the latest data file releases by following
                            us on Facebook, Twitter or subscribing to our feeds.
                        </li>
                        <li>
                            We'd love you to <span class="bold">host your data with us!</span> If you have data files
                            for a game system we don't have listed, <a href="https://github.com/BSData/catalogue-development/wiki/Getting-Started">contact us</a>.
                        </li>
                        <li>
                            All our data files (and this website) are <span class="bold">freely available on
                            <a href="https://github.com/BSData">GitHub</a></span> - you're welcome to fork us!
                        </li>
                    </ul>

                    <p class="olddata">
                        <span class="bold">Looking for data for BattleScribe 1.15.x?</span>
                        <br />
                        The data on this site is being migrated to BattleScribe 2.02 format. This format is not compatible with BattleScribe 1.15.x or below.
                        Some data authors have archived their old 1.15.x data on their <a href="https://github.com/BSData">GitHub page</a>.
                    </p>
                </div>

                <div id="container">
                {repos.repositories.map(repo=> {
                  return (
                    <li  class="detailsRow">
                        <a href={repo.indexUrl} class="detailsTitle"><img src={require("./img/blank.png")} />{repo.description}</a>
                        <div class="details">
                            {repo.lastUpdated ? <React.Fragment><span>Updated {repo.lastUpdated}</span><span class="detailsPadding"> | </span></React.Fragment> : null}
                            <a href={`#/repo/${repo.name}`}>Details/Report Bug</a><span class="detailsPadding"> | </span>
                            <a href={repo.githubUrl}><img src={require("./img/github_icon_16.png")}/></a>
                            <a style={{marginLeft: 4}} href={repo.feedUrl} type="application/atom+xml"><img src={require("./img/rss_icon_16.png")}/></a>
                        </div>
                    </li>
                  )
                })}
                </div>
            </div>
        </div>

    </div>
    </div>
  );
}

export default App;
