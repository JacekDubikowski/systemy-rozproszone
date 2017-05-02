#!/bin/bash

protoc -I=. --java_out=./src/main/java --plugin=protoc-gen-grpc-java=./protoc-gen-grpc-java.exe --grpc-java_out=./src/main/java MedicalData.proto
python -m grpc_tools.protoc -I. --python_out=./python --grpc_python_out=./python MedicalData.proto