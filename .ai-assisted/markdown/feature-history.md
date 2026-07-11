排盘历史页 feature/history

文件 HistoryViewModel.kt、HistoryScreen.kt。持久化见 data-history.md。

HistoryUiState 字段
isLoading。groups 全部分组。records 全部记录跨分组。selectedGroupId 可空 当前打开分组（空表示在分组列表页）。searchKeyword 生效关键词。searchDraft 输入草稿。isSearchDialogVisible。isRecordSelectionMode 记录多选。selectedRecordIds 已选记录集合。isGroupSelectionMode 分组多选。selectedGroupIds 已选分组集合。transientMessage 可空。

扩展函数
filteredRecords 按 selectedGroupId 筛选再按 searchKeyword 对 displayQuestion 忽略大小写子串匹配。currentGroup 当前分组。groupRecordCount 分组记录数。selectedEditableGroups 已选非系统分组。selectedGroups 已选全部分组。DivinationHistoryRecord.displayQuestion 取 result.question 空时返回无。

HistoryViewModel 方法
refresh IO 加载分组与记录，对已选 id 做交集保留防引用已删。
搜索：openSearchDialog、updateSearchDraft、confirmSearch（trim 生效）、dismissSearchDialog、clearSearch。
分组导航：openGroup（清搜索与选择）、closeCurrentGroup。
分组多选：enterGroupSelectionMode、toggleGroupSelection、exitGroupSelectionMode、toggleSelectAllGroups。
分组管理：createGroup（校验非空不重复返回布尔）、renameSelectedGroup（仅单个非系统）、moveSelectedGroupRecords、deleteSelectedGroups（系统分组不可删提示默认分组不能删除）、moveGroupByOffset（拖拽排序乐观更新失败 refresh 回退，index 0 固定默认分组，系统分组不参与排序）。
记录多选：enterSelectionMode、toggleRecordSelection、exitSelectionMode、toggleSelectAllFilteredRecords、invertFilteredRecordsSelection、deleteSelectedRecords、moveSelectedRecords。
consumeTransientMessage 消费消息。

HistoryScreen 结构
Scaffold 加 BackHandler 分层返回：分组多选到记录多选到搜索到分组详情到退出。
顶栏：分组列表页非多选显示新增，记录列表页非多选显示搜索，多选模式不显示。
底栏条件渲染：分组多选 GroupSelectionBar 全选重命名移动至删除；记录多选 RecordSelectionBar 全选反选移动删除。
内容：加载中占位、currentGroup 空显示 GroupListContent、筛选为空或有记录显示 HistoryRecordsContent。

多选交互约定
记录长按 enterSelectionMode 进入多选并选中被长按项，多选中单击切换选中，点击返回退出多选。
全选针对当前筛选结果：仍有未选则全选，已全选则再点取消这些选中。反选针对当前筛选结果已选变未选未选变已选，均不影响不可见记录选中状态。
删除必须先弹 WuchangConfirmDialog 确认再批量删。
分组多选长按进入，重命名要求仅选中一个非系统分组。分组删除先过滤非系统分组弹确认显示分组名与记录数，删除分组同时删其下全部记录，系统默认分组不可删。

其他交互
搜索：顶栏搜索按钮开 HistorySearchDialog，确认后 SearchResultHeader 显示搜索结果与取消搜索，BackHandler 优先清搜索。
新增分组 GroupNameDialog 校验后 createGroup。重命名分组底栏重命名 GroupNameDialog。移动分组记录或移动记录用 MoveTargetGroupDialog 选目标再二次确认。
分组排序：分组多选下非系统分组右侧 GroupDragHandle 拖拽手柄，detectDragGestures 累积超 42dp 阈值触发 onGroupMove 上下移。
记录项 HistoryRecordItem：首行问念粗体 14sp 单行截断，次行创建时间加本卦名到变卦名 11sp 灰，多选显 Checkbox，combinedClickable 支持单击与长按。

编辑入口
历史页无直接编辑入口，点击记录导航到结果页，已保存卦例结果页顶栏显示编辑按钮，编辑流程见 feature-result.md。
