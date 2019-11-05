import React, {Component} from 'react';
import {BrowserRouter as Router, Route, Switch} from 'react-router-dom';
import '../foundation/foundation.min.css';
import Login from '../auth/login/login.js';
import Search from '../tipsy/search/search.js';
import Register from '../auth/register/register.js';
import MyBars from '../tipsy/user/mybars/mybar.js';
import MyRecipes from '../tipsy/user/myrecipes/myrecipe.js';
import Admin from '../tipsy/admin/admin.js';
import Forgot from '../auth/forgot/forgot.js';
import Confirm from '../auth/confirm/confirm.js';
import Reset from '../auth/reset/reset.js';
import Recipe from '../tipsy/menu/recipe/recipe.js';
import Bar from '../tipsy/menu/bar/bar.js';
import User from '../tipsy/user/user.js';
import Game from '../tipsy/game/game.js';
import CreateBar from '../tipsy/user/createbar/createbar.js'
import CreateRecipe from '../tipsy/user/createrecipe/createrecipe.js';
import BarGears from '../tipsy/user/bargears/bargears.js';
import Gear from '../tipsy/menu/gear/gear.js';

import history from './history.js';
import {getCurrentUser} from '../util/APIUtils.js';
import * as constant from '../js/constants.js';
import {notification} from 'antd';

class Main extends Component {
    constructor(props) {
        super(props);
        this.state = {
            currentUser: null,
            isAuthenticated: false,
            isLoading: false
        }
        this.handleLogout = this
            .handleLogout
            .bind(this);
        this.loadCurrentUser = this
            .loadCurrentUser
            .bind(this);
        this.handleLogin = this
            .handleLogin
            .bind(this);
    }

    componentDidMount() {
        this.loadCurrentUser();
    }

    loadCurrentUser() {
        this.setState({isLoading: true});
        getCurrentUser().then(response => {
            this.setState({currentUser: response, isAuthenticated: true, isLoading: false});
        }).catch(error => {
            this.setState({isLoading: false});
        });
    }

    handleLogout(redirectTo = "/", notificationType = "success", description = "You're successfully logged out.") {
        localStorage.removeItem(constant.ACCESS_TOKEN);

        this.setState({currentUser: null, isAuthenticated: false});

        this
            .props
            .history
            .push(redirectTo);

        notification[notificationType]({message: 'Polling App', description: description});
    }

    handleLogin() {
        notification.success({message: 'Polling App', description: "You're successfully logged in."});
        this.loadCurrentUser();
        //We must redirect login
        console.log(history);
        history.push("/tipsy/search");
        window.location.reload();
    }

    render() {
        return (
            <Router>
                <Switch>
                    <Route
                        path="/" exact
                        render={(props) => <Login onLogin={this.handleLogin} {...props}/>}/>
                    <Route
                        path="/login"
                        render={(props) => <Login onLogin={this.handleLogin} {...props}/>}/>
                    <Route path="/forgot" component={Forgot}/>
                    <Route path="/confirm" component={Confirm}/>
                    <Route path="/reset" component={Reset}/>
                    <Route path="/register" component={Register}/>
                    <Route path="/tipsy/search" component={Search}/>
                    <Route path="/tipsy/myBars" exact component={MyBars} className="tab"/>
                    <Route path="/tipsy/myBars/bar" component={Bar}/>
                    <Route path="/tipsy/myRecipes" exact component={MyRecipes} className="tab"/>
                    <Route path="/tipsy/myRecipes/recipe" component={Recipe}/>
                    <Route path="/tipsy/barGears" exact component={BarGears} className="tab"/>
                    <Route path="/tipsy/barGears/gear" component={Gear}/>
                    <Route path="/tipsy/admin" exact component={Admin} className="tab"/>
                    <Route path="/tipsy/admin/user" component={User}/>
                    <Route path="/tipsy/game" component={Game}/>
                    <Route path="/tipsy/createbar" component={CreateBar}/>
                    <Route path="/tipsy/createRecipe" component={CreateRecipe}/>
                </Switch>
            </Router>
        )
    }
}

export default Main;