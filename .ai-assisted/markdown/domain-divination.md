排盘领域 domain/divination

三个文件：HexagramCalculator.kt 纯计算入口与全部规则表，DivinationModels.kt 输入输出模型，DivinationPromptHints.kt 提示与高亮计算。全部无 Android 依赖。改任何排盘规则前必须先核对规则来源，禁止凭记忆调整。规则表实现参考开源项目 najia，但运行时不依赖该库，只保留本地 Kotlin 实现。lunar-java 只负责时间干支，不负责排盘规则。

主入口
HexagramCalculator.calculate(request: DivinationRequest): DivinationResult，可见性 internal。

输入模型 DivinationModels
DivinationLine 枚举：SHAO_YANG 少阳 code1，SHAO_YIN 少阴 code2，LAO_YANG 老阳 code3，LAO_YIN 老阴 code4。属性 isYang（code 奇数为阳），isChanging（code 大于 2 为动爻），baseMark（阳 1 阴 0），changedMark（少阳老阴为 1，少阴老阳为 0，即老阳变阴老阴变阳），changeSymbol（老阳 o，老阴 x，不动空）。
GanzhiPillar：heavenlyStem 天干可空，earthlyBranch 地支可空，text 拼接文本。年月日时四柱。
DivinationDateInfo：displayText，timeType，solarText，lunarText，ganzhiText，termText，year month day hour 四个 GanzhiPillar。
DivinationSpirit：name 神煞名，value 值。
DivinationRequest：question 问念，methodLabel 起卦方式标签，dateInfo，linesTopDown 六爻从上到下。
DivinationTableRow：一行 17 字段。sixGod 六神；hiddenRelative hiddenStem hiddenBranch hiddenElement 伏神；baseRelative baseStem baseBranch baseElement baseLineSymbol 本卦；shiYingMark 世应（世应或空）；changingSymbol 动爻（o 或 x 或空）；changedLineSymbol changedRelative changedStem changedBranch changedElement 变卦。
DivinationResult：question，methodLabel，dateInfo，spirits，dayXunKong 日旬空，baseHexagramName palaceName hexagramType 本卦名宫卦型，changedHexagramName changedPalaceName changedHexagramType 变卦对应，rowsTopDown 六行从上到下。

起卦输入约定
HomeUiState 的 selectedYaoValues 与 selectedCoinValues 存储顺序为界面顺序即上爻到五四三二初爻，计算卦码前必须反转成初爻到上爻。
爻名起卦默认值自下而上少阳少阳少阳老阴少阳少阳。
铜钱起卦映射固定：背正正为少阳，背背正为少阴，背背背为老阳，正正正为老阴。
点选起卦映射：isYang 且 isMoving 老阳，仅 isYang 少阳，仅 isMoving 老阴，都否少阴。
在线摇卦按背面数量：三背老阳，二背少阴，一背少阳，零背老阴。
自动起卦对每爻 Random.nextInt(4) 均匀映射四象。

时间输入约定
公历农历起卦时干支统一通过 lunar-java 现算不手写推导。排盘取年柱 yearGanExact/yearZhiExact，月柱 monthGanExact/monthZhiExact，日柱 dayGan/dayZhi，时柱 timeGan/timeZhi。
干支起卦校验：月份地支不能为空，日辰干支不能为空。年柱与时柱允许为空，但日干不能为空，因为六神起法依赖日干。

计算顺序
校验爻数为 6，反转为从下到上 linesBottomUp。
生成本卦码 baseMark：每爻取 baseMark 属性拼 6 位字符串，初爻在字符串低位左端。少阳老阳记 1，少阴老阴记 0。
生成变卦码 changedMark：存在动爻时每爻取 changedMark，少阳老阴变后 1，少阴老阳变后 0。
寻世应 resolveShiYing 返回世爻位与应爻位。
认宫 resolvePalaceIndex 返回 0 到 7 宫索引。
buildHexagram 用卦码与宫位构造 HexagramValue 含纳甲六亲五行。
排六神 resolveSixGods 按日干定起始位轮转。
排神煞 resolveCommonSpirits 计算 13 种。
排旬空 resolveDayXunKong 按日柱算空亡二地支。
排伏神 resolveHiddenLines 取本宫纯卦补缺失六亲。
变卦仅在有动爻时生成，变卦独立算世应宫位卦型，但六亲沿用本卦卦宫五行计算不按变卦自身卦宫重定。
组装 rowsTopDown 六行返回 DivinationResult。

