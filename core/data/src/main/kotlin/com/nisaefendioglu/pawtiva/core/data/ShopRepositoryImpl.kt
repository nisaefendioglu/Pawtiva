package com.nisaefendioglu.pawtiva.core.data

import androidx.room.withTransaction
import com.nisaefendioglu.pawtiva.core.database.CurrencyTransactionEntity
import com.nisaefendioglu.pawtiva.core.database.CurrencyTransactionType
import com.nisaefendioglu.pawtiva.core.database.InventoryEntity
import com.nisaefendioglu.pawtiva.core.database.PawtivaDatabase
import com.nisaefendioglu.pawtiva.core.domain.LevelCalculator
import com.nisaefendioglu.pawtiva.core.domain.PawtivaClock
import com.nisaefendioglu.pawtiva.core.domain.ShopCatalog
import com.nisaefendioglu.pawtiva.core.model.AccessoryId
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.PurchaseOutcome
import com.nisaefendioglu.pawtiva.core.model.ShopItemState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class ShopRepositoryImpl @Inject constructor(
    private val db: PawtivaDatabase,
    private val clock: PawtivaClock,
    private val achievements: AchievementUnlocker,
) : ShopRepository {

    private val inventoryDao get() = db.inventoryDao()
    private val currencyDao get() = db.currencyDao()
    private val completionDao get() = db.completionDao()

    override fun observeShop(petType: PetType): Flow<List<ShopItemState>> = combine(
        inventoryDao.observeAll(),
        currencyDao.observeBalance(),
        completionDao.observeTotalXp(),
    ) { inventory, balance, xp ->
        val owned = inventory.associateBy { it.itemId }
        val level = LevelCalculator.level(xp)
        ShopCatalog.items.map { item ->
            val row = owned[item.accessory.storageId]
            ShopItemState(
                item = item,
                owned = row != null,
                equipped = row?.equipped == true,
                fitsCurrentPet = item.accessory.fits(petType),
                affordable = balance >= item.price,
                levelUnlocked = level >= item.requiredLevel,
            )
        }
    }.distinctUntilChanged()

    override fun observeEquipped(): Flow<Set<AccessoryId>> = inventoryDao.observeAll()
        .map { rows -> rows.filter { it.equipped }.mapNotNull { AccessoryId.fromStorageId(it.itemId) }.toSet() }
        .distinctUntilChanged()

    override suspend fun purchase(id: AccessoryId): PurchaseOutcome {
        val item = ShopCatalog.find(id) ?: return PurchaseOutcome.UnknownItem
        return db.withTransaction {
            if (inventoryDao.get(id.storageId) != null) return@withTransaction PurchaseOutcome.AlreadyOwned
            val level = LevelCalculator.level(completionDao.totalXp())
            if (level < item.requiredLevel) return@withTransaction PurchaseOutcome.LevelTooLow(item.requiredLevel)
            val balance = currencyDao.balance()
            if (balance < item.price) return@withTransaction PurchaseOutcome.InsufficientBalance(item.price - balance)

            val now = clock.now().toEpochMilli()
            currencyDao.insert(
                CurrencyTransactionEntity(
                    amount = -item.price,
                    type = CurrencyTransactionType.PURCHASE.name,
                    refKey = "purchase:${id.storageId}",
                    createdAtEpochMs = now,
                ),
            )
            inventoryDao.insert(InventoryEntity(itemId = id.storageId, purchasedAtEpochMs = now, equipped = false))
            val unlocked = achievements.unlockNew(hadPerfectDay = false)
            PurchaseOutcome.Purchased(item = item, balanceAfter = balance - item.price, unlockedAchievements = unlocked)
        }
    }

    override suspend fun equip(id: AccessoryId): Boolean = db.withTransaction {
        if (inventoryDao.get(id.storageId) == null) return@withTransaction false
        inventoryDao.getAll()
            .filter { it.equipped && it.itemId != id.storageId }
            .filter { AccessoryId.fromStorageId(it.itemId)?.slot == id.slot }
            .forEach { inventoryDao.setEquipped(it.itemId, false) }
        inventoryDao.setEquipped(id.storageId, true)
        true
    }

    override suspend fun unequip(id: AccessoryId) {
        inventoryDao.setEquipped(id.storageId, false)
    }
}
