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

if [ -f ${ASSINA_DIR}/sa.sh ]; then
		source ${ASSINA_DIR}/sa.sh
else
		echo "Could not find sa.sh in $ASSINA_DIR to setup envrionmnent"
fi

PIDFile="sa.pid"

function check_if_pid_file_exists {
  if [ ! -f $PIDFile ]
  then
    echo "PID file not found: $PIDFile"
    exit 1
  fi
}

function check_if_process_is_running {
  if ps -p $(print_process) > /dev/null
  then
    return 0
  else
    return 1
  fi
}

function print_process {
  echo $(<"$PIDFile")
}

check_if_pid_file_exists
if ! check_if_process_is_running
then
  echo "Process $(print_process) already stopped"
  exit 0
fi
kill -TERM $(print_process)
echo -ne "Waiting for process to stop"
NOT_KILLED=1
for i in {1..20}; do
  if check_if_process_is_running
  then
    echo -ne "."
    sleep 1
  else
    NOT_KILLED=0
  fi
done

echo
if [ $NOT_KILLED = 1 ]
then
  echo "Cannot kill process $(print_process)"
  exit 1
fi
echo "Process stopped"
exit 0
