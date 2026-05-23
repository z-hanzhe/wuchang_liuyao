# Repository Guidelines

## 项目结构与模块组织

本仓库当前是单模块 Android 应用，核心代码位于 `app/`，基于 API 24，作者对 Android 一窍不通代码将全权交给 AI 编写，理解需求后自行选择合适的方案进行开发，并自行维护文档

- `app/src/main/`：应用源码、`AndroidManifest.xml` 与 `res/` 资源
- `app/src/main/java/site/hanzhe/wuchang_liuyao/`：Kotlin 与 Jetpack Compose 代码，新文件按下方架构分包放置
- `app/src/test/`：JUnit4 本地单元测试
- `app/src/androidTest/`：AndroidX 与 Compose UI 仪器测试
- `gradle/libs.versions.toml`：依赖与插件版本集中管理
- `gradle/wrapper/`：Gradle Wrapper，必须保留并提交

当前项目采用 `单 Activity + Jetpack Compose + navigation-compose + 单模块内分层` 架构。

- 默认只保留 `MainActivity` 作为应用入口，新增页面优先接入 Compose 导航图，不要随意新增 Activity 做页面跳转
- 页面 UI 按职责拆分到独立 Kotlin 文件，避免将整页 UI、状态和弹层全部堆进 Activity
- 主题相关代码集中在 `app/src/main/java/site/hanzhe/wuchang_liuyao/ui/theme/`
- 根包只保留应用入口或极少量顶层装配代码，不再把业务页面、仓库、模型继续堆在根包

当前包结构约定如下：

- `navigation/`：应用导航图、路由常量、跨页面 ViewModel 组装，例如 `WuchangLiuyaoApp`
- `feature/home/`：首页录入页，包括输入问念、起卦方式、阴阳爻/铜钱录入等首页 UI 与状态
- `feature/time/`：时间选择页，包括公历、农历、干支时间选择相关 UI、状态和页面级逻辑
- `feature/settings/`：设置总览页、通用设置页、排盘设置页，以及设置项 UI、设置状态和设置项读写入口
- `feature/result/`：后续排盘结果页建议放置位置，用于展示本卦、变卦、六亲、六神、干支、神煞等结果
- `feature/history/`：后续排盘历史页建议放置位置，用于历史列表、详情入口、删除等历史 UI
- `domain/time/`：时间领域模型与纯业务逻辑，例如公历/农历/干支时间模型、格式化与转换辅助
- `domain/divination/`：后续排盘领域逻辑建议放置位置，例如起卦、变卦、六亲、六神、神煞等纯计算
- `data/calendar/`：公历、农历、干支、节气等时间计算入口，当前基于 `lunar-java`
- `data/settings/`：设置持久化
- `data/history/`：后续历史记录持久化建议放置位置
- `ui/common/`：跨页面复用的 Compose 小组件或 UI 扩展
- `ui/theme/`：主题、颜色、字体等视觉 token

常用命令记录：

调试安装命令：`.\gradlew.bat installDebug`
正式安装命令：`.\gradlew.bat installRelease`
调试打包命令：`.\gradlew.bat assembleDebug`
正式打包命令：`.\gradlew.bat assembleRelease`

## 代码风格与命名规范

- 类、对象、`@Composable` 使用 `PascalCase`
- 函数、属性使用 `camelCase`
- 资源文件名使用小写下划线，例如 `ic_launcher_background`
- 包名一律小写

优先拆分小型 Composable，避免把大量 UI 与业务逻辑堆在同一文件。

## 架构与状态管理规范

- 新功能默认继续保持单模块，不要因为新增页面就拆 Gradle module；只有出现明确构建隔离或复用收益时再讨论多模块
- 每个功能页使用独立 `ViewModel` 和独立 `UiState`，例如 `HomeViewModel`、`TimeSelectionViewModel`、`SettingsViewModel`
- 禁止重新引入“一个全局 MainViewModel 管所有页面状态”的结构
- 页面级状态默认放在 `ViewModel` 中，使用 `StateFlow` 暴露 UI 状态
- Compose 页面默认使用 `collectAsStateWithLifecycle()` 订阅 `ViewModel` 状态
- `Composable` 优先保持无状态，或只保留局部纯 UI 状态，例如当前选中的分页标签、输入焦点、动画中间态
- 不要把业务状态直接塞进 `Activity` 或大型 `remember/rememberSaveable` 变量里长期维护
- 页面间共享数据优先放在明确归属的上层状态中组装，或通过仓库/领域模型传递稳定 ID；不要依赖多个页面之间手工传大量临时参数
- `ViewModel` 负责页面状态编排、调用仓库和领域逻辑；复杂排盘、干支、六亲、六神、神煞等算法不要写在 `Composable` 里
- 纯计算逻辑优先放在 `domain/` 下，保持无 Android 依赖，便于后续复用和排查
- 仓库层只负责数据访问和错误建模，不承载页面交互状态

## 导航规范

