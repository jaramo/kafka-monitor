# kafka-monitor

create file `application.conf` (preferably on root dir)

 if application.conf present in root dir
    ```./start.sh```
 else
    ```./start.sh -Dconfig.file=path/to/application.conf```

## Tests
    ```./sbt test```