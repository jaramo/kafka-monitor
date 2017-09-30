#!/usr/bin/env bash

./sbt stage
./target/universal/stage/bin/monitor-app -Dconfig.file=application.conf "$@"
