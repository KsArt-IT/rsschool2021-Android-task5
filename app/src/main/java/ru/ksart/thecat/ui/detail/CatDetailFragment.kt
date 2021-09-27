package ru.ksart.thecat.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import coil.load
import coil.request.ImageRequest
import ru.ksart.thecat.R
import ru.ksart.thecat.databinding.FragmentCatDetailBinding

class CatDetailFragment: Fragment() {

    private var binding: FragmentCatDetailBinding? = null

    private val args: CatDetailFragmentArgs by navArgs()

    private val item by lazy { args.item }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    ): View = FragmentCatDetailBinding.inflate(inflater).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showDetail()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun showDetail() {
        binding?.run {
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
}
