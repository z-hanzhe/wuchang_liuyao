整体架构

定位
无常六爻排盘，Android 单模块应用，全程 AI 开发。架构为单 Activity 加 Jetpack Compose 加 navigation-compose 加单模块内分层。仅浅色主题，纯白背景。

分层与依赖方向
UI 层（feature 各页 Screen 与 ViewModel）依赖 domain 与 data；data 层依赖 domain 模型与第三方库；domain 层为纯 Kotlin 无 Android 依赖。依赖方向单向向下，domain 不反向依赖 feature 或 data。

四层职责
入口层：MainActivity 只做 enableEdgeToEdge 与 setContent，渲染 WuchangLiuyaoApp，不在此层包裹主题。navigation 层持有 NavController、创建并共享全部 ViewModel、维护路由与页面切换动画、组装排盘请求。
feature 层：每个功能页一个包，含 Screen（无状态 Composable）、ViewModel（StateFlow 暴露 UiState）、Models（UiState 与页面数据类）。
domain 层：time 放时间模型与时辰干支推算，divination 放排盘纯计算与提示计算，全部无 Android 依赖，可单元测试。
data 层：calendar 封装 lunar-java 历法计算，settings 封装 SharedPreferences 设置读写，history 封装 SharedPreferences 历史存储。

核心数据流：排盘
首页 HomeScreen 点击排盘，回调进入 navigation 层，调用 buildDivinationRequest 从 HomeUiState 与 TimeSelectionHostUiState 组装 DivinationRequest，成功则 resultViewModel.showResult 调用 HexagramCalculator.calculate 得到 DivinationResult，导航到 result 路由；失败则 homeViewModel.showDialogMessage 弹错误提示。结果页再用 buildDivinationTableRowHints 逐行生成日月冲合回头生克旬空提示后渲染。

核心数据流：时间
首页读取 TimeSelectionViewModel 的 divinationTime 与 calendarSummary 展示。进入时间选择页时 openTimeSelection 把已确认快照复制为编辑草稿，页面内修改任一字段都重新查询 CnCalendarRepository 做级联选项过滤，确认时 confirmTimeSelection 把草稿写回已确认快照并生成 DivinationTime。

核心数据流：历史
排盘结果页保存或自动保存调用 DivinationHistoryRepository.saveRecord，写入 SharedPreferences 的 JSON。历史页 refresh 读取全部分组与记录。点击记录导航到结果页并 showSavedResult 展示已保存卦例，结果页编辑弹窗保存调用 updateRecord 回写。

ViewModel 共享方式
全部五个 ViewModel（Home、TimeSelection、Settings、History、Result）在 WuchangLiuyaoApp 内以默认 viewModel 作用域创建并被各页共享，Activity 级生命周期。页面间不通过路由参数传大对象，而是读写同一 ViewModel 的 StateFlow。这是本项目刻意选择，新增页面沿用此模式，禁止回退到全局 MainViewModel。

跨页面联动
WuchangLiuyaoApp 内多个 LaunchedEffect 做副作用：时间启动失败 Toast 后 finishAffinity；各页 transientMessage Toast；时间选择关闭请求自动 popBackStack；历史分组变化刷新设置的自动保存分组；设置默认起卦方式同步到 Home；设置默认时间类型同步到 TimeSelection。

状态管理约束
Screen 级 Composable 全部无状态，只收 UiState 与事件回调 lambda，单向数据流。局部纯 UI 状态可用 remember。业务算法不写在 Composable，放 domain。仓库对外方法用 suspend，IO 切 Dispatchers.IO，纯计算切 Dispatchers.Default。

扩展原则
默认保持单模块不拆 module。主动识别高复用点抽离到 ui/common、ui/theme、domain。新增功能页遵循 feature 包内 Screen 加 ViewModel 加 Models 三件套，接入 NavHost 新增 route，复用现有公共组件与主题 token。
