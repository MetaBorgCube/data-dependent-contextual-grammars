#!/bin/bash

POSITIONAL=()

if [[ $# -eq 0 ]]; then
  PARSETABLEGENERATION=YES
  PARSETIME=YES
  OCAML=YES
  JAVA=YES
else

while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    -ptg|--ptgeneration)
    PARSETABLEGENERATION=YES
    shift # past argument
    ;;
    -pt|--parsetime)
    PARSETIME=YES
    shift # past argument
    ;;
    -o|--ocaml)
    OCAML=YES
    shift # past argument
    ;;
    -j|--java)
    JAVA=YES
    shift # past argument
    ;;
    *)    # unknown option
    POSITIONAL+=("$1") # save it in an array for later
    shift # past argument
    ;;
esac
done
fi

set -- "${POSITIONAL[@]}" # restore positional parameters

# echo PARSE TABLE GENERATION  = "${PARSETABLEGENERATION}"
# echo PARSE TIME     = "${PARSETIME}"
# echo OCAML    = "${OCAML}"
# echo JAVA         = "${JAVA}"


echo "--building benchmarks--"
mvn clean install

if [[ -n "$PARSETABLEGENERATION" ]]; then

 echo "--running benchmarks for measuring the time spent on parse table generation--"

   if [[ -n "$JAVA" ]]; then
   echo "JAVA"
   cd data-dependent
   mkdir -p Results/PTGen/Java
   java -jar target/benchmarks.jar ParseTableGenerationBenchmark -bm avgt -f 1 -i 25 -wi 25 -tu ms -t 1 -p a_lang=JAVA -o Results/PTGen/Java/java-result.txt
   cd ..
 fi
 if [[ -n "$OCAML" ]]; then
   echo "OCAML"
   cd data-dependent
   mkdir -p Results/PTGen/OCaml
   java -jar target/benchmarks.jar ParseTableGenerationBenchmark -bm avgt -f 1 -i 25 -wi 25 -tu ms -t 1 -p a_lang=OCAML -o Results/PTGen/OCaml/ocaml-result.txt
   cd ..
 fi
fi

if [[ -n "$PARSETIME" ]]; then
  echo "--running benchmarks for measuring parse time of each file--"
  if [[ -n "$JAVA" ]]; then
  echo "JAVA"
  cd data-dependent
  mkdir -p Results/
  for i in `find test/Java -name "*.java" -type f`; do
    DIRECTORY=$(dirname $i)
    filename=$(basename "$i")
    filename="${filename%.*}"
    echo "Running benchmark for file ${filename}"
    mkdir -p Results/$DIRECTORY
    java -jar target/benchmarks.jar DataDependentParsingBenchmark -bm avgt -f 1 -i 25 -wi 25 -tu ms -t 1 -p file=$i -p lang=1 -o Results/$DIRECTORY/$filename.txt
  done
  cd ..
fi
if [[ -n "$OCAML" ]]; then
  echo "OCAML"
  cd data-dependent
  mkdir -p Results/
  for i in `find test/OCaml -name "*.ml" -type f`; do
    DIRECTORY=$(dirname $i)
    filename=$(basename "$i")
    filename="${filename%.*}"
    echo "Running benchmark for file ${filename}"
    mkdir -p Results/$DIRECTORY
    java -jar target/benchmarks.jar DataDependentParsingBenchmark -bm avgt -f 1 -i 25 -wi 25 -tu ms -t 1 -p file=$i -p lang=1 -o Results/$DIRECTORY/$filename.txt
  done
  cd ..

fi

fi
