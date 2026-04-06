package com.dat.taxmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.dat.taxmanager.domain.usecase.CalculateTaxUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val calculateTaxUseCase: CalculateTaxUseCase // Tiêm bộ não tính thuế vào đây
) : ViewModel() {

    // Trạng thái lưu trữ số tiền thu nhập nhập vào
    private val _taxableIncome = MutableStateFlow("")
    val taxableIncome: StateFlow<String> = _taxableIncome.asStateFlow()

    // Trạng thái lưu số người phụ thuộc
    private val _dependents = MutableStateFlow("0")
    val dependents: StateFlow<String> = _dependents.asStateFlow()

    // Kết quả tiền thuế phải nộp
    private val _taxResult = MutableStateFlow(0.0)
    val taxResult: StateFlow<Double> = _taxResult.asStateFlow()

    // Hàm cập nhật thu nhập và tính lại thuế ngay lập tức
    fun updateIncome(income: String) {
        _taxableIncome.value = income
        calculateTax()
    }

    // Hàm cập nhật người phụ thuộc và tính lại thuế
    fun updateDependents(count: String) {
        _dependents.value = count
        calculateTax()
    }

    // Logic gọi UseCase để tính toán
    private fun calculateTax() {
        val incomeVal = _taxableIncome.value.toDoubleOrNull() ?: 0.0
        val depVal = _dependents.value.toIntOrNull() ?: 0

        // Gọi thẳng hàm execute của bộ não
        _taxResult.value = calculateTaxUseCase.execute(
            totalTaxableIncome = incomeVal,
            numberOfDependents = depVal
        )
    }
}