#!/usr/bin/env bash

# Copyright 2024 European Commission
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Sets up the environment for the RSSP

#export SERVER_PORT=80
export SERVER_PORT=8082
# Must point to the redirect on the node client server and port
export ASSINA_CLIENT_BASE_URL=http://assina.eu
#export ASSINA_CLIENT_BASE_URL=http://assina.westeurope.cloudapp.azure.com
#export ASSINA_CLIENT_BASE_URL=http://20.101.144.136
#export ASSINA_CLIENT_PORT=http://localhost:3000
export LOGS=./logs
mkdir -p $LOGS

export JAVA_HOME=/usr/bin/java

export ASSINA_OAUTH2_AUTHORIZEDREDIRECTURIS=${ASSINA_CLIENT_BASE_URL}

export SPRING_DATASOURCE_URL=jdbc:mysql://assinadb.mysql.database.azure.com:3306/mydb?useSSL=false&serverTimezone=UTC&useLegacyDatetimeCode=false
export SPRING_DATASOURCE_USERNAME=franciscobraga@assinadb
export SPRING_DATASOURCE_PASSWORD=Assinadb2021
export SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENTID=936504596876-6rlbd2q0le55f12g55qd7u1hlvcmn08k.apps.googleusercontent.com
export SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENTSECRET=4McvTO5ujBuIj2p7R-yMXVFL
export SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_REDIRECTURI={baseUrl}/oauth2/callback/{registrationId}
export SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GITHUB_CLIENTID=2e5857e1624a7f5b787c
export SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GITHUB_CLIENTSECRET=186c64955efe28273e2d541ebe138945e5a7185a
export SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GITHUB_REDIRECTURT={baseUrl}/oauth2/callback/{registrationId}

export ASSINA_AUTH_TOKENSECRET=217A25432A462D4A614E645267556B58
export ASSINA_AUTH_TOKENSECRET_lifetimeMinutes=600

export CSC_CRYPTO_ISSUER=assina.eu
export CSC_CRYPTO_MONTHSVALIDITY=12
export CSC_CRYPTO_KEYSIZE=2048
export CSC_CRYPTO_SIGNATUREALGORITHM=SHA256WithRSA
export CSC_CRYPTO_PASSPHRASE=442A472D4B6150645367566B59703373

export CSC_SAD_TYPE=SAD
export CSC_SAD_LIFETIMEMINUTES=5
export CSC_SAD_TOKENSECRET=2B4B6250655368566D59713374367639
