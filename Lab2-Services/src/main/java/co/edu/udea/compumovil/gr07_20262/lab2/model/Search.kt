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

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import co.edu.udea.compumovil.gr07_20262.lab2.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * A fake repo for searching.
 */
object SearchRepo {
    fun getCategories(): List<SearchCategoryCollection> = searchCategoryCollections
    fun getSuggestions(): List<SearchSuggestionGroup> = searchSuggestions

    suspend fun search(query: String): List<Snack> = withContext(Dispatchers.Default) {
        delay(200L) // simulate an I/O delay
        snacks.filter { it.name.contains(query, ignoreCase = true) }
    }
}

@Immutable
data class SearchCategoryCollection(
    val id: Long,
    val name: String,
    val categories: List<SearchCategory>,
    @StringRes val nameRes: Int? = null,
)

@Immutable
data class SearchCategory(val name: String, val imageRes: Int, @StringRes val nameRes: Int? = null)

@Immutable
data class SearchSuggestionGroup(
    val id: Long,
    val name: String,
    val suggestions: List<String>,
    @StringRes val nameRes: Int? = null,
)

/**
 * Static data
 */

private val searchCategoryCollections = listOf(
    SearchCategoryCollection(
        id = 0L,
        name = "Categories",
        nameRes = R.string.search_categories,
        categories = listOf(
            SearchCategory(
                name = "Chips & crackers",
                nameRes = R.string.category_chips,
                imageRes = R.drawable.chips,
            ),
            SearchCategory(
                name = "Fruit snacks",
                nameRes = R.string.category_fruit,
                imageRes = R.drawable.fruit,
            ),
            SearchCategory(
                name = "Desserts",
                nameRes = R.string.category_desserts,
                imageRes = R.drawable.desserts,
            ),
            SearchCategory(
                name = "Nuts",
                nameRes = R.string.category_nuts,
                imageRes = R.drawable.nuts,
            ),
        ),
    ),
    SearchCategoryCollection(
        id = 1L,
        name = "Lifestyles",
        nameRes = R.string.search_lifestyles,
        categories = listOf(
            SearchCategory(
                name = "Organic",
                nameRes = R.string.filter_organic,
                imageRes = R.drawable.organic,
            ),
            SearchCategory(
                name = "Gluten Free",
                nameRes = R.string.filter_gluten_free,
                imageRes = R.drawable.gluten_free,
            ),
            SearchCategory(
                name = "Paleo",
                nameRes = R.string.lifestyle_paleo,
                imageRes = R.drawable.paleo,
            ),
            SearchCategory(
                name = "Vegan",
                nameRes = R.string.lifestyle_vegan,
                imageRes = R.drawable.vegan,
            ),
            SearchCategory(
                name = "Vegetarian",
                nameRes = R.string.lifestyle_vegetarian,
                imageRes = R.drawable.organic,
            ),
            SearchCategory(
                name = "Whole30",
                nameRes = R.string.lifestyle_whole30,
                imageRes = R.drawable.paleo,
            ),
        ),
    ),
)

private val searchSuggestions = listOf(
    SearchSuggestionGroup(
        id = 0L,
        name = "Recent searches",
        nameRes = R.string.search_recent,
        suggestions = listOf(
            "Cheese",
            "Apple Sauce",
        ),
    ),
    SearchSuggestionGroup(
        id = 1L,
        name = "Popular searches",
        nameRes = R.string.search_popular,
        suggestions = listOf(
            "Organic",
            "Gluten Free",
            "Paleo",
            "Vegan",
            "Vegitarian",
            "Whole30",
        ),
    ),
)
