package com.dat.taxmanager.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.dat.taxmanager.data.local.entity.TransactionEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object ExportHelper {

    private val localeVN = Locale("vi", "VN")

    // ===========================================================
    // BỘ THÔNG DỊCH CHO MÁY IN PDF (Ép múi giờ UTC -> VN)
    // ===========================================================
    private fun parseTimestampToDate(timestampRaw: String): Date {
        var parsedDate: Date? = null
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss"
        )

        for (format in formats) {
            try {
                val parser = SimpleDateFormat(format, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                parsedDate = parser.parse(timestampRaw)
                if (parsedDate != null) break
            } catch (e: Exception) {
                // Bỏ qua lỗi, thử format tiếp theo
            }
        }

        return parsedDate ?: try {
            Date(timestampRaw.toLong())
        } catch (e: Exception) {
            Date()
        }
    }


    // =======================================================
    // 1. MÁY IN LỊCH SỬ GIAO DỊCH
    // =======================================================
    fun exportHistoryToPdf(context: Context, transactions: List<TransactionEntity>) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Khổ A4
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            val paint = Paint()

            // Tiêu đề
            paint.color = Color.BLACK
            paint.textSize = 24f
            paint.isFakeBoldText = true
            canvas.drawText("LỊCH SỬ GIAO DỊCH", 40f, 60f, paint)

            // Tiêu đề cột
            paint.textSize = 11f
            var y = 110f
            canvas.drawText("NGÀY", 40f, y, paint)
            canvas.drawText("GIỜ", 110f, y, paint)
            canvas.drawText("LOẠI", 170f, y, paint)
            canvas.drawText("SỐ TIỀN", 230f, y, paint)
            canvas.drawText("DANH MỤC", 340f, y, paint)
            canvas.drawText("GHI CHÚ", 460f, y, paint)

            // Đường gạch ngang
            y += 10f
            canvas.drawLine(40f, y, 560f, y, paint)
            y += 25f

            // Định dạng ngày giờ
            val sdfDate = SimpleDateFormat("dd/MM/yyyy", localeVN)
            val sdfTime = SimpleDateFormat("HH:mm:ss", localeVN)
            paint.isFakeBoldText = false

            // Đổ dữ liệu
            transactions.forEach { tx ->
                // SỬ DỤNG BỘ THÔNG DỊCH Ở ĐÂY
                val dateObj = parseTimestampToDate(tx.timestamp)

                val dateStr = sdfDate.format(dateObj)
                val timeStr = sdfTime.format(dateObj)
                val typeText = if (tx.type == "INCOME") "THU" else "CHI"

                canvas.drawText(dateStr, 40f, y, paint)
                canvas.drawText(timeStr, 110f, y, paint)
                canvas.drawText(typeText, 170f, y, paint)
                canvas.drawText("%,.0f".format(tx.amount), 230f, y, paint)
                canvas.drawText(tx.category, 340f, y, paint)
                canvas.drawText(tx.note.ifBlank { "" }, 460f, y, paint)
                y += 25f
            }

            pdfDocument.finishPage(page)

            val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(folder, "LichSuGiaoDich_${System.currentTimeMillis()}.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            Toast.makeText(context, "Đã lưu PDF Lịch sử giao dịch!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi xuất PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    // =======================================================
    // 2. MÁY IN BÁO CÁO THUẾ
    // =======================================================
    fun exportToPdf(context: Context, transactions: List<TransactionEntity>) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            val paint = Paint()

            val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
            val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val netIncome = totalIncome - totalExpense

            paint.color = Color.BLACK
            paint.textSize = 20f
            paint.isFakeBoldText = true
            canvas.drawText("BÁO CÁO THUẾ THU NHẬP CÁ NHÂN", 40f, 60f, paint)

            paint.textSize = 13f
            paint.isFakeBoldText = false
            var y = 100f
            canvas.drawText("Tổng thu nhập: %,.0f đ".format(totalIncome), 40f, y, paint)
            y += 20f
            canvas.drawText("Tổng chi tiêu / giảm trừ: %,.0f đ".format(totalExpense), 40f, y, paint)
            y += 30f

            paint.isFakeBoldText = true
            val finalTaxable = if (netIncome > 0) netIncome else 0.0
            canvas.drawText("THU NHẬP TÍNH THUẾ DỰ KIẾN: %,.0f đ".format(finalTaxable), 40f, y, paint)

            y += 20f
            canvas.drawLine(40f, y, 550f, y, paint)
            y += 35f

            paint.textSize = 12f
            canvas.drawText("Chi tiết các khoản thu nhập (kèm thời gian):", 40f, y, paint)
            y += 25f

            paint.isFakeBoldText = true
            canvas.drawText("NGÀY", 40f, y, paint)
            canvas.drawText("GIỜ", 120f, y, paint)
            canvas.drawText("SỐ TIỀN", 200f, y, paint)
            canvas.drawText("NGUỒN / DANH MỤC", 320f, y, paint)

            y += 10f
            canvas.drawLine(40f, y, 550f, y, paint)
            y += 25f

            paint.isFakeBoldText = false
            val sdfDate = SimpleDateFormat("dd/MM/yyyy", localeVN)
            val sdfTime = SimpleDateFormat("HH:mm:ss", localeVN)

            transactions.filter { it.type == "INCOME" }.forEach { tx ->
                // SỬ DỤNG BỘ THÔNG DỊCH Ở ĐÂY
                val dateObj = parseTimestampToDate(tx.timestamp)

                val dateStr = sdfDate.format(dateObj)
                val timeStr = sdfTime.format(dateObj)

                canvas.drawText(dateStr, 40f, y, paint)
                canvas.drawText(timeStr, 120f, y, paint)
                canvas.drawText("+ %,.0f".format(tx.amount), 200f, y, paint)
                canvas.drawText(tx.category, 320f, y, paint)
                y += 25f
            }

            pdfDocument.finishPage(page)

            val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(folder, "BaoCaoThue_TNCN_${System.currentTimeMillis()}.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            Toast.makeText(context, "Đã lưu Báo cáo Thuế TNCN!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi xuất báo cáo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}