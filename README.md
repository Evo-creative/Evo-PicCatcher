[English](./README_EN.md)

# Evo-PicCatcher（图片捕手）

本项目仓库地址[Evo-PicCatcher](https://github.com/Evo-creative/Evo-PicCatcher)

本项目基于 `https://github.com/Mingyueyixi/PicCatcher` 修改与扩展。

## 项目介绍

Evo-PicCatcher 是一个用于抓取应用运行过程中所显示图片的工具，适用于调试、分析与自动化等场景。

如果有新的功能建议，欢迎提交 Issue，作者会根据情况进行实现。

### 目前宿主app权限问题暂时未解决，遇到权限问题会将图片保存到外部位置`Pictures/PicCatcher`

## 使用说明
在LSPosed管理器中选择作用域app

当作用域内的app运行时显示图片就会被抓取并保存(目前抓取方式只能覆盖到极大部分内容，请见谅)

内部保存位置为`Android/data/com.evo.piccatcher/files/Pictures`，外部保存位置为`Pictures/PicCatcher`

特殊说明：
- 抓取的图片是显示出来的，所以只显示缩略图时只能抓到缩略图，不能抓到原图
- 抓取是从应用运行中抓取的，不会有任何网络流量产生

## 相较原项目的改进

- 使用 Material Design 3 规范重构用户界面  
- 增加更多图片捕捉方式：
  - ImageDecoder
  - Coil
  - Native
  - FileCatcher
  - RenderNode 拦截
  - Surface 级监控
- 支持将图片保存至应用私有目录（Android/data），避免被系统相册扫描  
- 新增日志功能，方便调试与问题定位

## 效果展示

| 首页 | 设置 |
|--------|--------|
| ![首页](./images/main.png) | ![设置](./images/Settings.png) |


## 注意

本项目的新增代码主要由 AI 辅助生成，请在使用前自行评估其稳定性与安全性。

## 隐私说明

所有图片处理均在本地完成，不会收集或上传任何数据。