- 默认使用 `navigation-compose`
- 导航图集中在 `navigation/WuchangLiuyaoApp.kt` 或同目录下拆分文件中维护
- 页面切换动画优先写在 `NavHost` 或导航目标上，不要继续依赖 Activity 切换动画资源
- 返回行为必须符合 Android 直觉：系统返回、手势返回、顶部返回按钮行为保持一致
- 新增页面优先新增 route 并接入现有 `NavHost`，不要新增 Activity
- 页面参数尽量保持简单稳定；复杂对象不要直接塞进路由字符串，优先通过仓库持久化后传 ID，或由上层状态组装
- 首页右上角入口先进入设置总览页；设置相关子页通过导航路由继续下钻，不要重新改回首页直达某个具体设置页

## 数据层与时间计算规范

- 通过 `cn.6tail:lunar` 进行公历、农历、干支和节气换算
- 时间选择范围按公开官方日历资料可对照的现代范围处理：公历 `1901..2100`
- 农历选项包含 `1900..2100`，并以同一公历边界过滤实际可选月日
- `data/calendar/` 负责整理时间计算结果、选项范围和错误建模，页面不直接调用第三方农历库
- 设置读写放在 `data/settings/`
- 后续排盘历史记录使用独立数据入口，建议放在 `data/history/`，不要混入时间计算仓库
- 文件 IO、数据库查询等操作禁止放在主线程；纯计算逻辑按需要切到 `Dispatchers.Default`
- 仓库层需要明确区分“业务未命中”和“技术异常”，不要用 `runCatching` 把不同错误全部吞成同一种结果
- 页面只消费仓库层整理后的数据模型，不直接拼 SQL 或直接操作游标
- 农历年份按正月初一更替；六爻干支纪年按立春节气交接时刻更替，二者不要混用
- 排盘结果保存时应保存稳定业务数据，不要只保存页面展示字符串；展示文案由 UI 或领域格式化逻辑生成

## 当前排盘实现约定

- 当前排盘纯计算入口在 `domain/divination/HexagramCalculator.kt`，输入模型在 `domain/divination/DivinationModels.kt`
- 首页点击排盘时，由 `navigation/DivinationRequestBuilders.kt` 把首页状态和时间状态组装成 `DivinationRequest`，导航层只负责组装和跳转，不在 `Composable` 内做排盘计算

### 起卦输入约定

- `HomeUiState.selectedYaoValues` 和 `selectedCoinValues` 的存储顺序是界面顺序，即 `上爻 -> 五爻 -> 四爻 -> 三爻 -> 二爻 -> 初爻`
- 真正计算卦码前必须先反转成 `初爻 -> 二爻 -> 三爻 -> 四爻 -> 五爻 -> 上爻`
- 爻名起卦默认值当前固定为：自下而上 `少阳 少阳 少阳 老阴 少阳 少阳`
- 铜钱起卦当前映射固定为：
- `背 正 正` = 少阳
- `背 背 正` = 少阴
- `背 背 背` = 老阳
- `正 正 正` = 老阴

### 时间输入约定

- 公历/农历起卦时，干支信息统一通过 `lunar-java 1.7.7` 现算，不手写干支推导
- 公历/农历起卦时，排盘使用：
- 年柱：`yearGanExact/yearZhiExact`
- 月柱：`monthGanExact/monthZhiExact`
- 日柱：`dayGan/dayZhi`
- 时柱：`timeGan/timeZhi`
- 干支起卦当前校验规则是：`月份的地支不能为空，日辰的干支不能为空`
- 干支起卦时，年柱和时柱允许为空，但日干不能为空，因为六神起法依赖日干

### 排盘计算顺序

- 先把六爻转成 `DivinationLine`，再生成本卦卦码 `baseMark`
- `baseMark` 规则：
- 少阳、老阳记为 `1`
- 少阴、老阴记为 `0`
- `changedMark` 规则：
- 少阳、老阴变后记 `1`
- 少阴、老阳变后记 `0`
- 世应按 `寻世诀` 实现，逻辑在 `resolveShiYing`
- 卦宫按 `认宫诀` 实现，逻辑在 `resolvePalaceIndex`
- 卦型当前只识别 `游魂 / 归魂 / 六冲 / 六合`
- 纳甲使用代码内置八宫纳甲表 `PalaceNaJia`，不是运行时查库
- 六亲按 `卦宫五行` 对 `纳甲地支五行` 的差值计算，逻辑在 `resolveSixRelative`
- 六神按日干起法固定轮转，逻辑在 `resolveSixGods`
- 伏神按 `本宫纯卦` 补本卦中缺失的六亲，逻辑在 `resolveHiddenLines`
- 变卦只在存在动爻时生成
- 变卦的六亲当前沿用 `本卦卦宫五行` 计算，不按变卦自身卦宫重新定六亲

### 规则表与来源约定

- 六十四卦名、八宫、卦宫五行、地支五行、纳甲表当前都直接固化在 `HexagramCalculator.kt`
- `lunar-java` 当前只负责时间相关干支，不负责六爻排盘规则
- 六爻规则表的实现参考曾查证过的开源项目 `najia`，但项目运行时不依赖该库，只保留本地 Kotlin 实现
- 后续如果要改世应、卦宫、纳甲、六亲、伏神规则，必须先核对规则来源后再改，不要凭记忆调整

