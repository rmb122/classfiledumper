## Class File Dumper

利用 java agent 在运行时对类文件进行转储

编译
```shell
mvn package -DskipTests
```

运行
```shell
# 列出当前可以 attach 的 jvm 实例
java -jar target/classfiledumper-1.0-SNAPSHOT.jar list

# 进行 dump
java -jar target/classfiledumper-1.0-SNAPSHOT.jar dump [-p interfacesOrParents] attachTarget packagePattern outputBaseDir
```

e.g.
```shell
java -jar target/classfiledumper-1.0-SNAPSHOT.jar dump -p javax.servlet.Filter -p javax.servlet.Servlet 114514 '.*' /tmp/dumps
java -jar target/classfiledumper-1.0-SNAPSHOT.jar dump 1919810 'com\.ctf\.problems\..*' /tmp/dumps
```

注意: 如果是老版本 (<= 8) java, 需要带上额外参数
```
-Xbootclasspath/a:${JAVA_HOME}/lib/tools.jar
```