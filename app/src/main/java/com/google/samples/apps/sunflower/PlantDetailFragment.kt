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
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.ui.core.setContent
import androidx.ui.material.Surface
import com.google.samples.apps.sunflower.compose.PlantDetailsScreen
import com.google.samples.apps.sunflower.compose.ProvideInsets
import dev.chrisbanes.accompanist.mdctheme.MaterialThemeFromMdcTheme

/**
 * A fragment representing a single Plant detail screen.
 */
class PlantDetailFragment : Fragment() {

    private val args: PlantDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_plant_detail, container, false
        ).apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

            findViewById<FrameLayout>(R.id.compose_frame)!!.apply {
                setContent(Recomposer.current()) {
                    MaterialThemeFromMdcTheme(context = requireContext()) {
                        ProvideInsets {
                            Surface {
                                PlantDetailsScreen(
                                    args.plantId,
                                    onBackClicked = {
                                        this.findNavController().navigateUp()
                                    },
                                    onShareClicked = {
                                        createShareIntent(it)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Helper function for calling a share functionality.
    // Should be used when user presses a share button/menu item.
    @Suppress("DEPRECATION")
    private fun createShareIntent(shareText: String) {
        val shareIntent = ShareCompat.IntentBuilder.from(requireActivity())
            .setText(shareText)
            .setType("text/plain")
            .createChooserIntent()
            .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        startActivity(shareIntent)
    }
}
