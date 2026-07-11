设置 feature/settings

文件 SettingsScreen.kt、GlobalSettingsScreen.kt、DivinationSettingsScreen.kt、SettingsComponents.kt、SettingsViewModel.kt。持久化在 data/settings 见本文末与 data-calendar 无关。

三页结构
SettingsScreen 设置总览：分组一软件设置含通用设置入口（进 settings/global）与排盘设置入口（进 settings/divination）；分组二更多功能含排盘记录入口（进 history）。首页右上角先进入本页。
GlobalSettingsScreen 通用设置页，分组标题通用设置。
DivinationSettingsScreen 排盘设置页，分组标题排盘设置，全部为默认关闭的开关。

通用设置项（GlobalSettingsScreen，key 为 SharedPreferences 键）
调整字体大小 key app_font_size Float 默认 1.00：弹 FontScaleDialog 范围 0.70 到 1.30 步进 0.05，存储经 AppFontScale.normalize 对齐，显示标准大小或 x0.95 倍等，兼容旧版字符串枚举。
显示农历卡片 key show_lunar_info Boolean 默认 true：控制首页农历卡片。
23点更替日柱 key change_day_pillar_at_23 Boolean 默认 true：hour 大于等于 23 用次日干支，关闭时弹二次确认。
默认起卦方式 key default_divination_method String 默认 YAO_NAME：值为 DivinationMethod 枚举名，WuchangOptionPickerDialog 选择，经 LaunchedEffect 同步到首页。
默认起卦时间 key default_divination_time_type String 默认 GREGORIAN：GREGORIAN 公历起卦、LUNAR 农历起卦、GANZHI 干支起卦，同步到时间选择页。
自动保存排盘 key auto_save_divination_mode String 默认 OFF：OFF 关闭、QUESTION_NOT_EMPTY 问念不为空、ALWAYS 始终，AutoSaveDivinationMode.shouldAutoSave 决定。
自动保存分组 key auto_save_history_group_id String 默认 default：点击先 refreshAutoSaveHistoryGroups 刷新分组再 WuchangOptionPickerDialog 选择，已选分组被删自动回退默认分组。

排盘设置项（DivinationSettingsScreen，全部 Boolean 默认 false）
显示天干 key show_heavenly_stem：显示藏爻本卦变卦天干，关闭时不显示天干但模型保留字段。
显示所有藏爻 key show_all_hidden_lines：展示全部伏神否则仅缺失六亲的伏神。
显示简洁六神 key compact_six_god：六神显示龙玄虎蛇勾雀，宫名去宫字。
显示简洁六亲 key compact_six_relative：六亲显示父兄孙财官。
回头生克提示 key hui_tou_sheng_ke_hint：标注回头生回头克。
回头冲合提示 key hui_tou_chong_he_hint：标注回头冲回头合。
日月冲合提示 key day_month_chong_he_hint：标注月破月合日冲日合。
地支旬空标记 key mark_branch_xun_kong：旬空地支加底色。
点击高亮标记 key click_highlight_hint：点击爻位高亮相关爻，关闭时强制清空选中。
各开关对结果页展示的具体效果见 feature-result.md。其余结果展示开关统一放排盘设置，不拆回旧装卦设置。

SettingsViewModel
继承 AndroidViewModel，内部建 AppSettingsRepository 与 DivinationHistoryRepository。SettingsUiState 含全部设置字段加 historyGroups。每个 setter 同步执行仓库 setXxx 加状态 update。refreshAutoSaveHistoryGroups 重读分组并校验当前 id 有效。枚举解析名称匹配失败回退默认。

AppSettingsRepository（data/settings/AppSettingsRepository.kt）
用 SharedPreferences 文件名 app_settings MODE_PRIVATE，写入 edit apply。对每个 key 提供 getXxx 与 setXxx。app_font_size 读取兼容 Double Int Long String 旧格式。
AutoSaveDivinationMode 枚举 shouldAutoSave：OFF 恒假，QUESTION_NOT_EMPTY 问念非空真，ALWAYS 恒真。

AppFontScale（data/settings/AppFontScale.kt）
Min 0.70 Max 1.30 Step 0.05 Default 1.00 DiscreteSteps 11。normalize 对齐最近 0.05 并 coerceIn 保留两位。formatSummary 等于 1.00 返回标准大小否则 x1.05 倍格式。fromLegacyKey 兼容 very_small 0.74 small 0.87 standard 1.00 large 1.13 very_large 1.26。

设置页组件（SettingsComponents.kt）
SettingsPageScaffold 页面骨架含 WuchangTopAppBar 与 safeDrawing。SettingsPageColumn 可滚动纵列间距 10dp。SettingsGroup 分组卡片 10dp 圆角标题 13sp Bold。SettingsLinkRow 导航行右侧点号。SettingsValueRow 值行右侧 primary 当前值。SettingsSwitchRow 开关行用 SettingsAnimatedSwitch。SettingsDivider 分割线。DialogActionButton 对话框按钮。
SettingsAnimatedSwitch：轨道 42x24dp 胶囊形，颜色 tween 220ms ON primary OFF surfaceVariant，滑块 spring 偏移 ON 18dp OFF 0，缩放 tween 180ms，滑块 18dp 白圆。
