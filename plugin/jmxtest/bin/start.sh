#!/bin/sh
dir=`pwd`
cd $dir
JVM_OPTS="-cp jmxtest-1.0-SNAPSHOT.jar"
JVM_OPTS="$JVM_OPTS -Dcom.sun.management.jmxremote.port=8999"
JVM_OPTS="$JVM_OPTS -Dcom.sun.management.jmxremote.rmi.port=8999"
JVM_OPTS="$JVM_OPTS -Dcom.sun.management.jmxremote.ssl=false"
JVM_OPTS="$JVM_OPTS -Dcom.sun.management.jmxremote.authenticate=true"
JVM_OPTS="$JVM_OPTS -Dcom.sun.management.jmxremote.password.file=./jmxremote.password"
JVM_OPTS="$JVM_OPTS -Dcom.sun.management.jmxremote.access.file=./jmxremote.access"

java $JVM_OPTS com.yufei.test.jmx.Launcher