

## 如何查看两个文件夹差异
diff -r dir dir2 

## 如何查看哪些文件是大文件

添加git配置

```
vim ~/.gitconfig

[alias]
    big-files = !"git rev-list --objects --all \
                 | git cat-file --batch-check='%(objecttype) %(objectname) %(objectsize) %(rest)' \
                 | sed -n 's/^blob //p' \
                 | sort -nk2 \
                 | cut -c 1-12,41- \
                 | $(command -v gnumfmt || echo numfmt) --field=2 --to=iec-i --suffix=B --padding=7 --round=nearest"
    blob-find = "!f() { \
        obj_name="$1"; \
        shift; \
        git log --pretty=format:'%T %h %s' \
        | while read tree commit subject ; \
        do \
            if git ls-tree -r $tree | grep -q "$obj_name" ; \
            then \
                echo $commit "$subject"; \
                git --no-pager branch -a --contains $commit ; \
            fi; \
        done; \
        }; f"
    sync-master = !"git fetch upstream;git checkout master;git merge upstream/master"
```

执行git命令获取文件大小
```
git big-files

a928144ef3d5  154MiB dependency/esdk_obs_api/huaweicloud-sdk-c-obs-3.19.9.3.tar.gz
173454a7023f  397MiB dependency/llvm/llvm-10.0.0.src.tar
```

## 如何大文件同步（大于100M）

1、安装git lfs
```
apt-get install git-lfs
```

2、添加big-file文件，git lfs会在工程目录下生成一个gitattributes
```
git lfs track "xxx/big-file"
```

3、提交代码
```
git add .
git commit -a
git push
```