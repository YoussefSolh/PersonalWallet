package com.youssefsolh.personalwallet

import android.app.Application
import com.youssefsolh.personalwallet.data.local.DataMigrationHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class WalletApplication : Application() {

    @Inject
    lateinit var dataMigrationHelper: DataMigrationHelper

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Migrate orphaned data from v1 to current user
        applicationScope.launch {
            dataMigrationHelper.migrateOrphanedDataIfNeeded()
        }
    }
}