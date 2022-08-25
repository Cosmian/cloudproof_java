#!/bin/sh
set -ex

# This script has to been executed from current folder.

CUR_DIR=$(pwd)
ROOT_DIR=$(pwd)/../../../..
DESTINATION_DIR=$ROOT_DIR/src/test/resources/linux-x86-64

build_native_library() {
  REPO=$1
  CRATE_NAME=$2
  GIT_TAG=$3
  LIB_NAME=$4
  rm -rf "$CRATE_NAME"
  git clone "$REPO/$CRATE_NAME.git"
  cp -f build_rust.sh "$CRATE_NAME"
  pushd "$CRATE_NAME"
  docker run \
    -v "$CUR_DIR/$CRATE_NAME":/root/project \
    -it gitlab.cosmian.com:5000/core/ci-java-8 \
    bash /root/project/build_rust.sh "$GIT_TAG"
  cp target/release/"${LIB_NAME}" "$DESTINATION_DIR"
  popd
}

build_native_library git@github.com:Cosmian abe_gpsw v1.1.1 libabe_gpsw.so
build_native_library git@github.com:Cosmian cover_crypt v4.0.0 libcover_crypt.so
build_native_library git@gitlab.cosmian.com:core findex v0.4.0 libcosmian_findex.so

# Since docker user is root, restore local permissions to current user
sudo chown -R "$(whoami)" .
