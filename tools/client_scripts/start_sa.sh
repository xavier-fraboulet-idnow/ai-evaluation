#!/usr/bin/env bash
# Sets up the environment and runs the spring boot server
cd /home/franciscorosa/assinasa

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

echo "Starting signing app. You can watch the logs with:"
echo "Use stop_sa.sh to shut it down, show_sa.sh to show the process"
echo "tail -f ${ASSINA_DIR}/logs/assina/assina-sa-logger.log"

nohup java -jar ${ASSINA_DIR}/*.jar start &



