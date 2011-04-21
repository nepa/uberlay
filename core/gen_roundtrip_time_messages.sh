#!/bin/bash
protoc --java_out=src/main/java/ src/main/resources/roundtrip_time_messages.proto
