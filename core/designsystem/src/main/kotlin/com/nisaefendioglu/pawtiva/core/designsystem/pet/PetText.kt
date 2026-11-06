package com.nisaefendioglu.pawtiva.core.designsystem.pet

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nisaefendioglu.pawtiva.core.designsystem.R
import com.nisaefendioglu.pawtiva.core.model.PetMood
import com.nisaefendioglu.pawtiva.core.model.PetType

@Composable
fun PetType.displayName(): String = stringResource(
    when (this) {
        PetType.CAT -> R.string.pawtiva_pet_cat
        PetType.DOG -> R.string.pawtiva_pet_dog
    },
)

@Composable
fun PetType.defaultName(): String = stringResource(
    when (this) {
        PetType.CAT -> R.string.pawtiva_pet_default_name_cat
        PetType.DOG -> R.string.pawtiva_pet_default_name_dog
    },
)

@Composable
fun PetMood.displayName(): String = stringResource(
    when (this) {
        PetMood.HAPPY -> R.string.pawtiva_mood_happy
        PetMood.EXCITED -> R.string.pawtiva_mood_excited
        PetMood.NORMAL -> R.string.pawtiva_mood_normal
        PetMood.SLEEPY -> R.string.pawtiva_mood_sleepy
        PetMood.CELEBRATING -> R.string.pawtiva_mood_celebrating
    },
)

/** Screen-reader description, e.g. "Mochi the kitten, happy". */
@Composable
fun petContentDescription(type: PetType, name: String, mood: PetMood): String =
    stringResource(R.string.pawtiva_pet_content_description, name, type.displayName(), mood.displayName())
