package com.nisaefendioglu.pawtiva.feature.shop

import android.content.res.Resources
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nisaefendioglu.pawtiva.core.designsystem.component.EmptyState
import com.nisaefendioglu.pawtiva.core.designsystem.component.LoadingState
import com.nisaefendioglu.pawtiva.core.designsystem.component.PawtivaButton
import com.nisaefendioglu.pawtiva.core.designsystem.component.PawtivaButtonStyle
import com.nisaefendioglu.pawtiva.core.designsystem.component.PawtivaCard
import com.nisaefendioglu.pawtiva.core.designsystem.component.ShopItemCard
import com.nisaefendioglu.pawtiva.core.designsystem.component.ShopItemStatusTone
import com.nisaefendioglu.pawtiva.core.designsystem.icon.PawtivaIcons
import com.nisaefendioglu.pawtiva.core.designsystem.pet.PetDisplay
import com.nisaefendioglu.pawtiva.core.designsystem.pet.PetIllustration
import com.nisaefendioglu.pawtiva.core.designsystem.pet.PetReaction
import com.nisaefendioglu.pawtiva.core.designsystem.pet.defaultName
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.designsystem.theme.Spacing
import com.nisaefendioglu.pawtiva.core.designsystem.theme.currentLocale
import com.nisaefendioglu.pawtiva.core.designsystem.theme.formatNumber
import com.nisaefendioglu.pawtiva.core.domain.ShopCatalog
import com.nisaefendioglu.pawtiva.core.model.AccessoryId
import com.nisaefendioglu.pawtiva.core.model.PetAppearance
import com.nisaefendioglu.pawtiva.core.model.PetMood
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.ShopItemState
import java.util.Locale

/** Shop tab entry point. */
@Composable
fun ShopFeature(viewModel: ShopViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalContext.current.resources
    val locale = currentLocale

    LaunchedEffect(viewModel, resources, locale) {
        viewModel.eventFlow.collect { event ->
            val purchased = event as? ShopEvent.Purchased
            val result = snackbarHostState.showSnackbar(
                message = event.message(resources, locale),
                actionLabel = purchased?.let { resources.getString(R.string.shop_message_wear_action) },
            )
            if (result == SnackbarResult.ActionPerformed && purchased != null) {
                viewModel.onEquip(purchased.id)
            }
        }
    }

    ShopContent(
        state = state,
        actions = ShopActions(
            onFilterSelected = viewModel::onFilterSelected,
            onPreview = viewModel::onPreview,
            onClearPreview = viewModel::onClearPreview,
            onBuy = viewModel::onBuyClicked,
            onConfirmPurchase = viewModel::onConfirmPurchase,
            onDismissPurchase = viewModel::onDismissPurchase,
            onEquip = viewModel::onEquip,
            onUnequip = viewModel::onUnequip,
        ),
        snackbarHostState = snackbarHostState,
    )
}

data class ShopActions(
    val onFilterSelected: (ShopFilter) -> Unit = {},
    val onPreview: (AccessoryId) -> Unit = {},
    val onClearPreview: () -> Unit = {},
    val onBuy: (AccessoryId) -> Unit = {},
    val onConfirmPurchase: () -> Unit = {},
    val onDismissPurchase: () -> Unit = {},
    val onEquip: (AccessoryId) -> Unit = {},
    val onUnequip: (AccessoryId) -> Unit = {},
)

internal fun ShopEvent.message(resources: Resources, locale: Locale): String = when (this) {
    is ShopEvent.Purchased -> resources.getString(R.string.shop_message_purchased, resources.getString(id.nameRes()))
    ShopEvent.AlreadyOwned -> resources.getString(R.string.shop_message_already_owned)
    is ShopEvent.InsufficientBalance ->
        resources.getQuantityString(R.plurals.shop_missing_coins, missing, formatNumber(missing, locale))
    is ShopEvent.LevelTooLow -> resources.getString(R.string.shop_message_level_locked, requiredLevel)
    ShopEvent.UnknownItem -> resources.getString(R.string.shop_message_unknown)
    ShopEvent.NotOwned -> resources.getString(R.string.shop_message_not_owned)
    is ShopEvent.DoesNotFitPet -> resources.getString(R.string.shop_message_not_fit)
}

