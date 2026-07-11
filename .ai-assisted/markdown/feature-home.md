首页录入页 feature/home

文件 HomeModels.kt、HomeViewModel.kt、HomeScreen.kt。

HomeUiState 字段
dialogMessage String 可空默认空：全局提示弹窗消息，非空弹 WuchangConfirmDialog。
question String 默认空：问念。
selectedMethod DivinationMethod 默认 YAO_NAME：当前起卦方式。
selectedYaoValues List String 默认 6 个未选择：爻名起卦六爻值，顺序上爻到初爻。
selectedCoinValues List String 默认 6 个未选择：铜钱起卦六爻值，顺序上爻到初爻。
pointSelectionLines List PointSelectionLineState 默认 6 个 isYang false isMoving false：点选起卦每爻状态，顺序上爻到初爻。
onlineShakeValues List String 默认 6 个空：在线摇卦已生成爻值，顺序上爻到初爻。
onlineShakeCoinFaces List OnlineShakeCoinFace 默认 3 个 FRONT：在线摇卦当前三枚铜钱正反面。
nextOnlineShakeIndex Int 默认 5：在线摇卦下一爻填入 index，从 5 初爻向 0 上爻递减。
isOnlineShakeAnimating Boolean 默认 false：铜钱翻转动画中。
isMethodSheetVisible Boolean 默认 false：起卦方式底部面板可见。
activeYaoPicker YaoPickerState 可空默认空：当前弹出的爻值面板状态含 sectionType 与 index。

辅助模型
YaoLineType YANG YIN。YaoValueOption label lineType marker。PointSelectionLineState isYang isMoving。OnlineShakeCoinFace FRONT 正 BACK 背。InputSectionType YAO_NAME COIN POINT_SELECT ONLINE_SHAKE。DivinationMethod 五种方式各关联一个 InputSectionType 可空（AUTO 为空）。YaoPickerState sectionType 加 index。
常量 YaoNames 为上爻五爻四爻三爻二爻初爻。YaoValueOptions 少阳少阴老阳老阴（老阳标记 O 老阴标记 X）。CoinValueOptions 背正正背背正背背背正正正。
resolveOnlineShakeYaoValue 按背面数量映射爻值。canStartDivination 扩展判断当前方式六爻是否填满，AUTO 恒真。

HomeViewModel 方法
onQuestionChange 更新问念。showDialogMessage 与 dismissDialogMessage 提示弹窗开关。
showMethodSheet 与 dismissMethodSheet 起卦方式面板开关。
selectMethod 切换方式，与当前不同则重置全部爻值输入并关面板。resetCurrentMethodValues 仅重置当前方式数据。
showYaoValueSheet(index) 打开爻值面板仅 YAO_NAME 与 COIN 有效，dismissYaoValueSheet 关闭，selectYaoValue 写入对应列表 index 并关闭。
togglePointSelectionLine 切阴阳，setPointSelectionLineMoving 设动爻勾选。
toggleOnlineShake 两阶段：首次点击置动画为真，二次点击随机三枚铜钱算爻值写入 onlineShakeValues 当前 index，index 递减，动画停止。
均为同步纯状态更新，无协程。

HomeScreen 结构
无状态 Composable，接收 uiState、showLunarInfo、calendarSummary、divinationTime 与事件回调。
Scaffold 顶栏 HomeTopAppBar（标题加设置按钮），底栏 BottomActionBar（点击排盘按钮 enabled 由 canStartDivination 决定），内容 LazyColumn。
LazyColumn 依次：CalendarSummaryCard 农历卡片（showLunarInfo 且 calendarSummary 非空时显示，上半渐变显示农历年月日节气，下半显示干支）、QuestionRow 问念输入、时间行 SelectableDetailRow 点击进时间选择页、起卦方式行 SelectableDetailRow 点击弹方式面板、按 selectedMethod.inputSectionType 分支渲染录入区。
录入区分支：YAO_NAME 与 COIN 用 YaoSection，POINT_SELECT 用 PointSelectionSection，ONLINE_SHAKE 用 OnlineShakeSection，null（AUTO）用 MethodPlaceholderSection 提示直接点排盘。
MethodSelectorSheet 用 WuchangOptionSelectorSheet 列五种方式，YaoValueSelectorSheet 用 ModalBottomSheet 列当前模式选项。

五种起卦方式交互
爻名起卦：点某行弹 YaoValueSelectorSheet 选少阳少阴老阳老阴，写入 selectedYaoValues。
铜钱起卦：同交互选背正正等四组合，写入 selectedCoinValues。
点选起卦：点阴阳线切阴阳，勾选 Checkbox 设动爻。
在线摇卦：nextOnlineShakeIndex 从 5 初爻起，首次点击铜钱区启动 180ms 间隔翻转动画，二次点击停止并生成爻值写入，从初爻向上爻录入六次。
自动起卦：占位提示，canStartDivination 恒真，排盘时随机生成。

存储顺序要点
所有爻值列表 index 0 为上爻 index 5 为初爻，与 YaoNames 一一对应。在线摇卦填入方向相反从初爻到上爻。计算卦码在 DivinationRequestBuilders 内反转，见 domain-divination.md。

页面衔接
农历卡片与时间来自 TimeSelectionViewModel。默认起卦方式由设置经 LaunchedEffect 同步。点击排盘进 navigation 层组装请求，见 navigation.md。
