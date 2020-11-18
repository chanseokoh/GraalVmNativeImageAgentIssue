# GraalVM Native Image Agent Issue Demo

```
$ which java
/home/chanseok/graalvm/bin/java
$ java -version
openjdk version "11.0.9" 2020-10-20
OpenJDK Runtime Environment GraalVM CE 20.3.0 (build 11.0.9+10-jvmci-20.3-b06)
OpenJDK 64-Bit Server VM GraalVM CE 20.3.0 (build 11.0.9+10-jvmci-20.3-b06, mixed mode, sharing)
$ echo $JAVA_HOME
/home/chanseok/graalvm
$
$ git clone https://github.com/chanseokoh/GraalVmNativeImageAgentIssue.git
$ cd GraalVmNativeImageAgentIssue
$ mvn clean package dependency:copy-dependencies
$ java -agentlib:native-image-agent=config-output-dir=/tmp/native-image-config \
    -cp target/dependency/*:target/native-image-agent-issue-1.jar \
    com.example.JacksonIssueDemo
1234
1234
$ native-image --static --no-fallback --no-server \
    -H:ConfigurationFileDirectories=/tmp/native-image-config \
    -cp target/dependency/*:target/native-image-agent-issue-1.jar \
    com.example.JacksonIssueDemo
$ ./com.example.jacksonissuedemo
Exception in thread "main" com.fasterxml.jackson.databind.JsonMappingException: Cannot set final field: com.example.JacksonIssueDemo$JsonTemplate.finalValue. Enable by specifying "allowWrite" for this field in the reflection configuration.
 at [Source: (String)"{"finalValue": 1234}"; line: 1, column: 16] (through reference chain: com.example.JacksonIssueDemo$JsonTemplate["finalValue"])
        at com.fasterxml.jackson.databind.deser.SettableBeanProperty._throwAsIOE(SettableBeanProperty.java:623)
        at com.fasterxml.jackson.databind.deser.SettableBeanProperty._throwAsIOE(SettableBeanProperty.java:611)
        at com.fasterxml.jackson.databind.deser.impl.FieldProperty.deserializeAndSet(FieldProperty.java:152)
        at com.fasterxml.jackson.databind.deser.BeanDeserializer.vanillaDeserialize(BeanDeserializer.java:293)
        at com.fasterxml.jackson.databind.deser.BeanDeserializer.deserialize(BeanDeserializer.java:156)
        at com.fasterxml.jackson.databind.ObjectMapper._readMapAndClose(ObjectMapper.java:4526)
        at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3468)
        at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3436)
        at com.example.JacksonIssueDemo.main(JacksonIssueDemo.java:14)
Caused by: java.lang.IllegalAccessException: Cannot set final field: com.example.JacksonIssueDemo$JsonTemplate.finalValue. Enable by specifying "allowWrite" for this field in the reflection configuration.
        at java.lang.reflect.Field.set(Field.java:780)
        at com.fasterxml.jackson.databind.deser.impl.FieldProperty.deserializeAndSet(FieldProperty.java:150)
        ... 6 more
$
$ # Now manually add "allowWrite" reflection configuration to fix this issue.
$ cat fix.patch
--- a/reflect-config.json       2020-11-18 15:49:44.605510971 -0500
+++ b/reflect-config.json       2020-11-18 15:39:38.432507858 -0500
@@ -3,7 +3,10 @@
   "name":"com.example.JacksonIssueDemo$JsonTemplate",
   "allDeclaredFields":true,
   "allDeclaredMethods":true,
-  "allDeclaredConstructors":true
+  "allDeclaredConstructors":true,
+  "fields":[
+    {"name":"finalValue", "allowWrite": true}
+  ]
 },
 {
   "name":"com.fasterxml.jackson.databind.ext.Java7HandlersImpl",
$ patch -p1 /tmp/native-image-config/reflect-config.json < fix.patch
patching file /tmp/native-image-config/reflect-config.json
$ native-image --static --no-fallback --no-server \
    -H:ConfigurationFileDirectories=/tmp/native-image-config \
    -cp target/dependency/*:target/native-image-agent-issue-1.jar \
    com.example.JacksonIssueDemo
$ # Works fine.
$ ./com.example.jacksonissuedemo
1234
1234
$
```
