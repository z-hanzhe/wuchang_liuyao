导航

入口
navigation/WuchangLiuyaoApp.kt 是应用根 Composable，由 MainActivity.setContent 渲染。内部 rememberNavController 创建并持有 NavController，用 viewModel 创建并共享五个 ViewModel，全部 UiState 用 collectAsStateWithLifecycle 收集。先判断时间加载状态再包裹 Wuchang_liuyaoTheme。

路由常量 AppRoute
home 首页 HomeScreen。
select_time 选择时间页 SelectTimeScreen。
settings 设置总览页 SettingsScreen。
settings/global 通用设置页 GlobalSettingsScreen。
settings/divination 排盘设置页 DivinationSettingsScreen。
history 排盘记录页 HistoryScreen。
result 排盘结果页 ResultScreen。
路由为纯字符串常量无参数，新增页面新增 route 接入现有 NavHost，禁止新增 Activity。

NavHost 结构
startDestination 为 home，七个 composable 平铺注册，无嵌套 NavGraph。

页面切换动画
enterTransition：目标非 home 时 slideInHorizontally tween 220ms 从右整屏宽度滑入，目标为 home 时 EnterTransition.None。
exitTransition：ExitTransition.None。
popEnterTransition：EnterTransition.None。
popExitTransition：slideOutHorizontally tween 220ms 向右整屏滑出。
效果为前进新页右滑入，返回当前页右滑出，被遮盖页不动。

首页返回
home 注册 BackHandler 实现两秒内双击退出，阈值 ExitConfirmIntervalMillis 2000 毫秒，首次提示再次点击返回键退出应用，二次调用 activity.finish。

跨页面副作用 LaunchedEffect
时间 startupFailure 非空 Toast 后 finishAffinity。
时间与历史与结果的 transientMessage Toast 后消费。
时间 shouldCloseTimeSelection 为真时 popBackStack 并消费。
历史 groups 变化刷新设置的自动保存分组。
设置 defaultDivinationMethod 同步到 HomeViewModel.selectMethod。
设置 defaultDivinationTimeType 在时间加载完成后同步到 TimeSelectionViewModel.applyDefaultTimeType。

排盘请求组装
navigation/DivinationRequestBuilders.kt 的 buildDivinationRequest 输入 HomeUiState、TimeSelectionHostUiState、changeDayPillarAt23，返回 Success DivinationRequest 或 Failure message。
六爻按 selectedMethod 分支取值：YAO_NAME 取 selectedYaoValues，COIN 取 selectedCoinValues，POINT_SELECT 取 pointSelectionLines，MANUAL 取 onlineShakeValues，AUTO 随机。
日期按 confirmedTimeSelectionUiState.selectedType 分支：GREGORIAN 用 Solar.fromYmdHms 且 23 点换日用 next(1).lunar，LUNAR 用 Lunar.fromYmdHms 闰月负月份，GANZHI 校验月支日干日支非空。
详细映射见 domain-divination.md 的起卦输入约定与时间输入约定。

页面跳转关键回调
首页时间行 onTimeClick：timeSelectionViewModel.openTimeSelection 后 navigate select_time。
时间页确认 onConfirmClick：confirmTimeSelection 后 popBackStack。
时间页返回 onBackClick：closeTimeSelection 后 popBackStack（不保存修改）。
首页起卦 onStartDivinationClick：buildDivinationRequest 成功则 resultViewModel.showResult 后 navigate result，失败则弹提示。
历史点击记录：resultViewModel.showSavedResult 后 navigate result。
