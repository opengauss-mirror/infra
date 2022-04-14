# infra

#### 介绍

这个仓库包含用于openGauss社区测试和自动化需求的工具和配置文件。

#### 编译

mvn clean install package -Dmaven.test.skip


#### 生成镜像

cp target/index-tool.jar  .
docker build -t index-tool:v1.0.5 .

