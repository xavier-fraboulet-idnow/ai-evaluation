/*
 Copyright 2024 European Commission

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      https://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import React, { Component } from 'react';
import {
  Route,
  Switch } from 'react-router-dom';
import AppHeader from '../common/AppHeader';
import Home from '../home/Home';
import Login from '../user/login/Login';
import Sign from '../user/sign/Sign';
import ManageUsers from '../admin/manageUsers/ManageUsers';
import Download from '../user/download/Download';
import Profile from '../user/profile/Profile';
import Edit from '../user/edit/Edit';
import Logs from '../user/logs/Logs';
import Certificates from '../user/certificates/Certificates';
import OAuth2RedirectHandler from '../user/oauth2/OAuth2RedirectHandler';
import NotFound from '../common/NotFound';
import LoadingIndicator from '../common/LoadingIndicator';
import { getCurrentUser, logout } from '../util/APIUtils';
import { ACCESS_TOKEN } from '../constants';
import PersonalizedRoute from '../common/PersonalizedRoute';
import './App.css';

import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";


class App extends Component {
  constructor(props) {
    super(props);
    this.state = {
      authenticated: false,
      currentUser: null,
      loading: false
    }

    this.loadCurrentlyLoggedInUser = this.loadCurrentlyLoggedInUser.bind(this);
    this.handleLogout = this.handleLogout.bind(this);
  }

  loadCurrentlyLoggedInUser() {
    this.setState({
      loading: true
    });

    getCurrentUser()
    .then(response => {
      this.setState({
        currentUser: response,
        authenticated: true,
        loading: false
      });
    }).catch(error => {
      this.setState({
        loading: false
      });  
    });    
  }

  handleLogout() {
    logout()
    localStorage.removeItem(ACCESS_TOKEN);
    this.setState({
      authenticated: false,
      currentUser: null
    });
    toast.success("You're safely logged out!");
    window.location.href = '/';
  }

  componentDidMount() {
    this.loadCurrentlyLoggedInUser();
  }

  render() {
    if(this.state.loading) {
      return <LoadingIndicator />
    }

    /**<PersonalizedRoute path="/profile" authenticated={this.state.authenticated} currentUser={this.state.currentUser} component={Profile}></PersonalizedRoute>
           */



    return (
      <div>
        <AppHeader authenticated={this.state.authenticated} onLogout={this.handleLogout} />
        <Switch>
          <Route exact path="/" component={Home}></Route>
          <Route path="/login" render={(props) => <Login {...props} />}></Route>

          <PersonalizedRoute path="/profile" authenticated={this.state.authenticated} currentUser={this.state.currentUser} component={Profile}></PersonalizedRoute>
          <PersonalizedRoute path="/sign" authenticated={this.state.authenticated} currentUser={this.state.currentUser} component={Sign}></PersonalizedRoute>
          <PersonalizedRoute path="/userManagement" authenticated={this.state.authenticated} currentUser={this.state.currentUser} component={ManageUsers}></PersonalizedRoute>
          <PersonalizedRoute path="/download" authenticated={this.state.authenticated} currentUser={this.state.currentUser} component={Download}></PersonalizedRoute>
          <PersonalizedRoute path="/edit" authenticated={this.state.authenticated} currentUser={this.state.currentUser} component={Edit}></PersonalizedRoute>             
          <PersonalizedRoute path="/logs" authenticated={this.state.authenticated} currentUser={this.state.currentUser} component={Logs}></PersonalizedRoute>
          <PersonalizedRoute path="/certificates" authenticated={this.state.authenticated} currentUser={this.state.currentUser} component={Certificates}></PersonalizedRoute> 
          <Route path="/oauth2/redirect" component={OAuth2RedirectHandler}></Route>  
          <Route component={NotFound}></Route>
        </Switch>
        
        {/*<Alert stack={{limit: 3}} 
          timeout = {3000}
    position='top-right' effect='slide' offset={65} />*/}
      </div>
    );
  }
}

export default App;
