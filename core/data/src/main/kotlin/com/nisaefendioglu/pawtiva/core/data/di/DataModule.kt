package com.nisaefendioglu.pawtiva.core.data.di

import com.nisaefendioglu.pawtiva.core.data.AchievementRepository
import com.nisaefendioglu.pawtiva.core.data.AchievementRepositoryImpl
import com.nisaefendioglu.pawtiva.core.data.AppCompatLanguageController
import com.nisaefendioglu.pawtiva.core.data.AppLanguageController
import com.nisaefendioglu.pawtiva.core.data.DataManagementRepository
import com.nisaefendioglu.pawtiva.core.data.DataManagementRepositoryImpl
import com.nisaefendioglu.pawtiva.core.data.DataStoreUserPreferencesRepository
import com.nisaefendioglu.pawtiva.core.data.HabitRepository
import com.nisaefendioglu.pawtiva.core.data.HabitRepositoryImpl
import com.nisaefendioglu.pawtiva.core.data.ProgressRepository
import com.nisaefendioglu.pawtiva.core.data.ProgressRepositoryImpl
import com.nisaefendioglu.pawtiva.core.data.ShopRepository
import com.nisaefendioglu.pawtiva.core.data.ShopRepositoryImpl
import com.nisaefendioglu.pawtiva.core.data.SystemPawtivaClock
import com.nisaefendioglu.pawtiva.core.data.UserPreferencesRepository
import com.nisaefendioglu.pawtiva.core.domain.PawtivaClock
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: DataStoreUserPreferencesRepository): UserPreferencesRepository

    @Binds
    abstract fun bindAppLanguageController(impl: AppCompatLanguageController): AppLanguageController

    @Binds
    @Singleton
    abstract fun bindClock(impl: SystemPawtivaClock): PawtivaClock

    @Binds
    @Singleton
    abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository

    @Binds
    @Singleton
    abstract fun bindShopRepository(impl: ShopRepositoryImpl): ShopRepository

    @Binds
    @Singleton
    abstract fun bindAchievementRepository(impl: AchievementRepositoryImpl): AchievementRepository

    @Binds
    @Singleton
    abstract fun bindDataManagementRepository(impl: DataManagementRepositoryImpl): DataManagementRepository
}
