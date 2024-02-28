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
import axios from 'axios';

import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

import { log_sign_file, log_download_file} from '../../util/APIUtils';
import {ACCESS_TOKEN, SA_BASE_URL, CSC_BASE_URL} from '../../constants';
import $ from "jquery";
import "smartwizard/dist/css/smart_wizard_all.css";
import smartWizard from 'smartwizard';
import LoadingIndicator from '../../common/LoadingIndicator';

import {InitializeStepper, InitPage, nextStep, back} from "./site";

import "./Sign.css";
import QRCode from 'react-qr-code';


class Sign extends Component {

    componentDidMount() {
        $(function() {
            //InitializeStepper();
            var currentStep = 0;

            $('#smartwizard').smartWizard({
                selected: 0, // Initial selected step, 0 = first step
                theme: 'square', // theme for the wizard, related css need to include for other than default theme
                justified: true, // Nav menu justification. true/false
                autoAdjustHeight: true, // Automatically adjust content height
                backButtonSupport: true, // Enable the back button support
                enableUrlHash: true, // Enable selection of the step based on url hash
                transition: {
                    animation: 'slideHorizontal', // Animation effect on navigation, none|fade|slideHorizontal|slideVertical|slideSwing|css(Animation CSS class also need to specify)
                    speed: '400', // Animation speed. Not used if animation is 'css'
                    easing: '', // Animation easing. Not supported without a jQuery easing plugin. Not used if animation is 'css'
                    prefixCss: '', // Only used if animation is 'css'. Animation CSS prefix
                    fwdShowCss: '', // Only used if animation is 'css'. Step show Animation CSS on forward direction
                    fwdHideCss: '', // Only used if animation is 'css'. Step hide Animation CSS on forward direction
                    bckShowCss: '', // Only used if animation is 'css'. Step show Animation CSS on backward direction
                    bckHideCss: '', // Only used if animation is 'css'. Step hide Animation CSS on backward direction
                },
                toolbar: {
                    position: 'bottom', // none|top|bottom|both
                    showNextButton: true, // show/hide a Next button
                    showPreviousButton: true, // show/hide a Previous button
                    // extraHtml: `
                    // <button class="btn btn-secondary shadow" id="back" style="display:none" onClick="back"><i class="bi bi-arrow-left-circle"></i> Back </button>
                    // <button class="btn btn-info shadow" id="toUpload" style="display:none" onClick="nextStep('upload')"> Upload Document <i class="bi bi-cloud-arrow-up"></i> </button>
                    // <button class="btn btn-info shadow" id="toSign" style="display:none" onClick="nextStep('sign')"> Sign Document <i class="bi bi-vector-pen"></i></button>
                    // ` // Extra html to show on toolbar
                },
                anchor: {
                    enableNavigation: true, // Enable/Disable anchor navigation 
                    enableNavigationAlways: false, // Activates all anchors clickable always
                    enableDoneState: true, // Add done state on visited steps
                    markPreviousStepsAsDone: true, // When a step selected by url hash, all previous steps are marked done
                    unDoneOnBackNavigation: false, // While navigate back, done state will be cleared
                    enableDoneStateNavigation: true // Enable/Disable the done state navigation
                },
                keyboard: {
                    keyNavigation: true, // Enable/Disable keyboard navigation(left and right keys are used if enabled)
                    keyLeft: [37], // Left key code
                    keyRight: [39] // Right key code
                },
                lang: { // Language variables for button
                    next: 'Next',
                    previous: 'Previous'
                },
                disabledSteps: [], // Array Steps disabled
                errorSteps: [], // Array Steps error
                warningSteps: [], // Array Steps warning
                hiddenSteps: [], // Hidden steps
                getContent: null // Callback function for content loading
              });
             
            $('#smartwizard').smartWizard("reset");

            // Step show event
            $("#smartwizard").on("showStep", function (e, anchorObject, stepIndex, stepDirection, stepPosition) {
              // Get step info from Smart Wizard
              let stepInfo = $('#smartwizard').smartWizard("getStepInfo");
              let totSteps = stepInfo.totalSteps;
              console.log("current : " , stepInfo.currentStep)
              var step_num = stepInfo.currentStep;
              if (step_num === 2 || step_num === 3) {
                  $('.sw-btn-next').hide();
                  $('.sw-btn-prev').hide();
              } else {
                  $('.sw-btn-next').show();
                  $('.sw-btn-prev').show();
              }

              if (stepPosition === 'first') {
                  $("#back").hide();
                  $("#toUpload").show();
                  $("#toSign").hide();
              } else if (stepPosition === 'last') {
                  $("#back").show();

              } else {
                  if(stepInfo.currentStep === 1)
                  {
                    $("#toUpload").hide();
                    $("#toSign").show();
                    $("#back").show();
                  }
                  else if(stepInfo.currentStep === 2)
                  {
                    //in sign page 
                    $("#toSign").hide();
                    $("#toDownload").show();
                  }
                  else if(stepInfo.currentStep === 3)
                  {
                    //in download page
                    $("#toDownload").hide();
                  }
              }
          });
        });
        // $("#smartwizard").on("showStep", (e, anchorObject, stepIndex, stepDirection, stepPosition) => {
        //     if (stepIndex === 3) { // Verifica se o passo é o passo 3 (índice 2)
        //         this.uploadFileData(); // Chama a função de upload do arquivo
        //     }
        // });
        InitPage();
    }

