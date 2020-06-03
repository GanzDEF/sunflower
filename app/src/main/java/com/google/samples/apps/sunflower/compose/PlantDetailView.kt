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

import android.widget.TextView
import androidx.compose.Composable
import androidx.compose.getValue
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LiveData
import androidx.ui.core.Alignment
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.padding
import androidx.ui.livedata.observeAsState
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.res.stringResource
import androidx.ui.text.font.FontWeight
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import androidx.ui.viewinterop.AndroidView
import com.google.samples.apps.sunflower.R
import com.google.samples.apps.sunflower.data.Plant
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
    VerticalScroller(modifier = Modifier.fillMaxSize().padding(24.dp).plus(modifier)) {
        Text(
            text = plant.name,
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
            text = getQuantityString(R.plurals.watering_needs_suffix, plant.wateringInterval),
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
                .gravity(Alignment.CenterHorizontally)
        )
        PlantDescription(plant.description)
    }
}

@Composable
private fun PlantDescription(description: String) {
    // Issue: User input doesn't work properly, HTML links cannot be clicked - b/158088138
    // HTML support coming to Compose - b/139320905
    AndroidView(resId = R.layout.item_plant_description) {
        (it as TextView).text = HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_COMPACT)
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
