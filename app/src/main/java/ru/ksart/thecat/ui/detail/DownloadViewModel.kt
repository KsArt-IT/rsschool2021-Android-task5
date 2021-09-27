package ru.ksart.thecat.ui.detail

import android.net.Uri
import androidx.annotation.IntegerRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.ksart.thecat.R
import ru.ksart.thecat.model.repositories.DownloadRepository
import ru.ksart.thecat.utils.DebugHelper
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val repository: DownloadRepository
) : ViewModel() {
    private val _isToast = MutableStateFlow<@IntegerRes Int>(-1)
    val isToast: StateFlow<Int> get() = _isToast.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading.asStateFlow()

    private val _saveTo = MutableStateFlow<Pair<String, String>>("" to "")
    val saveTo: StateFlow<Pair<String, String>> get() = _saveTo.asStateFlow()

    fun saveMedia(url: String) {
        if (url.isBlank()) {
            _isToast.value = R.string.no_link_to_media_file
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.saveMedia(url)
                _isToast.value = R.string.file_uploaded_to_gallery
            } catch (e: Exception) {
                DebugHelper.log("DownloadViewModel|saveMedia error", e)
                _isToast.value = R.string.file_upload_error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveAsMedia(url: String) {
        val name = File(url).name
        _saveTo.value = name to url
    }

    fun saveAsMediaTo(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.saveAsMedia(uri, saveTo.value.second)
                _isToast.value = R.string.file_uploaded_to_gallery
            } catch (e: Exception) {
                DebugHelper.log("DownloadViewModel|saveAsMedia error", e)
                _isToast.value = R.string.file_upload_error
            } finally {
                _isLoading.value = false
            }
        }
    }

}
