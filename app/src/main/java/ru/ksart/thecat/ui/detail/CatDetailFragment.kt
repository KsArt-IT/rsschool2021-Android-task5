package ru.ksart.thecat.ui.detail

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import coil.load
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.ksart.thecat.R
import ru.ksart.thecat.databinding.FragmentCatDetailBinding
import ru.ksart.thecat.ui.extensions.toast
import ru.ksart.thecat.utils.isAndroidQ
import timber.log.Timber

@AndroidEntryPoint
class CatDetailFragment : Fragment() {

    private var _binding: FragmentCatDetailBinding? = null
    private val binding get() = requireNotNull(_binding) { "Error: binding is not initialized" }

    private val viewModel by viewModels<DownloadViewModel>()

    // запрос на разрешения
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionToGrantedMap: Map<String, Boolean> ->
        val isGranted = permissionToGrantedMap.values.all { it }
        Timber.d("initPermissionResultListener isGranted=$isGranted")

        viewModel.onPermissions(isGranted)
    }

    private val args: CatDetailFragmentArgs by navArgs()

    private val item by lazy { args.item }

    // для выбора директории с помощью системного пикера, инициализировать в onCreate
    private val createMediaLauncher: ActivityResultLauncher<String> = createMediaLauncher()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentCatDetailBinding.inflate(inflater).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViewModel()
        initListener()
        showDetail()

        if (isAndroidQ.not() && hasPermission().not()) requestPermissionLauncher.launch(*PERMISSIONS.toTypedArray())
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun showDetail() {
        binding.run {
            if (item.breedResponses.isNotEmpty()) {
                breed.text = getString(R.string.breed_title, item.breedResponses[0].name)
                description.text = item.breedResponses[0].description
            } else {
                breed.text = getString(R.string.no_name_cat)
            }
            description.isVisible = description.text.toString().isNotBlank()
            imageDetail.load(item.url)
        }
    }

    private fun initListener() {
        binding.run {
            save.setOnClickListener {
                viewModel.saveMedia(item.url)
            }
            saveAs.setOnClickListener {
                viewModel.saveAsMedia(item.url)
            }
            share.setOnClickListener {
                viewModel.share(item.url)
            }
        }
    }

    private fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.isLoading.collectLatest(::showLoading) }
                launch { viewModel.isToast.collectLatest(::toast) }
                launch { viewModel.shareIntent.collect(::shareCatImage) }
                launch { viewModel.saveTo.collectLatest(createMediaLauncher::launch) }
                launch { viewModel.isPermissionsGranted.collect(::updateUiFromPermission) }
            }
        }
    }

    private fun createMediaLauncher() = registerForActivityResult(
        ActivityResultContracts.CreateDocument()
    ) { uri ->
        viewModel.saveAsMediaTo(uri)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun shareCatImage(shareIntent: Intent?) {
        // проверим, есть ли приложение, которое может принять этот Intent
        shareIntent?.resolveActivity(requireActivity().packageManager)?.let {
            // запускаем активити и передаем интент
            startActivity(shareIntent)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.run {
            save.isEnabled = isLoading.not() && viewModel.isPermissionsGranted.value
            saveAs.isEnabled = isLoading.not()
            share.isEnabled = isLoading.not() && viewModel.isPermissionsGranted.value
            progress.isVisible = isLoading
        }
    }

    private fun updateUiFromPermission(isGranted: Boolean) {
        binding.run {
            Timber.d("updateUiFromPermission isGranted=$isGranted")
            save.isEnabled = isGranted
            share.isEnabled = isGranted
        }
    }

    private fun hasPermission(): Boolean {
        return if (PERMISSIONS.isEmpty()) true
        else {
            PERMISSIONS.all { permission ->
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    companion object {
        private val PERMISSIONS = listOfNotNull(
//            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
                .takeIf { isAndroidQ.not() }
        )
    }
}
