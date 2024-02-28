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

# Sets up the environment for the SA

# Port this SA run on - must be specified in the client app
export SERVER_PORT=8083

#Change the variable "ASSINA_RSSP_BASE_URL" to the url of the rssp component:
#export ASSINA_RSSP_BASE_URL=http://assinarssp.westeurope.cloudapp.azure.com
#export ASSINA_RSSP_BASE_URL=http://13.93.7.40:80
export ASSINA_RSSP_BASE_URL=http://localhost:8082

# Used by CORS must match the URL that the browser uses for the client
# Must include the port number if it is not 80 or 443, must NOT include it otherwise
#export ASSINA_CLIENT_BASE_URL=http://assina.westeurope.cloudapp.azure.com
export ASSINA_CLIENT_BASE_URL=http://assina.eu
#export ASSINA_CLIENT_BASE_URL=http://20.101.144.136

export JAVA_HOME=/usr/bin/java

export FILE_UPLOADDIR=./files
# Must point to the redirect on the node client server and port
export LOGS=./logs
mkdir -p $LOGS

mkdir -p $FILE_UPLOADDIR
export RSSP_CSCBASEURL=${ASSINA_RSSP_BASE_URL}/csc/v1

export SPRING_SERVLET_MULTIPART_MAXFILESIZE=1MB
export SPRING_SERVLET_MULTIPART_MAXREQUESTSIZE=1MB
export SPRING_SERVLET_MULTIPART_FILESIZETHRESHOLD=20KB

export FILE_EXTENSIONS=pdf
