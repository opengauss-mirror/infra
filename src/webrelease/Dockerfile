FROM golang:1.12.1-alpine3.9 AS builder

WORKDIR $GOPATH/

## Set apk reponsitories to mirrors.ustc.edu.cn
#RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.ustc.edu.cn/g' /etc/apk/repositories

RUN apk update && \
    apk upgrade && \
    apk add --no-cache bash git openssh &&\
    apk add docker &&\
    apk add openssl

COPY . $GOPATH/src/webrelease/

WORKDIR $GOPATH/src/webrelease/

RUN go build main.go

## create log dir
RUN mkdir -p /var/log/web-release/

EXPOSE 8080

ENTRYPOINT ["./main"]
CMD ["-log_dir=/var/log/web-release/"]
