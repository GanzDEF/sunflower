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

import android.widget.TextView
import androidx.animation.TransitionState
import androidx.annotation.VisibleForTesting
import androidx.compose.Composable
import androidx.compose.StructurallyEqual
import androidx.compose.getValue
import androidx.compose.setValue
import androidx.compose.state
import androidx.core.text.HtmlCompat
import androidx.ui.animation.Transition
import androidx.ui.core.Alignment
import androidx.ui.core.ContentScale
import androidx.ui.core.ContextAmbient
import androidx.ui.core.DensityAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.drawLayer
import androidx.ui.core.drawOpacity
import androidx.ui.core.globalPosition
import androidx.ui.core.onPositioned
import androidx.ui.core.semantics.semantics
import androidx.ui.foundation.Box
import androidx.ui.foundation.Icon
import androidx.ui.foundation.ScrollerPosition
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.drawBackground
import androidx.ui.foundation.shape.corner.CircleShape
import androidx.ui.graphics.Color
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Column
import androidx.ui.layout.ColumnScope.gravity
import androidx.ui.layout.Row
import androidx.ui.layout.Stack
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.fillMaxWidth
import androidx.ui.layout.offset
import androidx.ui.layout.padding
import androidx.ui.layout.preferredHeight
import androidx.ui.layout.sizeIn
import androidx.ui.layout.wrapContentSize
import androidx.ui.livedata.observeAsState
import androidx.ui.material.EmphasisAmbient
import androidx.ui.material.FloatingActionButton
import androidx.ui.material.IconButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.ProvideEmphasis
import androidx.ui.material.Surface
import androidx.ui.material.TopAppBar
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.Add
import androidx.ui.material.icons.filled.ArrowBack
import androidx.ui.material.icons.filled.Share
import androidx.ui.res.stringResource
import androidx.ui.semantics.accessibilityLabel
import androidx.ui.text.font.FontWeight
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.Dp
import androidx.ui.unit.dp
import androidx.ui.viewinterop.AndroidView
import androidx.ui.viewmodel.viewModel
import com.google.samples.apps.sunflower.R
import com.google.samples.apps.sunflower.compose.Dimens
import com.google.samples.apps.sunflower.compose.utils.getQuantityString
import com.google.samples.apps.sunflower.compose.systemBarsPadding
import com.google.samples.apps.sunflower.compose.utils.TextSnackbarContainer
import com.google.samples.apps.sunflower.compose.visible
import com.google.samples.apps.sunflower.data.Plant
import com.google.samples.apps.sunflower.utilities.InjectorUtils
import com.google.samples.apps.sunflower.viewmodels.PlantDetailViewModel
import dev.chrisbanes.accompanist.coil.CoilImageWithCrossfade
import dev.chrisbanes.accompanist.mdctheme.MaterialThemeFromMdcTheme


/**
 * As these callbacks are passed in through multiple Composables, to avoid having to name
 * parameters to not mix them up, they're aggregated in this class.
 */
data class PlantDetailsCallbacks(
    val onFabClick: () -> Unit,
    val onBackClick: () -> Unit,
    val onShareClick: () -> Unit
)

@Composable
fun PlantDetailsScreen(
    plantId: String,
    onBackClick: () -> Unit,
    onShareClick: (String) -> Unit
) {
    // ViewModel and LiveDatas needed to populate the plant details info on the screen
    val plantDetailsViewModel: PlantDetailViewModel = viewModel(
        factory = InjectorUtils.providePlantDetailViewModelFactory(ContextAmbient.current, plantId)
    )
    val plant = plantDetailsViewModel.plant.observeAsState().value
    val isPlanted = plantDetailsViewModel.isPlanted.observeAsState().value
    val showSnackbar = plantDetailsViewModel.showSnackbar.observeAsState().value

    if (plant != null && isPlanted != null && showSnackbar != null) {
        Surface {
            TextSnackbarContainer(
                snackbarText = stringResource(R.string.added_plant_to_garden),
                showSnackbar = showSnackbar,
                onDismissSnackbar = { plantDetailsViewModel.dismissSnackbar() }
            ) {
                val context = ContextAmbient.current
                PlantDetails(plant, isPlanted, PlantDetailsCallbacks(
                    onBackClick = onBackClick,
                    onFabClick = {
                        plantDetailsViewModel.addPlantToGarden()
                    },
                    onShareClick = {
                        val shareText = context.resources.getString(R.string.share_text_plant, plant.name)
                        onShareClick(shareText)
                    }
                ))
            }
        }
    }
}

