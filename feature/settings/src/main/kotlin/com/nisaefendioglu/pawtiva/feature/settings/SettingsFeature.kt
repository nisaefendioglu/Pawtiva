package com.nisaefendioglu.pawtiva.feature.settings

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Settings entry point used by the app's navigation. Handles everything that needs Android
 * APIs (permission request, file picker, intents) and delegates the rest to [SettingsScreen].
 */
@Composable
fun SettingsFeature(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showRationale by rememberSaveable { mutableStateOf(false) }
    var showDenied by rememberSaveable { mutableStateOf(false) }

    val exportSuccess = stringResource(R.string.settings_export_success)
    val exportFailed = stringResource(R.string.settings_export_failed)
    val resetDone = stringResource(R.string.settings_reset_done)
    val noEmailApp = stringResource(R.string.settings_no_email_app)
    val feedbackSubject = stringResource(R.string.settings_feedback_subject)
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            val message = when (event) {
                SettingsEvent.ExportSucceeded -> exportSuccess
                SettingsEvent.ExportFailed -> exportFailed
                SettingsEvent.ResetDone -> resetDone
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.setRemindersEnabled(true) else showDenied = true
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.export { json ->
                withContext(Dispatchers.IO) {
                    val stream = context.contentResolver.openOutputStream(uri) ?: error("Cannot open $uri")
                    stream.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                }
            }
        }
    }

    val actions = SettingsActions(
        onLanguage = viewModel::setLanguage,
        onTheme = viewModel::setTheme,
        onDynamicColor = viewModel::setDynamicColor,
        onDateFormat = viewModel::setDateFormat,
        onChangePet = viewModel::changePet,
        onRenamePet = viewModel::renamePet,
        onReduceAnimations = viewModel::setReducePetAnimations,
        onRemindersToggle = { enabled ->
            when {
                !enabled -> viewModel.setRemindersEnabled(false)
                needsNotificationPermission(context) -> showRationale = true
                else -> viewModel.setRemindersEnabled(true)
            }
        },
        onReminderTime = viewModel::setReminderTime,
        onExport = { exportLauncher.launch("pawtiva-export-${LocalDate.now()}.json") },
        onReset = viewModel::resetAllData,
        onFeedback = {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
                .putExtra(Intent.EXTRA_SUBJECT, feedbackSubject)
            try {
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                scope.launch { snackbarHostState.showSnackbar(noEmailApp) }
            }
        },
    )

    SettingsScreen(
        state = state,
        actions = actions,
        onBack = onBack,
        versionName = remember(context) { appVersion(context) },
        dynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
        snackbarHostState = snackbarHostState,
    )

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text(stringResource(R.string.settings_reminder_rationale_title)) },
            text = { Text(stringResource(R.string.settings_reminder_rationale_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) { Text(stringResource(R.string.settings_continue)) }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) { Text(stringResource(R.string.settings_cancel)) }
            },
        )
    }

    if (showDenied) {
        AlertDialog(
            onDismissRequest = { showDenied = false },
            title = { Text(stringResource(R.string.settings_reminder_denied_title)) },
            text = { Text(stringResource(R.string.settings_reminder_denied_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showDenied = false
                    openNotificationSettings(context)
                }) { Text(stringResource(R.string.settings_open_system_settings)) }
            },
            dismissButton = {
                TextButton(onClick = { showDenied = false }) { Text(stringResource(R.string.settings_cancel)) }
            },
        )
    }
}

private fun needsNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED

private fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
        )
    }
}

private fun appVersion(context: Context): String = runCatching {
    val packageManager = context.packageManager
    val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(context.packageName, 0)
    }
    info.versionName.orEmpty()
}.getOrDefault("")
