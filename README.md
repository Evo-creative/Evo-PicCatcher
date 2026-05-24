[English](./README_EN.md)

[有活人感的README](./README_h.md)

# Evo-PicCatcher（图片捕手）

本项目仓库地址：[Evo-PicCatcher](https://github.com/Evo-creative/Evo-PicCatcher)

> **注意**：Xposed 模块仓库仅发布 Release，不提交源码。本项目基于 [Mingyueyixi/PicCatcher](https://github.com/Mingyueyixi/PicCatcher) 进行深度重构与功能扩展。

## 项目介绍

Evo-PicCatcher 是一款功能强大的 Android 图片自动化抓取工具。它通过 Xposed 框架深入应用渲染流水线，实现对目标 App 运行时显示的图片（包括网络图、缓存图、自绘图等）进行实时捕捉与导出。

## 功能亮点

- **全方位拦截体系**：支持从底层图形引擎（Skia, Canvas）到现代 UI 框架（Flutter, Compose, WebView）以及主流图片加载库（Glide, Coil, Fresco）的全链路拦截。
- **智能分级预设**：提供“高影响/底层引擎”、“中影响/框架容器”、“低开销/标准组件”三级拦截模式，支持一键快捷开启，平衡抓取能力与系统性能。
- **深度引擎优化**：针对 Flutter 等自绘引擎进行专项适配，通过 Hook JNI 与纹理注册通道，大幅提升复杂场景下的抓取成功率。
- **高效去重与异步导出**：内置智能去重过滤引擎，所有图片保存均在独立线程异步完成，确保不干扰目标 App 的运行流畅度。

## 使用说明

1.  **环境要求**：需安装 **LSPosed** 管理器，且设备已获取 **Root 权限**。
2.  **激活模块**：在 LSPosed 中勾选“图片捕手”，并选择目标作用域 App。
3.  **配置拦截**：
    *   打开“图片捕手”App -> 进入设置。
    *   根据需求使用**快捷预设**开启拦截等级，或手动调整具体开关。
4.  **图片查看**：
    *   **内部保存**：`Android/data/com.evo.piccatcher/files/Pictures`
    *   **外部保存**：`Pictures/PicCatcher`

## 页面展示

| 首页 | 设置 | 设置 (拦截开关) | 应用预设 |
|--------|--------|--------|--------|
| ![首页](./images/mainpage.png) | ![设置](./images/settings.png) | ![设置](./images/settings_full.png) | ![应用预设](./images/preset.png)|

## 隐私与安全说明

*   **本地处理**：所有图片提取、分析与保存流程均在本地完成，模块不具备任何联网上传功能。
*   **无额外流量**：抓取过程是拦截已加载的内存或文件数据，不会产生额外的网络请求。
*   **AI 辅助说明**：本项目的部分底层重构代码由 AI 辅助生成，并经过人工严格 Review 与稳定性测试。

---
如果有新的功能建议或发现拦截盲区，欢迎提交 [Issue](https://github.com/Evo-creative/Evo-PicCatcher/issues)。
