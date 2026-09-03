package site.hanzhe.wuchang_liuyao.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WuchangTopAppBarHeight = 48.dp
private val TopBarTitleVerticalOffset = 2.dp
private val TopBarButtonSize = 44.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WuchangTopAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    modifier = Modifier.offset(y = TopBarTitleVerticalOffset),
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                )
            },
            expandedHeight = WuchangTopAppBarHeight,
            navigationIcon = {
                if (onBackClick != null) {
                    TopBarBackIcon(onClick = onBackClick)
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

/** 绘制带返回语义的顶栏返回图标。 */
@Composable
internal fun TopBarBackIcon(onClick: () -> Unit) {
    val color = MaterialTheme.colorScheme.onSurface
    TopBarIconButton(onClick = onClick, contentDescription = "返回") {
        val strokeWidth = 2.2.dp.toPx()
        drawLine(
            color,
            Offset(size.width * 0.56f, size.height * 0.35f),
            Offset(size.width * 0.44f, size.height * 0.5f),
            strokeWidth,
            StrokeCap.Round
        )
        drawLine(
            color,
            Offset(size.width * 0.44f, size.height * 0.5f),
            Offset(size.width * 0.56f, size.height * 0.65f),
            strokeWidth,
            StrokeCap.Round
        )
    }
}

/** 绘制打开设置页的顶栏菜单图标。 */
@Composable
internal fun TopBarMenuIcon(onClick: () -> Unit) {
    val color = MaterialTheme.colorScheme.onSurface
    TopBarIconButton(onClick = onClick, contentDescription = "设置") {
        val strokeWidth = 2.2.dp.toPx()
        repeat(3) { index ->
            val y = size.height * (0.36f + index * 0.14f)
            drawLine(
                color,
                Offset(size.width * 0.3f, y),
                Offset(size.width * 0.7f, y),
                strokeWidth,
                StrokeCap.Round
            )
        }
    }
}

/** 绘制设置项右侧的展开箭头。 */
@Composable
internal fun ChevronIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconSize: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(iconSize)) {
        val strokeWidth = 1.8.dp.toPx()
        drawLine(
            tint,
            Offset(size.width * 0.35f, size.height * 0.2f),
            Offset(size.width * 0.65f, size.height * 0.5f),
            strokeWidth,
            StrokeCap.Round
        )
        drawLine(
            tint,
            Offset(size.width * 0.65f, size.height * 0.5f),
            Offset(size.width * 0.35f, size.height * 0.8f),
            strokeWidth,
            StrokeCap.Round
        )
    }
}

/** 绘制干支输入项的清除图标。 */
@Composable
internal fun ClearIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.error
) {
    Canvas(modifier = modifier.size(20.dp)) {
        val strokeWidth = 1.8.dp.toPx()
        drawLine(
            tint,
            Offset(size.width * 0.3f, size.height * 0.3f),
            Offset(size.width * 0.7f, size.height * 0.7f),
            strokeWidth,
            StrokeCap.Round
        )
        drawLine(
            tint,
            Offset(size.width * 0.7f, size.height * 0.3f),
            Offset(size.width * 0.3f, size.height * 0.7f),
            strokeWidth,
            StrokeCap.Round
        )
    }
}

/** 绘制带点击语义的顶栏图标按钮。 */
@Composable
private fun TopBarIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    drawIcon: DrawScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .size(TopBarButtonSize)
            .offset(y = TopBarTitleVerticalOffset)
            .noRippleClick(onClick = onClick)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(TopBarButtonSize), onDraw = drawIcon)
    }
}


@Composable
internal fun TopBarTextAction(
    text: String,
    onClick: () -> Unit,
    fontSize: TextUnit = 15.sp,
    fontWeight: FontWeight = FontWeight.SemiBold
) {
    Box(
        modifier = Modifier
            .height(TopBarButtonSize)
            .padding(start = 8.dp, end = 12.dp)
            .noRippleClick(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            fontSize = fontSize,
            fontWeight = fontWeight
        )
    }
}
