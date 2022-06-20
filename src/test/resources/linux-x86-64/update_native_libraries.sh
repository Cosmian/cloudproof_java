#!/bin/sh
set -ex

CUR_DIR=$(pwd)
ROOT_DIR=$(pwd)/../../../..
DESTINATION_DIR=$ROOT_DIR/src/test/resources/linux-x86-64

build_native_library() {
  CRATE_NAME=$1
  GIT_TAG=$2
  rm -rf $CRATE_NAME
  git clone git@github.com:Cosmian/"$CRATE_NAME".git
  cp -f build_rust.sh "$CRATE_NAME"
  pushd "$CRATE_NAME"
  docker run \
    -v "$CUR_DIR/$CRATE_NAME":/root/project \
    -it gitlab.cosmian.com:5000/core/ci-java-8 \
    bash /root/project/build_rust.sh "$GIT_TAG"
  cp target/release/"lib${CRATE_NAME}.so" "$DESTINATION_DIR"
  popd
}

build_native_library abe_gpsw v0.7.0
build_native_library cover_crypt v2.0.1

# Since docker user is root, restore local permissions to current user
sudo chown -R "$(whoami)" .
