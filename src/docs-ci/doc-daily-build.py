# -*- coding: utf-8 -*-
# @Time    : 2023/4/24 18:56
# @Author  : Tom_zc
# @FileName: doc-daily-build.py
# @Software: PyCharm
import sys
from functools import wraps

import gevent
from gevent.pool import Pool
from gevent import monkey

monkey.patch_all(thread=False)

import os
import re
import click
import subprocess
import requests
import shutil
import time
import traceback
import yaml

ALL_FAILURE = dict()


class Config:
    cmd = "cd /root && git clone {} > /dev/null"
    path = "/root/{}"
    relatively_path = "/root/"
    reg = r"(https:\/\/|http:\/\/|ftp:\/\/)([\w\-\.@?^=%&amp;:\!/~\+#]*[\w\-\@?^=%&amp;/~\+#])?"
    ignore_file_suffix = [
        ".github", ".gitignore", ".git", ".png", ".jpg", ".woff2", ".woff", ".svg", ".gif", ".JPG"
    ]
    ignore_file_dict = None


def func_retry(tries=3, delay=1):
    def deco_retry(fn):
        @wraps(fn)
        def inner(*args, **kwargs):
            for i in range(tries):
                try:
                    return fn(*args, **kwargs)
                except Exception as e:
                    print("func_retry:{} e:{} traceback: {}".format(fn.__name__, e, traceback.format_exc()))
                    time.sleep(delay)
            else:
                raise RuntimeError("func_retry:{} over tries, failed".format(fn.__name__))

        return inner

    return deco_retry


def load_ignore_url(file_path="ignore_url.yaml", method="load"):
    if Config.ignore_file_dict is None:
        yaml_load_method = getattr(yaml, method)
        with open(file_path, "r", encoding="utf-8") as file:
            doc_list = yaml_load_method(file, Loader=yaml.FullLoader)
        ignore_url_dict = dict()
        for doc_temp in doc_list:
            ignore_url_dict[doc_temp["path"]] = doc_temp["url"]
        Config.ignore_file_dict = ignore_url_dict
    return Config.ignore_file_dict


def prepare_env(filename):
    path = Config.path.format(filename)
    if os.path.exists(path):
        shutil.rmtree(path)


def execute_cmd(cmd, timeout=600):
    try:
        p = subprocess.Popen(cmd, stderr=subprocess.PIPE, stdout=subprocess.PIPE, shell=True, close_fds=True)
        t_wait_seconds = 0
        while True:
            if p.poll() is not None:
                break
            if timeout >= 0 and t_wait_seconds >= (timeout * 100):
                p.terminate()
                return -1, "", "execute_cmd exceeded time {0} seconds in executing: {1}".format(timeout, cmd)
            time.sleep(0.01)
            t_wait_seconds += 1
        out, err = p.communicate()
        ret = p.returncode
        return ret, out, err
    except Exception as e:
        return -1, "", "execute_cmd exceeded raise, e={0}, trace={1}".format(e.args[0], traceback.format_exc())


def parse_file(path):
    with open(path, "r", encoding="utf-8") as f:
        return f.read()


def extract_url(content):
    url_list = re.findall(Config.reg, content)
    new_url_list = list()
    for url in url_list:
        url = "".join(url)
        if url in [r"http://", r"https://"]:
            continue
        new_url_list.append(url)
    return list(set(new_url_list))


def get_url(abs_path, url_list):
    failure_list = list()
    for url in url_list:
        ret = None
        try:
            headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:83.0) Gecko/20100101 Firefox/83.0"}
            ret = requests.head(url, headers=headers, timeout=(30, 30))
            if not str(ret.status_code).startswith("2") and not str(ret.status_code).startswith("3"):
                ret.close()
                ret = requests.get(url, headers=headers, timeout=(30, 30))
                if not str(ret.status_code).startswith("2") and not str(ret.status_code).startswith("3"):
                    raise Exception("find the dead link:{}--->{}".format(url, ret.status_code))
        except Exception as e:
            print("Url--->path:{}, url:{}, e:{}".format(abs_path, url, e))
            failure_list.append(url)
        finally:
            try:
                if ret:
                    ret.close()
            except Exception as e:
                print(e)

    return failure_list


def worker(abs_path):
    relatively_path = abs_path.split(Config.relatively_path)[-1]
    try:
        content = parse_file(abs_path)
        url_list = extract_url(content)
        failure_list = get_url(abs_path, url_list)
        if failure_list:
            ignore_doc_dict = load_ignore_url()
            if relatively_path in ignore_doc_dict.keys():
                failure_list = list(set(failure_list) - set(ignore_doc_dict[relatively_path]))
            if failure_list:
                ALL_FAILURE.update({relatively_path: failure_list})
    except Exception as e:
        print("Task--->path: {}, err:{}".format(relatively_path, e))


@func_retry()
def download_rep(repo_name, repo):
    print("----------------1.start to git clone:{}-----------------".format(repo))
    prepare_env(repo_name)
    cmd = Config.cmd.format(repo)
    code, std_out, std_err = execute_cmd(cmd)
    if code != 0:
        raise RuntimeError(std_err)
    return std_out


def list_file(repo_name, lookup_dir):
    print("----------------2.start to list repo:{}-----------------".format(repo_name))
    path = Config.path.format(repo_name)
    if lookup_dir:
        path = os.path.join(path, lookup_dir)
    file_list = list()
    for dir_path, _, filenames in os.walk(path):
        for filename in filenames:
            abs_path = os.path.join(dir_path, filename)
            is_ignore_file = False
            for file_suffix in Config.ignore_file_suffix:
                if file_suffix in abs_path:
                    is_ignore_file = True
                    break
            if is_ignore_file:
                continue
            file_list.append(abs_path)
    coroutine_pool = Pool(200)
    coroutine_task = [coroutine_pool.spawn(worker, i) for i in file_list]
    gevent.joinall(coroutine_task)


def print_content():
    print("----------------3.start to output invalid url-----------------")
    for path, url_list in ALL_FAILURE.items():
        for url in url_list:
            content = "check dead_link, the path is ：{}, the url is：{}\n".format(path, url)
            print(content)


@click.command()
@click.option("--repo", help="the repository of git", default="https://gitee.com/opengauss/docs.git")
def main(repo):
    start_time = time.time()
    repo_name = repo.split(r"/")[-1].split(".")[0]
    if not len(repo_name) or r".." in repo_name:
        raise RuntimeError("The params of repository is invalid")
    if repo_name == "docs":
        lookup_dir = "content"
    else:
        lookup_dir = ""
    download_rep(repo_name, repo)
    list_file(repo_name, lookup_dir)
    print_content()
    end_time = time.time()
    print("-----spend time：{}-----".format(end_time - start_time))
    if ALL_FAILURE:
        sys.exit(1)


if __name__ == '__main__':
    main()