@VisibleForTesting
@Composable
fun PlantDetails(
    plant: Plant,
    isPlanted: Boolean,
    callbacks: PlantDetailsCallbacks,
    modifier: Modifier = Modifier
) {
    // PlantDetails owns the scrollerPosition to simulate CollapsingToolbarLayout's behavior
    val scrollerPosition = ScrollerPosition()
    var plantScroller by state {
        PlantDetailsScroller(scrollerPosition, Float.MIN_VALUE)
    }
    val toolbarState = plantScroller.getToolbarState(DensityAmbient.current)

    // Transition that fades in/out the header with the image and the Toolbar
    Transition(
        definition = toolbarTransitionDefinition,
        toState = toolbarState
    ) { transitionState ->
        Stack(modifier) {
            PlantDetailsContent(
                scrollerPosition = plantScroller.scrollerPosition,
                toolbarState = toolbarState,
                onNamePosition = { newNamePosition ->
                    // Comparing to Float.MIN_VALUE as we are just interested on the original
                    // position of name on the screen
                    if (plantScroller.namePosition == Float.MIN_VALUE) {
                        plantScroller = plantScroller.copy(namePosition = newNamePosition)
                    }
                },
                plant = plant,
                isPlanted = isPlanted,
                callbacks = callbacks,
                transitionState = transitionState
            )
            PlantHeader(toolbarState, plant.name, callbacks, transitionState)
        }
    }
}

@Composable
private fun PlantDetailsContent(
    scrollerPosition: ScrollerPosition,
    toolbarState: ToolbarState,
    plant: Plant,
    isPlanted: Boolean,
    onNamePosition: (Float) -> Unit,
    callbacks: PlantDetailsCallbacks,
    transitionState: TransitionState
) {
    VerticalScroller(scrollerPosition) {
        PlantImageHeader(
            scrollerPosition, plant.imageUrl, callbacks.onFabClick, isPlanted,
            Modifier.visible { toolbarState == ToolbarState.HIDDEN },
            Modifier.drawLayer(alpha = transitionState[contentAlphaKey])
        )
        PlantInformation(
            name = plant.name,
            wateringInterval = plant.wateringInterval,
            description = plant.description,
            onNamePosition = { onNamePosition(it) },
            toolbarState = toolbarState
        )
    }
}

@Composable
private fun PlantHeader(
    toolbarState: ToolbarState,
    plantName: String,
    callbacks: PlantDetailsCallbacks,
    transitionState: TransitionState
) {
    if (toolbarState == ToolbarState.SHOWN) {
        PlantDetailsToolbar(
            plantName = plantName,
            onBackClick = callbacks.onBackClick,
            onShareClick = callbacks.onShareClick,
            modifier = Modifier.drawOpacity(transitionState[toolbarAlphaKey])
        )
    } else {
        PlantHeaderActions(
            onBackClick = callbacks.onBackClick,
            onShareClick = callbacks.onShareClick,
            modifier = Modifier.drawOpacity(transitionState[contentAlphaKey])
        )
    }
}

