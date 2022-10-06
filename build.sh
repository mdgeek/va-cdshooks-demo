#!/bin/bash

set -e

if [[ "$2" == "" ]]
then
  options="-U"
else
  options="$2 $3 $4 $5 $6"
fi

case "$1" in

"")
  echo You must specify a build profile.
  echo For example: ./build.sh dev
  echo Recognized profiles are: alpha, beta, debug, dev, prod, rc
  exit 1
;;

"prod" | "rc")
  logLevels=$'
log4j.logger.edu.utah=ERROR
log4j.logger.org.opencds=ERROR
'
;;


"alpha" | "beta" | "debug" | "dev")
  logLevels=$'
log4j.logger.edu.utah=DEBUG
log4j.logger.org.opencds=DEBUG
'
;;

*)
  echo Unrecognized build profile: $1
  exit 1
;;

esac

echo Building war...
mvn clean package $options -Dbuild.profile="$1" -Dlog.levels="$logLevels"


