### 现在openGauss已经完成代码托管平台的切换（https://gitcode.com/opengauss）。
### 对于Gitee平台，完成如下操作：
    1. openGauss组织设置公告：通知开发者和用户关于openGauss代码托管平台迁移的事宜；
    2. openGauss组织所有代码仓状态更新至“暂停”，关闭在线编辑功能，关闭新增、修改PR和ISSUE功能；
    3. openGauss组织所有代码仓 README.md 新增迁移通知，代码仓根目录新增空文件夹已提示迁移动作；
### 对于GitCode平台，完成如下操作：
    1. 所有代码仓、PR、ISSUE、评论数据同步至GitCode并完成一致性验证
    2. ISSUE标签所有字段同步至Gitcode看板（模板看板：https://gitcode.com/org/opengauss/kanban/7）
    3. 机器人、门禁、构建已经完成GitCode上线、测试；
    4. 官网、文档、CLA、账号等配套服务的上线并验证OK；
    5. 整理收集到的仓库成员信息，同步至tc仓sig-info.yaml文件（https://gitcode.com/opengauss/tc），完成成员权限配置；

### 希望推动社区开发者进一步完成：
    1. 引导开发者在GitCode平台账号注册、个人信息迁移同意书的签署；
    2. 维护openGauss各个SIG组信息，并按照既定规则填写至tc仓（https://gitcode.com/opengauss/tc）sig-info.yaml文件
    3. 针对从Gitee同步到GitCode平台的未合入PR，需要close后重新提交（因为在Gitcode平台没有PR源分支）；

如在GitCode平台遇到使用问题，欢迎随时联系我们支撑。感谢^_^