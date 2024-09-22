## Class File Dumper

利用 java agent 在运行时对类文件进行转储

编译
```shell
mvn package -DskipTests
```

运行

1. premain 模式

```shell
java '-javaagent:target/classfiledumper-1.0-SNAPSHOT.jar=outputBaseDir[:packagePattern]' -jar target.jar
```

其中 packagePattern 为可选参数, 指定需要 dump 的类名, 如果不指定则默认为 `.*`

e.g.
```shell
java '-javaagent:target/classfiledumper-1.0-SNAPSHOT.jar=/tmp/dumps' -jar target.jar
java '-javaagent:target/classfiledumper-1.0-SNAPSHOT.jar=/tmp/dumps:some\.ctf\.challenges\..*' -jar target.jar
```

2. attach 模式
```shell
# 列出当前可以 attach 的 jvm 实例
java -jar target/classfiledumper-1.0-SNAPSHOT.jar list

# 进行 dump
java -jar target/classfiledumper-1.0-SNAPSHOT.jar dump [-p interfacesOrParents] attachTarget outputBaseDir [packagePattern]
```

相比 premain 模式, 增加 -p 参数, 可以只转储继承该父类/实现该接口的类. 同时可以输入多个 -p 参数, 各个 -p 参数之间为或关系, 同一个 -p 参数内也可以使用 `,` 进行分割, 此时为与关系.

e.g.
```shell
java -jar target/classfiledumper-1.0-SNAPSHOT.jar dump -p javax.servlet.Filter -p javax.servlet.Servlet 114514 /tmp/dumps '.*'
java -jar target/classfiledumper-1.0-SNAPSHOT.jar dump -p javax.servlet.Filter,java.lang.ClassLoader -p javax.servlet.Servlet 1919810 /tmp/dumps '.*'
java -jar target/classfiledumper-1.0-SNAPSHOT.jar dump 1919810 /tmp/dumps 'some\.ctf\.challenges\..*'
```

注意: 如果是老版本 (<= 8) java, attach 时需要带上额外参数, 且在该模式下不建议使用对生产环境使用 `.*` 转储全部类, 有小概率导致 JVM 崩溃
```
-Xbootclasspath/a:${JAVA_HOME}/lib/tools.jar
```