寻世诀 resolveShiYing
卦码前三位为内卦后三位为外卦，index 0 初四、1 二五、2 三上。
三爻上爻同且二五异初四异，世二应五；三上异但二五同初四同，世五应二。
二五同且初四异三上异，世四应一；二五异但初四同三上同，世三应六。
初四同且二五异三上异，世四应一；初四异但二五同三上同，世一应四。
内外卦全同八纯卦，世六应三，兜底世三应六。

认宫诀 resolvePalaceIndex
先 resolveSoulType 判游魂归魂。归魂卦宫为内卦在 TrigramMarks 索引。世爻在 1236 宫为外卦索引。世爻在 45 或游魂卦宫为内卦取反后索引。

卦型识别
resolveSoulType：二五同且初四异三上异为游魂；初四同三上同但二五异为归魂；否则空。
resolveHexagramType：先游魂归魂，再 isSixClash 为真则六冲（八纯卦或内外卦为 100 与 111 组合），否则卦名含六合卦名表任一字则六合。
六合卦名表：否困旅豫节贲复泰。

纳甲 resolveNaJia
内外卦分别在 TrigramMarks 查索引，内卦用 PalaceNaJia 该索引 first，外卦用 second，各得天干加三地支组三个 GanZhiValue，合六个（初爻到上爻）。
PalaceNaJia 八宫表（内卦天干三地支，外卦天干三地支）：乾 甲子寅辰 壬午申戌；兑 丁巳卯丑 丁亥酉未；离 己卯丑亥 己酉未巳；震 庚子寅辰 庚午申戌；巽 辛丑亥酉 辛未巳卯；坎 戊寅辰午 戊申戌子；艮 丙辰午申 丙戌子寅；坤 乙未巳卯 癸丑亥酉。

六亲 resolveSixRelative
五行序列木火土金水，取宫五行索引减爻地支五行索引对 5 取模，映射六亲序列兄弟父母官鬼妻财子孙。

六神 resolveSixGods
按日干定起始：甲乙青龙 0，丙丁朱雀 1，戊勾陈 2，己螣蛇 3，庚辛白虎 4，壬癸玄武 5，初爻到上爻依次取起始加 i 对 6 取模。六神序列青龙朱雀勾陈螣蛇白虎玄武。

伏神 resolveHiddenLines
用 TrigramMarks 该宫索引重复两次成本宫纯卦码，对其纳甲加六亲得六个 HiddenLineValue（relative stem branch element）。结果页再按设置决定显示全部伏神还是仅缺失六亲的伏神。

旬空 resolveDayXunKong
取日柱天干地支索引校验奇偶一致，地支索引小于等于天干索引则加 12，旬空索引为差除 2 减 1，查 XunKongBranches 返回两地支。XunKongBranches：子丑寅卯辰巳午未申酉戌亥六对。

神煞 resolveCommonSpirits 共 13 种
按日干查表：禄神 LuByStem，羊刃 YangBladeByStem，文昌 WenChangByStem，贵人 NoblemanByStem 返回两地支。
按日支三合组查：驿马桃花将星劫煞灾煞华盖谋星。三合组申子辰、巳酉丑、寅午戌、亥卯未。
按月支查：天医（月支索引加 11 对 12 取模），天喜（按季节分组寅卯辰对戌、巳午未对丑、申酉戌对辰、亥子丑对未）。

提示与高亮 DivinationPromptHints
buildDivinationTableRowHints(result, row) 返回 DivinationTableRowHints：hiddenPromptTags basePromptTags changedPromptTags 三处提示标签，hiddenXunKong baseXunKong changedXunKong 三处旬空布尔。
日月冲合标签 buildDayMonthPromptTags：地支与月支六冲月破、与月支六合月合、与日支六冲日冲、与日支六合日合。
回头标签 buildHuiTouPromptTags 仅动爻行：变卦五行生本卦回头生、克回头克，变卦地支冲本卦回头冲、合回头合。
BranchHighlightType 枚举 NONE SAME CLASH COMBINE。resolveBranchHighlightType 单地支与选中地支判定，resolveBranchesHighlightType 多地支文本逐字判定，优先级 SAME 大于 CLASH 大于 COMBINE 大于 NONE。
内置表：地支六冲子午丑未寅申卯酉辰戌巳亥，地支六合子丑寅亥卯戌辰酉巳申午未，五行相生木火土金水循环，五行相克木土火金土水金木水火，十二地支集合。

内置规则表清单
六神序列、六亲序列、五行序列、八宫名乾兑离震巽坎艮坤、八宫五行金金火木木水土土、八经卦二进制 TrigramMarks、天干地支与地支五行、旬空 XunKongBranches、贵人禄神羊刃文昌查表、纳甲 PalaceNaJia、六合卦名、六十四卦名 HexagramNames（6 位二进制到卦名全称）。全部固化在 HexagramCalculator.kt，运行时不查库。
