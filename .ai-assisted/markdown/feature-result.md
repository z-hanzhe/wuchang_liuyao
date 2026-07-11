排盘结果页 feature/result

文件 ResultViewModel.kt、ResultScreen.kt。

ResultViewModel 状态
ResultGenerationStatus 密封 Success 与 Failure(message)。
ResultEditDraft：question currentSituation judgment 三字段编辑草稿。
ResultUiState：result 排盘结果可空，request 请求可空，savedRecordId 可空，isSaved，isSaving，currentSituation，judgment，editDraft 可空（非空则编辑弹窗可见），transientMessage 可空。
派生 showSaveAction 为 result request 存在且未保存未保存中；showEditAction 为存在且已保存且有 savedRecordId 且未保存中。保存与编辑按钮互斥。

ResultViewModel 方法
showResult(request, autoSaveDivinationMode, autoSaveHistoryGroupId)：调 HexagramCalculator.calculate 计算并重置状态，按自动保存模式决定是否自动存档，返回 Success 或 Failure。
showSavedResult(record)：从历史打开已保存卦例，填充 result request currentSituation judgment 并标记 isSaved 真。
saveCurrentResult：手动保存到历史仓库（IO），成功更新 savedRecordId 与 isSaved 并发保存成功消息。
openEditDialog：仅已保存且有 savedRecordId 时生效，用当前值初始化 editDraft。dismissEditDialog 关闭。
updateEditQuestion、updateEditCurrentSituation、updateEditJudgment 更新草稿。
saveEditDraft：调 historyRepository.updateRecord 回写（IO），成功刷新 result request currentSituation judgment 并清草稿发消息。
consumeTransientMessage 消费消息。
私有 autoSaveCurrentResultIfNeeded 按 AutoSaveDivinationMode（OFF、QUESTION_NOT_EMPTY、ALWAYS）判断，updateAfterAutoSave 带竞态保护乐观更新。

编辑功能
弹窗三字段：问念单行输入，现状多行输入最小 110dp 占位可补充现状对轨信息，断语多行输入最小 110dp 占位在这里添加断语。
仅已保存卦例显示编辑按钮，未保存只显示保存，二者互斥。
编辑弹窗只能通过底部保存或取消关闭，不允许点背景或返回直接关闭。
可编辑并持久化三字段问念现状断语，问念改动同步影响结果页展示与历史列表标题（updateRecord 内同步写 request.question 与 result.question）。

ResultScreen 结构
Scaffold 顶栏 ResultTopAppBar（保存或编辑按钮互斥）加 BackHandler，内容垂直滚动 Column padding start12 top18 end12。
Column 依次：问念文本 14sp、现状文本（非空显示）、断语文本（非空显示）、神煞面板 ResultSpiritsPanel（每行最多 5 个溢出降 4）、时间面板（公历行、农历行去末尾时分附节气、干支四柱行月日柱红色旬空括号红色）、圆角 18dp 卡片内 BoxWithConstraints 包 ResultGridTable。
编辑弹窗 ResultEditDialog 在 editDraft 非空时弹出。

表格实现
用 Android 原生 TableLayout 经 AndroidView 嵌入，外包 HorizontalScrollView 支持水平溢出，原因是统一测量列宽避免 Compose 手写表格列宽漂移。
视觉行 13 行：1 表头行加 6 数据行加 6 提示行（每数据行下带一提示行）。数据行对应上爻到初爻。

列结构最多 6 逻辑列按可见性动态显隐
六神列始终可见。
伏神组列（伏神六亲天干地支五行）在任一爻伏神有内容时可见 visibleColumns.hiddenGroup。
本卦爻线加六亲天干地支五行组列始终可见。
世应标记列始终可见。
动爻符号列（o 或 x）在任一爻有动爻时可见 visibleColumns.changing。
变卦爻线加六亲天干地支五行组列在有变卦时可见。

表头合并
宫名 span 为伏神列可见时 2 否则 1。本卦名 span 2 合并爻线与世应两列。变卦名 span 由 changing 与变卦列可见性决定最多 2。

列间距 ResultColumnGap 7dp 设在单元格 rightMargin。行间距数据行 16dp，提示行 2dp。

爻线绘制
阳爻一条实色 View 宽 34dp 高 7dp。阴爻两段各宽 13dp 中间隔 7dp。尺寸随 fontScale 按系数 1 加 (fontScale 减 1) 乘 1.35 缩放并 clamp 到 0.78 到 1.72。

点击高亮（clickHighlightHint 开关控制）
点击地支单元格设 branchSelection（branch 加 cellId 如 base 加行索引）。该地支在全表匹配 resolveBranchHighlightType：SAME 同支淡绿，CLASH 六冲淡红，COMBINE 六合淡黄，各有背景与文字色。神煞面板用 resolveBranchesHighlightType 对多字符值逐字匹配。点同单元格取消，点空白清空，被选单元格文字变非粗体区分。

旬空标记（markBranchXunKong 开关）
地支在日旬空中时加蓝灰底色。

小字提示行渲染
每数据行下提示行 9sp 红色（colorScheme.error），内容按 tag 过滤：月破月合日冲日合需 showDayMonthChongHeHint，回头生回头克需 showHuiTouShengKeHint，回头冲回头合需 showHuiTouChongHeHint。伏神提示左对齐，本卦与变卦提示右对齐，最小高度 13dp。
特例：动爻列与变卦列同时可见时变卦提示单元格 span 2 合并动爻列与变卦列，仅提示行此处理其他行列结构不变。

设置开关影响展示
compactSixGod 六神缩写龙玄虎蛇勾雀且宫名去宫字。showHeavenlyStem 控制天干渲染并影响伏神与本卦是否相同判断。showAllHiddenLines 为假仅显示六亲不在本卦六亲集合的伏神，为真显示全部与本卦不同的伏神。compactSixRelative 六亲缩写父兄孙财官。三类冲合回头提示与旬空标记与点击高亮见上。

数据类
ResultVisibleColumns、ResultBranchSelection、ResultDisplayOptions（8 布尔开关）、ResultGroupContent、ResultTableViewStyle（像素级样式）、ResultHintRowContent、ResultBranchHighlightPalette（三态背景与文字色）。
