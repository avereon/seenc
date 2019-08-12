#!/bin/bash

scp target/product.jar travis@avereon.com:/opt/avn/store/stable/seenc
scp target/target/main/java/META-INF/*.card travis@avereon.com:/opt/avn/store/stable/seenc
