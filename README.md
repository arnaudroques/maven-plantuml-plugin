A maven plugin to generate UML diagrams using PlantUML syntax. !http://stillmaintained.com/jeluard/maven-plantuml-plugin.png!

# Usage

To generate images from PlantUML description add following dependency to your pom.xml:

```xml
...
  <dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>${project.artifactId}</artifactId>
    <version>${project.version}</version>
  </dependency>
```

Then execute command:

```
mvn clean com.github.jeluard:maven-plantuml-plugin:generate
```

Released under [Apache 2 license](http://www.apache.org/licenses/LICENSE-2.0.html).
