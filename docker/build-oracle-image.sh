#!/bin/sh
git clone git@github.com:oracle/docker-images.git
cd docker-images/OracleDatabase/SingleInstance/dockerfiles
./buildContainerImage.sh -v 18.4.0 -x -t oracled

cd -
pwd