/** Stateless shop UI, used by tests. */
@Composable
fun ShopContent(
    state: ShopUiState,
    actions: ShopActions,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val petName = state.petName ?: state.petType.defaultName()
    val listState = rememberLazyListState()
    // Bring the pet preview into view whenever a new item is previewed further down the list.
    LaunchedEffect(state.previewing) {
        if (state.previewing != null && listState.firstVisibleItemIndex > PREVIEW_ITEM_INDEX) {
            listState.animateScrollToItem(PREVIEW_ITEM_INDEX)
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PawtivaTheme.colors.background),
    ) {
        if (state.loading) {
            LoadingState(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(key = "header") { ShopHeader(state) }
                item(key = "preview") { PreviewCard(state, petName, actions.onClearPreview) }
                item(key = "filters") { FilterRow(state.filter, actions.onFilterSelected) }
                if (state.visibleItems.isEmpty()) {
                    item(key = "empty") {
                        EmptyState(
                            title = stringResource(R.string.shop_owned_empty_title),
                            message = stringResource(R.string.shop_owned_empty_message),
                            icon = PawtivaIcons.Shop,
                        )
                    }
                }
                items(state.visibleItems, key = { it.item.accessory.storageId }) { itemState ->
                    ShopItemRow(
                        itemState = itemState,
                        petType = state.petType,
                        petName = petName,
                        balance = state.progress.balance,
                        previewing = state.previewing == itemState.item.accessory,
                        actions = actions,
                    )
                }
                item(key = "bottom") { Spacer(Modifier.height(Spacing.xxl)) }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(Spacing.md),
        )

        state.pendingPurchase?.let { pending ->
            PurchaseDialog(
                itemState = pending,
                petType = state.petType,
                balance = state.progress.balance,
                purchasing = state.purchasing,
                onConfirm = actions.onConfirmPurchase,
                onDismiss = actions.onDismissPurchase,
            )
        }
    }
}

@Composable
private fun ShopHeader(state: ShopUiState) {
    val colors = PawtivaTheme.colors
    val locale = currentLocale
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.shop_title),
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.shop_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(Spacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            InfoPill(
                icon = PawtivaIcons.Coin,
                text = stringResource(R.string.shop_balance_pill, formatNumber(state.progress.balance, locale)),
            )
            InfoPill(
                icon = PawtivaIcons.Paw,
                text = stringResource(R.string.shop_level_pill, state.progress.level),
            )
        }
    }
}

@Composable
private fun InfoPill(icon: ImageVector, text: String) {
    val colors = PawtivaTheme.colors
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(colors.secondaryContainer)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = colors.onSecondaryContainer, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(Spacing.xxs))
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = colors.onSecondaryContainer)
    }
}

