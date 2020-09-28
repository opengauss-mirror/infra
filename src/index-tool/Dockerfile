FROM java:8
MAINTAINER qwx557901<qinwei23@huawei.com>
RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
WORKDIR /usr/local/
COPY index-tool.jar /usr/local/index-tool.jar
COPY updateDoc.sh /opengauss/updateDoc.sh
RUN apt-get install -y git
RUN chmod +x /opengauss/updateDoc.sh
EXPOSE 8080
ENTRYPOINT ["java","-jar","index-tool.jar","--spring.config.location=/opengauss/application.yaml"]
