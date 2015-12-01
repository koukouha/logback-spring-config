Logback Spring Config
---------------------
Extends [`Logback Extensions :: Spring`](https://github.com/qos-ch/logback-extensions/wiki/Spring) to allow moving appender XML configuration from `logback.xml` to Spring XML without much change, e.g. from:

```xml
<!-- logback.xml -->
<configuration>

    <appender name="consoleAppender" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%date %-5level [%thread] %logger{36} %m%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="consoleAppender"/>
    </root>

</configuration>
```

to

```xml
<!-- logback.xml -->
<configuration>

    <appender name="consoleAppender" class="ch.qos.logback.ext.spring.DelegatingLogbackAppender"/>

    <root level="INFO">
        <appender-ref ref="consoleAppender"/>
    </root>

</configuration>
```
```xml
<!-- Spring XML -->
<beans ... xsi:schemaLocation="... http://logback.qos.ch logback-lenient.xsd">

    <appender name="consoleAppender" class="ch.qos.logback.core.ConsoleAppender" xmlns="http://logback.qos.ch">
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>%date %-5level [%thread] %logger{36} %m%n</pattern>
        </encoder>
    </appender>

</beans>
```

### Supported
- Appender
```xml
<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender" xmlns="http://logback.qos.ch"/>
```
- Simple Property
```xml
<appender name="FILE" class="ch.qos.logback.core.FileAppender" xmlns="http://logback.qos.ch">
  <file>log/file.log</file>
</appender>
```
- Complex Property
```xml
<appender name="FILE" class="ch.qos.logback.core.FileAppender" xmlns="http://logback.qos.ch">
  <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
    <pattern>%level - %msg%n</pattern>
  </encoder>
</appender>
```
- Param
```xml
<appender name="FILE" class="ch.qos.logback.core.FileAppender" xmlns="http://logback.qos.ch">
  <param name="file" value="log/file.log"/>
</appender>
```
- Appender Reference
```xml
<appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender" xmlns="http://logback.qos.ch">
  <appender-ref ref="FILE"/>
</appender>

<appender name="FILE" class="ch.qos.logback.core.FileAppender" xmlns="http://logback.qos.ch">
  <file>log/file.log</file>
  <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
    <pattern>%level - %msg%n</pattern>
  </encoder>
</appender>
```
- Spring Property Placeholder
```xml
<bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
  <property name="properties">
    <value>
      appender.name=FILE
      appender.class=ch.qos.logback.core.FileAppender
      appender.filepath=log/file.log
      appender.prop.name=prudent
      appender.prop.value=true
      appender.encoder.class=ch.qos.logback.classic.encoder.PatternLayoutEncoder
      appender.encoder.pattern=%level - %msg%n
    </value>
  </property>
</bean>

<appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender" xmlns="http://logback.qos.ch">
  <appender-ref ref="${appender.name}"/>
</appender>

<appender name="FILE" class="${appender.class}" xmlns="http://logback.qos.ch">
  <file>${appender.filepath}</file>
  <param name="${appender.prop.name}" value="${appender.prop.value}"/>
  <encoder class="${appender.encoder.class}">
    <pattern>${appender.encoder.pattern}</pattern>
  </encoder>
</appender>
```
- Spring Expression
```xml
<appender name="FILE" class="ch.qos.logback.core.FileAppender" xmlns="http://logback.qos.ch">
  <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
    <pattern>#{T(com.company.MyDefaults).getPattern()}</pattern>
  </encoder>
</appender>
```

### Not Supported
- Default Class for Complex Property
```xml
<appender name="FILE" class="ch.qos.logback.core.FileAppender" xmlns="http://logback.qos.ch">
  <encoder> <!-- Error - does NOT auto-resolve to ch.qos.logback.classic.encoder.PatternLayoutEncoder -->
    <pattern>%level - %msg%n</pattern>
  </encoder>
</appender>
```
- Spring Type Conversion (`PropertyEditor`/`ConversionService`/`TypeConverter`)
- Conditionals (`if`/`then`/`else`)
- Sifting Appender

Trivia
======
- You can replace `appender-ref` with nested appender, e.g from:
```xml
<appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender" xmlns="http://logback.qos.ch">
  <appender-ref ref="FILE"/>
</appender>

<appender name="FILE" class="ch.qos.logback.core.FileAppender" xmlns="http://logback.qos.ch">
  <file>log/file.log</file>
  <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
    <pattern>%level - %msg%n</pattern>
  </encoder>
</appender>
```
to
```xml
<appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender" xmlns="http://logback.qos.ch">
  <appender name="FILE" class="ch.qos.logback.core.FileAppender">
    <file>log/file.log</file>
    <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
      <pattern>%level - %msg%n</pattern>
    </encoder>
  </appender>
</appender>
```
NOTE: Seems like you can also do this in `logback.xml`, but it won't pick up the nested appender's `name` attribute (not a problem if you don't need it?).
- Share encoder configuration by creating a prototype bean, e.g. instead of:
```xml
<appender name="FILE" class="ch.qos.logback.core.FileAppender" xmlns="http://logback.qos.ch">
  <file>log/file.log</file>
  <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
    <immediateFlush>false</immediateFlush>
    <pattern>%level - %msg%n</pattern>
  </encoder>
</appender>

<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender" xmlns="http://logback.qos.ch">
  <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
    <immediateFlush>false</immediateFlush>
    <pattern>%level - %msg%n</pattern>
  </encoder>
</appender>
```
you can have this:
```xml
<bean id="myEncoder" class="ch.qos.logback.classic.encoder.PatternLayoutEncoder"
    scope="prototype"> <!-- So each appender get their own individual instance -->
  <property name="immediateFlush" value="false"/>
  <property name="pattern" value="%level - %msg%n"/>
</bean>

<appender name="FILE" class="ch.qos.logback.core.FileAppender" xmlns="http://logback.qos.ch">
  <file>log/file.log</file>
  <encoder>#{myEncoder}</encoder>
</appender>

<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender" xmlns="http://logback.qos.ch">
  <encoder>#{myEncoder}</encoder>
</appender>
```
