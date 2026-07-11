构建与运行

模块结构
单模块应用，根 build.gradle.kts 为聚合脚本，应用模块在 app。settings.gradle.kts 声明 rootProject.name 为 wuchang_liuyao，include app，并配置腾讯云与阿里云 maven 镜像加速。

SDK 与语言
minSdk 24，targetSdk 36，compileSdk 36（minorApiLevel 1）。Java 源码与目标兼容 VERSION_11。Kotlin 2.2.10。buildFeatures.compose 开启。
命名空间与 applicationId 均为 site.hanzhe.wuchang_liuyao，versionCode 1，versionName 0.1.0。

依赖版本（集中在 gradle/libs.versions.toml）
agp 9.1.0，kotlin 2.2.10，composeBom 2024.09.00。
androidx lifecycle-runtime-compose 与 lifecycle-viewmodel-compose 均 2.10.0，activity-compose 1.12.4，navigation-compose 2.9.7。
compose 相关（ui、ui-graphics、ui-tooling-preview、material3）由 compose-bom 统一管理版本。
历法库 cn.6tail lunar 1.7.7。
插件 android-application 与 kotlin-compose。
新增依赖走 version catalog，不在模块内硬编码版本。

签名配置
release 签名读取 rootProject 的 local.properties 中 KEYSTORE_PASSWORD、KEY_ALIAS、KEY_PASSWORD，keystore 文件为 app/keystore/hanzhe.keystore。release 未开启 minify，proguard 使用 proguard-android-optimize.txt 加 proguard-rules.pro。local.properties 不入库。

常用命令（项目根目录，Windows 用 gradlew.bat）
调试安装：gradlew.bat installDebug
正式安装：gradlew.bat installRelease
调试打包：gradlew.bat assembleDebug
正式打包：gradlew.bat assembleRelease

源码目录
应用源码 app/src/main，含 AndroidManifest.xml 与 res 资源。
Kotlin 与 Compose 代码在 app/src/main/java/site/hanzhe/wuchang_liuyao，按架构分包。
本地单元测试 app/src/test（JUnit4），仪器测试 app/src/androidTest（AndroidX 与 Compose UI）。
gradle/wrapper 必须保留并提交。

资源
res/values 含 colors.xml、strings.xml、themes.xml。res/drawable 与 mipmap-anydpi-v26 含启动图标。res/xml 含 backup_rules.xml 与 data_extraction_rules.xml。

编码要求
所有代码文件统一 UTF-8 无 BOM，禁止 GBK/ANSI 或乱码。
