package com.fakarni.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * The rounded line icon set from the Liquid Glass concept (24×24, 1.9 stroke).
 * Paths are kept as SVG path strings so they match the design file exactly.
 * Icons are black; [androidx.compose.material3.Icon] tints them.
 */
object FakIcons {
    val Today by lazy { line("Today", circle(12f, 12f, 9f), "M12 7v5l3 2") }
    val Flame by lazy { line("Flame", "M12 3c1 4 5 5.5 5 10a5 5 0 0 1-10 0c0-2.5 1.5-3.5 2-5 1 1.5 2 2 3 2-1-3 0-5 0-7z") }
    val List by lazy { line("List", "M8 6h13M8 12h13M8 18h13M3.5 6h.01M3.5 12h.01M3.5 18h.01", autoMirror = true) }
    val Sliders by lazy { line("Sliders", "M4 7h10M18 7h2M4 17h2M10 17h10", circle(16f, 7f, 2f), circle(8f, 17f, 2f)) }
    val Plus by lazy { line("Plus", "M12 5v14M5 12h14", width = 2.3f) }
    val Minus by lazy { line("Minus", "M5 12h14", width = 2.3f) }
    val Check by lazy { line("Check", "M5 12.5l4.5 4.5L19 7.5", width = 2.4f) }
    val Close by lazy { line("Close", "M6 6l12 12M18 6 6 18", width = 2.1f) }
    val Forward by lazy { line("Forward", "M9 6l6 6-6 6", width = 2.1f, autoMirror = true) }
    val Back by lazy { line("Back", "M15 6l-6 6 6 6", width = 2.1f, autoMirror = true) }
    val Search by lazy { line("Search", circle(11f, 11f, 7f), "m20 20-3.5-3.5") }
    val Snooze by lazy { line("Snooze", circle(12f, 13f, 7f), "M12 10v3l2 2M5 4 3 6M19 4l2 2") }
    val Edit by lazy { line("Edit", "M4 20h4L19 9l-4-4L4 16z M14 6l4 4") }
    val Trash by lazy { line("Trash", "M4 7h16M10 11v6M14 11v6M6 7l1 13h10l1-13M9 7V4h6v3") }
    val Calendar by lazy { line("Calendar", rect(3f, 5f, 18f, 16f, 3f), "M3 10h18M8 3v4M16 3v4") }
    val Clock by lazy { line("Clock", circle(12f, 12f, 9f), "M12 7v5l3 2") }
    val Repeat by lazy { line("Repeat", "M4 11V9a3 3 0 0 1 3-3h12l-3-3M20 13v2a3 3 0 0 1-3 3H5l3 3") }
    val Tag by lazy { line("Tag", "M3 12V4h8l10 10-8 8z", "M7.5 7.5h.01") }
    val Mic by lazy { line("Mic", rect(9f, 3f, 6f, 11f, 3f), "M5 11a7 7 0 0 0 14 0M12 18v3") }
    val Sparkle by lazy { line("Sparkle", "M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8z", "M19 16l.7 1.8 1.8.7-1.8.7L19 21l-.7-1.8-1.8-.7 1.8-.7z") }
    val Moon by lazy { line("Moon", "M20 14.5A8 8 0 1 1 9.5 4a6.5 6.5 0 0 0 10.5 10.5z") }
    val Sun by lazy { line("Sun", circle(12f, 12f, 4f), "M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4") }
    val Auto by lazy {
        builder("Auto")
            .stroke(circle(12f, 12f, 9f))
            .fill("M12 3a9 9 0 0 0 0 18z")
            .build()
    }
    val Globe by lazy { line("Globe", circle(12f, 12f, 9f), "M3 12h18M12 3a14 14 0 0 1 0 18M12 3a14 14 0 0 0 0 18") }
    val Bell by lazy { line("Bell", "M6 16V11a6 6 0 1 1 12 0v5l1.5 2h-15z", "M10 20a2 2 0 0 0 4 0") }
    val Alarm by lazy { line("Alarm", circle(12f, 13f, 7f), "M12 10v3l2 2M5 4 3 6M19 4l2 2") }
    val CheckCircle by lazy { line("CheckCircle", circle(12f, 12f, 9f), "M8 12.5l3 3 5-6") }
    val Warning by lazy { line("Warning", circle(12f, 12f, 9f), "M12 7.5v5.5M12 16.5h.01") }

    // Reminder glyphs
    val Car by lazy { line("Car", "M4 16v-4l2-5h12l2 5v4z", "M4 12h16M7 16v3M17 16v3") }
    val Film by lazy { line("Film", rect(3f, 5f, 18f, 14f, 2f), "M3 9h18M8 5l2 4M13 5l2 4") }
    val Drop by lazy { line("Drop", "M12 3s6 6.5 6 11a6 6 0 0 1-12 0c0-4.5 6-11 6-11z") }
    val Pill by lazy { line("Pill", rect(4f, 4f, 16f, 16f, 4f), "M12 8v8M8 12h8") }
    val Home by lazy { line("Home", "M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6h-6v6H4a1 1 0 0 1-1-1z") }
    val Card by lazy { line("Card", rect(3f, 6f, 18f, 13f, 2.5f), "M3 10h18M7 15h4") }
    val Wrench by lazy { line("Wrench", "M14.5 5.5a4 4 0 0 0-5 5L4 16l4 4 5.5-5.5a4 4 0 0 0 5-5l-2.5 2.5-2.5-.5-.5-2.5z") }
    val Cart by lazy { line("Cart", "M3 4h2l2.4 11h10.2L20 8H6.2", circle(9f, 19f, 1.5f), circle(17f, 19f, 1.5f)) }
    val Walk by lazy { line("Walk", circle(13f, 4.5f, 1.5f), "M9 21l2-6 3 2v4M7 12l3-4 4 1 3 3M10 8l1 7") }
    val Book by lazy { line("Book", "M4 5.5A1.5 1.5 0 0 1 5.5 4H11v16H5.5A1.5 1.5 0 0 1 4 18.5z", "M20 5.5A1.5 1.5 0 0 0 18.5 4H13v16h5.5a1.5 1.5 0 0 0 1.5-1.5z") }

    private fun line(name: String, vararg paths: String, width: Float = 1.9f, autoMirror: Boolean = false): ImageVector {
        val b = builder(name, autoMirror)
        paths.forEach { b.stroke(it, width) }
        return b.build()
    }

    private fun builder(name: String, autoMirror: Boolean = false) = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
        autoMirror = autoMirror,
    )

    private fun ImageVector.Builder.stroke(path: String, width: Float = 1.9f): ImageVector.Builder = addPath(
        pathData = addPathNodes(path),
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = width,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    )

    private fun ImageVector.Builder.fill(path: String): ImageVector.Builder = addPath(
        pathData = addPathNodes(path),
        fill = SolidColor(Color.Black),
    )

    private fun circle(cx: Float, cy: Float, r: Float): String =
        "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0"

    private fun rect(x: Float, y: Float, w: Float, h: Float, rx: Float): String =
        "M${x + rx} ${y}h${w - 2 * rx}a$rx $rx 0 0 1 $rx ${rx}v${h - 2 * rx}a$rx $rx 0 0 1 ${-rx} ${rx}" +
            "h${-(w - 2 * rx)}a$rx $rx 0 0 1 ${-rx} ${-rx}v${-(h - 2 * rx)}a$rx $rx 0 0 1 $rx ${-rx}z"
}
