#!/bin/bash

echo 'avereon.com ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBAX0k5tSvrXVpKl7HNPIPglp6Kyj0Ypty6M3hgR783ViTzhRnojEZvdCXuYiGSVKEzZWr9oYQnLr03qjU/t0SNw=' >> $HOME/.ssh/known_hosts

mkdir -p /tmp/travis
openssl aes-256-cbc -K $encrypted_c26b694af5a7_key -iv $encrypted_c26b694af5a7_iv -in .travis/id_rsa.enc -out /tmp/travis/id_rsa -d
chmod 600 /tmp/travis/id_rsa
ssh-add /tmp/travis/id_rsa
ssh-keygen /tmp/travis/id_rsa

#eval "$(ssh-agent -s)"
scp -v target/product.jar travis@avereon.com:/opt/avn/store/stable/seenc
#scp target/target/main/java/META-INF/*.card travis@avereon.com:/opt/avn/store/stable/seenc
