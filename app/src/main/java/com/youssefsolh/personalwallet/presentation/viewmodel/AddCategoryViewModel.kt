package com.youssefsolh.personalwallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youssefsolh.personalwallet.domain.model.Category
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.usecase.CreateCategoryUseCase
import com.youssefsolh.personalwallet.domain.usecase.GetCategoryByIdUseCase
import com.youssefsolh.personalwallet.domain.usecase.UpdateCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddCategoryViewModel @Inject constructor(
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val getCategoryByIdUseCase: GetCategoryByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCategoryUiState())
    val uiState: StateFlow<AddCategoryUiState> = _uiState.asStateFlow()

    private var existingCategory: Category? = null

    fun loadCategory(categoryId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val category = getCategoryByIdUseCase(categoryId)
                if (category != null) {
                    existingCategory = category
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loadedCategory = category,
                        isEditMode = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Category not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load category"
                )
            }
        }
    }

    fun saveCategory(
        name: String,
        icon: String,
        color: String,
        type: TransactionType
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                if (_uiState.value.isEditMode && existingCategory != null) {
                    // Update existing category
                    val updatedCategory = existingCategory!!.copy(
                        name = name,
                        icon = icon,
                        color = color,
                        type = type,
                        updatedAt = System.currentTimeMillis()
                    )
                    updateCategoryUseCase(updatedCategory)
                } else {
                    // Create new category
                    val category = Category(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        icon = icon,
                        color = color,
                        type = type,
                        isCustom = true,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    createCategoryUseCase(category)
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save category"
                )
            }
        }
    }
}

data class AddCategoryUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isEditMode: Boolean = false,
    val loadedCategory: Category? = null,
    val error: String? = null
)
