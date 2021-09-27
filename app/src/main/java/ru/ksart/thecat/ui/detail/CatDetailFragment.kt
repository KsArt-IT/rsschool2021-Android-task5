package ru.ksart.thecat.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import coil.load
import coil.request.ImageRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import ru.ksart.thecat.ui.extensions.toast
import ru.ksart.thecat.R
import ru.ksart.thecat.databinding.FragmentCatDetailBinding

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
        // установим свою анимацию перехода
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(R.transition.movie)
        // отложить переход входа
        postponeEnterTransition()
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
            imageDetail.apply { transitionName = item.id }
                .load(item.url) {
                    listener(
                        // pass two arguments
                        onSuccess = { _, _ ->
                            startPostponedEnterTransition()
                        },
                        onError = { request: ImageRequest, throwable: Throwable ->
                            startPostponedEnterTransition()
//                            request.error
                        }
                    )
                    build()
                }
            if (item.breeds.isNotEmpty()) {
                breed.text = getString(R.string.breed_title, item.breeds[0].name)
                description.text = item.breeds[0].description
            }
        }
    }

    private fun initListener() {
        binding.save.setOnClickListener {
            viewModel.saveMedia(item.url)
        }
        binding.saveAs.setOnClickListener {
            viewModel.saveAsMedia(item.url)
        }
    }

    private fun bindViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.isLoading.collect(::showLoading)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.isToast.collect(::toast)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.saveTo.collectLatest { (name, _) ->
                if (name.isNotBlank()) createFile(name)
            }
        }
    }

    private fun createMediaLauncher() = registerForActivityResult(
        ActivityResultContracts.CreateDocument()
    ) { uri ->
        viewModel.saveAsMediaTo(uri)
    }

    private fun createFile(name: String) {
        createMediaLauncher.launch(name)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.save.isEnabled = isLoading.not()
        binding.saveAs.isEnabled = isLoading.not()
        binding.progress.isVisible = isLoading
    }
}
