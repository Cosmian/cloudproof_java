#!/bin/sh
set -ex

GIT_TAG=$1

cd /root/project
git checkout "$GIT_TAG"
cargo build --release --features ffi
