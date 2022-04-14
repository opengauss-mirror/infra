#!/bin/bash
if [ -d "/usr/local/docs" ]; then
  rm -rf /usr/local/docs/source/*
  rm -rf /usr/local/docs/target/*
fi
mkdir -p /usr/local/docs/source
mkdir -p /usr/local/docs/target/events/zh/events
mkdir -p /usr/local/docs/target/events/en/events
mkdir -p /usr/local/docs/target/news/zh/news
mkdir -p /usr/local/docs/target/news/en/news
mkdir -p /usr/local/docs/target/blogs/zh/
mkdir -p /usr/local/docs/target/blogs/en/
cd /usr/local/docs/source
git clone https://gitee.com/opengauss/website.git
cp -r ./website/content/zh/events/* ../target/events/zh/events
cp -r ./website/content/en/events/* ../target/events/en/events
cp -r ./website/content/zh/news/* ../target/news/zh/news
cp -r ./website/content/en/news/* ../target/news/en/news
git clone https://gitee.com/opengauss/blog.git
cp -r ./blog/content/zh/* ../target/blogs/zh
cp -r ./blog/content/en/* ../target/blogs/en
git clone https://gitee.com/opengauss/docs.git
cd ./docs
versions=(
  "1.0.0"
  "1.0.1"
  "1.1.0"
  "2.0.0"
  "2.0.1"
  "2.1.0"
  "3.0.0"
)
for r in $(git branch -r --list "origin/*"); do
  b=${r##*origin/}
  if [[ "${versions[@]}" =~ $b ]]; then
    git checkout $b
    mkdir -p /usr/local/docs/target/docs/$b/zh/
    mkdir -p /usr/local/docs/target/docs/$b/en/
    cp -r ./content/zh/* /usr/local/docs/target/docs/$b/zh/
    cp -r ./content/en/* /usr/local/docs/target/docs/$b/en/
  fi
done
