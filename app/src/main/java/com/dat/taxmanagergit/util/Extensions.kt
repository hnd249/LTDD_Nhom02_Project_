presentationpackage com.dat.taxmanager.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

// Định dạng tiền tệ: 5000000 -> 5.000.000 đ
fun Double.toCurrency(): String {
    val formatter = DecimalFormat("#,### đ")
    return formatter.format(this)
}

// Định dạng ngày tháng: Long -> 15/04/2024
fun Long.toDateString(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}