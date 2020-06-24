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

import androidx.compose.Composable
import androidx.compose.launchInComposition
import androidx.ui.core.Alignment
import androidx.ui.core.DensityAmbient
import androidx.ui.core.Modifier
import androidx.ui.foundation.ScrollerPosition
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.layout.Stack
import androidx.ui.layout.padding
import androidx.ui.material.Snackbar
import androidx.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Gives a parallax effect to the content at the top of a [VerticalScroller].
 */
@Composable
fun ParallaxEffect(
    scrollerPosition: ScrollerPosition,
    parallaxDelta: Float,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit
) {
    val offset = scrollerPosition.value / parallaxDelta
    val offsetDp = with(DensityAmbient.current) { offset.toDp() }
    content(Modifier.padding(top = offsetDp).plus(modifier))
}

/**
 * Simple API to display a Snackbar with text on the screen
 */
@Composable
fun TextSnackbarHolder(
    snackbarText: String,
    showSnackbar: Boolean,
    onDismissSnackbar: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Stack(modifier) {
        content()
        if (showSnackbar) {
            launchInComposition(showSnackbar) {
                delay(5000)
                onDismissSnackbar()
            }

            Snackbar(
                modifier = Modifier
                    .gravity(Alignment.BottomCenter)
                    .systemBarsPadding(bottom = true)
                    .padding(all = 8.dp),
                text = { Text(snackbarText) },
                shape = RoundedCornerShape(4.dp)
            )
        }
    }
}
