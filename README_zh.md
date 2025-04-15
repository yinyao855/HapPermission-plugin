# ArkPermission

## 介绍
ArkPermission 是一个基于 ArkAnalyzer 的权限分析工具，旨在帮助开发者和审核人员快速分析鸿蒙应用的权限使用情况，并检测应用是否滥用权限。

## 使用
在项目文件夹下执行`npm install`安装依赖，还需要修改`arkpermission_config.json`文件的内容，然后通过以下命令进行使用
```shell
npx ts-node ./src/main/ts/entry/main.ts --app [app_path] --sdk [sdk_path] --output [output_path] --format excel
```
**示例**
```shell
npx ts-node ./src/main/ts/entry/main.ts --app /Users/yaoyin/DevEcoStudioProjects/FormHost --sdk /Users/yaoyin/Library/OpenHarmony/Sdk/14/ets --output . --format excel
```

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=yinyao855/arkpermission&type=Date)](https://www.star-history.com/#yinyao855/arkpermission&Date)