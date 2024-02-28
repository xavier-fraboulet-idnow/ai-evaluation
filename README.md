# TrustProvider Signer

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

TrustProvider Signer is a remote signing service provider, and client.

----------------

### Requirements

- Node (nodejs & npm)
- Java: 11
- Maven

----------------

### Database

- MySQL

The current program uses a MySQL database.

To run it locally, it is necessary to have a MySQL server running. 

```
sudo apt install mysql-server -y
sudo systemctl start mysql.service
```


After installing MySQL, it is necessary to create a database called "assina" and a user "assinaadmin".
In MySQL:

```bash
CREATE DATABASE assina;
CREATE USER 'assinaadmin'@ip identified by 'assinaadmin';
GRANT ALL PRIVILEGES ON *.* TO 'assinaadmin'@ip;
```

This MySQL database is used by the RSSP component. Therefore, when configuring the database user, the **"ip" in the previous commands should be changed to the RSSP component ip**. 

Example: in the previous command:
```bash
CREATE USER 'assinaadmin'@'localhost' identified by 'assinaadmin';
GRANT ALL PRIVILEGES ON *.* TO 'assinaadmin'@'localhost';
```
If the RSSP program and the database run in the same system.

----------------

### HTTP Request to EJBCA

The current implementation executes HTTP requests to a EJBCA. To execute this HTTP Requests, it is necessary to complete the file **ejbca.conf** in *assina-server*

```
# Values required to access the EJBCA:
ejbca.CAHost= # the address:port of the EJBCA
ejbca.clientP12ArchiveFilepath= # the filepath to the pfx file
ejbca.ManagementCA= # the filepath of the ManagementCA file

# Endpoint: 
ejbca.Endpoint= /certificate/pkcs10enroll # the endpoint of EJBCA to which the requests will be directed

# Values required by the endpoint "/pkcs10enroll":
ejbca.certificateProfileName= # the Certificate Profile Name (e.g.: ENDUSER)
ejbca.endEntityProfileName= # (e.g.: EMPTY)
ejbca.certificateAuthorityName= # (e.g.: IACA ROOT)
ejbca.username= 
ejbca.password= 
ejbca.includeChain= # (e.g.: true)
```

----------------

### Running the whole application

In the root directory, do the following:

```bash
./runRSSP.sh
./runSA.sh
./runFEND.sh
```

It will install every dependency needed to run the whole app and start both the Frontend and Backend applications.

It is mandatory that 'runRSSP.sh' is executed before 'runSA.sh'. As the scripts start Java programs and occupy the bash, additional bash scripts were developed with nohup.

After executing all the scripts, a React program is avaliable on **port 3000**.

----------------

### Features

- Creating new User
- Create new credentials for User
- Edit User profile
- Sign document

----------------

### Testing

Please use your PID for testing.

You need to have at least 1 certificate in order to request the signing of a document.

If the signing is successful, you will be redirected to a signing page where you can sign a PDF and download your signed pdf file.

-------------

## How to contribute

We welcome contributions to this project. To ensure that the process is smooth for everyone
involved, follow the guidelines found in [CONTRIBUTING.md](CONTRIBUTING.md).

## License

### License details

Copyright (c) 2023 European Commission

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
