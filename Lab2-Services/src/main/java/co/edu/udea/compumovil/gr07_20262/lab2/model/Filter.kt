/*
 * Copyright 2020 The Android Open Source Project
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

package co.edu.udea.compumovil.gr07_20262.lab2.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import co.edu.udea.compumovil.gr07_20262.lab2.R

@Stable
/** [name] es la clave estable del filtro; [labelRes] es el texto traducido que se muestra. */
class Filter(
    val name: String,
    enabled: Boolean = false,
    @DrawableRes val icon: Int? = null,
    @StringRes val labelRes: Int? = null,
) {
    val enabled = mutableStateOf(enabled)
}

val filters = listOf(
    Filter(name = "Organic", labelRes = R.string.filter_organic),
    Filter(name = "Gluten-free", labelRes = R.string.filter_gluten_free),
    Filter(name = "Dairy-free", labelRes = R.string.filter_dairy_free),
    Filter(name = "Sweet", labelRes = R.string.filter_sweet),
    Filter(name = "Savory", labelRes = R.string.filter_savory),
)
val priceFilters = listOf(
    Filter(name = "$"),
    Filter(name = "$$"),
    Filter(name = "$$$"),
    Filter(name = "$$$$"),
)
val sortFilters = listOf(
    Filter(name = "Android's favorite (default)", icon = R.drawable.ic_android, labelRes = R.string.android_favorites),
    Filter(name = "Rating", icon = R.drawable.ic_star, labelRes = R.string.rating),
    Filter(name = "Alphabetical", icon = R.drawable.ic_sort_by_alpha, labelRes = R.string.alphabetical),
)

val categoryFilters = listOf(
    Filter(name = "Chips & crackers", labelRes = R.string.category_chips),
    Filter(name = "Fruit snacks", labelRes = R.string.category_fruit),
    Filter(name = "Desserts", labelRes = R.string.category_desserts),
    Filter(name = "Nuts", labelRes = R.string.category_nuts),
)
val lifeStyleFilters = listOf(
    Filter(name = "Organic", labelRes = R.string.filter_organic),
    Filter(name = "Gluten-free", labelRes = R.string.filter_gluten_free),
    Filter(name = "Dairy-free", labelRes = R.string.filter_dairy_free),
    Filter(name = "Sweet", labelRes = R.string.filter_sweet),
    Filter(name = "Savory", labelRes = R.string.filter_savory),
)

var sortDefault = sortFilters.get(0).name
