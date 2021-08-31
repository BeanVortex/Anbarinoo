import './App.css';
import React from 'react';
import {lazy, Suspense} from "react";
import {BrowserRouter, Router, Switch} from "react-router-dom";


const App = () => {
    const About = lazy(()=> import("./About"));
    const Home = lazy(()=> import("./Home"));

    return (
        <Suspense render={<h1>wait</h1>}>
            <BrowserRouter>
                <Switch>
                    <Router component={Home} to="/" />
                    <Router component={About} to="/About" />

                </Switch>
            </BrowserRouter>
        </Suspense>
    );
}

export default App;
