package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.repository.BackupRepository
import javax.inject.Inject

class RestoreDataUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(fileId: String): Result<Unit> {
        return backupRepository.downloadBackupFromDrive(fileId).fold(
            onSuccess = { backupData ->
                backupRepository.restoreBackup(backupData)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }
}
