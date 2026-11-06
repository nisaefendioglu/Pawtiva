package com.nisaefendioglu.pawtiva.core.data

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.nisaefendioglu.pawtiva.core.model.AppLanguage
import javax.inject.Inject

/**
 * Applies the in-app language through AppCompat's per-app locale API. AppCompat persists the
 * choice itself (Android 13+: system per-app setting; older: autoStoreLocales in the manifest),
 * so it is the single source of truth and is not duplicated in DataStore.
 * Call from the main thread.
 */
interface AppLanguageController {
    fun current(): AppLanguage
    fun apply(language: AppLanguage)
}

internal class AppCompatLanguageController @Inject constructor() : AppLanguageController {
    override fun current(): AppLanguage {
        val tags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        return when {
            tags.startsWith("tr") -> AppLanguage.TURKISH
            tags.startsWith("en") -> AppLanguage.ENGLISH
            else -> AppLanguage.SYSTEM
        }
    }

    override fun apply(language: AppLanguage) {
        if (language == current()) return
        val locales = when (language) {
            AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            AppLanguage.TURKISH -> LocaleListCompat.forLanguageTags("tr")
            AppLanguage.ENGLISH -> LocaleListCompat.forLanguageTags("en")
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
