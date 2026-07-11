本项目全程将由 AI 开发，开发中需注意：

- 架构视野：开发时应具备项目整体架构视野，主动识别并抽离高复用模块（样式/工具函数/业务组件等等）
- 三方库：需求实现前判断自己编写或第三方库。第三方库需申请并附理由
- 规范维护：项目规范应自行维护至 AGENTS.md / CLAUDE.md 的 Prompt 节下便于 AI 工作（仅修改节后内容，节前为用户区），俩文件内容保持一致
- 文档索引：.ai-assisted/markdown/ 为功能文档目录，index.md 为入口。任务前先读索引定位按需加载，任务后按需更新文档与索引
- 格式约束：AGENTS.md / CLAUDE.md 的 Prompt 节及 .ai-assisted/markdown/ 下文档禁用无意义的格式缩进换行，压缩表述，控制文档长度禁止只增不减，修改前先审查现有内容，合并同类、移除过期信息，另外 Prompt 节前已存在的规范不要在 Prompt 下重复声明，防止提示词膨胀

# Prompt

本节由 AI 维护，纯文本左对齐，段落用【】起头。

【项目定位】
无常六爻排盘，Android 单模块应用，纯 AI 开发，作者不懂 Android，方案由 AI 决策并维护文档。技术栈单 Activity 加 Jetpack Compose 加 navigation-compose 加单模块内分层。包名与 applicationId 均 site.hanzhe.wuchang_liuyao，minSdk 24，targetSdk 与 compileSdk 36，versionName 0.1.0，仅浅色主题纯白背景。构建命令依赖版本签名配置见 build-and-run.md。

【基本纪律】
UTF-8 无 BOM 读写，禁止 GBK/ANSI 或乱码。新增文案（注释日志异常提示语）用简体中文，禁止擅自改译删现有文案，不当文案先提醒。方法体内正常导包禁用全限定名。不过度抽象，三行内短逻辑且复用少于三次直接写在调用处。改代码先查全局影响，接口或逻辑变更连带更新所有调用方。API 依赖配置版本等不确定信息先查证（优先仓库代码与官方一手资料），禁止凭记忆猜测写结论性代码。函数必须加文档注释，函数内适当加单行注释。

【整体架构】
仅保留 MainActivity 单入口，新增页面一律接入 Compose 导航图，禁止新增 Activity 跳转。根包只放入口或极少顶层装配，业务页面仓库模型不堆根包。页面 UI 按职责拆到独立 Kotlin 文件，禁止整页 UI 状态弹层堆进一个文件或 Activity。默认保持单模块，非明确构建隔离或复用收益不拆 module。高复用点抽到 ui/common、ui/theme 或 domain。分层与数据流详见 architecture.md。

【包结构职责】
navigation 导航图路由常量跨页面 ViewModel 组装，入口 WuchangLiuyaoApp，请求组装 DivinationRequestBuilders。
feature/home 首页录入；feature/time 时间选择；feature/settings 设置三页；feature/result 结果页含编辑；feature/history 历史页。
domain/time 时间模型；domain/divination 排盘纯计算与提示，均无 Android 依赖。
data/calendar 历法计算（lunar-java）；data/settings 设置持久化；data/history 历史持久化。
ui/common 复用组件与扩展；ui/theme 主题颜色字体 token。

【命名规范】
类对象 Composable 用 PascalCase，函数属性用 camelCase，资源文件小写下划线（如 ic_launcher_background），包名全小写。优先拆小型 Composable，避免 UI 与业务逻辑堆在同一文件。

【状态管理规范】
每个功能页独立 ViewModel 与独立 UiState（Home/TimeSelection/Settings/History/Result），禁止全局 MainViewModel 统管。页面级状态放 ViewModel 用 StateFlow 暴露，Compose 用 collectAsStateWithLifecycle 订阅。Screen 级 Composable 无状态只收 UiState 与事件回调，单向数据流，局部纯 UI 状态（选中标签焦点动画）可用 remember，业务状态不塞 Activity 或长期 remember/rememberSaveable。ViewModel 负责状态编排与调仓库领域逻辑，复杂排盘干支六亲六神神煞算法禁写在 Composable，纯计算放 domain 无 Android 依赖，仓库只做数据访问与错误建模不承载交互状态。本项目五个 ViewModel 在 WuchangLiuyaoApp 默认作用域创建共享，页面间不经路由传大对象而是读写同一 StateFlow。

