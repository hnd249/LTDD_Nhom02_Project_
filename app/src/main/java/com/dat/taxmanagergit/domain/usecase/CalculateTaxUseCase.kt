package com.dat.taxmanager.domain.usecase

import javax.inject.Inject

class CalculateTaxUseCase @Inject constructor() {

    /**
     * Hàm tính thuế TNCN theo tháng chuẩn luật Việt Nam hiện hành
     */
    fun execute(
        totalTaxableIncome: Double, // Tổng thu nhập chịu thuế (từ DB truyền vào)
        numberOfDependents: Int = 0, // Số người phụ thuộc
        insuranceDeduction: Double = 0.0 // Các khoản bảo hiểm bắt buộc (nếu có)
    ): Double {
        // 1. Các mức giảm trừ cố định (Cập nhật theo luật mới nhất)
        val personalDeduction = 11_000_000.0 // Giảm trừ bản thân: 11 triệu/tháng
        val dependentDeduction = 4_400_000.0 * numberOfDependents // Giảm trừ phụ thuộc: 4.4 triệu/người/tháng

        // Tổng các khoản được giảm trừ
        val totalDeduction = personalDeduction + dependentDeduction + insuranceDeduction

        // 2. Thu nhập tính thuế (Phần tiền dùng để áp vào biểu thuế)
        val assessableIncome = totalTaxableIncome - totalDeduction

        // Nếu thu nhập sau giảm trừ <= 0 thì không phải đóng 1 đồng thuế nào
        if (assessableIncome <= 0) return 0.0

        // 3. Tính thuế theo Biểu thuế lũy tiến từng phần (7 bậc)
        return calculateProgressiveTax(assessableIncome)
    }

    private fun calculateProgressiveTax(income: Double): Double {
        var tax = 0.0

        // Bậc 1: Đến 5 triệu (Thuế suất 5%)
        if (income > 0) {
            val taxable = if (income > 5_000_000) 5_000_000.0 else income
            tax += taxable * 0.05
        }
        // Bậc 2: Trên 5 triệu đến 10 triệu (Thuế suất 10%)
        if (income > 5_000_000) {
            val taxable = if (income > 10_000_000) 5_000_000.0 else (income - 5_000_000)
            tax += taxable * 0.10
        }
        // Bậc 3: Trên 10 triệu đến 18 triệu (Thuế suất 15%)
        if (income > 10_000_000) {
            val taxable = if (income > 18_000_000) 8_000_000.0 else (income - 10_000_000)
            tax += taxable * 0.15
        }
        // Bậc 4: Trên 18 triệu đến 32 triệu (Thuế suất 20%)
        if (income > 18_000_000) {
            val taxable = if (income > 32_000_000) 14_000_000.0 else (income - 18_000_000)
            tax += taxable * 0.20
        }
        // Bậc 5: Trên 32 triệu đến 52 triệu (Thuế suất 25%)
        if (income > 32_000_000) {
            val taxable = if (income > 52_000_000) 20_000_000.0 else (income - 32_000_000)
            tax += taxable * 0.25
        }
        // Bậc 6: Trên 52 triệu đến 80 triệu (Thuế suất 30%)
        if (income > 52_000_000) {
            val taxable = if (income > 80_000_000) 28_000_000.0 else (income - 52_000_000)
            tax += taxable * 0.30
        }
        // Bậc 7: Trên 80 triệu (Thuế suất 35%)
        if (income > 80_000_000) {
            val taxable = income - 80_000_000
            tax += taxable * 0.35
        }

        return tax
    }
}