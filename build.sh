#!/bin/bash
set -e

echo "=========================================================="
echo " Building Smart Lost & Found Management System"
echo "=========================================================="

mkdir -p bin docs data

echo "[1/4] Compiling Java source files with MongoDB drivers..."
javac -cp "lib/*" -d bin $(find src -name "*.java")

echo "[2/4] Compiling and running automated test suite..."
javac -d bin -cp "bin:lib/*" $(find test -name "*.java")
java -cp "bin:lib/*" com.smartlostfound.TestSuite

echo "[3/4] Packaging self-contained executable Fat JAR with MongoDB drivers..."
(cd bin && for jarfile in ../lib/*.jar; do jar xf "$jarfile" 2>/dev/null || true; done && rm -f META-INF/MANIFEST.MF)
jar cfe SmartLostFound.jar com.smartlostfound.Main -C bin .

echo "[4/4] Generating JavaDoc documentation..."
javadoc -d docs -cp "lib/*" -sourcepath src -subpackages com.smartlostfound -Xdoclint:none -quiet

echo "=========================================================="
echo " Build successful! Executable: SmartLostFound.jar"
echo " Run with: ./run.sh or java -jar SmartLostFound.jar"
echo "=========================================================="