    constructor(props) {
        super(props);
        this.state = {file: '', fileName: '', fileLink: '', msg: '', pin: '', credentials: [],
                    selectedCredential: '', credentialName: '',
                    token: localStorage.getItem(ACCESS_TOKEN), selectedCredential: '',
                    answerFromOID4VP: false, visible: false, deeplink: ''};

        const headers = {
	        'Authorization': 'Bearer '+this.state.token
        };
      
        axios.post(CSC_BASE_URL + '/credentials/list',
            {},
            {
                headers: headers
            })
            .then(res=>{
                if(res.data.credentialInfo.length != 0) {
                    this.setState({
                        credentials: res.data.credentialInfo
                    })
                    console.log(this.state.credentials);    
                }
            })
            .catch(error=>{
                console.log(error);
            }
        )
        
        this.changeCredential = this.changeCredential.bind(this);
        this.authorizationWithOID4vP = this.authorizationWithOID4vP.bind(this);
        this.redirectClick = this.redirectClick.bind(this);
        this.handleClick = this.handleClick.bind(this);
        this.onFileChange = this.onFileChange.bind(this);
    }

    handleClick = (event) => {
        log_download_file();
        event.preventDefault();
        window.open(this.state.fileLink);
        window.location = '/profile';
    }

	onFileChange = (event) => {
        this.setState({
			file: event.target.files[0],
            fileName: event.target.files[0].name
		});
        const file = event.target.files[0];
        const reader = new FileReader();
    
        reader.onload = (event) => {
            const fileContent = event.target.result;
            const embedElement = document.getElementById('pdfEmbed');
            embedElement.setAttribute('src', fileContent);
        };
    
        reader.readAsDataURL(file);
    }

    redirectClick = (event) => {
        window.location.href = this.state.deeplink;
    }

    authorizationWithOID4vP = (event) => {
        event.preventDefault();

        this.setState({
            answerFromOID4VP: true
        })

        const headers = {
            'Authorization': 'Bearer '+this.state.token
        };

        if(this.state.file === '') {
            this.setState({
                answerFromOID4VP: false
            })
            return(<div>{toast.warning("Please select a file")}</div>)
        }

        const data = new FormData();
        data.append('file', this.state.file);
        data.append('pin', "");
        data.append('credential', this.state.selectedCredential);


        axios.get( SA_BASE_URL + '/getOIDRedirectLink', { headers: headers } )
        .then(res => {
            console.log(res.data.link);
            data.append('nonce', res.data.nonce);
            data.append('presentationId', res.data.presentationId);

            const deeplink = res.data.link;
            this.setState({
                deeplink: deeplink,
                visible: true
            })

            axios.post( SA_BASE_URL + '/signFile', data, {headers: headers} )
            .then(res => {
                log_sign_file(1);
                console.log(res.data);
                console.log(res.data.fileDownloadUri);
                this.setState({
                    answerFromOID4VP: false,
                    fileLink: res.data.fileDownloadUri,
                })
                $('#smartwizard').smartWizard("goToStep", 3);
            })          
        })
    } 

    changeCredential = (alias) => {
        const credential = this.state.credentials.find(c => c.alias === alias);
        if (credential) {
            this.setState({
                selectedCredential: alias,
                credentialName: credential.alias
            }, () => {
                console.log(this.state.selectedCredential);
            });
        }
    }

