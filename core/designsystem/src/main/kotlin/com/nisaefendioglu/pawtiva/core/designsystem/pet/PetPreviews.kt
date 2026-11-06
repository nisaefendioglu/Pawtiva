package com.nisaefendioglu.pawtiva.core.designsystem.pet

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.model.AccessoryId
import com.nisaefendioglu.pawtiva.core.model.PetAppearance
import com.nisaefendioglu.pawtiva.core.model.PetMood
import com.nisaefendioglu.pawtiva.core.model.PetType

// Android Studio preview gallery: every mood for both pets, then every accessory on its own.
@Preview(name = "Pet moods", showBackground = true, widthDp = 420)
@Preview(name = "Pet moods dark", showBackground = true, widthDp = 420, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PetMoodsPreview() {
    PawtivaTheme {
        Column(Modifier.background(PawtivaTheme.colors.background).padding(8.dp)) {
            PetType.entries.forEach { type ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    PetMood.entries.forEach { mood ->
                        PetDisplay(PetAppearance(type, mood), "$type $mood", Modifier.size(78.dp), animate = false)
                    }
                }
            }
        }
    }
}

@Preview(name = "Accessories", showBackground = true, widthDp = 420)
@Composable
private fun AccessoriesPreview() {
    PawtivaTheme {
        Column(Modifier.background(PawtivaTheme.colors.background).padding(8.dp)) {
            PetType.entries.forEach { type ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AccessoryId.entries.forEach { item ->
                        PetDisplay(PetAppearance(type, PetMood.HAPPY, setOf(item)), item.name, Modifier.size(66.dp), animate = false)
                    }
                }
            }
        }
    }
}
