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

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import androidx.annotation.RawRes
import androidx.compose.Composable
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation.findNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.ui.core.ContextAmbient
import androidx.ui.test.android.AndroidComposeTestRule
import androidx.ui.test.assertIsDisplayed
import androidx.ui.test.assertIsNotDisplayed
import androidx.ui.test.createComposeRule
import androidx.ui.test.findByTag
import androidx.ui.test.findByText
import androidx.ui.test.runOnIdleCompose
import androidx.ui.test.runOnUiThread
import com.google.common.truth.Truth
import com.google.samples.apps.sunflower.compose.PlantDetails
import com.google.samples.apps.sunflower.compose.PlantDetailsCallbacks
import com.google.samples.apps.sunflower.compose.SunflowerTestTags.Companion.PlantDetails_Fab
import com.google.samples.apps.sunflower.data.Plant
import com.google.samples.apps.sunflower.utilities.testPlant
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlantDetailFragmentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun plantDetails_checkIsNotPlanted() {
        composeTestRule.setContent {
            PlantDetails(
                plantObservable = MutableLiveData(plantForTesting()),
                isPlantedObservable = MutableLiveData(false),
                callbacks = PlantDetailsCallbacks({ }, { }, { })
            )
        }

        findByText("Apple").assertIsDisplayed()
        findByTag(PlantDetails_Fab).assertExists()
    }

    @Test
    fun plantDetails_checkIsPlanted() {
        composeTestRule.setContent {
            PlantDetails(
                plantObservable = MutableLiveData(plantForTesting()),
                isPlantedObservable = MutableLiveData(true),
                callbacks = PlantDetailsCallbacks({ }, { }, { })
            )
        }

        findByText("Apple").assertIsDisplayed()
        findByTag(PlantDetails_Fab).assertDoesNotExist()
    }

//    @get:Rule
//    val composeTestRule = AndroidComposeTestRule<GardenActivity>()
//
//    @Before
//    fun jumpToPlantDetailFragment() {
//        composeTestRule.activityRule.scenario.onActivity { gardenActivity ->
//            runOnUiThread {
//                val bundle = Bundle().apply { putString("plantId", testPlant.plantId) }
//                findNavController(gardenActivity, R.id.nav_host)
//                    .navigate(R.id.plant_detail_fragment, bundle)
//            }
//        }
//    }
//
//    @Ignore("Share button redesign pending")
//    @Test
//    fun testShareTextIntent() {
//        val shareText = composeTestRule.activityTestRule.activity.getString(
//            R.string.share_text_plant,
//            testPlant.name
//        )
//
//        Intents.init()
//        onView(withId(R.id.action_share)).perform(click())
//        intended(
//            chooser(
//                allOf(
//                    hasAction(Intent.ACTION_SEND),
//                    hasType("text/plain"),
//                    hasExtra(Intent.EXTRA_TEXT, shareText)
//                )
//            )
//        )
//        Intents.release()
//
//        // dismiss the Share Dialog
//        InstrumentationRegistry.getInstrumentation()
//            .uiAutomation
//            .performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
//    }
}

@Composable
private fun plantForTesting(): Plant {
    return Plant(
        plantId = "malus-pumila",
        name = "Apple",
        description = "An apple is a sweet, edible fruit produced by an apple tree (Malus pumila). Apple trees are cultivated worldwide, and are the most widely grown species in the genus Malus. The tree originated in Central Asia, where its wild ancestor, Malus sieversii, is still found today. Apples have been grown for thousands of years in Asia and Europe, and were brought to North America by European colonists. Apples have religious and mythological significance in many cultures, including Norse, Greek and European Christian traditions.<br><br>Apple trees are large if grown from seed. Generally apple cultivars are propagated by grafting onto rootstocks, which control the size of the resulting tree. There are more than 7,500 known cultivars of apples, resulting in a range of desired characteristics. Different cultivars are bred for various tastes and uses, including cooking, eating raw and cider production. Trees and fruit are prone to a number of fungal, bacterial and pest problems, which can be controlled by a number of organic and non-organic means. In 2010, the fruit's genome was sequenced as part of research on disease control and selective breeding in apple production.<br><br>Worldwide production of apples in 2014 was 84.6 million tonnes, with China accounting for 48% of the total.<br><br>(From <a href=\\\"https://en.wikipedia.org/wiki/Apple\\\">Wikipedia</a>)",
        growZoneNumber = 3,
        wateringInterval = 30,
        imageUrl = rawUri(R.raw.apple).toString()
    )
}

@Composable
fun rawUri(@RawRes id: Int): Uri {
    return "${ContentResolver.SCHEME_ANDROID_RESOURCE}://${ContextAmbient.current.packageName}/$id"
        .toUri()
}
