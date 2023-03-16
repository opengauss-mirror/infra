# infra

#### 介绍hahaha

这个仓库包含用于openGauss社区测试和自动化需求的工具和配置文件。

#### 编译
```
$ mvn clean install package -Dmaven.test.skip
```

#### 生成镜像

```
$ cp target/index-tool.jar  .

$ docker build -t index-tool:v1.0.5 .
```

### 运行
```
$ docker run  -v /local_path/application.yaml:/opengauss/application.yaml  -d  index-tool:v1.0.5
```


## 上传镜像 (可选)
```
$ docker tag index-tool:v1.0.5 swr.ap-southeast-1.myhuaweicloud.com/opengauss/index-tool:v1.0.5
$ docker push swr.ap-southeast-1.myhuaweicloud.com/opengauss/index-tool:v1.0.5
```
