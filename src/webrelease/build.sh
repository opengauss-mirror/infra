#!/bin/bash

git_url=$1
git_branch=$2
git_dir=$3
image_name=$4
image_tag=$5
swr_region=$6
swr_image_group=$7
ak=$8
sk=$9

echo "##################"
echo "git_url is $git_url"
echo "git_branch is $git_branch"
echo "git_dir is $git_dir"
echo "image_name is $image_name"
echo "image_tag is $image_tag"
echo "swr_region is $swr_region"
echo "swr_image_group is $swr_image_group"
echo "##################"

tmp_dir=/tmp/release
swr_endpoint=swr.${swr_region}.myhuaweicloud.com

##generate long-term docker login command
login_key=`printf ${ak} | openssl dgst -binary -sha256 -hmac ${sk} | od -An -vtx1 | sed 's/[ \n]//g' | sed 'N;s/\n//'`

function clone_code()
{
    mkdir -p ${tmp_dir}
    cd ${tmp_dir}
    git clone ${git_url} -b ${git_branch} ${git_dir}
}

function build_images()
{
    cd ${git_dir}
    docker build -t ${image_name}:${image_tag} .
}

function upload_images()
{
    docker login -u ${swr_region}@${ak} -p ${login_key} ${swr_endpoint}

    docker tag ${image_name}:${image_tag} ${swr_endpoint}/${swr_image_group}/${image_name}:${image_tag}

    docker push ${swr_endpoint}/${swr_image_group}/${image_name}:${image_tag}
}

function  delete_tmp_dir()
{
    if [ -d "${tmp_dir}/${git_dir}" ];then
        cd ${tmp_dir}
        rm -rf ${git_dir}
    fi
}

clone_code;

build_images;

upload_images;

delete_tmp_dir;