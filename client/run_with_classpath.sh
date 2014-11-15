#!/bin/sh
export CLASSPATH="$(find lib/ -name '*.jar' | tr '\n' ':')":build/
$@
