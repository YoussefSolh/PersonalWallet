package com.youssefsolh.personalwallet.data.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.presentation.viewmodel.TransactionWithCategory
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for exporting transactions to CSV format
 */
@Singleton
class CsvExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val fileNameDateFormatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * Export transactions to CSV file
     * @param transactions List of transactions with category info
     * @param walletName Optional wallet name for filtering
     * @return URI of the created CSV file
     */
    fun exportTransactionsToCsv(
        transactions: List<TransactionWithCategory>,
        walletName: String? = null
    ): Result<Uri> {
        return try {
            val fileName = generateFileName(walletName)
            val file = createCsvFile(fileName)

            FileWriter(file).use { writer ->
                // Write CSV header
                writer.append("Date,Type,Amount,Category,Description,Wallet\n")

                // Write transaction data
                transactions.forEach { txnWithCategory ->
                    val transaction = txnWithCategory.transaction
                    val category = txnWithCategory.category

                    writer.append(
                        "${dateFormatter.format(Date(transaction.timestamp))}," +
                        "${transaction.type.name}," +
                        "${transaction.amount}," +
                        "${escapeCsvField(category?.name ?: "Uncategorized")}," +
                        "${escapeCsvField(transaction.description)}," +
                        "${escapeCsvField(walletName ?: "All Wallets")}\n"
                    )
                }
            }

            // Get shareable URI using FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export transactions in detailed format with more fields
     */
    fun exportTransactionsToDetailedCsv(
        transactions: List<TransactionWithCategory>,
        walletName: String? = null
    ): Result<Uri> {
        return try {
            val fileName = generateFileName(walletName, detailed = true)
            val file = createCsvFile(fileName)

            FileWriter(file).use { writer ->
                // Write detailed CSV header
                writer.append("Date,Time,Type,Amount,Currency,Category,Category Color,Description,Wallet,Transaction ID\n")

                // Write transaction data
                transactions.forEach { txnWithCategory ->
                    val transaction = txnWithCategory.transaction
                    val category = txnWithCategory.category
                    val dateTime = Date(transaction.timestamp)

                    writer.append(
                        "${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dateTime)}," +
                        "${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(dateTime)}," +
                        "${transaction.type.name}," +
                        "${transaction.amount}," +
                        "USD," + // TODO: Get from wallet
                        "${escapeCsvField(category?.name ?: "Uncategorized")}," +
                        "${category?.color ?: ""}," +
                        "${escapeCsvField(transaction.description)}," +
                        "${escapeCsvField(walletName ?: "All Wallets")}," +
                        "${transaction.id}\n"
                    )
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create share intent for CSV file
     */
    fun createShareIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_SUBJECT, "Personal Wallet Transactions")
            putExtra(Intent.EXTRA_TEXT, "Exported transactions from Personal Wallet")
        }
    }

    private fun generateFileName(walletName: String?, detailed: Boolean = false): String {
        val timestamp = fileNameDateFormatter.format(Date())
        val prefix = if (detailed) "transactions_detailed" else "transactions"
        val wallet = walletName?.replace(" ", "_") ?: "all"
        return "${prefix}_${wallet}_${timestamp}.csv"
    }

    private fun createCsvFile(fileName: String): File {
        val exportDir = File(context.getExternalFilesDir(null), "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        return File(exportDir, fileName)
    }

    private fun escapeCsvField(field: String): String {
        // Escape quotes and wrap in quotes if contains comma, quote, or newline
        val escaped = field.replace("\"", "\"\"")
        return if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            "\"$escaped\""
        } else {
            escaped
        }
    }

    /**
     * Get list of previously exported CSV files
     */
    fun getExportedFiles(): List<File> {
        val exportDir = File(context.getExternalFilesDir(null), "exports")
        return if (exportDir.exists()) {
            exportDir.listFiles()?.filter { it.extension == "csv" }?.sortedByDescending { it.lastModified() } ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * Delete exported CSV file
     */
    fun deleteExportedFile(file: File): Boolean {
        return file.delete()
    }
}
