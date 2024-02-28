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
import { Link, NavLink } from 'react-router-dom';
import './AppHeader.css';

import logo from '../img/logo.svg';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../site.css';

class AppHeader extends Component {

    render(){
        if(this.props.authenticated){
            return(
                <div className='app-header'>
                <nav className='navbar navbar-expand-lg fixed-top' style={{ color:"white", backgroundColor:"#767676" }}>
                    <div className='container'> 
                        <div>
                            <Link to="/" className="navbar-brand"> <img src={logo} alt="Logo"/></Link>
                        </div>
                        <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                            <span className="navbar-toggler-icon"></span>
                        </button>
                        <div className="collapse navbar-collapse" id="navbarNav">
                            <ul className="navbar-nav me-auto">
                                <li className="nav-item">
                                    <Link to="/profile" className="nav-link active" aria-current="page">rQES</Link>
                                </li>
                                <li className="nav-item">
                                    <Link to="/certificates" className="nav-link">View</Link>
                                </li>
                                <li className="nav-item">
                                    <Link to="/sign" className="nav-link">Signing</Link>
                                </li>
                                {/*<li className="nav-item">
                                    <a className="nav-link" href="help.html">Help</a>
                                </li>*/}
                            </ul>
                            <ul className="navbar-nav ml-auto">
                                <li className="nav-item dropdown">
                                    <a className="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false"> Account </a>
                                    <ul className="dropdown-menu" aria-labelledby="navbarDropdown">
                                        {/*<li><NavLink to="/edit" className="dropdown-item">View Profile</NavLink></li>*/}
                                        <li><NavLink to="/logs" className="dropdown-item">View Logs</NavLink></li>
                                        <li><hr className="dropdown-divider"/></li>
                                        <li><a className="dropdown-item" onClick={this.props.onLogout}>Logout</a></li>
                                    </ul>
                                </li>
                            </ul>
                        </div>
                    </div>
                </nav>
                </div>
            );
        }
        else return (
            <nav className='navbar navbar-expand-lg fixed-top' style={{ color:"white", backgroundColor:"#767676" }}>
                    <div className='container'> 
                        <div>
                            <Link to="/" className="navbar-brand"> <img src={logo} alt="Logo"/></Link>
                        </div>
                        <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                            <span className="navbar-toggler-icon"></span>
                        </button>
                        <div className="collapse navbar-collapse" id="navbarNav">
                            <ul className="navbar-nav me-auto">
                                <li className="nav-item">
                                    <Link to="/" className="nav-link active" aria-current="page">Home</Link>
                                </li>
                                <li className="nav-item">
                                    <Link to="/login" className="nav-link">Login</Link>
                                </li>
                            </ul>
                        </div>
                    </div>
                </nav>
        );
    }
}

export default AppHeader;