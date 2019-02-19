#!/bin/bash
echo Using Git Repo: ${GIT_REPO:="https://github.com/SpectraLogic/ds3_java_cli.git"}
echo Using Git Branch: ${GIT_BRANCH:="master"}

echo DS3_ENDPOINT ${DS3_ENDPOINT}
echo DS3_SECRET_KEY ${DS3_SECRET_KEY}
echo DS3_ACCESS_KEY ${DS3_ACCESS_KEY}

set -x

cd /opt

if [ ${GIT_BRANCH} != "master" ]; then
  git clone ${GIT_REPO} --branch ${GIT_BRANCH} --single-branch
else
  git clone ${GIT_REPO}
fi

cd ds3_java_cli
git rev-parse HEAD

# Build all projects and run all the certification integration tests.
./gradlew ds3-cli-certification:test
