#!/bin/bash

cd app/src/main/java/MML2Audio/

javadoc -d ../../../../../javadoc *.java Channel/*.java Exception/*.java Note/*.java Util/*.java -private
