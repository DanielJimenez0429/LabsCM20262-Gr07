# Laboratorio 2 - Workers (Grupo 07, 2026-2)

Proyecto basado en **Jetsnack** (android/compose-samples).
Paquete: `co.edu.udea.compumovil.gr07_20262.lab2` · Módulo: `Lab2-Services`

## Qué se implementó

| Funcionalidad | Archivos |
| --- | --- | --- | --- |
| Checkout del carrito con worker | `work/CheckoutWorker.kt`, `CartViewModel.kt`, `Cart.kt` |

## Cómo funciona el Checkout
1. El usuario pulsa *Pagar* en el carrito (`Cart.kt`).
2. `CartViewModel.checkout()` guarda los datos del pedido y encola un `OneTimeWorkRequest`.
3. `CheckoutWorker` (un `CoroutineWorker`) procesa el pedido en segundo plano.
4. Al terminar muestra una notificación.

## Archivos nuevos
- `work/CheckoutWorker.kt`: worker del checkout.

## Archivos modificados
- `ui/home/cart/Cart.kt`: el botón Checkout ahora llama a `onCheckout`.
- `ui/home/cart/CartViewModel.kt`: nueva función `checkout()`.
- `ui/MainActivity.kt`: pide el permiso de notificaciones.
- `AndroidManifest.xml`: permiso `POST_NOTIFICATIONS`.
- `build.gradle.kts`: dependencia `work-runtime-ktx`.
- `strings.xml` (en/es): textos nuevos.

## Cómo probarlo
1. Ejecutar la app y aceptar el permiso de notificaciones.
2. Agregar un snack al carrito y pulsar *Pagar*.
3. A los 5 segundos llega la notificación "Pedido confirmado".