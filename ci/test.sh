#!/usr/bin/env bash
export JAVA_HOME='/etc/alternatives/java_sdk_1.8.0'
export PATH=$JAVA_HOME/bin:$PATH
SCRIPTS_PATH=$(dirname $0)

SBT_OPTS=$MAVEN_OPTS sbt test

has_features=`find ./ -name '*.feature' | wc -l`
if [ "$has_features" -ne 0 ]
then
  SBT_OPTS=$MAVEN_OPTS $SCRIPTS_PATH/run-cucumber.sh
fi
