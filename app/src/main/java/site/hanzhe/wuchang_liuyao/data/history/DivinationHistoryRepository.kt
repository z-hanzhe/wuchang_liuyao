package site.hanzhe.wuchang_liuyao.data.history

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationDateInfo
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationLine
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationRequest
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationResult
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationSpirit
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationTableRow
import site.hanzhe.wuchang_liuyao.domain.divination.GanzhiPillar
import site.hanzhe.wuchang_liuyao.domain.time.DivinationTimeType

private const val DivinationHistoryPreferencesName = "divination_history"
private const val DivinationHistoryEntriesKey = "entries"

internal data class DivinationHistoryRecord(
    val id: String,
    val createdAtMillis: Long,
    val request: DivinationRequest,
    val result: DivinationResult,
    val currentSituation: String = "",
    val judgment: String = ""
)

internal class DivinationHistoryRepository(
    context: Context
) {
    private val sharedPreferences = context.getSharedPreferences(
        DivinationHistoryPreferencesName,
        Context.MODE_PRIVATE
    )

    fun getAllRecords(): List<DivinationHistoryRecord> {
        val rawEntries = sharedPreferences.getString(DivinationHistoryEntriesKey, null).orEmpty()
        if (rawEntries.isBlank()) {
            return emptyList()
        }
        val records = buildList {
            val entries = JSONArray(rawEntries)
            for (index in 0 until entries.length()) {
                val recordJson = entries.getJSONObject(index)
                add(recordJson.toHistoryRecord())
            }
        }
        return records.sortedByDescending { it.createdAtMillis }
    }

    fun saveRecord(
        request: DivinationRequest,
        result: DivinationResult
    ): DivinationHistoryRecord {
        val record = DivinationHistoryRecord(
            id = UUID.randomUUID().toString(),
            createdAtMillis = System.currentTimeMillis(),
            request = request,
            result = result,
            currentSituation = "",
            judgment = ""
        )
        persistRecords(getAllRecords() + record)
        return record
    }

    fun updateRecord(
        recordId: String,
        question: String,
        currentSituation: String,
        judgment: String
    ): DivinationHistoryRecord {
        val records = getAllRecords()
        var updatedRecord: DivinationHistoryRecord? = null
        val updatedRecords = records.map { record ->
            if (record.id != recordId) {
                record
            } else {
                record.copy(
                    request = record.request.copy(question = question),
                    result = record.result.copy(question = question),
                    currentSituation = currentSituation,
                    judgment = judgment
                ).also { resolvedRecord ->
                    updatedRecord = resolvedRecord
                }
            }
        }
        val resolvedRecord = updatedRecord ?: error("未找到要更新的排盘记录")
        persistRecords(updatedRecords)
        return resolvedRecord
    }

    fun deleteRecords(recordIds: Set<String>) {
        if (recordIds.isEmpty()) {
            return
        }
        persistRecords(
            getAllRecords().filterNot { record -> record.id in recordIds }
        )
    }

    private fun persistRecords(records: List<DivinationHistoryRecord>) {
        val recordsJson = JSONArray().apply {
            records.sortedByDescending { it.createdAtMillis }.forEach { record ->
                put(record.toJson())
            }
        }
        sharedPreferences.edit()
            .putString(DivinationHistoryEntriesKey, recordsJson.toString())
            .apply()
    }
}

private fun DivinationHistoryRecord.toJson(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("createdAtMillis", createdAtMillis)
        put("request", request.toJson())
        put("result", result.toJson())
        put("currentSituation", currentSituation)
        put("judgment", judgment)
    }
}

private fun JSONObject.toHistoryRecord(): DivinationHistoryRecord {
    return DivinationHistoryRecord(
        id = getString("id"),
        createdAtMillis = getLong("createdAtMillis"),
        request = getJSONObject("request").toDivinationRequest(),
        result = getJSONObject("result").toDivinationResult(),
        currentSituation = getNullableString("currentSituation").orEmpty(),
        judgment = getNullableString("judgment").orEmpty()
    )
}

private fun DivinationRequest.toJson(): JSONObject {
    return JSONObject().apply {
        put("question", question)
        put("methodLabel", methodLabel)
        put("dateInfo", dateInfo.toJson())
        put(
            "linesTopDown",
            JSONArray().apply {
                linesTopDown.forEach { line -> put(line.code) }
            }
        )
    }
}

private fun JSONObject.toDivinationRequest(): DivinationRequest {
    val linesJson = getJSONArray("linesTopDown")
    return DivinationRequest(
        question = getString("question"),
        methodLabel = getString("methodLabel"),
        dateInfo = getJSONObject("dateInfo").toDivinationDateInfo(),
        linesTopDown = buildList {
            for (index in 0 until linesJson.length()) {
                add(linesJson.getInt(index).toDivinationLine())
            }
        }
    )
}

private fun DivinationResult.toJson(): JSONObject {
    return JSONObject().apply {
        put("question", question)
        put("methodLabel", methodLabel)
        put("dateInfo", dateInfo.toJson())
        put(
            "spirits",
            JSONArray().apply {
                spirits.forEach { spirit -> put(spirit.toJson()) }
            }
        )
        put("dayXunKong", dayXunKong)
        put("baseHexagramName", baseHexagramName)
        put("palaceName", palaceName)
        putNullableString("hexagramType", hexagramType)
        putNullableString("changedHexagramName", changedHexagramName)
        putNullableString("changedPalaceName", changedPalaceName)
        putNullableString("changedHexagramType", changedHexagramType)
        put(
            "rowsTopDown",
            JSONArray().apply {
                rowsTopDown.forEach { row -> put(row.toJson()) }
            }
        )
    }
}

