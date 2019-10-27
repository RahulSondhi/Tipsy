import React, {Component} from 'react';
import {
    BrowserRouter as Router,
    Route,
    Switch
  } from 'react-router-dom';
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

import { getCurrentUser } from '../util/APIUtils.js';
import { ACCESS_TOKEN } from '../constants/constants.js';
import { notification } from 'antd';

class Main extends Component {
    constructor(props) {
        super(props);
        this.state = {
          currentUser: null,
          isAuthenticated: false,
          isLoading: false
        }
        this.handleLogout = this.handleLogout.bind(this);
        this.loadCurrentUser = this.loadCurrentUser.bind(this);
        this.handleLogin = this.handleLogin.bind(this);

    }

    componentDidMount() {
        this.loadCurrentUser();
    }
    
    loadCurrentUser() {
        this.setState({
          isLoading: true
        });
        getCurrentUser()
        .then(response => {
          this.setState({
            currentUser: response,
            isAuthenticated: true,
            isLoading: false
          });
        }).catch(error => {
          this.setState({
            isLoading: false
          });  
        });
      }

    handleLogout(redirectTo="/", notificationType="success", description="You're successfully logged out.") {
        localStorage.removeItem(ACCESS_TOKEN);
    
        this.setState({
          currentUser: null,
          isAuthenticated: false
        });
    
        this.props.history.push(redirectTo);
        
        notification[notificationType]({
          message: 'Polling App',
          description: description,
        });
      }
    
      handleLogin() {
        notification.success({
          message: 'Polling App',
          description: "You're successfully logged in.",
        });
        this.loadCurrentUser();
        //We must redirect login
        this.props.history.push("/");
      }
    

    render() {
        return (
            <Router>
                <Switch>
                    <Route path="/" exact component={Login}/>
                    <Route path="/login" component={Login}/>
                    <Route path="/login" render={ (props) => <Login onLogin={this.handleLogin} {...props} /> }/>
                    <Route path="/forgot" component={Forgot}/>
                    <Route path="/confirm" component={Confirm}/>
                    <Route path="/reset" component={Reset}/>
                    <Route path="/tipsy/search" component={Search}/>
                    <Route path="/register" component={Register}/>
                    <Route path="/tipsy/myBars" component={MyBars} className="tab"/>
                    <Route path="/tipsy/myRecipes" component={MyRecipes} className="tab"/>
                    <Route path="/tipsy/admin" component={Admin} className="tab"/>
                </Switch>
            </Router>
        )
    }
}

export default Main;