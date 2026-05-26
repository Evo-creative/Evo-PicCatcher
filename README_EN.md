[简体中文](./README.md)

# Evo-PicCatcher

Project repository: [Evo-PicCatcher](https://github.com/Evo-creative/Evo-PicCatcher)

> **Note**: The Xposed module repository only publishes Releases and does not host source code.  
> This project is a deeply refactored and feature-extended version of [Mingyueyixi/PicCatcher](https://github.com/Mingyueyixi/PicCatcher).

## Introduction

Evo-PicCatcher is an Android image auto-capturing tool.  
Think of it as an “image funnel” — it hooks into the App rendering pipeline through the Xposed framework and automatically saves images displayed inside Apps to your device.

## Supported Capture Methods

To ensure images can be captured from different Apps, multiple interception methods are provided (all can be enabled or disabled freely in settings):

* **Standard image library interception (low overhead, recommended)**:
    * **Popular frameworks**: Supports mainstream image loading libraries such as Glide, Coil, Fresco, and Picasso.
    * **System native components**: Intercepts system image decoding (Bitmap) and display components (ImageView).
    * **Network & files**: Directly extracts images from App network requests or local file reads.

* **Modern framework adaptation (medium impact)**:
    * **Web images**: Supports capturing images inside webpages (WebView).
    * **Cross-platform frameworks**: Special adaptations for modern App frameworks such as **Flutter**, **Jetpack Compose**, **React Native**, and Litho.

* **Low-level rendering interception (strong mode, fallback solution)**:
    * **Rendering engines**: Directly intercepts low-level drawing instructions (Canvas, Skia).
    * **Screen rendering**: Monitors screen rendering processes (Surface, RenderNode, HardwareRenderer).  
      If it appears on the screen, there is a chance it can be captured.

## Features

- **Smart presets**: Provides quick “High / Medium / Low” presets for easy configuration.
- **Automatic deduplication**: Identical images will not be saved repeatedly, saving storage space.
- **Smooth performance**: Image saving is performed asynchronously in the background without affecting normal App usage.
- **Prevent gallery spam**: Supports generating `.nomedia` files to prevent captured images from flooding the system gallery (can be disabled in settings).

## Usage

1. **Requirements**:
    * Requires **LSPosed**
    * Device must have **Root access**

2. **Activate the module**:
    * Enable “PicCatcher” inside LSPosed
    * Select the target Apps you want to capture images from

3. **Configure interception methods**:
    * Open the “PicCatcher” App → Settings
    * It is recommended to try the “Low overhead” preset first
    * If images cannot be captured, enable “Medium impact” or “High impact” modes

4. **View captured images**:
    * Default save path:
      `Pictures/PicCatcher`

## Screenshots

| Home | Settings | Settings (Interception Switches) | App Presets |
|--------|--------|--------|--------|
| ![Home](./images/mainpage.png) | ![Settings](./images/settings.png) | ![Settings](./images/settings_full.png) | ![Presets](./images/preset.png)|

## Privacy & Security

* **Local processing only**: All image capturing and saving is performed locally on your device. No upload functionality exists.
* **No extra traffic**: The module only intercepts already-loaded data and does not generate additional network traffic.

---

If you have feature suggestions or discover Apps whose images cannot be captured, feel free to submit an [Issue](https://github.com/Evo-creative/Evo-PicCatcher/issues).

## License & Authorization

This project has been officially authorized by the original author **Mingyueyixi** and is licensed under **GPL-v3.0**.