private fun JSONObject.toDivinationResult(): DivinationResult {
    val spiritsJson = getJSONArray("spirits")
    val rowsJson = getJSONArray("rowsTopDown")
    return DivinationResult(
        question = getString("question"),
        methodLabel = getString("methodLabel"),
        dateInfo = getJSONObject("dateInfo").toDivinationDateInfo(),
        spirits = buildList {
            for (index in 0 until spiritsJson.length()) {
                add(spiritsJson.getJSONObject(index).toDivinationSpirit())
            }
        },
        dayXunKong = getString("dayXunKong"),
        baseHexagramName = getString("baseHexagramName"),
        palaceName = getString("palaceName"),
        hexagramType = getNullableString("hexagramType"),
        changedHexagramName = getNullableString("changedHexagramName"),
        changedPalaceName = getNullableString("changedPalaceName"),
        changedHexagramType = getNullableString("changedHexagramType"),
        rowsTopDown = buildList {
            for (index in 0 until rowsJson.length()) {
                add(rowsJson.getJSONObject(index).toDivinationTableRow())
            }
        }
    )
}

private fun DivinationDateInfo.toJson(): JSONObject {
    return JSONObject().apply {
        put("displayText", displayText)
        put("timeType", timeType.name)
        put("solarText", solarText)
        put("lunarText", lunarText)
        put("ganzhiText", ganzhiText)
        put("termText", termText)
        put("year", year.toJson())
        put("month", month.toJson())
        put("day", day.toJson())
        put("hour", hour.toJson())
    }
}

private fun JSONObject.toDivinationDateInfo(): DivinationDateInfo {
    return DivinationDateInfo(
        displayText = getString("displayText"),
        timeType = DivinationTimeType.valueOf(getString("timeType")),
        solarText = getString("solarText"),
        lunarText = getString("lunarText"),
        ganzhiText = getString("ganzhiText"),
        termText = optString("termText"),
        year = getJSONObject("year").toGanzhiPillar(),
        month = getJSONObject("month").toGanzhiPillar(),
        day = getJSONObject("day").toGanzhiPillar(),
        hour = getJSONObject("hour").toGanzhiPillar()
    )
}

private fun GanzhiPillar.toJson(): JSONObject {
    return JSONObject().apply {
        putNullableString("heavenlyStem", heavenlyStem)
        putNullableString("earthlyBranch", earthlyBranch)
    }
}

private fun JSONObject.toGanzhiPillar(): GanzhiPillar {
    return GanzhiPillar(
        heavenlyStem = getNullableString("heavenlyStem"),
        earthlyBranch = getNullableString("earthlyBranch")
    )
}

private fun DivinationSpirit.toJson(): JSONObject {
    return JSONObject().apply {
        put("name", name)
        put("value", value)
    }
}

private fun JSONObject.toDivinationSpirit(): DivinationSpirit {
    return DivinationSpirit(
        name = getString("name"),
        value = getString("value")
    )
}

private fun DivinationTableRow.toJson(): JSONObject {
    return JSONObject().apply {
        put("sixGod", sixGod)
        put("hiddenRelative", hiddenRelative)
        put("hiddenStem", hiddenStem)
        put("hiddenBranch", hiddenBranch)
        put("hiddenElement", hiddenElement)
        put("baseRelative", baseRelative)
        put("baseStem", baseStem)
        put("baseBranch", baseBranch)
        put("baseElement", baseElement)
        put("baseLineSymbol", baseLineSymbol)
        put("shiYingMark", shiYingMark)
        put("changingSymbol", changingSymbol)
        put("changedLineSymbol", changedLineSymbol)
        put("changedRelative", changedRelative)
        put("changedStem", changedStem)
        put("changedBranch", changedBranch)
        put("changedElement", changedElement)
    }
}

private fun JSONObject.toDivinationTableRow(): DivinationTableRow {
    return DivinationTableRow(
        sixGod = getString("sixGod"),
        hiddenRelative = getString("hiddenRelative"),
        hiddenStem = getString("hiddenStem"),
        hiddenBranch = getString("hiddenBranch"),
        hiddenElement = getString("hiddenElement"),
        baseRelative = getString("baseRelative"),
        baseStem = getString("baseStem"),
        baseBranch = getString("baseBranch"),
        baseElement = getString("baseElement"),
        baseLineSymbol = getString("baseLineSymbol"),
        shiYingMark = getString("shiYingMark"),
        changingSymbol = getString("changingSymbol"),
        changedLineSymbol = getString("changedLineSymbol"),
        changedRelative = getString("changedRelative"),
        changedStem = getString("changedStem"),
        changedBranch = getString("changedBranch"),
        changedElement = getString("changedElement")
    )
}

private fun Int.toDivinationLine(): DivinationLine {
    return DivinationLine.entries.firstOrNull { line -> line.code == this }
        ?: error("未知爻值编码: $this")
}

private fun JSONObject.putNullableString(key: String, value: String?) {
    put(key, value ?: JSONObject.NULL)
}

private fun JSONObject.getNullableString(key: String): String? {
    return if (isNull(key)) null else getString(key)
}