【导航规范】
用 navigation-compose，导航图集中在 navigation/WuchangLiuyaoApp.kt 或同目录。路由为无参字符串常量定义在 AppRoute：home、select_time、settings、settings/global、settings/divination、history、result，新增页面加 route 接入现有 NavHost 禁止新增 Activity。复杂对象不塞路由，优先持久化传 ID 或上层状态组装。切换动画写在 NavHost（非首页右滑入右滑出，回首页无动画）禁止依赖 Activity 动画资源。返回符合 Android 直觉（系统手势顶部按钮一致，首页两秒双击退出）。首页右上角先进设置总览，设置子页路由下钻不直达具体设置页。

【数据层规范】
公农历干支节气换算统一经 lunar-java（cn.6tail lunar 1.7.7）页面不直调。范围公历 1901..2100、农历 1900..2100 并按同一公历边界过滤月日。data/calendar 整理计算结果选项范围与错误建模，设置放 data/settings、历史放 data/history 不混入时间仓库。文件 IO 与 SharedPreferences 禁主线程，纯计算切 Dispatchers.Default，仓库对外用 suspend。仓库区分业务未命中与技术异常（参考 CalendarQueryResult 的 Success/NotFound/CalculationError）禁止 runCatching 吞成一种。页面只消费整理后模型不拼 SQL 不操作游标。农历年按正月初一更替、六爻干支纪年按立春交接更替，二者不混用，年月柱取 lunar 的 yearInGanZhiExact 与 monthInGanZhiExact。结果保存稳定业务数据（爻 code 干支卦名）不只存展示串，展示文案由 UI 或领域格式化生成。

【UI 与主题规范】
顶栏用 WuchangTopAppBar（Material3 CenterAlignedTopAppBar）禁手写状态栏占位标题栏。提示确认弹窗用 WuchangConfirmDialog（圆角描边标题竖条底部双按钮）禁直用 AlertDialog。单列列表选择弹窗（时间年月日、默认起卦方式默认时间自动保存分组）用 WuchangOptionPickerDialog，底部操作选择（起卦方式爻值）用 WuchangOptionSelectorSheet，二者不混用。单行轻量输入用 WuchangSingleLineInput（圆角描边背景 background 占位 AppTextHint 正文 14sp）禁直用 OutlinedTextField。无涟漪点击用 UiExtensions 的 noRippleClick。颜色边框强调色统一在 ui/theme，页面优先取 MaterialTheme.colorScheme 与 Typography 不散落常量。主题 Wuchang_liuyaoTheme 按屏幕短边自适应缩放并叠加 appFontScale，独立弹层需自包裹主题保字号。新增界面复用现有卡片表单行分段控件设置行，勿重复造近似组件。

【错误处理规范】
启动致命错误可终止流程但先给明确中文提示（参考时间选择启动失败）。业务失败与技术故障分开建模。非必要不静默失败。

【排盘领域规范】
纯计算入口 HexagramCalculator.kt，模型 DivinationModels.kt，提示与高亮 DivinationPromptHints.kt；首页起卦由 DivinationRequestBuilders.kt 组装 DivinationRequest，导航层只组装跳转不在 Composable 计算。六十四卦名八宫卦宫五行地支五行纳甲神煞表全固化在 HexagramCalculator.kt，运行时不查库不依赖 najia，改世应卦宫纳甲六亲伏神规则前先核对来源禁凭记忆。爻值存储为界面顺序上爻到初爻，计算卦码前反转成初爻到上爻。详细算法规则见 domain-divination.md，结果页表格见 feature-result.md，设置项见 feature-settings.md，历史交互与持久化见 feature-history.md 与 data-history.md。

【三方库规范】
引库权衡维护性与体积。版本集中在 gradle/libs.versions.toml，新增依赖走 version catalog 不硬编码。改依赖相关代码前先确认锁定版本，不按记忆套其他版本写法。

【文档维护规范】
功能改动同步更新对应文档保持与代码一致。通用工作规范在本 Prompt 节，功能细节知识在 .ai-assisted/markdown 文档，二者不重复堆叠。
