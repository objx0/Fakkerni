package com.fakarni.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fakarni.app.ui.theme.LocalGlass

/**
 * Soft colour field that sits behind every screen. Glass panels only read as
 * glass when there is colour behind them to tint, so this draws large
 * radial blooms instead of a flat background. Pure drawing, works on API 26+.
 */
@Composable
fun GlassBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val glass = LocalGlass.current
    val strength = if (glass.isDark) .42f else .85f
    Box(modifier.fillMaxSize().background(glass.base)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val blooms = listOf(
                Triple(Offset(w * 1.0f, h * 0.02f), w * 0.95f, glass.blobs[0]),
                Triple(Offset(w * -0.05f, h * 0.22f), w * 0.9f, glass.blobs[1]),
                Triple(Offset(w * 1.05f, h * 0.68f), w * 0.85f, glass.blobs[2]),
                Triple(Offset(w * 0.0f, h * 1.0f), w * 0.8f, glass.blobs[3]),
            )
            blooms.forEach { (center, radius, color) ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = strength), color.copy(alpha = strength * .35f), Color.Transparent),
                        center = center,
                        radius = radius,
                    ),
                    radius = radius,
                    center = center,
                )
            }
        }
        content()
    }
}

/**
 * Translucent glass material: diagonal tint, bright specular top edge and a
 * soft coloured shadow. [strong] is for surfaces that hold dense content
 * (sheets, dialogs) and need more opacity for legibility.
 */
@Composable
fun Modifier.glass(
    shape: Shape,
    strong: Boolean = false,
    elevation: Dp = 14.dp,
): Modifier {
    val glass = LocalGlass.current
    val top = if (strong) glass.fillStrongTop else glass.fillTop
    val bottom = if (strong) glass.fillStrongBottom else glass.fillBottom
    val shadowAlpha = if (glass.isDark) .45f else .18f
    return this
        .shadow(elevation, shape, clip = false, ambientColor = glass.shadow.copy(alpha = shadowAlpha), spotColor = glass.shadow.copy(alpha = shadowAlpha))
        .clip(shape)
        .background(Brush.linearGradient(listOf(top, bottom), start = Offset.Zero, end = Offset.Infinite), shape)
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(listOf(glass.highlight, glass.edge.copy(alpha = glass.edge.alpha * .35f))),
            shape = shape,
        )
}

/** Column laid out on a glass panel. */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    strong: Boolean = false,
    elevation: Dp = 14.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val base = modifier.glass(shape, strong, elevation)
    Column(modifier = if (onClick == null) base else base.clickable(role = Role.Button, onClick = onClick), content = content)
}

/** Small round glass button for icons (settings, close, search). */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .defaultMinSize(size, size)
            .glass(CircleShape, elevation = 8.dp)
            .clickable(role = Role.Button, onClickLabel = contentDescription, onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

/** Solid "ink" pill — the one high-contrast action per screen. */
@Composable
fun InkButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 56.dp,
    content: @Composable RowScope.() -> Unit,
) {
    val glass = LocalGlass.current
    val shape = RoundedCornerShape(height / 2)
    Row(
        modifier
            .height(height)
            .shadow(if (enabled) 12.dp else 0.dp, shape, ambientColor = glass.shadow, spotColor = glass.shadow)
            .clip(shape)
            .background(if (enabled) glass.ink else glass.ink.copy(alpha = .28f), shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/** Pill chip. Selected chips flip to the ink colour so state never relies on tint alone. */
@Composable
fun GlassChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val glass = LocalGlass.current
    val shape = RoundedCornerShape(50)
    val bg by animateColorAsState(if (selected) glass.ink else glass.fillTop, tween(160), label = "chip-bg")
    Row(
        modifier
            .height(44.dp)
            .clip(shape)
            .background(bg, shape)
            .border(1.dp, if (selected) Color.Transparent else glass.edge, shape)
            .semantics { this.selected = selected }
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
fun chipContentColor(selected: Boolean): Color =
    if (selected) LocalGlass.current.onInk else MaterialTheme.colorScheme.onSurface

/** Segmented control with a raised glass "lens" on the chosen option. */
@Composable
fun <T> GlassSegmented(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
) {
    val glass = LocalGlass.current
    val outer = RoundedCornerShape(24.dp)
    Row(
        modifier
            .clip(outer)
            .background(if (glass.isDark) Color.White.copy(alpha = .07f) else glass.ink.copy(alpha = .06f), outer)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            val inner = RoundedCornerShape(20.dp)
            Box(
                Modifier
                    .weight(1f)
                    .height(44.dp)
                    .then(if (isSelected) Modifier.glass(inner, strong = true, elevation = 4.dp) else Modifier)
                    .semantics { this.selected = isSelected }
                    .clickable(role = Role.Tab) { onSelect(option) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label(option),
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Rounded tile with a vivid gradient that holds a category icon. */
@Composable
fun IconTile(colors: List<Color>, modifier: Modifier = Modifier, size: Dp = 44.dp, content: @Composable BoxScope.() -> Unit) {
    val shape = RoundedCornerShape(size * .34f)
    Box(
        modifier
            .defaultMinSize(size, size)
            .clip(shape)
            .background(Brush.linearGradient(colors), shape),
        contentAlignment = Alignment.Center,
        content = content,
    )
}
