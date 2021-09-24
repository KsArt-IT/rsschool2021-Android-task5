package ru.ksart.thecat.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import coil.load
import ru.ksart.thecat.R
import ru.ksart.thecat.databinding.FragmentCatDetailBinding

class CatDetailFragment: Fragment() {

    private var binding: FragmentCatDetailBinding? = null

    private val args: CatDetailFragmentArgs by navArgs()

    private val item by lazy { args.item }

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
                .load(item.url)
            if (item.breeds.isNotEmpty()) {
                breed.text = getString(R.string.breed_title, item.breeds[0].name)
                description.text = item.breeds[0].description
            }
        }
    }
}
