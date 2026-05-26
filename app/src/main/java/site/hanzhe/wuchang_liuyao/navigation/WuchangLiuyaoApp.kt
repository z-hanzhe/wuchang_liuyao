package site.hanzhe.wuchang_liuyao.navigation

import android.app.Activity
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import site.hanzhe.wuchang_liuyao.feature.history.HistoryScreen
import site.hanzhe.wuchang_liuyao.feature.history.HistoryViewModel
import site.hanzhe.wuchang_liuyao.feature.home.HomeScreen
import site.hanzhe.wuchang_liuyao.feature.home.HomeViewModel
import site.hanzhe.wuchang_liuyao.feature.result.ResultGenerationStatus
import site.hanzhe.wuchang_liuyao.feature.result.ResultScreen
import site.hanzhe.wuchang_liuyao.feature.result.ResultViewModel
import site.hanzhe.wuchang_liuyao.feature.settings.DivinationSettingsScreen
import site.hanzhe.wuchang_liuyao.feature.settings.GlobalSettingsScreen
import site.hanzhe.wuchang_liuyao.feature.settings.SettingsScreen
import site.hanzhe.wuchang_liuyao.feature.settings.SettingsViewModel
import site.hanzhe.wuchang_liuyao.feature.time.SelectTimeScreen
import site.hanzhe.wuchang_liuyao.feature.time.TimeSelectionViewModel
import site.hanzhe.wuchang_liuyao.ui.common.WuchangConfirmDialog
import site.hanzhe.wuchang_liuyao.ui.theme.Wuchang_liuyaoTheme

private object AppRoute {
    const val Home = "home"
    const val SelectTime = "select_time"
    const val Settings = "settings"
    const val GlobalSettings = "settings/global"
    const val DivinationSettings = "settings/divination"
    const val History = "history"
    const val Result = "result"
}

private const val ExitConfirmIntervalMillis = 2_000L

