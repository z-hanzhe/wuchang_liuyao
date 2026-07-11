公共组件与主题 ui/common ui/theme

公共组件复用规范：顶栏用 WuchangTopAppBar，确认弹窗用 WuchangConfirmDialog，列表选择弹窗用 WuchangOptionPickerDialog，底部操作选择用 WuchangOptionSelectorSheet，单行输入用 WuchangSingleLineInput，无涟漪点击用 noRippleClick。禁止绕过这些复用另写近似组件。

WuchangTopAppBar（ui/common/WuchangTopAppBar.kt）
参数 title 居中标题、onBackClick 可空非空显左返回、actions 右侧操作区。基于 CenterAlignedTopAppBar 高 52dp，底部 1dp outline 分割线，背景 surface，标题 19sp Bold。
辅助 TopBarGlyphButton（glyph onClick fontSize fontWeight，44dp 方形 noRippleClick）。TopBarTextAction（text onClick 默认 15sp primary 色，44dp 高）。

WuchangConfirmDialog（ui/common/WuchangConfirmDialog.kt）
参数 title、message、confirmText、onConfirm、onDismiss、dismissText 默认取消（为空不显取消）。Surface 圆角 20dp 水平边距全宽减 40dp，usePlatformDefaultWidth false，底部取消（surfaceVariant）加确认（primary）各 weight 1。纯提示确认类弹窗统一用它，视觉规范来源原 GlobalSettingsScreen 自定义 Dialog。
辅助 WuchangDialogTitle（标题带 4x18dp primary 竖条）。WuchangDialogActionButton（modifier text isPrimary onClick，42dp 高 12dp 圆角）。

WuchangOptionPickerDialog（ui/common/WuchangOptionPickerDialog.kt）
泛型参数 title、options、selectedOptionId、onDismiss、onOptionSelected、optionId 提取器、optionText 提取器、itemVerticalPadding 默认 11dp。LazyColumn 最大高 360dp 自动滚到已选，已选高亮 surfaceVariant 加 secondary 文字加已选标签，外包 Wuchang_liuyaoTheme 确保字号一致，底部关闭按钮。用于时间选择年月日与设置默认起卦方式默认时间自动保存分组等单列列表弹窗，行高比旧时间弹窗紧凑。

WuchangOptionSelectorSheet（ui/common/WuchangOptionSelectorSheet.kt）
泛型参数 options、selectedOption 对象等值比较、onDismiss、onOptionSelected、optionText。ModalBottomSheet 容器 surface，选项 18dp 圆角背景 background，已选右侧当前 primary，未选选择 AppTextHint，标准 clickable 有涟漪。用于首页起卦方式与爻值等底部唤起操作选择，不与列表弹窗混用。

WuchangSingleLineInput（ui/common/WuchangSingleLineInput.kt）
参数 value、onValueChange、placeholder、modifier。基于 BasicTextField singleLine，最小高 44dp 12dp 圆角背景 background outline 描边 alpha 0.72，空值显 placeholder（AppTextHint 14sp），输入文字 onSurface 14sp Medium。用于搜索或轻量输入，禁止直接用 OutlinedTextField。

UiExtensions（ui/common/UiExtensions.kt）
Modifier.noRippleClick(onClick)：composed 内 clickable indication null 无涟漪，全项目广泛使用。

主题颜色 token（ui/theme/Color.kt，映射见 Theme.kt）
AppPrimary 3477FF 映射 primary。AppPrimarySurface 2F66EA 映射 secondary。AppPrimaryDark 2658D7 映射 tertiary。AppPrimaryContainer F5F5F5 映射 surfaceVariant。AppBackground FFFFFF 映射 background。AppSurface White 映射 surface。AppTextPrimary 25324A 映射 onBackground 与 onSurface。AppTextSecondary 7785A2 映射 onSurfaceVariant。AppTextHint B0BBD0 独立常量不在 ColorScheme，供 WuchangSingleLineInput 与 WuchangOptionSelectorSheet 做占位与 hint。AppBorder E3E8F2 映射 outline。AppHighlightRed E53935 映射 error。
仅浅色 LightColorScheme 无深色主题。页面优先经 MaterialTheme.colorScheme 取值不散落颜色常量。

字体（ui/theme/Type.kt）
Typography 仅自定义 bodyLarge（Default 16sp 行高 24sp letterSpacing 0.5sp），其余用 Material3 默认。

主题与缩放（ui/theme/Theme.kt）
Wuchang_liuyaoTheme(appFontScale 默认 1，content)。以 360dp 短边为基准算 uiScale 为短边除 360 clamp 到 0.92 到 1.18 做自适应 UI 缩放。Density 覆写 density 乘 uiScale，fontScale 乘 appFontScale。CompositionLocal 注入 LocalDensity 与 LocalAppFontScale（staticCompositionLocalOf 默认 1）。弹窗等独立层需自行包裹主题保持字号一致。包裹 MaterialTheme 用 LightColorScheme 与 Typography。
自适应常量 AdaptiveReferenceShortestWidthDp 360、AdaptiveMinScale 0.92、AdaptiveMaxScale 1.18。

MainActivity
仅 enableEdgeToEdge 加 setContent 渲染 WuchangLiuyaoApp，主题在 WuchangLiuyaoApp 内包裹，配合 Scaffold 的 WindowInsets.safeDrawing 适配安全区。
