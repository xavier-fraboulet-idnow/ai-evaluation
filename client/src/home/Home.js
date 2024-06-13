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

import React, { Component } from "react";
import "./Home.css";

class Home extends Component {
  render() {
    return (
      <div style={{ width: "100%", margin: "auto" }} className="home-container">
        <div className="container">
          <div className="graf-bg-container">
            <div className="graf-layout">
              <div className="graf-circle"></div>
              <div className="graf-circle"></div>
              <div className="graf-circle"></div>
              <div className="graf-circle"></div>
              <div className="graf-circle"></div>
              <div className="graf-circle"></div>
              <div className="graf-circle"></div>
              <div className="graf-circle"></div>
              <div className="graf-circle"></div>
              <div className="graf-circle"></div>
              <div className="graf-circle"></div>
            </div>
          </div>
          <h1 className="home-title">Login to sign PDF Documents</h1>
        </div>
      </div>
    );
  }
}

export default Home;
