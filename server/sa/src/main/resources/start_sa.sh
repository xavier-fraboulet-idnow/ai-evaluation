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

# Sets up the environment and runs the spring boot server
export ASSINA_DIR=$( cd "$( dirname ${BASH_SOURCE[0]} )" && pwd )

if [ -f ${ASSINA_DIR}/sa_env.sh ]; then
		source ${ASSINA_DIR}/sa_env.sh
else
		echo "Could not find sa_env.sh in $ASSINA_DIR to setup envrionmnent"
fi


# Fail if JAVA_HOME not set
if [ -z ${JAVA_HOME} ]; then
	echo "JAVA_HOME must be set to the root of a Java 8 JDK before continuing"
	exit 1
fi


# On *nix, where /dev/urandom exists, we use it as the entropy file for java security
# otherwise crypto operations are very slow
if [ -e "/dev/urandom" ]; then
  export DC_JAVA_OPTS="-Djava.security.egd=file:///dev/urandom $DC_JAVA_OPTS"
fi

JAVA_EXE="${JAVA_HOME}/bin/java ${JAVA_OPTS}"

nohup java -jar ${ASSINA_DIR}/*.jar start &



