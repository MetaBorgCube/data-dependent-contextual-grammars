#!/bin/bash

mvn clean install
cd data-dependent
java -jar target/benchmarks.jar -bm avgt -f 1 -i 5 -wi 5 -tu ms
cd ..
