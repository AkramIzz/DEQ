#!/bin/bash

echo "javac tool"
javac -d bin/ tool/*.java

echo "java GenerateAst"
java -cp bin/ com.interpreter.tool.GenerateAst interpreter

echo "javac interpreter"
javac -d bin/ interpreter/*.java

echo "java Interpreter"
java -cp bin/ com.interpreter.Interpreter
