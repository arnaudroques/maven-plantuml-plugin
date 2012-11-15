A maven plugin to generate UML diagrams using PlantUML syntax.

# Usage

To generate images from PlantUML description add following dependency to your pom.xml:

```xml
...
<build>
  <plugins>
    <plugin>
      <groupId>com.github.jeluard</groupId>
      <artifactId>maven-plantuml-plugin</artifactId>
      <version>7940</version>
      <configuration>
        <sourceFiles>
          <directory>${basedir}</directory>
	  <includes>
	    <include>src/main/plantuml/**/*.txt</include>
	  </includes>
        </sourceFiles>
      </configuration>
    </plugin>
  </plugins>
</build>
```

Then execute command:

```
mvn clean com.github.jeluard:maven-plantuml-plugin:generate
```

# Extra configuration options

`outputDirectory` Directory where generated images are generated. Defaults to `${basedir}/target/plantuml`

`outputInSourceDirectory` Whether or not to generate images in same directory as the source file. Defaults to `false`.

`format` Output format. Defaults to `png`.

`verbose` Wether or not to output details during generation. Defaults to `false`.


Released under [Apache 2 license](http://www.apache.org/licenses/LICENSE-2.0.html).
