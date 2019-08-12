#!/bin/bash

# Maven deploy
mvn --settings .travis/settings.xml -DskipTests=true -Dmaven.javadoc.skip=true -B -U -V deploy

# Avereon deploy
echo 'avereon.com ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBAX0k5tSvrXVpKl7HNPIPglp6Kyj0Ypty6M3hgR783ViTzhRnojEZvdCXuYiGSVKEzZWr9oYQnLr03qjU/t0SNw=' >> $HOME/.ssh/known_hosts
openssl aes-256-cbc -K $encrypted_c26b694af5a7_key -iv $encrypted_c26b694af5a7_iv -in .travis/id_rsa.enc -out $HOME/.ssh/id_rsa -d
chmod 600 $HOME/.ssh/*
scp -B target/product.jar travis@avereon.com:/opt/avn/store/stable/seenc
scp -B target/main/java/META-INF/product.card travis@avereon.com:/opt/avn/store/stable/seenc