    render() {
        return (
                <div className="container" style={{marginTop:"3em"}}>
                    <ToastContainer/>
                    
                    <div className="row">
                        <div className="col-md-12 text-center">
                            <h3>Signing Documents</h3>
                        </div>
                    </div>

                    <div className="row mt-5">
                        <div className="col-md-12 card shadow">
                            <div id="smartwizard">
                                <ul className="nav" style={{ marginTop:"10px" }}>
                                    <li className="nav-item">
                                        <a className="nav-link" href="#step-1">
                                            <div className="num">1</div>
                                            Select
                                        </a>
                                    </li>
                                    <li className="nav-item">
                                        <a className="nav-link" href="#step-2">
                                            <span className="num">2</span>
                                            Upload
                                        </a>
                                    </li>
                                    <li className="nav-item">
                                        <a className="nav-link" href="#step-3">
                                            <span className="num">3</span>
                                            Sign
                                        </a>
                                    </li>
                                    <li className="nav-item">
                                        <a className="nav-link " href="#step-4">
                                            <span className="num">4</span>
                                            Download
                                        </a>
                                    </li>
                                </ul>
                                <div className="tab-content" style={{ maxHeight:"500px", display:"flex", flexWrap:"wrap" }}>
                                    <div id="step-1" className="tab-pane" role="tabpanel" aria-labelledby="step-1" style={{width:"100%"}}>
                                        <div className="row certificateContainer">
                                            <div className="col-md-12">
                                            <h4>
                                                Selected Certificate: <b>{this.state.credentialName}</b>
                                            </h4>
                                            <table className="table table-hover">
                                                    <thead>
                                                        <tr>
                                                            <th>Alias</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        {this.state.credentials.map(c => {
                                                            return (
                                                                <tr key={c.alias} onClick={() => this.changeCredential(c.alias)}>
                                                                    <td>{c.alias}</td>
                                                                </tr>
                                                            )
                                                        })}
                                                    </tbody>
                                                </table>
                                            </div>
                                        </div>
                                    </div>
                                    <div id="step-2" className="tab-pane" role="tabpanel" aria-labelledby="step-2" style={{width:"100%"}}>
                                        <div className="row">
                                            <div className="col-md-12">
                                                <div className="row mt-1">
                                                    <div className="col-md-12">
                                                        <div className="input-group mb-3">
                                                            <input type="file" className="form-control" accept=".pdf" id="inputGroupFile02" name="file" onChange={this.onFileChange} required />
                                                        </div>
                                                    </div>
                                                </div>
                                                <div className="row mt-1">
                                                    <div className="col-md-12">
                                                        <embed id="pdfEmbed" width="100%" height="500" type="application/pdf" />
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div id="step-3" className="tab-pane" role="tabpanel" aria-labelledby="step-3" style={{width:"100%", height:"auto"}}>
                                        <div className="row">
                                            <div className="col-md-12">
                                                <div className="row mt-1">
                                                    <div className="col-md-12 text-center">
                                                        <h4>
                                                            Selected Certificate: <b>{this.state.credentialName}</b>
                                                        </h4>
                                                    </div>
                                                </div>
                                                <div className="row mt-1">
                                                    <div className="col-md-12 text-center">
                                                        <h4>
                                                            Uploaded File: <b>{this.state.fileName}</b>
                                                        </h4>
                                                    </div>
                                                </div>
                                                <div className="row mt-3">
                                                    <div className="col-md-12 text-center">
                                                        <button onClick={this.authorizationWithOID4vP} className="btn btn-outline-primary btn-lg" disabled={this.state.answerFromOID4VP}>
                                                            <h5>
                                                                Sign with EUDI Wallet
                                                                <img src="Images/logo.svg" alt="logo" height="30" />
                                                            </h5>
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
                                                                </div>
                                                            }
                                                        </div>           
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div id="step-4" className="tab-pane" role="tabpanel" aria-labelledby="step-4" style={{width:"100%"}}>
                                        <div className="row">
                                            <div className="col-md-12">
                                                <div className="row mt-1">
                                                    <div className="col-md-12 text-center">
                                                        <h3 className="text-success">The file was successfully signed!</h3>
                                                    </div>
                                                </div>
                                                <div className="row mt-3">
                                                    <div className="col-md-12 text-center">
                                                        <button onClick={this.handleClick} className="btn btn-outline-success btn-lg">Download File <i className="bi bi-download"></i> </button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            );
        }
    }
    
export default Sign;