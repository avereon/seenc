#!/bin/bash

echo 'avereon.com ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBAX0k5tSvrXVpKl7HNPIPglp6Kyj0Ypty6M3hgR783ViTzhRnojEZvdCXuYiGSVKEzZWr9oYQnLr03qjU/t0SNw=' >> $HOME/.ssh/known_hosts

openssl aes-256-cbc -K $encrypted_c26b694af5a7_key -iv $encrypted_c26b694af5a7_iv -in .travis/id_rsa.enc -out /home/travis/.ssh/id_rsa -d
cp .travis/id_rsa.pub /home/travis/.ssh/id_rsa.pub
chmod 600 /home/travis/.ssh/*
ssh-add /home/travis/.ssh/id_rsa
ssh-keygen -lf /home/travis/.ssh/id_rsa

eval "$(ssh-agent -s)"
scp -v target/product.jar travis@avereon.com:/opt/avn/store/stable/seenc
#scp target/target/main/java/META-INF/*.card travis@avereon.com:/opt/avn/store/stable/seenc
