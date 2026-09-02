# 设计系统

## 主题边界

- 应用仅提供浅色主题，主题入口是 `ui/theme/Theme.kt`；颜色与文字样式集中在 `ui/theme/`，页面优先通过 `MaterialTheme` 获取。
- 主题按屏幕短边做有限自适应，并叠加用户字体比例。独立 Dialog、Sheet 或其他弹层必须保持同一主题和字体缩放上下文。
- 应用采用 edge-to-edge，页面外壳通过系统安全区域或既有 Scaffold 统一处理系统栏，不手写固定状态栏占位。
- 页面布局可以按需求调整，只有主题策略、公共组件职责或跨页面交互模式变化时才更新本文；颜色值、尺寸和动画参数以代码为准。

## 公共组件

| 入口 | 使用场景 |
| --- | --- |
| `WuchangTopAppBar` | 页面统一顶栏及返回/操作区域 |
| `WuchangConfirmDialog` | 提示、确认和不可逆操作确认 |
| `WuchangOptionPickerDialog` | 时间、默认项、分组等单列选项 |
| `WuchangOptionSelectorSheet` | 起卦方式等简单文本底部操作选择 |
| `WuchangSingleLineInput` | 搜索、命名等轻量单行输入 |
| `SettingsComponents.kt` | 设置页骨架、分组和设置行 |
| `UiExtensions.noRippleClick` | 明确需要无涟漪反馈的点击区域 |

- 爻值选择包含爻线和动爻标记，当前由首页的专用 Sheet 负责，不属于通用文本选择器。
- 新界面先核对现有公共组件是否表达相同交互语义；只有职责不同或出现稳定复用需求时新增组件，不以视觉近似作为唯一抽象依据。
