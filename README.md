dm-lily-showcase-athens
=======================

Load XML example record into Lily, build index and search

Build using:
    mvn install

Run using:
    mvn exec:java

To connect to a non-local lily cluster:
    mvn exec:java -DzkConn=zkserver.example.com:2181