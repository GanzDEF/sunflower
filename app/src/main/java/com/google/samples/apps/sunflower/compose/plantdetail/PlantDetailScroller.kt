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

package com.google.samples.apps.sunflower.compose.plantdetail

import androidx.animation.FloatPropKey
import androidx.animation.Spring
import androidx.animation.transitionDefinition
import androidx.compose.Composable
import androidx.ui.foundation.ScrollerPosition
import androidx.ui.unit.Density
import androidx.ui.unit.Dp

private const val HeaderTransitionOffset = 150f
private const val ParallaxFactor = 2f

/**
 * Class that contains derived state for when the toolbar should be shown
 */
data class PlantDetailsScroller(
    val scrollerPosition: ScrollerPosition,
    val namePosition: Float
) {
    val toolbarState: ToolbarState
        get() =
            if (namePosition != 0f &&
                scrollerPosition.value > (namePosition + HeaderTransitionOffset)
            ) {
                ToolbarState.SHOWN
            } else {
                ToolbarState.HIDDEN
            }
}

// Toolbar state related classes and functions to achieve the CollapsingToolbarLayout animation
enum class ToolbarState { HIDDEN, SHOWN }

val toolbarAlphaKey = FloatPropKey()
val contentAlphaKey = FloatPropKey()

val toolbarTransitionDefinition = transitionDefinition {
    state(ToolbarState.HIDDEN) {
        this[toolbarAlphaKey] = 0f
        this[contentAlphaKey] = 1f
    }
    state(ToolbarState.SHOWN) {
        this[toolbarAlphaKey] = 1f
        this[contentAlphaKey] = 0f
    }
    transition {
        toolbarAlphaKey using physics<Float> {
            stiffness = Spring.StiffnessLow
        }
        contentAlphaKey using physics<Float> {
            stiffness = Spring.StiffnessLow
        }
    }
}

@Composable
fun Density.scrollerParallaxOffset(
    scrollerPosition: ScrollerPosition
): Dp = (scrollerPosition.value / ParallaxFactor).toDp()
