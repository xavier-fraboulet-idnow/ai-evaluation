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

import React, {Component} from 'react';

class Footer extends Component {
    render(){
        return(
            <footer>
              <div className="row m-0">
                <div className="col">
                  <div className="footer-column">
                    <a href="#">Link 1</a>
                    <a href="#">Link 2</a>
                    <a href="#">Link 3</a>
                  </div>
                </div>
                <div className="col footer-image">
                  <div className="footer-column ">
                    <a href="#"><img src="/Images/logo.svg" alt="Image 1"/></a>
                    <small className="mt-2">Version 1.0.0</small>
                  </div>
                </div>
                <div className="col">
                  <div className="footer-column">
                    <a href="#">Contact Us</a>
                    <a href="#">About Us</a>
                    <a href="#">Privacy Policy</a>
                  </div>
                </div>
              </div>
            </footer>
        );
    }
}


export default Footer