@Composable
internal fun WuchangLiuyaoApp(
    homeViewModel: HomeViewModel = viewModel(),
    timeSelectionViewModel: TimeSelectionViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    historyViewModel: HistoryViewModel = viewModel(),
    resultViewModel: ResultViewModel = viewModel()
) {
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val timeUiState by timeSelectionViewModel.uiState.collectAsStateWithLifecycle()
    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val historyUiState by historyViewModel.uiState.collectAsStateWithLifecycle()
    val resultUiState by resultViewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(timeUiState.startupFailure) {
        val startupFailure = timeUiState.startupFailure ?: return@LaunchedEffect
        Toast.makeText(context, startupFailure.message, Toast.LENGTH_LONG).show()
        activity?.finishAffinity()
    }

    LaunchedEffect(timeUiState.transientMessage) {
        val transientMessage = timeUiState.transientMessage ?: return@LaunchedEffect
        Toast.makeText(context, transientMessage, Toast.LENGTH_SHORT).show()
        timeSelectionViewModel.consumeTransientMessage()
    }

    LaunchedEffect(timeUiState.shouldCloseTimeSelection) {
        if (!timeUiState.shouldCloseTimeSelection) {
            return@LaunchedEffect
        }
        if (navController.currentBackStackEntry?.destination?.route == AppRoute.SelectTime) {
            navController.popBackStack()
        }
        timeSelectionViewModel.consumeTimeSelectionCloseRequest()
    }

    LaunchedEffect(historyUiState.transientMessage) {
        val transientMessage = historyUiState.transientMessage ?: return@LaunchedEffect
        Toast.makeText(context, transientMessage, Toast.LENGTH_SHORT).show()
        historyViewModel.consumeTransientMessage()
    }

    LaunchedEffect(historyUiState.groups) {
        if (!historyUiState.isLoading) {
            settingsViewModel.refreshAutoSaveHistoryGroups()
        }
    }

    LaunchedEffect(resultUiState.transientMessage) {
        val transientMessage = resultUiState.transientMessage ?: return@LaunchedEffect
        Toast.makeText(context, transientMessage, Toast.LENGTH_SHORT).show()
        resultViewModel.consumeTransientMessage()
    }

    LaunchedEffect(settingsUiState.defaultDivinationMethod) {
        homeViewModel.selectMethod(settingsUiState.defaultDivinationMethod)
    }

    LaunchedEffect(settingsUiState.defaultDivinationTimeType, timeUiState.isLoading) {
        if (!timeUiState.isLoading) {
            timeSelectionViewModel.applyDefaultTimeType(settingsUiState.defaultDivinationTimeType)
        }
    }

    if (timeUiState.isLoading || timeUiState.startupFailure != null) {
        Wuchang_liuyaoTheme(
            appFontScale = settingsUiState.fontScale
        ) {
            StartupLoadingScreen()
        }
        return
    }

    Wuchang_liuyaoTheme(
        appFontScale = settingsUiState.fontScale
    ) {
        homeUiState.dialogMessage?.let { dialogMessage ->
            WuchangConfirmDialog(
                title = "提示",
                message = dialogMessage,
                confirmText = "确定",
                dismissText = null,
                onConfirm = homeViewModel::dismissDialogMessage,
                onDismiss = homeViewModel::dismissDialogMessage
            )
        }

        NavHost(
            navController = navController,
            startDestination = AppRoute.Home,
            enterTransition = {
                if (targetState.destination.route != AppRoute.Home) {
                    slideInHorizontally(
                        animationSpec = tween(durationMillis = 220)
                    ) { fullWidth ->
                        fullWidth
                    }
                } else {
                    EnterTransition.None
                }
            },
            exitTransition = {
                ExitTransition.None
            },
            popEnterTransition = {
                EnterTransition.None
            },
            popExitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(durationMillis = 220)
                ) { fullWidth ->
                    fullWidth
                }
            }
        ) {
            composable(AppRoute.Home) {
                val lastBackPressElapsedTime = remember { mutableStateOf(0L) }

                BackHandler {
                    val now = SystemClock.elapsedRealtime()
                    // 首页双击返回直接结束 Activity，避免退到后台
                    if (now - lastBackPressElapsedTime.value <= ExitConfirmIntervalMillis) {
                        activity?.finish()
                    } else {
                        lastBackPressElapsedTime.value = now
                        Toast.makeText(context, "再次点击返回键退出应用", Toast.LENGTH_SHORT).show()
                    }
                }

                HomeScreen(
                    uiState = homeUiState,
                    showLunarInfo = settingsUiState.showLunarInfo,
                    calendarSummary = timeUiState.calendarSummary,
                    divinationTime = timeUiState.divinationTime,
                    onQuestionChange = homeViewModel::onQuestionChange,
                    onTimeClick = {
                        timeSelectionViewModel.openTimeSelection()
                        navController.navigate(AppRoute.SelectTime)
                    },
                    onCalendarSummaryClick = timeSelectionViewModel::refreshCalendarSummary,
                    onSettingsClick = { navController.navigate(AppRoute.Settings) },
                    onStartDivinationClick = {
                        when (
                            val requestResult = buildDivinationRequest(
                                homeUiState = homeUiState,
                                timeUiState = timeUiState,
                                changeDayPillarAt23 = settingsUiState.changeDayPillarAt23
                            )
                        ) {
                            is DivinationRequestBuildResult.Failure -> {
                                homeViewModel.showDialogMessage(requestResult.message)
                            }

                            is DivinationRequestBuildResult.Success -> {
                                when (
                                    resultViewModel.showResult(
                                        request = requestResult.request,
                                        autoSaveDivinationMode = settingsUiState.autoSaveDivinationMode,
                                        autoSaveHistoryGroupId = settingsUiState.autoSaveHistoryGroupId
                                    )
                                ) {
                                    is ResultGenerationStatus.Failure -> {
                                        homeViewModel.showDialogMessage("排盘失败，请检查输入后重试")
                                    }

                                    ResultGenerationStatus.Success -> {
                                        navController.navigate(AppRoute.Result)
                                    }
                                }
                            }
                        }
                    },
                    onMethodClick = homeViewModel::showMethodSheet,
                    onMethodDismiss = homeViewModel::dismissMethodSheet,
                    onMethodSelected = homeViewModel::selectMethod,
                    onResetCurrentMethodValues = homeViewModel::resetCurrentMethodValues,
                    onYaoClick = homeViewModel::showYaoValueSheet,
                    onPointLineClick = homeViewModel::togglePointSelectionLine,
                    onPointLineMovingChange = homeViewModel::setPointSelectionLineMoving,
                    onOnlineShakeClick = homeViewModel::toggleOnlineShake,
                    onYaoValueDismiss = homeViewModel::dismissYaoValueSheet,
                    onYaoValueSelected = homeViewModel::selectYaoValue
                )
            }
            composable(AppRoute.SelectTime) {
                val timeSelectionUiState =
                    timeUiState.timeSelectionUiState ?: timeUiState.confirmedTimeSelectionUiState
                if (timeSelectionUiState != null) {
                    SelectTimeScreen(
                        uiState = timeSelectionUiState,
                        onTypeSelected = timeSelectionViewModel::selectTimeType,
                        onPickerClick = timeSelectionViewModel::showTimePicker,
                        onPickerOptionSelected = timeSelectionViewModel::selectTimePickerOption,
                        onGanzhiFieldClick = timeSelectionViewModel::selectGanzhiField,
                        onGanzhiFieldClear = timeSelectionViewModel::clearGanzhiField,
                        onGanzhiDeleteDismiss = timeSelectionViewModel::dismissGanzhiFieldDelete,
                        onGanzhiOptionSelected = timeSelectionViewModel::selectGanzhiOption,
                        onGanzhiReset = timeSelectionViewModel::resetGanzhiSelection,
                        onPickerDismiss = timeSelectionViewModel::dismissTimePicker,
                        onUseCurrentTimeClick = timeSelectionViewModel::useCurrentTimeForTimeSelection,
                        onConfirmClick = {
                            timeSelectionViewModel.confirmTimeSelection()
                            navController.popBackStack()
                        },
                        onBackClick = {
                            timeSelectionViewModel.closeTimeSelection()
                            navController.popBackStack()
                        }
                    )
                } else {
                    StartupLoadingScreen()
                }
            }
            composable(AppRoute.Settings) {
                SettingsScreen(
                    onGlobalSettingsClick = { navController.navigate(AppRoute.GlobalSettings) },
                    onDivinationSettingsClick = { navController.navigate(AppRoute.DivinationSettings) },
                    onHistoryClick = { navController.navigate(AppRoute.History) },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(AppRoute.GlobalSettings) {
                GlobalSettingsScreen(
                    showLunarInfo = settingsUiState.showLunarInfo,
                    fontScale = settingsUiState.fontScale,
                    changeDayPillarAt23 = settingsUiState.changeDayPillarAt23,
                    defaultDivinationMethod = settingsUiState.defaultDivinationMethod,
                    defaultDivinationTimeType = settingsUiState.defaultDivinationTimeType,
                    autoSaveDivinationMode = settingsUiState.autoSaveDivinationMode,
                    autoSaveHistoryGroupId = settingsUiState.autoSaveHistoryGroupId,
                    historyGroups = settingsUiState.historyGroups,
                    onShowLunarInfoChange = settingsViewModel::setShowLunarInfo,
                    onFontScaleChange = settingsViewModel::setAppFontScale,
                    onChangeDayPillarAt23Change = settingsViewModel::setChangeDayPillarAt23,
                    onDefaultDivinationMethodChange = settingsViewModel::setDefaultDivinationMethod,
                    onDefaultDivinationTimeTypeChange = settingsViewModel::setDefaultDivinationTimeType,
                    onAutoSaveDivinationModeChange = settingsViewModel::setAutoSaveDivinationMode,
                    onAutoSaveHistoryGroupChange = settingsViewModel::setAutoSaveHistoryGroup,
                    onAutoSaveHistoryGroupClick = settingsViewModel::refreshAutoSaveHistoryGroups,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(AppRoute.DivinationSettings) {
                DivinationSettingsScreen(
                    compactSixGod = settingsUiState.compactSixGod,
                    showHeavenlyStem = settingsUiState.showHeavenlyStem,
                    showAllHiddenLines = settingsUiState.showAllHiddenLines,
                    compactSixRelative = settingsUiState.compactSixRelative,
                    showHuiTouShengKeHint = settingsUiState.showHuiTouShengKeHint,
                    showHuiTouChongHeHint = settingsUiState.showHuiTouChongHeHint,
                    showDayMonthChongHeHint = settingsUiState.showDayMonthChongHeHint,
                    markBranchXunKong = settingsUiState.markBranchXunKong,
                    clickHighlightHint = settingsUiState.clickHighlightHint,
                    onCompactSixGodChange = settingsViewModel::setCompactSixGod,
                    onShowHeavenlyStemChange = settingsViewModel::setShowHeavenlyStem,
                    onShowAllHiddenLinesChange = settingsViewModel::setShowAllHiddenLines,
                    onCompactSixRelativeChange = settingsViewModel::setCompactSixRelative,
                    onShowHuiTouShengKeHintChange = settingsViewModel::setShowHuiTouShengKeHint,
                    onShowHuiTouChongHeHintChange = settingsViewModel::setShowHuiTouChongHeHint,
                    onShowDayMonthChongHeHintChange = settingsViewModel::setShowDayMonthChongHeHint,
                    onMarkBranchXunKongChange = settingsViewModel::setMarkBranchXunKong,
                    onClickHighlightHintChange = settingsViewModel::setClickHighlightHint,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(AppRoute.History) {
                LaunchedEffect(Unit) {
                    historyViewModel.refresh()
                }
                HistoryScreen(
                    uiState = historyUiState,
                    onRecordClick = { record ->
                        resultViewModel.showSavedResult(record)
                        navController.navigate(AppRoute.Result)
                    },
                    onRecordLongClick = historyViewModel::enterSelectionMode,
                    onRecordSelectionToggle = historyViewModel::toggleRecordSelection,
                    onGroupClick = historyViewModel::openGroup,
                    onGroupLongClick = historyViewModel::enterGroupSelectionMode,
                    onGroupSelectionToggle = historyViewModel::toggleGroupSelection,
                    onGroupMove = historyViewModel::moveGroupByOffset,
                    onAddGroupConfirm = historyViewModel::createGroup,
                    onRenameGroupConfirm = historyViewModel::renameSelectedGroup,
                    onMoveGroupRecordsConfirm = historyViewModel::moveSelectedGroupRecords,
                    onMoveSelectedRecordsConfirm = historyViewModel::moveSelectedRecords,
                    onDeleteSelectedGroupsConfirm = historyViewModel::deleteSelectedGroups,
                    onSearchClick = historyViewModel::openSearchDialog,
                    onSearchDraftChange = historyViewModel::updateSearchDraft,
                    onSearchConfirm = historyViewModel::confirmSearch,
                    onSearchDismiss = historyViewModel::dismissSearchDialog,
                    onSearchClear = historyViewModel::clearSearch,
                    onSelectAllGroupsClick = historyViewModel::toggleSelectAllGroups,
                    onSelectAllClick = historyViewModel::toggleSelectAllFilteredRecords,
                    onInvertSelectionClick = historyViewModel::invertFilteredRecordsSelection,
                    onDeleteSelectedClick = historyViewModel::deleteSelectedRecords,
                    onExitGroupSelectionMode = historyViewModel::exitGroupSelectionMode,
                    onExitSelectionMode = historyViewModel::exitSelectionMode,
                    onCloseCurrentGroup = historyViewModel::closeCurrentGroup,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(AppRoute.Result) {
                resultUiState.result?.let { result ->
                    ResultScreen(
                        result = result,
                        compactSixGod = settingsUiState.compactSixGod,
                        showHeavenlyStem = settingsUiState.showHeavenlyStem,
                        showAllHiddenLines = settingsUiState.showAllHiddenLines,
                        compactSixRelative = settingsUiState.compactSixRelative,
                        showHuiTouShengKeHint = settingsUiState.showHuiTouShengKeHint,
                        showHuiTouChongHeHint = settingsUiState.showHuiTouChongHeHint,
                        showDayMonthChongHeHint = settingsUiState.showDayMonthChongHeHint,
                        markBranchXunKong = settingsUiState.markBranchXunKong,
                        clickHighlightHint = settingsUiState.clickHighlightHint,
                        currentSituation = resultUiState.currentSituation,
                        judgment = resultUiState.judgment,
                        showSaveAction = resultUiState.showSaveAction,
                        showEditAction = resultUiState.showEditAction,
                        editDraft = resultUiState.editDraft,
                        onSaveClick = resultViewModel::saveCurrentResult,
                        onEditClick = resultViewModel::openEditDialog,
                        onEditQuestionChange = resultViewModel::updateEditQuestion,
                        onEditCurrentSituationChange = resultViewModel::updateEditCurrentSituation,
                        onEditJudgmentChange = resultViewModel::updateEditJudgment,
                        onEditDismiss = resultViewModel::dismissEditDialog,
                        onEditSave = resultViewModel::saveEditDraft,
                        onBackClick = {
                            historyViewModel.refresh()
                            navController.popBackStack()
                        }
                    )
                } ?: StartupLoadingScreen()
            }
        }
    }
}

@Composable
private fun StartupLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "正在加载...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
