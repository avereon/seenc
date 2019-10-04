#!/bin/bash

RELEASE="latest"
PRODUCT='seenc'
PLATFORM='linux'

#RELEASE github.ref [refs/heads/master, refs/heads/stable]
case "${GITHUB_REF}" in
  "refs/heads/master") RELEASE="latest" ;;
  "refs/heads/stable") RELEASE="stable" ;;
esac

#OS matrix.os [ubuntu-latest, macOS-latest, windows-latest ]
case "${MATRIX_OS}" in
  "ubuntu-latest") PLATFORM="linux" ;;
  "macOS-latest") PLATFORM="macosx" ;;
esac

if [ "${PLATFORM}" == "linux" ]; then
  export DISPLAY=:99
  Xvfb ${DISPLAY} -screen 0 1920x1080x24 -nolisten unix &
fi

rm -rf target/jlink
mvn deploy -B -U -V -P testui,platform-specific-assemblies --settings .github/settings.xml --file pom.xml

echo "Build date=$(date)"
echo "[github.ref]=${GITHUB_REF}"
echo "[matrix.os]=${MATRIX_OS}"
echo "Deploy path=/opt/avn/store/$RELEASE/$PRODUCT"

mkdir ${HOME}/.ssh
echo "${TRAVIS_SSH_KEY}" > ${HOME}/.ssh/id_rsa
echo "${TRAVIS_SSH_PUB}" > ${HOME}/.ssh/id_rsa.pub
echo 'avereon.com ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBAX0k5tSvrXVpKl7HNPIPglp6Kyj0Ypty6M3hgR783ViTzhRnojEZvdCXuYiGSVKEzZWr9oYQnLr03qjU/t0SNw=' >> ${HOME}/.ssh/known_hosts
chmod 600 ${HOME}/.ssh/id_rsa
chmod 600 ${HOME}/.ssh/id_rsa.pub
chmod 600 ${HOME}/.ssh/known_hosts

scp -B target/*product.jar travis@avereon.com:/opt/avn/store/$RELEASE/$PRODUCT
scp -B target/main/java/META-INF/*.card travis@avereon.com:/opt/avn/store/$RELEASE/$PRODUCT