# 安卓股票分析软件 — 项目提示词

## 项目概述

开发一个 Android 原生股票分析 App，支持实时行情、K 线图、技术指标分析、自选股管理，并通过 GitHub Actions 自动编译打包 APK。

---

## 技术栈

| 层级 | 技术选型 |
|------|----------|
| 语言 | **Kotlin** |
| UI | **Jetpack Compose** + Material 3 |
| 架构 | **MVVM** + Clean Architecture |
| 网络 | **Retrofit** + OkHttp |
| 数据 | **Room** 本地数据库 |
| 图表 | 自定义 Canvas 绘制 K 线 |
| DI | **Hilt** 依赖注入 |
| 构建 | **Gradle Kotlin DSL** |
| CI/CD | **GitHub Actions** 自动编译 |

---

## 功能模块

### 1. 行情列表
- 沪深 A 股、港股、美股实时数据
- 涨跌幅、成交量、换手率排序
- 支持搜索股票代码/名称
- 下拉刷新、分页加载

### 2. K 线图
- 日/周/月/分钟级 K 线
- 十字光标跟随显示 OHLC
- 缩放平移手势
- 分时图叠加

### 3. 技术指标
- **MA** (移动均线: 5/10/20/60)
- **MACD** (平滑异同移动平均线)
- **KDJ** (随机指标)
- **RSI** (相对强弱指标)
- **BOLL** (布林带)
- 指标参数可自定义

### 4. 自选股
- 添加/删除自选
- 自选分组管理
- 桌面向导小组件

### 5. 详情页
- 公司基本面 (PE/PB/市值)
- 分时走势 + K 线切换
- 五档盘口
- 资金流向

### 6. 数据来源 (免费 API)
- **新浪财经 API**: `hq.sinajs.cn`
- **腾讯财经 API**: `qt.gtimg.cn`
- 本地缓存减少请求频率
- 非交易日使用缓存数据

---

## 项目结构

```
StockAnalyzer/
├── app/
│   ├── src/main/
│   │   ├── java/com/stock/analyzer/
│   │   │   ├── App.kt                    # Application 入口
│   │   │   ├── MainActivity.kt
│   │   │   ├── di/                        # Hilt 模块
│   │   │   ├── data/
│   │   │   │   ├── local/                 # Room DAO / Entity
│   │   │   │   ├── remote/                # Retrofit API 接口
│   │   │   │   ├── repository/            # 仓库层
│   │   │   │   └── model/                 # 数据模型
│   │   │   ├── domain/
│   │   │   │   ├── usecase/               # 业务用例
│   │   │   │   └── model/                 # 领域模型
│   │   │   └── ui/
│   │   │       ├── navigation/            # 导航图
│   │   │       ├── market/                # 行情列表
│   │   │       ├── detail/                # 股票详情
│   │   │       ├── kline/                 # K 线图表
│   │   │       ├── watchlist/             # 自选股
│   │   │       └── common/               # 通用组件
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts                       # 根构建文件
├── settings.gradle.kts
├── gradle.properties
├── gradle/
│   ├── wrapper/
│   └── libs.versions.toml                 # 版本目录
└── .github/
    └── workflows/
        └── build.yml                      # GitHub Actions
```

---

## GitHub Actions CI/CD (`build.yml`)

```yaml
name: Build Android APK

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]
  workflow_dispatch:  # 手动触发

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
      - name: Cache Gradle
        uses: actions/cache@v4
        with:
          path: ~/.gradle/caches
          key: gradle-${{ hashFiles('**/*.gradle*', '**/libs.versions.toml') }}
      - name: Grant execute permission
        run: chmod +x gradlew
      - name: Build APK (Debug)
        run: ./gradlew assembleDebug
      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: stock-analyzer-debug
          path: app/build/outputs/apk/debug/*.apk
```

---

## 开发阶段

| 阶段 | 内容 | 预计工时 |
|------|------|----------|
| **P0** 项目脚手架 | Gradle、Hilt、Navigation 搭建 | 1 天 |
| **P1** 数据层 | API 接入、Room 建表、Repository | 2 天 |
| **P2** 行情列表 | 数据展示、搜索、排序、刷新 | 2 天 |
| **P3** K 线图 | Canvas 自定义绘制、手势交互 | 3 天 |
| **P4** 技术指标 | MA/MACD/KDJ/RSI/BOLL 计算与绘制 | 3 天 |
| **P5** 自选股 | 增删改查、Widget | 1 天 |
| **P6** 详情页 | 基本面、分时图、盘口 | 2 天 |
| **P7** 优化 | 缓存策略、错误处理、ProGuard | 1 天 |
| **P8** CI/CD | GitHub Actions 编译、发布 | 0.5 天 |
| **合计** | | **~15 天** |

---

## 关键 API 示例

### 新浪实时行情
```
https://hq.sinajs.cn/list=sh600519,sz000001
// 返回: var hq_str_sh600519="贵州茅台,1700.00,1698.00,1712.00,1720.00,1695.00,..."
```

### 腾讯 K 线数据
```
https://web.ifeng.com/web/stock/kline?code=sh600519&type=day&count=100
```

---

## 下一步

确认以上方案后，我会依次生成：
1. 完整的 Gradle 构建脚本 (`libs.versions.toml`, `build.gradle.kts`)
2. 数据层代码 (API 接口、Room Entity、Repository)
3. UI 层代码 (Compose 页面、ViewModel)
4. K 线图自定义 View
5. 技术指标计算引擎
6. GitHub Actions 工作流

**是否继续？**