### 结果页表格约定

- 当前结果页在 `feature/result/ResultScreen.kt`
- 表格固定为 `7 行 17 列`
- 第 `1` 行整行预留为空
- 第 `2..7` 行对应 `上爻 -> 初爻`
- 红色提示小字行在存在“动爻符号”列时，最右侧“变卦提示”单元格与左侧动爻符号列合并，仅该提示行这样处理，其他行列结构不变
- 列顺序固定为：
- `六神`
- `藏爻六亲`
- `藏爻天干`
- `藏爻地支`
- `藏爻五行`
- `本卦六亲`
- `本卦天干`
- `本卦地支`
- `本卦五行`
- `本卦爻符号`
- `世应标记`
- `动爻符号`
- `变卦爻符号`
- `变卦六亲`
- `变卦天干`
- `变卦地支`
- `变卦五行`
- 列间距约定：
- `2~5` 列之间无额外间距
- `6~9` 列之间无额外间距
- `14~17` 列之间无额外间距

### 设置项约定

- 设置总览页的首个分组固定为“软件设置”，当前包含“通用设置”和“排盘设置”两个入口
- 设置总览页新增第二个分组“更多功能”，当前包含“排盘记录”入口
- “调整字体大小”“显示农历卡片”“23点更替日柱”归类为“通用设置”
- “显示天干”归类为“排盘设置”，默认关闭；关闭后结果页不显示藏爻、本卦、变卦的天干内容，但排盘结果模型仍保留天干字段
- “显示简洁六神”归类为“排盘设置”，默认关闭；开启后六神显示为 `龙 / 玄 / 虎 / 蛇 / 勾 / 雀`，卦宫显示单字
- “显示简洁六亲”归类为“排盘设置”，默认关闭；开启后六亲显示为 `父 / 兄 / 孙 / 财 / 官`
- 其余结果展示相关开关继续统一放在“排盘设置”，不要再拆回旧的“装卦设置”

### 当前历史记录交互约定

- 历史记录页长按记录进入多选模式，点击返回退出多选模式
- 底部“全选”针对当前筛选结果工作：若仍有未选中记录则全部选中，若当前筛选结果已全部选中则再次点击改为取消这些选中
- 底部“反选”针对当前筛选结果工作：已选变未选，未选变已选
- 底部“删除”必须先弹出确认提示，再执行批量删除
- 已保存的排盘从结果页右上角显示“编辑”按钮，弹出编辑界面后只能通过底部“保存 / 取消”关闭，不允许点背景或按返回直接关闭
- 已保存排盘当前允许编辑并持久化三个字段：问念、现状信息、断语；其中问念会同步影响结果页展示和排盘记录列表标题

## UI 与主题规范

- 顶部标题栏默认使用 Material3 的 `TopAppBar/CenterAlignedTopAppBar`，不要继续手写状态栏占位式标题栏
- 公共标题栏统一复用 `ui/common/WuchangTopAppBar.kt`，保持略宽松的上下留白和更醒目的返回按钮
- 纯提示/确认类弹窗统一复用 `ui/common/WuchangConfirmDialog.kt`，不要直接使用 Material3 默认 `AlertDialog`
- 当前确认弹窗视觉规范来源于原 `GlobalSettingsScreen.kt` 的自定义 Dialog 样式，后续新增纯提示/确认弹窗应保持相同的圆角、描边、标题引导条和底部双按钮风格
- 时间选择页公历年月日这类单列列表选择弹窗，以及通用设置里的“默认起卦方式”，统一优先复用 `ui/common/WuchangOptionPickerDialog.kt`，保持居中的列表弹窗风格，列表项高度比旧时间选择弹窗更紧凑
- 首页起卦方式、爻值这类底部唤起的操作选择继续使用现有底部弹层，不要和时间选择类列表弹窗混成一套
- 单行文本输入统一优先复用 `ui/common/WuchangSingleLineInput.kt`，不要直接使用默认 `OutlinedTextField` 做搜索框或轻量输入框
- 当前输入框视觉规范为：圆角描边、背景使用 `MaterialTheme.colorScheme.background`、占位文字使用 `AppTextHint`、正文 `14sp`，与现有首页问题输入和时间选择页表单风格保持一致
- 颜色、边框、背景、强调色统一放在 `ui/theme` 中维护，不要在页面文件里散落大量颜色常量
- 页面尽量通过 `MaterialTheme.colorScheme`、`Typography` 等主题能力取值，减少硬编码视觉 token
- 新增界面优先复用现有的卡片、表单行、分段控件等模式，避免重复实现近似组件

## 错误处理规范

- 启动阶段的致命错误可以终止页面流程，但必须先给出明确中文提示
- 业务规则导致的失败与技术故障要分开建模和处理，便于后续排查
- 非必要不要直接静默失败
