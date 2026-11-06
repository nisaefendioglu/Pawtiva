package com.nisaefendioglu.pawtiva.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Pawtiva's own rounded line icons, drawn on a 24 x 24 grid.
 * Original artwork, so no third-party icon license applies. Icons are tinted by `Icon(tint = …)`.
 */
object PawtivaIcons {
    val Check: ImageVector by lazy { strokeIcon("Check", "M5 12.5l4.5 4.5L19 7.5") }
    val Add: ImageVector by lazy { strokeIcon("Add", "M12 5v14M5 12h14") }
    val Close: ImageVector by lazy { strokeIcon("Close", "M6 6l12 12M18 6L6 18") }
    val Back: ImageVector by lazy { strokeIcon("Back", "M15 5l-7 7 7 7") }
    val Home: ImageVector by lazy {
        strokeIcon("Home", "M4 11l8-7 8 7v8.5a1.5 1.5 0 0 1-1.5 1.5H15v-6H9v6H5.5A1.5 1.5 0 0 1 4 19.5z")
    }
    val Habits: ImageVector by lazy {
        strokeIcon("Habits", "M9 6h11M9 12h11M9 18h11M4 6h0.01M4 12h0.01M4 18h0.01")
    }
    val Shop: ImageVector by lazy {
        strokeIcon(
            "Shop",
            "M5 8h14l-1.2 11.2a2 2 0 0 1-2 1.8H8.2a2 2 0 0 1-2-1.8z",
            "M9 8V6.5a3 3 0 0 1 6 0V8",
        )
    }
    val Stats: ImageVector by lazy { strokeIcon("Stats", "M5 20v-9M12 20V5M19 20v-6") }
    val Settings: ImageVector by lazy {
        strokeIcon(
            "Settings",
            "M10.14 5.05L10.33 2.55L13.67 2.55L13.86 5.05L15.6 5.76L17.51 4.14L19.86 6.49L18.24 8.4L18.95 10.14" +
                "L21.45 10.33L21.45 13.67L18.95 13.86L18.24 15.6L19.86 17.51L17.51 19.86L15.6 18.24L13.86 18.95" +
                "L13.67 21.45L10.33 21.45L10.14 18.95L8.4 18.24L6.49 19.86L4.14 17.51L5.76 15.6L5.05 13.86" +
                "L2.55 13.67L2.55 10.33L5.05 10.14L5.76 8.4L4.14 6.49L6.49 4.14L8.4 5.76Z",
            "M12 8.8a3.2 3.2 0 1 0 0 6.4a3.2 3.2 0 1 0 0-6.4z",
        )
    }
    val Delete: ImageVector by lazy {
        strokeIcon(
            "Delete",
            "M4.5 7h15M10 4h4",
            "M6.5 7l0.9 12.1a2 2 0 0 0 2 1.9h5.2a2 2 0 0 0 2-1.9L17.5 7",
            "M10 11v6M14 11v6",
        )
    }
    val Bell: ImageVector by lazy {
        strokeIcon("Bell", "M6 16v-5a6 6 0 0 1 12 0v5l1.5 2h-15z", "M10 20.5a2 2 0 0 0 4 0")
    }
    val Coin: ImageVector by lazy {
        strokeIcon(
            "Coin",
            "M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18z",
            "M12 7.5a4.5 4.5 0 1 0 0 9a4.5 4.5 0 1 0 0-9z",
        )
    }
    val Paw: ImageVector by lazy {
        fillIcon(
            "Paw",
            "M12 12.5c-3 0-5.5 2.7-5.5 5.2 0 1.6 1.2 2.6 2.7 2.6 1.1 0 1.7-0.6 2.8-0.6s1.7 0.6 2.8 0.6c1.5 0 2.7-1 2.7-2.6 0-2.5-2.5-5.2-5.5-5.2z",
            "M3.6 10.2a1.9 2.3 0 1 0 3.8 0a1.9 2.3 0 1 0-3.8 0z",
            "M16.6 10.2a1.9 2.3 0 1 0 3.8 0a1.9 2.3 0 1 0-3.8 0z",
            "M7.4 6a1.9 2.4 0 1 0 3.8 0a1.9 2.4 0 1 0-3.8 0z",
            "M12.8 6a1.9 2.4 0 1 0 3.8 0a1.9 2.4 0 1 0-3.8 0z",
        )
    }

    private fun builder(name: String) = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    )

    private fun strokeIcon(name: String, vararg paths: String): ImageVector =
        builder(name).apply {
            paths.forEach { data ->
                addPath(
                    pathData = addPathNodes(data),
                    fill = null,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()

    private fun fillIcon(name: String, vararg paths: String): ImageVector =
        builder(name).apply {
            paths.forEach { data ->
                addPath(pathData = addPathNodes(data), fill = SolidColor(Color.Black))
            }
        }.build()
}
