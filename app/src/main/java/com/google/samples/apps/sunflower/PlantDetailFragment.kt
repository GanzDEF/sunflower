/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.sunflower

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.Recomposer
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.ui.core.setContent
import androidx.ui.material.Surface
import com.google.android.material.snackbar.Snackbar
import com.google.samples.apps.sunflower.compose.PlantDetails
import com.google.samples.apps.sunflower.utilities.InjectorUtils
import com.google.samples.apps.sunflower.viewmodels.PlantDetailViewModel
import dev.chrisbanes.accompanist.mdctheme.MaterialThemeFromMdcTheme

/**
 * A fragment representing a single Plant detail screen.
 */
class PlantDetailFragment : Fragment() {

    private val args: PlantDetailFragmentArgs by navArgs()

    private val plantDetailViewModel: PlantDetailViewModel by viewModels {
        InjectorUtils.providePlantDetailViewModelFactory(requireActivity(), args.plantId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_plant_detail, container, false)

//
//            toolbar.setNavigationOnClickListener { view ->
//                view.findNavController().navigateUp()
//            }
//
//            toolbar.setOnMenuItemClickListener { item ->
//                when (item.itemId) {
//                    R.id.action_share -> {
//                        createShareIntent()
//                        true
//                    }
//                    else -> false
//                }
//            }
//        }
//        setHasOptionsMenu(true)

        val composeFrame = view?.findViewById<FrameLayout>(R.id.compose_frame)!!
        composeFrame.setContent(Recomposer.current()) {
            MaterialThemeFromMdcTheme(context = requireContext()) {
                Surface {
                    PlantDetails(
                        plantDetailViewModel.plant,
                        plantDetailViewModel.isPlanted,
                        onFabClicked = {
                            plantDetailViewModel.addPlantToGarden()
                            Snackbar.make(getView()!!,
                                R.string.added_plant_to_garden, Snackbar.LENGTH_LONG).show()
                        }
                    )
                }
            }
        }

        view.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        return view
    }

    // Helper function for calling a share functionality.
    // Should be used when user presses a share button/menu item.
    @Suppress("DEPRECATION")
    private fun createShareIntent() {
        val shareText = plantDetailViewModel.plant.value.let { plant ->
            if (plant == null) {
                ""
            } else {
                getString(R.string.share_text_plant, plant.name)
            }
        }
        val shareIntent = ShareCompat.IntentBuilder.from(activity!!)
            .setText(shareText)
            .setType("text/plain")
            .createChooserIntent()
            .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        startActivity(shareIntent)
    }
}
