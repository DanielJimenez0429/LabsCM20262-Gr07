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
import kotlin.random.Random

@Immutable
data class SnackCollection(
    val id: Long,
    val name: String,
    val snacks: List<Snack>,
    val type: CollectionType = CollectionType.Normal,
    @StringRes val nameRes: Int? = null,
)

enum class CollectionType { Normal, Highlight }

/**
 * A fake repo
 */
object SnackRepo {
    fun getSnacks(): List<SnackCollection> = snackCollections
    fun getSnack(snackId: Long) = snacks.find { it.id == snackId }!!
    fun getRelated(@Suppress("UNUSED_PARAMETER") snackId: Long) = related
    fun getInspiredByCart() = inspiredByCart
    fun getFilters() = filters
    fun getPriceFilters() = priceFilters
    fun getCart() = cart
    fun getSortFilters() = sortFilters
    fun getCategoryFilters() = categoryFilters
    fun getSortDefault() = sortDefault
    fun getLifeStyleFilters() = lifeStyleFilters
}

/**
 * Static data
 */

private val tastyTreats = SnackCollection(
    id = 1L,
    name = "Android's picks",
    nameRes = R.string.collection_android_picks,
    type = CollectionType.Highlight,
    snacks = snacks.subList(0, 13),
)

private val popular = SnackCollection(
    id = Random.nextLong(),
    name = "Popular on Jetsnack",
    nameRes = R.string.collection_popular,
    snacks = snacks.subList(14, 19),
)

private val wfhFavs = tastyTreats.copy(
    id = Random.nextLong(),
    name = "WFH favourites",
    nameRes = R.string.collection_wfh,
)

private val newlyAdded = popular.copy(
    id = Random.nextLong(),
    name = "Newly Added",
    nameRes = R.string.collection_newly_added,
)

private val exclusive = tastyTreats.copy(
    id = Random.nextLong(),
    name = "Only on Jetsnack",
    nameRes = R.string.collection_exclusive,
)

private val also = tastyTreats.copy(
    id = Random.nextLong(),
    name = "Customers also bought",
    nameRes = R.string.collection_also_bought,
)

private val inspiredByCart = tastyTreats.copy(
    id = Random.nextLong(),
    name = "Inspired by your cart",
    nameRes = R.string.collection_inspired_by_cart,
)

private val snackCollections = listOf(
    tastyTreats,
    popular,
    wfhFavs,
    newlyAdded,
    exclusive,
)

private val related = listOf(
    also.copy(id = Random.nextLong()),
    popular.copy(id = Random.nextLong()),
)

private val cart = listOf(
    OrderLine(snacks[4], 2),
    OrderLine(snacks[6], 3),
    OrderLine(snacks[8], 1),
)

@Immutable
data class OrderLine(val snack: Snack, val count: Int)
