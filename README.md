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

<table>
    <tr>
        <th>Supported</th>
        <th>Not Supported</th>
    </tr>
    <tr>
        <td>
Appender
<pre>
&lt;appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender"/&gt;
</pre>
        </td>
        <td>
Default Class for Property
<pre>
&lt;appender name="FILE" class="ch.qos.logback.core.FileAppender"&gt;
  &lt;encoder&gt; &lt;!-- Does not auto-resolve to ch.qos.logback.classic.encoder.PatternLayoutEncoder --&gt;
    &lt;pattern&gt;%level - %msg%n&lt;/pattern&gt;
  &lt;/encoder&gt;
&lt;/appender&gt;
</pre>
        </td>
    </tr>
    <tr>
        <td>
Simple Property
<pre>
&lt;appender name="FILE" class="ch.qos.logback.core.FileAppender"&gt;
  &lt;file&gt;log/file.log&lt;/file&gt;
&lt;/appender&gt;
</pre>
        </td>
        <td>
            Spring Type Conversion (`PropertyEditor`/`ConversionService`/`TypeConverter`)
        </td>
    </tr>
    <tr>
        <td>
Complex Property
<pre>
&lt;appender name="FILE" class="ch.qos.logback.core.FileAppender"&gt;
  &lt;encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder"&gt;
    &lt;pattern&gt;%level - %msg%n&lt;/pattern&gt;
  &lt;/encoder&gt;
&lt;/appender&gt;
</pre>
        </td>
        <td>
            Sifting Appender
        </td>
    </tr>
    <tr>
        <td>
Param
<pre>
&lt;appender name="FILE" class="ch.qos.logback.core.FileAppender"&gt;
  &lt;param name="file" value="log/file.log"/&gt;
&lt;/appender&gt;
</pre>
        </td>
    </tr>
    <tr>
        <td>
Appender Reference
<pre>
&lt;appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender"&gt;
  &lt;appender-ref ref="FILE"/&gt;
&lt;/appender&gt;

&lt;appender name="FILE" class="ch.qos.logback.core.FileAppender"&gt;
  &lt;file&gt;log/file.log&lt;/file&gt;
  &lt;encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder"&gt;
    &lt;pattern&gt;%level - %msg%n&lt;/pattern&gt;
  &lt;/encoder&gt;
&lt;/appender&gt;
</pre>
        </td>
    </tr>
    <tr>
        <td>
            Spring Property Placeholder
<pre>
&lt;appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender"&gt;
  &lt;appender-ref ref="${appender.name}"/&gt;
&lt;/appender&gt;

&lt;appender name="FILE" class="${appender.class}"&gt;
  &lt;file&gt;${appender.filepath}&lt;/file&gt;
  &lt;param name="${appender.prop.name}" value="${appender.prop.value}"/&gt;
  &lt;encoder class="${appender.encoder.class}"&gt;
    &lt;pattern&gt;${appender.encoder.pattern}&lt;/pattern&gt;
  &lt;/encoder&gt;
&lt;/appender&gt;
</pre>
        </td>
    </tr>
    <tr>
        <td>
Spring Expression
<pre>
&lt;appender name="FILE" class="ch.qos.logback.core.FileAppender"&gt;
  &lt;encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder"&gt;
    &lt;pattern&gt;#{T(com.company.MyDefaults).getPattern()}&lt;/pattern&gt;
  &lt;/encoder&gt;
&lt;/appender&gt;
</pre>
        </td>
    </tr>
</table>
