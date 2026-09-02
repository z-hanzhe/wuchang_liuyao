# 架构

## 职责与边界

- 应用是单模块 Android 项目，使用单 `MainActivity`、Jetpack Compose、Material 3 与 navigation-compose；`MainActivity` 只负责系统窗口初始化和挂载 `WuchangLiuyaoApp`。
- `feature/` 按首页、时间、设置、结果、历史组织页面状态与交互；`domain/` 保存无 Android 依赖的时间及排盘模型和计算；`data/` 封装历法查询、设置和历史持久化；`ui/` 保存跨页面设计能力。
- `navigation/WuchangLiuyaoApp.kt` 是应用装配边界，持有导航、共享页面级 ViewModel、主题和跨页面联动；`DivinationRequestBuilders.kt` 是首页及时间状态进入排盘领域的适配边界。

## 状态与导航

- Home、TimeSelection、Settings、History、Result 各自拥有 `UiState + ViewModel`，通过 `StateFlow` 暴露业务状态；Screen 只接收状态与事件，局部焦点、弹层或选中态可留在 Compose。
- 五个 ViewModel 在应用根部创建并跨目的地共享。路由只标识页面，不携带排盘或历史大对象；首页到结果、历史到结果均先更新共享状态再导航。
- 首页输入、已确认时间和未保存结果由根部 ViewModel 保存在内存，可跨导航和配置变更延续，但进程重建或重新启动不保证恢复；需要恢复的数据必须进入明确的状态保存或持久化边界。
- 新页面接入现有 NavHost 并保持独立状态所有权。跨页联动集中在应用根部，避免功能页互相直接操纵对方 ViewModel；页面转场由 NavHost 统一，系统返回与顶栏返回应保持相同结果，功能页只处理自身草稿或选择模式，首页以再次返回确认退出。

## 核心数据流

- 起卦：Home 状态 + 已确认时间 + 设置口径 -> `DivinationRequestBuilders` -> `ResultViewModel` -> `HexagramCalculator` -> Result Screen。
- 保存：Result ViewModel -> History Repository -> SharedPreferences JSON；历史选择记录后经 Result ViewModel 回到结果页。
- 设置：Settings Repository -> Settings ViewModel -> 应用根部把默认方式、默认时间和展示选项分发给首页、时间及结果。

## 运行边界

- `CnCalendarRepository` 是 suspend 查询边界并在 `Dispatchers.Default` 执行历法计算；历史和设置仓库目前是同步 SharedPreferences API。History/Result 在 IO 调度器调用历史仓库，Settings 当前同步读写，不能假设所有仓库天然异步。
- Android 系统备份当前已开启，备份与数据提取规则没有排除 SharedPreferences；调整备份范围时应同时评估设置和历史数据的恢复、迁移及隐私影响。
- 历法查询区分正常结果、业务未命中和计算异常；启动时间无法建立时属于阻断应用的错误，其余交互失败由对应 ViewModel 转为用户可见状态。
- 默认保持单模块；只有出现明确构建隔离或跨应用复用收益时才评估拆分。新增抽象应对应真实复用或依赖边界，不为文件数量本身分层。
