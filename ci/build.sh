#!/usr/bin/env bash
set -ex

export JAVA_HOME='/etc/alternatives/java_sdk_1.8.0'
export PATH=$JAVA_HOME/bin:$PATH

echo ">>> Building jar."
SBT_OPTS=$MAVEN_OPTS sbt assembly

echo ">>> Cleaning SOURCES, SRPMS, RPMS directories."
cd infrastructure
rm -fr SOURCES SRPMS RPMS

echo ">>> Creating .tar.gz file."
mkdir SOURCES
tar -czf ./SOURCES/asset-store-aggregate.tar.gz ../target/scala-2.11/asset-store-aggregate.jar ../conf ./asset-store-aggregate.service ./bake-scripts

echo ">>> Building RPM."
mock-build --os 7 --define "%buildnum $BUILD_NUMBER"

echo ">>> Releasing to Cosmos."
cosmos-release service asset-store-aggregate RPMS/*.rpm --os centos7

cosmos-release service asset-store-aggregate-training RPMS/*.rpm --os centos7
