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

import android.content.ContentResolver
import android.net.Uri
import androidx.annotation.PluralsRes
import androidx.annotation.RawRes
import androidx.compose.Composable
import androidx.compose.launchInComposition
import androidx.core.net.toUri
import androidx.ui.core.Alignment
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.layout.Stack
import androidx.ui.layout.padding
import androidx.ui.material.Snackbar
import androidx.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Load a string with grammatically correct pluralization for the given quantity,
 * using the given arguments.
 *
 * TODO: Remove when b/158065051 is fixed
 *
 * @param id the resource identifier
 * @param quantity The number used to get the correct string for the current language's
 *           plural rules.
 *
 * @return the string data associated with the resource
 */
@Composable
fun getQuantityString(@PluralsRes id: Int, quantity: Int): String {
    val context = ContextAmbient.current
    return context.resources.getQuantityString(id, quantity, quantity)
}

/**
 * Returns the Uri of a given raw resource
 */
@Composable
fun rawUri(@RawRes id: Int): Uri {
    return "${ContentResolver.SCHEME_ANDROID_RESOURCE}://${ContextAmbient.current.packageName}/$id"
        .toUri()
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
