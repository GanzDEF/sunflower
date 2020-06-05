/*
 * Copyright 2020 Google LLC
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

package com.google.samples.apps.sunflower.compose

import android.util.Log
import android.widget.TextView
import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.state
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LiveData
import androidx.ui.core.Alignment
import androidx.ui.core.ContentScale
import androidx.ui.core.ContextAmbient
import androidx.ui.core.DensityAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.onPositioned
import androidx.ui.foundation.Box
import androidx.ui.foundation.ScrollerPosition
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.layout.ColumnScope.gravity
import androidx.ui.layout.Stack
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.fillMaxWidth
import androidx.ui.layout.offset
import androidx.ui.layout.padding
import androidx.ui.layout.preferredHeight
import androidx.ui.livedata.observeAsState
import androidx.ui.material.FloatingActionButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.res.stringResource
import androidx.ui.text.font.FontWeight
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.Dp
import androidx.ui.unit.Px
import androidx.ui.unit.dp
import androidx.ui.unit.px
import androidx.ui.unit.toPx
import androidx.ui.viewinterop.AndroidView
import com.google.samples.apps.sunflower.R
import com.google.samples.apps.sunflower.data.Plant
import dev.chrisbanes.accompanist.coil.CoilImageWithCrossfade
import dev.chrisbanes.accompanist.mdctheme.MaterialThemeFromMdcTheme

@Composable
fun PlantDetails(plants: LiveData<Plant>) {
    val plant by plants.observeAsState()
    if (plant != null) {
        PlantOverview(plant!!)
    }
}

@Composable
private fun PlantOverview(plant: Plant, modifier: Modifier = Modifier) {
    val scrollerPosition = ScrollerPosition()
    val imageHeight = state { Px.Zero }

    VerticalScroller(
        scrollerPosition = scrollerPosition,
        modifier = Modifier.fillMaxSize().plus(modifier)
    ) {
        Log.e("TEST", "Scroller position ${scrollerPosition.value}")
        Stack(Modifier.fillMaxWidth()) {
            PlantImage(scrollerPosition, plant.imageUrl, Modifier.onPositioned {
                imageHeight.value = it.size.height.toPx()
            })
            val fabModifier = if (imageHeight.value != Px.Zero) {
                Modifier.gravity(Alignment.TopEnd).padding(end = 8.dp)
                    .offset(y = getFabOffset(imageHeight.value, scrollerPosition))
            } else {
                Modifier
            }
            FloatingActionButton(onClick = {}, modifier = fabModifier) {

            }
        }
        PlantInformation(
            name = plant.name, wateringInterval = plant.wateringInterval,
            description = plant.description
        )
    }
}

@Composable
private fun PlantImage(
    scrollerPosition: ScrollerPosition,
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    ParallaxEffect(scrollerPosition, modifier) { parallaxModifier ->
        CoilImageWithCrossfade(
            data = imageUrl,
            contentScale = ContentScale.Crop,
            modifier = parallaxModifier.fillMaxWidth().preferredHeight(278.dp)
        )
    }
}

@Composable
private fun PlantInformation(
    name: String,
    wateringInterval: Int,
    description: String
) {
    Box(modifier = Modifier.padding(24.dp)) {
        Text(
            text = name,
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
                .gravity(Alignment.CenterHorizontally)
        )
        Text(
            text = stringResource(id = R.string.watering_needs_prefix),
            color = MaterialTheme.colors.primaryVariant,
            style = MaterialTheme.typography.body1.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 8.dp).gravity(Alignment.CenterHorizontally)
        )
        Text(
            text = getQuantityString(R.plurals.watering_needs_suffix, wateringInterval),
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
                .gravity(Alignment.CenterHorizontally)
        )
        PlantDescription(description)
    }
}

@Composable
private fun ParallaxEffect(
    scrollerPosition: ScrollerPosition,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit
) {
    val offset = scrollerPosition.value.px / 2
    val offsetDp = with(DensityAmbient.current) { offset.value.toDp() }
    content(Modifier.padding(top = offsetDp).plus(modifier))
}

@Composable
private fun PlantDescription(description: String) {
    // Issue: User input doesn't work properly, HTML links cannot be clicked - b/158088138
    // HTML support coming to Compose - b/139320905
    AndroidView(resId = R.layout.item_plant_description) {
        (it as TextView).text = HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }
}

/**
 * Calculates offset FAB needs to keep aligned in the middle of the bottom of the picture.
 *
 * As the onPositioned in the image is invoked after scrollPosition has changed, there's a frame
 * delay.
 */
@Composable
private fun getFabOffset(imageHeight: Px, scrollerPosition: ScrollerPosition): Dp {
    return with(DensityAmbient.current) {
        imageHeight.value.toDp() + scrollerPosition.value.toDp() - (56 / 2).dp
    }
}

@Preview
@Composable
private fun PlantOverviewPreview() {
    MaterialThemeFromMdcTheme(ContextAmbient.current) {
        Surface {
            PlantOverview(
                Plant("plantId", "Tomato", "HTML<br>description", 6)
            )
        }
    }
}
