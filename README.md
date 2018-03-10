# ErisCasper.java

[![Build Status](https://travis-ci.org/princesslana/ErisCasper.java.svg?branch=master)](https://travis-ci.org/princesslana/ErisCasper.java)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=com.github.princesslana%3AErisCasper.java&metric=sqale_index)](https://sonarcloud.io/dashboard?id=com.github.princesslana%3AErisCasper.java)
[![Code Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.github.princesslana%3AErisCasper.java&metric=coverage)](https://sonarcloud.io/dashboard?id=com.github.princesslana%3AErisCasper.java)

**Latest Release:** 
![Maven Central](https://img.shields.io/maven-central/v/com.github.princesslana/ErisCasper.java.svg)
[![Javadocs](http://javadoc.io/badge/com.github.princesslana/ErisCasper.java.svg)](http://javadoc.io/doc/com.github.princesslana/ErisCasper.java)

**Latest Snapshot:** ![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.github.princesslana/ErisCasper.java.svg)

Documentation for the latest release is available at https://princesslana.github.io/ErisCasper.java

## Snapshots

Snapshots of master are automatically published to sonatype.

To use from maven add the sonatype snapshot repository:

```xml
  <repositories>
    <repository>
      <id>ossrh</id>
      <url>https://oss.sonatype.org/content/repositories/snapshots</url>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
    </repository>
  </repositories>
```

And add the ErisCasper.java dependency.
Snapshot versions can be found [here](https://oss.sonatype.org/#nexus-search;quick~ErisCasper.java).

```xml
  <dependency>
    <groupId>com.github.princesslana</groupId>
    <artifactId>ErisCasper.java</artifactId>
    <version>LATEST.SNAPSHOT.VERSION</version>
  </dependency>
```
