package ru.ksart.thecat.ui.extensions

import android.app.Activity
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.annotation.StringRes

// показать тост из ресурсов
fun Activity.toast(@StringRes stringId: Int) {
    if (stringId == -1) return
    Toast.makeText(this, stringId, Toast.LENGTH_LONG).show()
}

fun Activity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Fragment.toast(@StringRes stringId: Int) {
    if (stringId == -1) return
    Toast.makeText(requireContext(), stringId, Toast.LENGTH_LONG).show()
}

// показать тост строковый
fun Fragment.toast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
}
