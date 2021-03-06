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

gpg --quiet --batch --yes --decrypt --passphrase=$AVN_GPG_PASSWORD --output .github/avereon.keystore .github/avereon.keystore.gpg
ls -al .github/avereon.keystore
sha1sum .github/avereon.keystore

rm -rf target/jlink && mvn deploy -B -U -V -P testui,platform-specific-assemblies --settings .github/settings.xml --file pom.xml
if [ $? -ne 0 ]; then exit 1; fi

echo "Build date=$(date)"
echo "[github.ref]=${GITHUB_REF}"
echo "[matrix.os]=${MATRIX_OS}"
echo "Deploy path=/opt/avn/store/$RELEASE/$PRODUCT"

mkdir "${HOME}/.ssh"
gpg --quiet --batch --yes --decrypt --passphrase=$AVN_GPG_PASSWORD --output $HOME/.ssh/id_rsa .github/id_rsa.gpg
gpg --quiet --batch --yes --decrypt --passphrase=$AVN_GPG_PASSWORD --output $HOME/.ssh/id_rsa.pub .github/id_rsa.pub.gpg
gpg --quiet --batch --yes --decrypt --passphrase=$AVN_GPG_PASSWORD --output $HOME/.ssh/known_hosts .github/known_hosts.gpg

chmod 600 ${HOME}/.ssh/id_rsa
chmod 600 ${HOME}/.ssh/id_rsa.pub
chmod 600 ${HOME}/.ssh/known_hosts

ls -al "$HOME/.ssh"
sha1sum "$HOME/.ssh/id_rsa"
sha1sum "$HOME/.ssh/id_rsa.pub"
sha1sum "$HOME/.ssh/known_hosts"

scp -B target/*product.jar travis@avereon.com:/opt/avn/store/$RELEASE/$PRODUCT 2>&1
if [ $? -ne 0 ]; then exit 1; fi
scp -B target/main/java/META-INF/*.card travis@avereon.com:/opt/avn/store/$RELEASE/$PRODUCT 2>&1
if [ $? -ne 0 ]; then exit 1; fi
