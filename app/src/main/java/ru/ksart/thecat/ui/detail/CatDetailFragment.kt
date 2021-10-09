package ru.ksart.thecat.ui.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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

@AndroidEntryPoint
class CatDetailFragment : Fragment() {

    private var _binding: FragmentCatDetailBinding? = null
    private val binding get() = requireNotNull(_binding) { "Error: binding is not initialized" }

    private val viewModel by viewModels<DownloadViewModel>()

    private val args: CatDetailFragmentArgs by navArgs()

    private val item by lazy { args.item }

    // для выбора директории с помощью системного пикера, инициализировать в onCreate
    private lateinit var createMediaLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createMediaLauncher = createMediaLauncher()
    }

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
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun showDetail() {
        binding.run {
            if (item.breeds.isNotEmpty()) {
                breed.text = getString(R.string.breed_title, item.breeds[0].name)
                description.text = item.breeds[0].description
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
            save.isEnabled = isLoading.not()
            saveAs.isEnabled = isLoading.not()
            share.isEnabled = isLoading.not()
            progress.isVisible = isLoading
        }
    }
}
