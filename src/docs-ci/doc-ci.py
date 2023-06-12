# -*- coding: utf-8 -*-
# @Time    : 2023/4/21 15:16
# @Author  : Tom_zc
# @FileName: doc-ci.py
# @Software: PyCharm
import click
import requests
import time
import re
import sys
import traceback
from functools import wraps


class Config:
    reg = r"(https:\/\/|http:\/\/|ftp:\/\/)([\w\-\.@?^=%&amp;:\!/~\+#]*[\w\-\@?^=%&amp;/~\+#])?"
    ignore_file_suffix = [
        ".github", ".gitignore", ".git", ".png", ".jpg", ".woff2", ".woff", ".svg", ".gif", ".JPG"
    ]
    pr_info_url = "https://gitee.com/opengauss/docs/pulls/{}.diff"
    header = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:83.0) Gecko/20100101 Firefox/83.0"}


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


def extract_url(content):
    url_list = re.findall(Config.reg, content)
    new_url_list = list()
    for url in url_list:
        url = "".join(url)
        print("check:{}".format(url))
        if url in [r"http://", r"https://"]:
            continue
        new_url_list.append(url)
    return new_url_list


@func_retry(tries=3, delay=2)
def request_pr_info(url):
    ret = requests.get(url, headers=Config.header, timeout=(30, 30))
    if not str(ret.status_code).startswith("2") and not str(ret.status_code).startswith("3"):
        raise Exception("request pr info failed: {}--->{}".format(url, ret.status_code))
    else:
        return ret.content


def parse_pr_info(content):
    url_list = list()
    content = content.decode("utf-8")
    content_list = content.split("diff --git")
    for content_temp in content_list:
        path = content_temp.split("\n", maxsplit=1)[0].split(r" ")[-1][1:]
        if path.startswith(r"b/content"):
            for ignore_file in Config.ignore_file_suffix:
                if ignore_file in path:
                    break
            for line in content_temp.split("\n"):
                if line.startswith(r"@@"):
                    continue
                elif line.startswith(r"-"):
                    continue
                url_list.extend(extract_url(line))
    return list(set(url_list))


def check_url(url_list):
    failure_list = list()
    for url in url_list:
        ret = None
        try:
            ret = requests.head(url, headers=Config.header, timeout=(30, 30))
            if not str(ret.status_code).startswith("2") and not str(ret.status_code).startswith("3"):
                ret.close()
                ret = requests.get(url, headers=Config.header, timeout=(30, 30))
                if not str(ret.status_code).startswith("2") and not str(ret.status_code).startswith("3"):
                    raise Exception("find the dead link:{}--->{}".format(url, ret.status_code))
                else:
                    print("check valid url:{}".format(url))
            else:
                print("check valid url:{}".format(url))
        except Exception as e:
            print("check_url--->url:{}, e:{}".format(url, e))
            failure_list.append(url)
        finally:
            try:
                if ret:
                    ret.close()
            except Exception as e:
                print(e)
    return failure_list


def worker(pr_id):
    print("----------------1.start to check pr-----------------------------")
    try:
        url = Config.pr_info_url.format(pr_id)
        pr_content = request_pr_info(url)
        url_list = parse_pr_info(pr_content)
        failed_list = check_url(url_list)
        return failed_list
    except Exception as e:
        raise RuntimeError("doc-ci--->pr_id: {}, e:{}, detail:{}".format(pr_id, str(e), traceback.format_exc()))


def print_content(failed_list):
    print("----------------2.Start checking your pr for invalid links-----------------")
    for url in failed_list:
        content = "Check that your pr contains invalid links：{}\n".format(url)
        print(content)


@click.command()
@click.option("--pr_id", help="the pr_id of git")
def main(pr_id):
    if not pr_id:
        raise RuntimeError("invalid pr_id")
    start_time = time.time()
    failed_list = worker(pr_id)
    print_content(failed_list)
    end_time = time.time()
    print("----------------spend time：{}-----".format(end_time - start_time))
    if failed_list:
        sys.exit(1)


if __name__ == '__main__':
    main()
