[English](./README_EN.md)

[简体中文](./README.md)

# Evo-PicCatcher（图片捕手）

本项目仓库地址：[Evo-PicCatcher](https://github.com/Evo-creative/Evo-PicCatcher)

> **注意**：Xposed 模块仓库仅发布 Release哦，不提交源码的。本项目基于 [Mingyueyixi/PicCatcher](https://github.com/Mingyueyixi/PicCatcher) 进行深度重构与功能扩展。(说是基于，实际上就是重写了一遍( ゜- ゜)

## 项目介绍

Evo-PicCatcher 是一个十分强大的 Android 图片捕手。使用LSPosed框架（注意不支持其他Xposed框架的），实现对目标 App 运行时显示的图片（包括网络图、缓存图、自绘图等）进行实时捕捉与导出。

## 强大在哪?

- **用了好多拦截方式**：支持从底层图形引擎（Skia, Canvas）到现代 UI 框架（Flutter, Compose, WebView）以及主流图片加载库（Glide, Coil, Fresco）的全链路拦截。
- **能自己设置使用哪些方式**：提供“高影响/底层引擎”、“中影响/框架容器”、“低开销/标准组件”三级拦截模式，支持一键快捷开启，平衡抓取能力与系统性能（防止卡死的）。
- **写了点更深层的方式**：针对 Flutter 等自绘引擎进行专项适配，通过 Hook JNI 与纹理注册通道，大幅提升复杂场景下的抓取成功率。
- **重写了保存逻辑**：用了Root权限直接执行Linux命令，防止I/O开销太大，用MD5值去重，所有图片保存均在独立线程异步完成，确保不干扰目标 App 的运行流畅度。

## 使用说明

1.  **环境要求**：需要LSPosed框架（别的Xposed框架不行的!），Root权限
2.  **激活模块**：在 LSPosed 中勾选“图片捕手”，并选择目标作用域 App。还要Root权限的。
3.  **配置拦截**：
    *   打开“图片捕手”App -> 进入设置。
    *   根据需求使用**应用预设**开启拦截等级，或手动调整具体开关。
4.  **图片查看**：
    *   **内部保存**：`Android/data/com.evo.piccatcher/files/Pictures`
    *   **外部保存**：`Pictures/PicCatcher`

## 效果展示

| 首页 | 设置 | 设置 (拦截开关) | 应用预设 |
|--------|--------|--------|--------|
| ![首页](./images/mainpage.png) | ![设置](./images/settings.png) | ![设置](./images/settings_full.png) | ![应用预设](./images/preset.png)|

## 隐私与安全说明

*   **本地处理**：所有图片提取、分析与保存流程均在本地完成，模块不具备任何联网上传功能。
*   **无额外流量**：抓取过程是拦截已加载的内存或文件数据，不会产生额外的网络请求。
*   **AI 辅助说明**：本项目的部分底层重构代码由 AI 辅助生成，并经过人工严格 Review 与稳定性测试。

---
如果有新的功能建议或发现拦截盲区，欢迎提交 [Issue](https://github.com/Evo-creative/Evo-PicCatcher/issues)。
