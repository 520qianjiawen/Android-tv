package com.v2ray.ang.ui.main

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Visual feedback only; the existing clickable or Material control owns focus and activation. */
@Composable
internal fun Modifier.mainFocusOutline(shape: Shape = RoundedCornerShape(8.dp)): Modifier {
    var focused by remember { mutableStateOf(false) }
    val outline = if (focused) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape) else Modifier
    return this.onFocusChanged { focused = it.isFocused }.then(outline)
}
