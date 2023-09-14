#!/bin/sh
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/master/ci-build.sh
mvn -Djavadoc.skip -pl scijava/scijava-taglets clean install
export BUILD_ARGS="-X"
sh ci-build.sh
