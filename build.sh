#!/bin/bash
set -e

echo "=========================================================="
echo " Building Smart Lost & Found Management System"
echo "=========================================================="

mkdir -p bin docs data

echo "[1/4] Compiling Java source files..."
javac -d bin $(find src -name "*.java")

echo "[2/4] Compiling and running automated test suite..."
javac -d bin -cp bin $(find test -name "*.java")
java -cp bin com.smartlostfound.TestSuite

echo "[3/4] Packaging executable JAR..."
jar cfe SmartLostFound.jar com.smartlostfound.Main -C bin com

echo "[4/4] Generating JavaDoc documentation..."
javadoc -d docs -sourcepath src -subpackages com.smartlostfound -Xdoclint:none -quiet

echo "=========================================================="
echo " Build successful! Executable: SmartLostFound.jar"
echo " Run with: ./run.sh or java -jar SmartLostFound.jar"
echo "=========================================================="
