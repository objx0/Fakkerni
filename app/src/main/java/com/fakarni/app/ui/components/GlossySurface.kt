package com.fakarni.app.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Kept for existing call sites; renders the Liquid Glass material. */
@Composable
fun GlossySurface(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(28.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) = GlassSurface(modifier = modifier.fillMaxWidth(), shape = shape, onClick = onClick, content = content)
