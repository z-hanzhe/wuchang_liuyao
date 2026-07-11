历史记录持久化 data/history

文件 data/history/DivinationHistoryRepository.kt。

存储方式
Android SharedPreferences 文件名 divination_history，非数据库。两个 key 各存一个 JSON 字符串：entries 为记录 JSONArray，groups 为分组 JSONArray。

数据模型与序列化
DivinationHistoryGroup：id String（默认分组固定 default）、name、sortOrder Int、isSystem Boolean。JSON 字段 id name sortOrder isSystem。
DivinationHistoryRecord：id UUID、createdAtMillis Long、groupId 默认 default、request DivinationRequest、result DivinationResult、currentSituation、judgment。JSON 字段同名。
DivinationRequest 序列化：question、methodLabel、dateInfo 嵌套、linesTopDown 为六个爻 code 的 Int 数组（1 少阳 2 少阴 3 老阳 4 老阴）。
DivinationResult 序列化：question、methodLabel、dateInfo、spirits（name value 数组）、dayXunKong、baseHexagramName、palaceName、hexagramType 可空、changedHexagramName 可空、changedPalaceName 可空、changedHexagramType 可空、rowsTopDown 六行。
DivinationDateInfo 序列化：displayText、timeType 枚举名、solarText、lunarText、ganzhiText、termText、year month day hour 各为 heavenlyStem 与 earthlyBranch 可空对象。
DivinationTableRow 序列化 17 字段：sixGod、hiddenRelative hiddenStem hiddenBranch hiddenElement、baseRelative baseStem baseBranch baseElement baseLineSymbol、shiYingMark、changingSymbol、changedLineSymbol、changedRelative changedStem changedBranch changedElement。
保存稳定业务数据不只存展示字符串，展示文案由 UI 或领域格式化生成。

重复性判断
saveRecord 保存前遍历已有记录调 DivinationRequest.isSameDivination 判重，三条件全等则返回已有不新增：question 相同；dateInfo.isSameDivinationTime 相同（比较 timeType solarText lunarText ganzhiText year month day hour 八字段）；linesTopDown 相同（比较爻 code）。

分组规范化 normalizeGroups
默认分组始终存在 id default 名默认分组 sortOrder 0 isSystem true，JSON 缺失或损坏也恢复。自定义分组按 sortOrder 再 name 排序 sortOrder 从 1 重编号。默认分组名与 isSystem 读取时强制覆盖不受用户改动。

仓库公共方法
getAllGroups 读并 normalize。getAllRecords 读并按 createdAtMillis 降序。saveRecord 保存新记录先判重 groupId 不存在回退默认。updateRecord 更新 question（同步写 request.question 与 result.question）与 currentSituation judgment。deleteRecords 批量删。createGroup 校验非空不重复 sortOrder 为当前分组数。renameGroup 校验不重复系统分组不可改。moveRecordsToGroup 源分组记录移到目标。moveRecords 指定记录移到目标。deleteGroups 删分组及其下记录默认分组过滤保留。reorderGroups 按传入顺序排列默认分组恒第一未出现的追加末尾。
IO 操作在协程，不放主线程。
