# 小森课堂项目配置文档
## 1. Nacos 配置
### 1.1 获取镜像
`docker pull nacos/nacos-server:1.4.1`
### 1.2 创建nacos数据库
[nacos.sql](resource/nacos.sql)
### 1.3 创建data目录、conf目录、logs目录
`mkdir /data/nacos/data`
`mkdir /data/nacos/conf`
`mkdir /data/nacos/logs`
### 1.4 nacos链接外部数据库
创建application.properties
``` java
spring.datasource.platform=mysql
db.num=1
db.url.0=jdbc:mysql://数据库地址/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC
db.user=username
db.password=password
# JVM 参数
spring.jvm.xms=512m
spring.jvm.xmx=512m
spring.jvm.xmn=256m
spring.jvm.metaspace=128m
spring.jvm.maxMetaspace=256m
```
### 1.5 启动nacos
```
docker run -p 8848:8848 \
 --name nacos \
 -v /data/nacos/logs:/home/nacos/logs \
 -v /data/nacos/conf/application.properties:/home/nacos/conf/application.properties \
 --privileged=true --restart=always \
 --env MODE=standalone \
 --env SPRING_DATASOURCE_PLATFORM=mysql \
 -d nacos/nacos-server:1.4.1\
```
### 1.6 其它命令
```
docker logs nacos 查看日志
docker stop nacos 关闭
docker start nacos
docker rm nacos 关闭
docker rm -f nacos 强制关闭
```
## 2. Minio 配置
### 2.1 获取镜像
`docker pull minio/minio`
### 2.2 启动minio
`mkdir /data/minio`

9000为api端口，9001为webui端口
`docker run -d   -p 9000:9000   -p 9001:9001   -v /data/minio:/data   minio/minio server --address ":9000" --console-address ":9001" /data`
### 2.3 登录webui
用户名密码都是 minioadmin
### 2.4 其它命令
开机自启
`docker update --restart=always minio`