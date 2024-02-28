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
import './Login.css';
import { ACCESS_TOKEN, OAUTH2_REDIRECT_URI } from '../../constants';
import LoadingIndicator from '../../common/LoadingIndicator';

import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

import {ASSINA_RSSP_BASE_URL} from '../../constants';
import axios from 'axios';
import QRCode from 'react-qr-code';
import logo from '../../img/logo.svg';

import 'bootstrap/dist/css/bootstrap.min.css';
import '../../site.css';
import './Login.css';


class Login extends Component {
    /*componentDidMount() {
        if(this.props.location.state && this.props.location.state.error) {
            setTimeout(() => {
                toast.error(this.props.location.state.error, {
                    position: toast.POSITION.TOP_RIGHT,
                });
                this.props.history.replace({
                    pathname: this.props.location.pathname,
                    state: {}
                });
            }, 100);
        }
    }*/

    render(){
        return(   
            <div className="container-fluid p-0" >
                <ToastContainer/>
                <div className="row w-100 h-100 m-0" >
                    <div className="col-md-7 d-flex justify-content-center align-items-center" id="formCol" style={{ height: '100vh', width: '100vw' }}>
                        <div className="login-form row">
                            <div className="col-md-12">
                                <div className="row">
                                    <div className="col-md-12 text-center">
                                        <img src={logo} className="img-fluid mt-2 mb-2" width="150" alt="logo of EUDIW"/>
                                    </div>
                                </div>
                                <SocialLogin/>
                            </div>
                        </div>
                    </div>
                    <div className="col-md-5 p-0 login-image">
                        <div className="h-100"></div>
                    </div>
                </div>
            </div>
        );
    }
}

class SocialLogin extends Component {

    constructor(props) {
        super(props);
        this.state = { answerFromOID4VP: false, visible: false, deeplink: "" };
        this.handleClick = this.handleClick.bind(this);
        this.redirectClick = this.redirectClick.bind(this);
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

            this.setState({
                deeplink: deeplink,
                visible: true
            })
        
            const data = new FormData();
            data.append('nonce', nonce);
            data.append('presentationId', presentationId);
            axios.post( ASSINA_RSSP_BASE_URL+'/oauth2/token', data, {})
                .then( responseAutheticationToken => {
                    localStorage.setItem(ACCESS_TOKEN, responseAutheticationToken.data.accessToken);
                    this.setState({
                        answerFromOID4VP: false,
                        visible: false,
                        deeplink: ''
                    })
                    toast.success("You're successfully logged in!");
                    window.location.href='/profile';
            }).catch(error =>{
                toast.error((error && error.message) || 'Oops! Something went wrong. Please try again!');
                this.setState({
                    answerFromOID4VP: false,
                    visible: false,
                    deeplink: ''
                })
            } );
        })
        .catch(error => {
            toast.error((error && error.message) || 'Oops! Something went wrong. Please try again!');
            this.setState({
                answerFromOID4VP: false,
                visible: false,
                deeplink: ''
            })

            console.log(error)
        });
    }

    redirectClick() {
        window.location.href = this.state.deeplink;
    }

    redirectClickRedirectUri(){
        const link =  this.state.deeplink + "&redirect_uri="+OAUTH2_REDIRECT_URI;
        window.location.href = link;
    }

    render() {
        return (
            <div className="row mt-3">
                <div className="col-md-12">
                    <button className="btn btn-outline-primary w-100 mt-3" style={{borderRadius: "1px"}}
                       onClick={this.handleClick} disabled={this.state.answerFromOID4VP}>
                            PID Authentication
                        <img src={logo} className="img-fluid mt-2 mb-2" width="80"  alt="logo of EUDIW"/>
                    </button>
                    <div className="oid4vp_container">
                        {
                            this.state.answerFromOID4VP && !this.state.visible &&
                            <LoadingIndicator/>
                        }

                        {
                          this.state.visible && 
                          <div className='qrcode_container'>
                              <QRCode title="EUDIWallet" value={this.state.deeplink} size={200}/>
                              <a className="btn_redirect" onClick={this.redirectClick}> Redirect to EUDI Wallet </a>
                              {/*<a className="btn_redirect" onClick={this.redirectClickRedirectUri}> Redirect to EUDI Wallet: Test with Redirect_uri </a>*/}
                          </div>
                        }
                    </div>
                </div>
            </div>
        );
    }
}

/*class LoginForm extends Component {
    constructor(props) {
        super(props);
        this.state = {
            usernameOrEmail: '',
            password: ''
        };
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

        const loginRequest = Object.assign({}, this.state);

        login(loginRequest)
        .then(response => {
            localStorage.setItem(ACCESS_TOKEN, response.accessToken);
            Alert.success("You're successfully logged in!");
            window.location.href='/profile';
        }).catch(error => {
            Alert.error((error && error.message) || 'Oops! Something went wrong. Please try again!');
        });
    }

    render() {
        return (
            <form onSubmit={this.handleSubmit}>
                <div className="form-item">
                    <input type="text" name="usernameOrEmail"
                        className="form-control" placeholder="Username or email"
                        value={this.state.usernameOrEmail} onChange={this.handleInputChange} required/>
                </div>
                <div className="form-item">
                    <input type="password" name="password"
                        className="form-control" placeholder="Password"
                        value={this.state.password} onChange={this.handleInputChange} required/>
                </div>
                <div className="form-item">
                    <button type="submit" className="btn btn-block btn-primary">Login</button>
                </div>
            </form>
        );
    }
}*/

export default Login
