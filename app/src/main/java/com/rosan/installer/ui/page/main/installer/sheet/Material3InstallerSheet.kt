// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 InstallerX Revived contributors
package com.rosan.installer.ui.page.main.installer.sheet

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.rosan.installer.ui.library.liquid.InnerShadow
import com.rosan.installer.ui.library.liquid.innerShadow
import com.rosan.installer.ui.page.main.installer.dialog.DialogParams
import com.rosan.installer.ui.page.main.installer.dialog.dialogInnerWidget
import com.rosan.installer.ui.util.WindowBlurEffect
import top.yukonga.miuix.kmp.nav.gesture.WindowNavigationEventBridge

private val SheetTopCornerRadius = 28.dp

/** Outer inset so the sheet reads as a floating glass panel instead of a flush surface. */
private val SheetOuterPadding = 8.dp

private val SheetHorizontalPadding = 16.dp

/**
 * Material 3 flavour of the installer UI, rendered as a bottom sheet with a
 * glass surface instead of the centred [androidx.compose.ui.window.Dialog].
 *
 * The per-stage content is reused from the existing dialog layer via
 * [DialogParams], so every stage keeps its current behaviour and only the
 * container changes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3InstallerSheet(
    params: DialogParams,
    useBlur: Boolean,
    isDark: Boolean,
    isDismissible: () -> Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = remember {
        RoundedCornerShape(topStart = SheetTopCornerRadius, topEnd = SheetTopCornerRadius)
    }

    // Read dismissibility lazily: capturing a changing local would recreate the
    // sheet state and reset the sheet.
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
        confirmValueChange = { sheetValue ->
            if (sheetValue == SheetValue.Hidden) isDismissible() else true
        },
    )

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        contentColor = colorScheme.onSurface,
        contentWindowInsets = { WindowInsets(left = 0.dp, top = 0.dp, right = 0.dp, bottom = 0.dp) },
    ) {
        WindowNavigationEventBridge()
        WindowBlurEffect(
            useBlur = useBlur,
            blurRadius = if (sheetState.targetValue == SheetValue.Expanded) 30 else 0,
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            Material3GlassSheetSurface(
                shape = shape,
                useBlur = useBlur,
                isDark = isDark,
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = SheetOuterPadding),
            )
            InstallerSheetContent(
                params = params,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SheetOuterPadding)
                    .windowInsetsPadding(WindowInsets.navigationBars),
            )
        }
    }
}

/**
 * The glass panel behind the sheet content: a translucent tinted surface with a
 * soft outer shadow and a specular inner highlight along the top edge.
 */
@Composable
private fun Material3GlassSheetSurface(
    shape: Shape,
    useBlur: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val containerColor = MaterialTheme.colorScheme.surfaceContainer
    val surfaceColor = if (useBlur) containerColor.copy(alpha = 0.82f) else containerColor

    Box(
        modifier = modifier
            .dropShadow(
                shape = shape,
                shadow = Shadow(
                    radius = 24.dp,
                    color = Color.Black,
                    alpha = if (isDark) 0.45f else 0.20f,
                ),
            )
            .background(color = surfaceColor, shape = shape)
            .then(
                if (useBlur) {
                    Modifier.innerShadow(shape = shape) {
                        InnerShadow(
                            radius = 12.dp,
                            offset = DpOffset(0.dp, 8.dp),
                            color = Color.White.copy(alpha = if (isDark) 0.10f else 0.28f),
                        )
                    }
                } else {
                    Modifier
                },
            ),
    )
}

@Composable
private fun InstallerSheetContent(params: DialogParams, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    var buttonHeightPx by remember { mutableIntStateOf(0) }
    val buttonHeight = (buttonHeightPx / density.density).dp
    val animatedButtonHeight by animateDpAsState(
        targetValue = buttonHeight,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "sheet_button_height",
    )

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = animatedButtonHeight)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SheetHorizontalPadding)
                .padding(top = 12.dp, bottom = 12.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            dialogInnerWidget(params.icon)?.let { content ->
                Box(modifier = Modifier.padding(bottom = 12.dp)) {
                    content()
                }
            }
            dialogInnerWidget(params.title)?.let { content ->
                ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        content()
                    }
                }
            }
            dialogInnerWidget(params.subtitle)?.let { content ->
                ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        content()
                    }
                }
            }
            // Content takes precedence over plain text, matching the dialog layout.
            val body = dialogInnerWidget(params.content) ?: dialogInnerWidget(params.text)
            body?.let { content ->
                ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        content()
                    }
                }
            }
        }

        // Keep the action row pinned to the bottom of the sheet.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged { buttonHeightPx = it.height }
                .padding(horizontal = SheetHorizontalPadding, vertical = 12.dp),
        ) {
            dialogInnerWidget(params.buttons)?.invoke()
        }
    }
}
