无常六爻排盘 功能文档索引

本目录为功能细节文档库，通用工作规范见 AGENTS.md / CLAUDE.md 的 Prompt 节，此处不重复。

文档清单与用途（每篇末标注何时读）
architecture.md：整体架构、分层、数据流、模块依赖、ViewModel 共享。改跨层结构或新增页面前读。
build-and-run.md：构建运行命令、依赖版本、SDK、签名、目录结构。涉及依赖或构建前读。
navigation.md：路由常量、NavHost、切换动画、首页双击退出、跨页面副作用、请求组装入口。改导航或跳转前读。
domain-time.md：时间模型、时间类型、时辰干支推算、格式化。改时间模型或干支推算前读。
data-calendar.md：公农历干支节气仓库、lunar-java 交互、选项过滤、错误建模。改历法计算前读。
domain-divination.md：排盘核心，起卦与时间输入约定、卦码世应卦宫纳甲六亲六神伏神变卦算法、神煞旬空、冲合回头与高亮提示、规则表。改任何排盘规则前必读。
feature-home.md：首页录入，UiState、五种起卦录入交互、农历卡片、爻值存储顺序。改首页前读。
feature-time.md：时间选择，双快照编辑、公农历干支三模式与切换、级联过滤、干支阴阳兼容。改时间选择前读。
feature-result.md：结果页，7 行 17 列表格、列顺序与合并、点击高亮、旬空与冲合回头小字提示、编辑弹窗、原生 TableLayout。改结果展示前必读。
feature-history.md：历史页，分组与记录多选、全选反选删除、搜索、移动、拖拽排序。改历史交互前读。
feature-settings.md：设置三页、全部设置项分组归属 key 默认值与效果、字体缩放、自动保存。改设置项前必读。
data-history.md：历史持久化，SharedPreferences JSON 格式、序列化字段、重复性判断、分组规范化、仓库 API。改历史存储前必读。
ui-common-theme.md：公共组件 API 与复用规范、颜色 token、字体与自适应缩放。改 UI 组件或视觉 token 前读。

任务阅读顺序建议
排盘规则先 domain-divination 再 feature-result；时间先 domain-time 与 data-calendar 再 feature-time；存储设置看 feature-settings 历史看 data-history；新增页面先 architecture 与 navigation 再 ui-common-theme。
