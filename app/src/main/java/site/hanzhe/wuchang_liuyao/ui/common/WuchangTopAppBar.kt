package site.hanzhe.wuchang_liuyao.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WuchangTopAppBarHeight = 52.dp
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
                    TopBarGlyphButton(
                        glyph = "‹",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        onClick = onBackClick
                    )
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

@Composable
internal fun TopBarGlyphButton(
    glyph: String,
    onClick: () -> Unit,
    fontSize: TextUnit,
    fontWeight: FontWeight = FontWeight.SemiBold
) {
    Box(
        modifier = Modifier
            .size(TopBarButtonSize)
            .noRippleClick(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = glyph,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = fontSize,
            fontWeight = fontWeight
        )
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
