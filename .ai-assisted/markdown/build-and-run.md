# 构建与运行

## 工程入口

- 根工程只包含 `:app`，模块配置在 `app/build.gradle.kts`；SDK、applicationId 和版本号以该文件为准。
- 依赖及插件版本唯一来源是 `gradle/libs.versions.toml`，新增依赖使用 version catalog。业务第三方依赖目前只有 lunar-java。
- 插件与依赖仓库由 `settings.gradle.kts` 集中管理并拒绝模块自行声明仓库；现有仓库无法解析新依赖时，应在工程解析边界评估并配置来源。
- Gradle Wrapper 及校验信息在 `gradle/wrapper/`，守护进程工具链为 JDK 21，源码和目标字节码兼容级别为 Java 11。

## 常用命令

在项目根目录使用 Windows Wrapper：

- 调试构建：`.\gradlew.bat assembleDebug`
- 调试安装：`.\gradlew.bat installDebug`
- 正式构建：`.\gradlew.bat assembleRelease`
- 正式安装：`.\gradlew.bat installRelease`

当前仓库没有 `app/src/test` 或 `app/src/androidTest` 测试集；代码变更至少执行与范围匹配的构建验证，领域规则变更应补充可重复验证，默认使用 installDebug 如果没有可用设备则使用 assembleDebug

## 正式签名

- release 从根目录 `local.properties` 读取 `KEYSTORE_PASSWORD`、`KEY_ALIAS`、`KEY_PASSWORD`，并使用本机 `app/keystore/hanzhe.keystore`。
- `local.properties` 与 keystore 均不入库；新环境必须自行提供 Android SDK 路径和签名材料，禁止把密钥值写入文档或提交。
- release 当前不混淆。调整签名、压缩或打包行为时以模块构建脚本为单一事实来源。