@Composable
private fun PlantDetailsToolbar(
    plantName: String,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface {
        TopAppBar(
            modifier = modifier.systemBarsPadding(bottom = false),
            backgroundColor = MaterialTheme.colors.surface
        ) {
            IconButton(onBackClick, Modifier.gravity(Alignment.CenterVertically)) {
                Icon(Icons.Filled.ArrowBack)
            }
            Text(
                text = plantName,
                style = MaterialTheme.typography.h6,
                // As title in TopAppBar has extra inset on the left, need to do this: b/158829169
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
            val shareAccessibilityLabel = stringResource(R.string.menu_item_share_plant)
            IconButton(
                onShareClick,
                Modifier.gravity(Alignment.CenterVertically).semantics {
                    accessibilityLabel = shareAccessibilityLabel
                }
            ) {
                Icon(Icons.Filled.Share)
            }
        }
    }
}

@Composable
private fun PlantImageHeader(
    scrollerPosition: ScrollerPosition,
    imageUrl: String,
    onFabClick: () -> Unit,
    isPlanted: Boolean,
    modifier: Modifier = Modifier,
    transitionModifier: Modifier = Modifier
) {
    var imageHeight by state(StructurallyEqual) { 0 }

    Stack(modifier.fillMaxWidth()) {
        PlantImage(scrollerPosition, imageUrl, transitionModifier.onPositioned {
            imageHeight = it.size.height
        })
        if (!isPlanted) {
            val fabModifier = if (imageHeight != 0) {
                Modifier
                    .gravity(Alignment.TopEnd)
                    .padding(end = Dimens.PaddingSmall)
                    .offset(y = getFabOffset(imageHeight, scrollerPosition))
                    .plus(transitionModifier)
            } else {
                Modifier.visible { false }
            }
            val fabAccessibilityLabel = stringResource(R.string.add_plant)
            FloatingActionButton(
                onClick = onFabClick,
                shape = MaterialTheme.shapes.small,
                modifier = fabModifier.semantics {
                    accessibilityLabel = fabAccessibilityLabel
                }
            ) {
                Icon(Icons.Filled.Add)
            }
        }
    }
}

@Composable
private fun PlantImage(
    scrollerPosition: ScrollerPosition,
    imageUrl: String,
    modifier: Modifier = Modifier,
    placeholderColor: Color = MaterialTheme.colors.onSurface.copy(0.2f)
) {
    val parallaxOffset = with(DensityAmbient.current) {
        scrollerParallaxOffset(this, scrollerPosition)
    }
    CoilImageWithCrossfade(
        data = imageUrl,
        contentScale = ContentScale.Crop,
        loading = {
            Box(modifier = Modifier.fillMaxSize(), backgroundColor = placeholderColor)
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(top = parallaxOffset)
            .preferredHeight(Dimens.PlantDetailAppBarHeight)
    )
}

@Composable
private fun PlantHeaderActions(
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(top = true)
            .padding(top = Dimens.ToolbarIconPadding),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val iconModifier = Modifier
            .sizeIn(maxWidth = Dimens.ToolbarIconSize, maxHeight = Dimens.ToolbarIconSize)
            .drawBackground(color = MaterialTheme.colors.surface, shape = CircleShape)

        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(start = Dimens.ToolbarIconPadding).plus(iconModifier)
        ) {
            Icon(Icons.Filled.ArrowBack)
        }
        val shareAccessibilityLabel = stringResource(R.string.menu_item_share_plant)
        IconButton(
            onClick = onShareClick,
            modifier = Modifier
                .padding(end = Dimens.ToolbarIconPadding)
                .plus(iconModifier)
                .semantics {
                    accessibilityLabel = shareAccessibilityLabel
                }
        ) {
            Icon(Icons.Filled.Share)
        }
    }
}

@Composable
private fun PlantInformation(
    name: String,
    wateringInterval: Int,
    description: String,
    onNamePosition: (Float) -> Unit,
    toolbarState: ToolbarState
) {
    Column(modifier = Modifier.padding(Dimens.PaddingLarge)) {
        Text(
            text = name,
            style = MaterialTheme.typography.h5,
            modifier = Modifier
                .padding(
                    start = Dimens.PaddingSmall,
                    end = Dimens.PaddingSmall,
                    bottom = Dimens.PaddingNormal
                )
                .gravity(Alignment.CenterHorizontally)
                .onPositioned { onNamePosition(it.globalPosition.y) }
                .visible { toolbarState == ToolbarState.HIDDEN }
        )
        Text(
            text = stringResource(id = R.string.watering_needs_prefix),
            color = MaterialTheme.colors.primaryVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(horizontal = Dimens.PaddingSmall)
                .gravity(Alignment.CenterHorizontally)
        )
        ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
            Text(
                text = getQuantityString(R.plurals.watering_needs_suffix, wateringInterval),
                modifier = Modifier
                    .padding(
                        start = Dimens.PaddingSmall,
                        end = Dimens.PaddingSmall,
                        bottom = Dimens.PaddingNormal
                    )
                    .gravity(Alignment.CenterHorizontally)
            )
        }
        PlantDescription(description)
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

/**
 * Calculates offset FAB needs to keep aligned in the middle of the bottom of the picture.
 *
 * As the [Modifier.onPositioned] in the image is invoked after scrollPosition has changed,
 * there's a frame delay.
 */
@Composable
private fun getFabOffset(imageHeight: Int, scrollerPosition: ScrollerPosition): Dp {
    return with(DensityAmbient.current) {
        imageHeight.toDp() + scrollerParallaxOffset(this, scrollerPosition) - (56 / 2).dp
    }
}

@Preview
@Composable
private fun PlantOverviewPreview() {
    MaterialThemeFromMdcTheme(ContextAmbient.current) {
        Surface {
            PlantDetails(
                Plant("plantId", "Tomato", "HTML<br>description", 6),
                true,
                PlantDetailsCallbacks({ }, { }, { })
            )
        }
    }
}
