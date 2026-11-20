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

package co.edu.udea.compumovil.gr07_20262.lab2.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import co.edu.udea.compumovil.gr07_20262.lab2.R
import co.edu.udea.compumovil.gr07_20262.lab2.data.Order
import co.edu.udea.compumovil.gr07_20262.lab2.data.OrderRepository
import co.edu.udea.compumovil.gr07_20262.lab2.data.OrderStatus
import co.edu.udea.compumovil.gr07_20262.lab2.data.SnackInfoRepository
import co.edu.udea.compumovil.gr07_20262.lab2.ui.components.JetsnackButton
import co.edu.udea.compumovil.gr07_20262.lab2.ui.components.JetsnackDivider
import co.edu.udea.compumovil.gr07_20262.lab2.ui.components.JetsnackSurface
import co.edu.udea.compumovil.gr07_20262.lab2.ui.theme.JetsnackTheme
import co.edu.udea.compumovil.gr07_20262.lab2.ui.utils.formatPrice
import co.edu.udea.compumovil.gr07_20262.lab2.work.WorkScheduler
import java.text.DateFormat
import java.util.Date

/** Perfil: historial de pedidos (los envía el CheckoutWorker) y estado de la sincronización con el servidor. */
@Composable
fun Profile(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val orderRepo = remember { OrderRepository.get(context) }
    val snackRepo = remember { SnackInfoRepository.get(context) }
    val orders by orderRepo.orders.collectAsStateWithLifecycle()
    val lastSync by snackRepo.lastSync.collectAsStateWithLifecycle()
    val syncState by remember { WorkScheduler.snackSyncState(context) }
        .collectAsStateWithLifecycle(initialValue = null)
    val syncing = syncState == WorkInfo.State.RUNNING || syncState == WorkInfo.State.ENQUEUED

    ProfileContent(
        orders = orders,
        lastSync = lastSync,
        syncing = syncing,
        onSyncNow = { WorkScheduler.syncSnacksNow(context) },
        modifier = modifier,
    )
}

@Composable
private fun ProfileContent(
    orders: List<Order>,
    lastSync: Long,
    syncing: Boolean,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    JetsnackSurface(modifier = modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(horizontal = 24.dp)) {
            item {
                Column {
                    Spacer(
                        Modifier.windowInsetsTopHeight(
                            WindowInsets.statusBars.add(WindowInsets(top = 24.dp)),
                        ),
                    )
                    Text(
                        text = stringResource(R.string.catalog_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = JetsnackTheme.colors.brand,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (lastSync == 0L) {
                                stringResource(R.string.catalog_never)
                            } else {
                                stringResource(R.string.catalog_last_sync, formatDate(lastSync))
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = JetsnackTheme.colors.textSecondary,
                            modifier = Modifier.weight(1f),
                        )
                        JetsnackButton(onClick = onSyncNow, enabled = !syncing) {
                            Text(
                                text = stringResource(
                                    if (syncing) R.string.catalog_syncing else R.string.catalog_sync_now,
                                ),
                                maxLines = 1,
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    JetsnackDivider()
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.orders_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = JetsnackTheme.colors.brand,
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
            if (orders.isEmpty()) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                    ) {
                        Image(
                            painterResource(R.drawable.empty_state_search),
                            contentDescription = null,
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.orders_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = JetsnackTheme.colors.textSecondary,
                        )
                    }
                }
            } else {
                items(orders, key = { it.id }) { order -> OrderRow(order) }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun OrderRow(order: Order) {
    val (statusText, statusColor) = when (order.status) {
        OrderStatus.PENDING ->
            stringResource(R.string.order_status_pending) to JetsnackTheme.colors.textHelp
        OrderStatus.CONFIRMED ->
            stringResource(R.string.order_status_confirmed) to JetsnackTheme.colors.brand
        OrderStatus.FAILED ->
            stringResource(R.string.order_status_failed) to JetsnackTheme.colors.error
    }
    Column(Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatDate(order.createdAt),
                style = MaterialTheme.typography.titleSmall,
                color = JetsnackTheme.colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelLarge,
                color = statusColor,
            )
        }
        Text(
            text = order.items,
            style = MaterialTheme.typography.bodyMedium,
            color = JetsnackTheme.colors.textHelp,
            modifier = Modifier.padding(top = 4.dp),
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        ) {
            Text(
                text = if (order.status == OrderStatus.CONFIRMED) {
                    stringResource(R.string.order_ref, order.remoteId)
                } else {
                    ""
                },
                style = MaterialTheme.typography.labelSmall,
                color = JetsnackTheme.colors.textHelp,
            )
            Text(
                text = formatPrice(order.total),
                style = MaterialTheme.typography.titleSmall,
                color = JetsnackTheme.colors.textPrimary,
            )
        }
        Spacer(Modifier.height(12.dp))
        JetsnackDivider()
    }
}

private fun formatDate(millis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(millis))

@Preview("default")
@Composable
fun ProfilePreview() {
    JetsnackTheme {
        ProfileContent(
            orders = listOf(
                Order(
                    id = 1,
                    createdAt = 1_700_000_000_000,
                    items = "Donut x2, Pie x1",
                    itemCount = 3,
                    total = 897,
                    status = OrderStatus.CONFIRMED,
                    remoteId = "12",
                ),
            ),
            lastSync = 1_700_000_000_000,
            syncing = false,
            onSyncNow = {},
        )
    }
}