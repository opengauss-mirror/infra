# docker-webrelease

This docker listener the pull request from gitee webhook. and then build docker images and upload to swr.

## Installation

Go to webrelease dir and run docker build command to build images.
```
docker build -t web-release:version .
```

## Quick Start

Run the image

```
docker run -v /var/run/docker.sock:/var/run/docker.sock --name="web-release" -p 8080:8080 -d web-release:version
```

The webhook request url : POST https://${host}:${port}/webhook/build_image
