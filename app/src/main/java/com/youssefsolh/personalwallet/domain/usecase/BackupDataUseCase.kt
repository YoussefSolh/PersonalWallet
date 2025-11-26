package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.repository.BackupRepository
import javax.inject.Inject

class BackupDataUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(): Result<String> {
        return backupRepository.createBackup().fold(
            onSuccess = { backupData ->
                backupRepository.uploadBackupToDrive(backupData)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }
}
