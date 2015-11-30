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
```
<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender"/>
```
- Simple Property
```
<appender name="FILE" class="ch.qos.logback.core.FileAppender">
  <file>log/file.log</file>
</appender>
```
- Complex Property
```
<appender name="FILE" class="ch.qos.logback.core.FileAppender">
  <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
    <pattern>%level - %msg%n</pattern>
  </encoder>
</appender>
```
- Param
```
<appender name="FILE" class="ch.qos.logback.core.FileAppender">
  <param name="file" value="log/file.log"/>
</appender>
```
- Appender Reference
```
<appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
  <appender-ref ref="FILE"/>
</appender>

<appender name="FILE" class="ch.qos.logback.core.FileAppender">
  <file>log/file.log</file>
  <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
    <pattern>%level - %msg%n</pattern>
  </encoder>
</appender>
```
- Spring Property Placeholder
```
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

<appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
  <appender-ref ref="${appender.name}"/>
</appender>

<appender name="FILE" class="${appender.class}">
  <file>${appender.filepath}</file>
  <param name="${appender.prop.name}" value="${appender.prop.value}"/>
  <encoder class="${appender.encoder.class}">
    <pattern>${appender.encoder.pattern}</pattern>
  </encoder>
</appender>
```
- Spring Expression
```
<appender name="FILE" class="ch.qos.logback.core.FileAppender">
  <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
    <pattern>#{T(com.company.MyDefaults).getPattern()}</pattern>
  </encoder>
</appender>
```

### Not Supported
- Default Class for Complex Property
```
<appender name="FILE" class="ch.qos.logback.core.FileAppender">
  <encoder> <!-- Does NOT auto-resolve to ch.qos.logback.classic.encoder.PatternLayoutEncoder -->
    <pattern>%level - %msg%n</pattern>
  </encoder>
</appender>
```
- Spring Type Conversion (`PropertyEditor`/`ConversionService`/`TypeConverter`)
- Sifting Appender