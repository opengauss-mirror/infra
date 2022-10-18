#!/bin/bash
if [ -d "/usr/local/docs" ]; then
  rm -rf /usr/local/docs/source/*
  rm -rf /usr/local/docs/target/*
fi
mkdir -p /usr/local/docs/source/
mkdir -p /usr/local/docs/target/zh/
mkdir -p /usr/local/docs/target/en/


cd /usr/local/docs/source
git clone https://gitee.com/opengauss/website.git
git clone https://gitee.com/opengauss/blog.git


mkdir -p /usr/local/docs/target/zh/events/
mkdir -p /usr/local/docs/target/zh/news/
mkdir -p /usr/local/docs/target/zh/post/
cp -r ./website/content/zh/events/* ../target/zh/events/
cp -r ./website/content/zh/news/* ../target/zh/news/
cp -r ./blog/content/zh/post/* ../target/zh/post/

mkdir -p /usr/local/docs/target/en/events/
mkdir -p /usr/local/docs/target/en/news/
mkdir -p /usr/local/docs/target/en/post/
cp -r ./website/content/en/events/* ../target/en/events/
cp -r ./website/content/en/news/* ../target/en/news/
cp -r ./blog/content/en/post/* ../target/en/post/


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
  "3.1.0"
)
for r in $(git branch -r --list "origin/*"); do
  b=${r##*origin/}
  if [[ "${versions[@]}" =~ $b ]]; then
    git checkout $b
    mkdir -p /usr/local/docs/target/zh/docs/$b/docs
    mkdir -p /usr/local/docs/target/en/docs/$b/docs
    cp -r ./content/zh/docs/* /usr/local/docs/target/zh/docs/$b/docs/
    cp -r ./content/en/docs/* /usr/local/docs/target/en/docs/$b/docs/
  fi
done