@Composable
private fun PreviewCard(state: ShopUiState, petName: String, onClearPreview: () -> Unit) {
    val colors = PawtivaTheme.colors
    PawtivaCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PetDisplay(
                appearance = PetAppearance(state.petType, PetMood.HAPPY, state.previewAccessories),
                contentDescription = stringResource(R.string.shop_pet_preview_description, petName),
                modifier = Modifier.size(132.dp),
                reaction = PetReaction.CELEBRATE,
                reactionKey = state.celebrationKey.takeIf { it > 0 },
            )
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.shop_preview_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                )
                val previewing = state.previewing
                Text(
                    text = if (previewing != null) {
                        stringResource(R.string.shop_preview_trying, stringResource(previewing.nameRes()))
                    } else {
                        stringResource(R.string.shop_preview_hint)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
                if (previewing != null) {
                    PawtivaButton(
                        text = stringResource(R.string.shop_preview_clear),
                        onClick = onClearPreview,
                        style = PawtivaButtonStyle.Text,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(selected: ShopFilter, onSelect: (ShopFilter) -> Unit) {
    val colors = PawtivaTheme.colors
    val filters = buildList {
        add(ShopFilter.All)
        add(ShopFilter.Owned)
        ShopCatalog.categories.forEach { add(ShopFilter.Category(it)) }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        filters.forEach { filter ->
            val label = when (filter) {
                ShopFilter.All -> stringResource(R.string.shop_filter_all)
                ShopFilter.Owned -> stringResource(R.string.shop_filter_owned)
                is ShopFilter.Category -> stringResource(filter.category.nameRes())
            }
            val isSelected = filter == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(filter) },
                label = { Text(label) },
                leadingIcon = if (isSelected) {
                    { Icon(PawtivaIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else {
                    null
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = colors.surface,
                    labelColor = colors.textPrimary,
                    selectedContainerColor = colors.primaryContainer,
                    selectedLabelColor = colors.onPrimaryContainer,
                    selectedLeadingIconColor = colors.onPrimaryContainer,
                ),
            )
        }
    }
}

@Composable
private fun ShopItemRow(
    itemState: ShopItemState,
    petType: PetType,
    petName: String,
    balance: Int,
    previewing: Boolean,
    actions: ShopActions,
) {
    val locale = currentLocale
    val item = itemState.item
    val id = item.accessory
    val name = stringResource(id.nameRes())
    val price = pluralStringResource(R.plurals.shop_price_coins, item.price, formatNumber(item.price, locale))
    val missing = (item.price - balance).coerceAtLeast(0)

    val badge = when {
        itemState.equipped -> stringResource(R.string.shop_badge_wearing)
        itemState.owned -> stringResource(R.string.shop_badge_owned)
        else -> null
    }
    val (status, tone) = when {
        itemState.owned && !itemState.fitsCurrentPet ->
            stringResource(R.string.shop_status_not_fit, petName) to ShopItemStatusTone.Warning
        itemState.equipped -> stringResource(R.string.shop_status_wearing, petName) to ShopItemStatusTone.Positive
        itemState.owned -> null to ShopItemStatusTone.Neutral
        !itemState.levelUnlocked ->
            stringResource(R.string.shop_status_level_locked, item.requiredLevel) to ShopItemStatusTone.Warning
        !itemState.affordable ->
            pluralStringResource(R.plurals.shop_missing_coins, missing, formatNumber(missing, locale)) to ShopItemStatusTone.Neutral
        else -> null to ShopItemStatusTone.Neutral
    }

    ShopItemCard(
        thumbnail = {
            PetIllustration(
                type = petType,
                mood = PetMood.HAPPY,
                contentDescription = "",
                modifier = Modifier.size(70.dp),
                animate = false,
                accessories = if (id.fits(petType)) listOf(id) else emptyList(),
            )
        },
        name = name,
        description = stringResource(id.descriptionRes()),
        priceText = price,
        onClick = { actions.onPreview(id) },
        badgeText = badge,
        statusText = status,
        statusTone = tone,
        previewing = previewing,
        primaryActionLabel = when {
            itemState.equipped -> stringResource(R.string.shop_action_take_off)
            itemState.owned -> stringResource(R.string.shop_action_wear)
            else -> stringResource(R.string.shop_action_buy)
        },
        primaryActionEnabled = when {
            itemState.equipped -> true
            itemState.owned -> itemState.fitsCurrentPet
            else -> itemState.canBuy
        },
        onPrimaryAction = {
            when {
                itemState.equipped -> actions.onUnequip(id)
                itemState.owned -> actions.onEquip(id)
                else -> actions.onBuy(id)
            }
        },
        primaryActionStyle = if (itemState.owned) PawtivaButtonStyle.Secondary else PawtivaButtonStyle.Primary,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PurchaseDialog(
    itemState: ShopItemState,
    petType: PetType,
    balance: Int,
    purchasing: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = PawtivaTheme.colors
    val locale = currentLocale
    val item = itemState.item
    val name = stringResource(item.accessory.nameRes())
    val after = (balance - item.price).coerceAtLeast(0)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Text(
                text = stringResource(R.string.shop_confirm_title, name),
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                PetIllustration(
                    type = petType,
                    mood = PetMood.EXCITED,
                    contentDescription = "",
                    modifier = Modifier.size(110.dp),
                    animate = false,
                    accessories = if (item.accessory.fits(petType)) listOf(item.accessory) else emptyList(),
                )
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = stringResource(
                        R.string.shop_confirm_price,
                        pluralStringResource(R.plurals.shop_price_coins, item.price, formatNumber(item.price, locale)),
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary,
                )
                Text(
                    text = stringResource(
                        R.string.shop_confirm_balance_after,
                        pluralStringResource(R.plurals.shop_price_coins, after, formatNumber(after, locale)),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            }
        },
        confirmButton = {
            PawtivaButton(
                text = stringResource(R.string.shop_confirm_buy),
                onClick = onConfirm,
                enabled = !purchasing,
                leadingIcon = PawtivaIcons.Coin,
            )
        },
        dismissButton = {
            PawtivaButton(
                text = stringResource(R.string.shop_confirm_cancel),
                onClick = onDismiss,
                style = PawtivaButtonStyle.Text,
            )
        },
    )
}

/** Position of the preview card in the shop list (header is 0). */
private const val PREVIEW_ITEM_INDEX = 1
