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
import './Signup.css';
import { Link, Navigate } from 'react-router-dom';
import { signup } from '../../util/APIUtils';
import Alert from 'react-s-alert';

import {ASSINA_RSSP_BASE_URL, ACCESS_TOKEN} from '../../constants';
import axios from 'axios';

class Signup extends Component {
    render() {
        if(this.props.authenticated) {
            return <Navigate
                to={{
                pathname: "/",
                state: { from: this.props.location }
            }}/>;
        }

        return (
            <div className="signup-container">
                <div className="signup-content">
                    <h1 className="signup-title">Signup</h1>
                    <SocialSignup />
                    <div className="or-separator">
                        <span className="or-text">OR</span>
                    </div>
                    <SignupForm {...this.props} />
                    <span className="login-link">Already have an account? <Link to="/login">Login!</Link></span>
                </div>
            </div>
        );
    }
}


class SocialSignup extends Component {
    constructor(props) {
        super(props);
        this.state = {answerFromOID4VP: false, answerFromForm: false};
        this.handleClick = this.handleClick.bind(this);
    }


    handleClick() {
        this.setState({
            answerFromOID4VP: true
        })

        axios.get( ASSINA_RSSP_BASE_URL+'/oauth2/link', {}, {})
        .then(responseRedirectLink => {
            const deeplink = responseRedirectLink.data.link;
            const nonce = responseRedirectLink.data.nonce;
            const presentationId = responseRedirectLink.data.presentationId;
            console.log(deeplink);

            const data = new FormData();
            data.append('nonce', nonce);
            data.append('presentationId', presentationId);
        

            axios.post( ASSINA_RSSP_BASE_URL+'/oauth2/token', data, {})
                .then( responseAutheticationToken => {
                    console.log(responseAutheticationToken);
                    localStorage.setItem(ACCESS_TOKEN, responseAutheticationToken.data.accessToken);
                    this.setState({
                        answerFromOID4VP: false
                    })
                    Alert.success("You're successfully logged in!");
                    window.location.href='/profile';
            }).catch(error =>{
                Alert.error((error && error.message) || 'Oops! Something went wrong. Please try again!');
                this.setState({
                    answerFromOID4VP: false
                })
            } );

            const goToApp = window.confirm("Redirect to EUDI Wallet?");
            if (goToApp) {
                window.location.href = deeplink;
            } else {
                console.log("Usuário cancelou a ação.");
            }
        })
        .catch(error => console.log(error));
    }
    
    render() {
        return (
            <div className="social-signup" onClick={this.handleClick} disabled={this.state.answerFromOID4VP}>
                <a className="btn btn-block social-btn google">Sign up with OID4VP</a>
            </div>
        );
    }
}

class SignupForm extends Component {
    constructor(props) {
        super(props);
        this.state = {
            name: '',
            username: '',
            email: '',
            password: '',
            pin: ''
        }
        this.handleInputChange = this.handleInputChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleInputChange(event) {
        const target = event.target;
        const inputName = target.name;
        const inputValue = target.value;

        this.setState({
            [inputName] : inputValue
        });
    }

    handleSubmit(event) {
        event.preventDefault();

        if(this.state.pin.length !== 4) {
            return(
                <div>{Alert.warning("Please enter the pin in the correct format (4 digits)")}</div>
            )
        }

        const signUpRequest = Object.assign({}, this.state);

        signup(signUpRequest)
        .then(response => {
            Alert.success("You're successfully registered. Please login to continue!");
            this.props.history.push("/login");
        }).catch(error => {
            Alert.error((error && error.message) || 'Oops! Something went wrong. Please try again!');
        });
    }

    render() {
        return (
            <form onSubmit={this.handleSubmit}>
                <div className="form-item">
                    <input type="text" name="name"
                        className="form-control" placeholder="Full Name"
                        value={this.state.name} onChange={this.handleInputChange} required/>
                </div>
                <div className="form-item">
                    <input type="text" name="username"
                           className="form-control" placeholder="Username"
                           value={this.state.username} onChange={this.handleInputChange} required/>
                </div>
                <div className="form-item">
                    <input type="email" name="email"
                        className="form-control" placeholder="Email"
                        value={this.state.email} onChange={this.handleInputChange} required/>
                </div>
                <div className="form-item">
                    <input type="password" name="password"
                        className="form-control" placeholder="Password"
                        value={this.state.password} onChange={this.handleInputChange} required/>
                </div>
                <div className="form-item">
                    <input type="password" pattern="[0-9]*" name="pin" className="form-control" placeholder="Pin"
                            value={this.state.pin} onChange={this.handleInputChange} required/>
                </div>
                <div className="form-item">
                    <button type="submit" className="btn btn-block btn-primary" >Sign Up</button>
                </div>
            </form>

        );
    }
}

export default Signup
