# Evo-PicCatcher

This project is a modified and extended version of [PicCatcher](https://github.com/Mingyueyixi/PicCatcher).

## Introduction

Evo-PicCatcher is a tool designed to capture images displayed during app runtime.  
It can be useful for debugging, analysis, and automation purposes.

If you have any feature suggestions, feel free to open an Issue. Contributions and ideas are welcome.

## Improvements over the original project

- Rebuilt UI following Material Design 3 guidelines  
- Added more image capture methods:
  - ImageDecoder  
  - Coil  
  - Native  
- Option to save images to the app's private directory (`Android/data`), preventing them from appearing in gallery apps  
- Added logging functionality for easier debugging and issue tracking  

## 效果展示

| Main | Settings |
|--------|--------|
| ![Main](./images/main.png) | ![Settings](./images/Settings.png) |

## Notice

Some newly added code in this project is generated with the assistance of AI.  
Please evaluate its reliability and security before use.

## Privacy

All image processing is performed locally on the device.  
No data is collected or uploaded.
