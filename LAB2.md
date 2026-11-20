# Laboratorio 2 - Workers y consumo de servicios REST (Grupo 07, 2026-2)

Proyecto basado en **Jetsnack** (`android/compose-samples`), al que se le completaron funcionalidades que venían sin terminar usando **WorkManager (CoroutineWorker)** y el consumo de un **servicio REST (mockapi.io) con Retrofit**.

---

| Criterio | Qué se hizo | Dónde |
| --- | --- | --- | --- |
| Uso de workers y/o corutine-workers| Dos `CoroutineWorker` con WorkManager: `CheckoutWorker` (envía el pedido al servidor, con reintentos) y `SnackSyncWorker` (tarea **recurrente** que sincroniza los datos del catálogo cada 15 minutos). | `work/` |
| Implementación de al menos 2 funcionalidades del ejemplo | **4 funcionalidades** de Jetsnack que estaban incompletas: agregar al carrito, checkout, historial de pedidos en el Perfil y detalle del snack con datos del servicio REST. | Ver sección *Funcionalidades implementadas* |

**Objetivos específicos del laboratorio**

| Objetivo | Cómo se cumple |
| --- | --- |
| Usar el módulo de Navigation de Compose | Jetsnack ya navega con Navigation Compose (inicio, búsqueda, carrito, perfil y detalle del snack). |
| Usar librerías para consumo de servicios web | **Retrofit 3.0.0** con **Gson** (`converter-gson`). |
| Uso de workers | **WorkManager** (`work-runtime-ktx` 2.10.1) con `CoroutineWorker`. |

---

## Funcionalidades implementadas

| # | Funcionalidad | Cómo estaba en Jetsnack | Qué se hizo | Archivos principales |
| --- | --- | --- | --- | --- |
| 1 | **Agregar al carrito** | El botón *ADD TO CART* tenía un `/* todo */` y el carrito mostraba productos fijos de ejemplo. | El carrito ahora es compartido entre el detalle y la pantalla del carrito. Agrega el snack con la cantidad elegida y suma si ya existía. Muestra un mensaje de confirmación. | `data/CartStore.kt`, `ui/snackdetail/SnackDetail.kt`, `ui/home/cart/CartViewModel.kt` |
| 2 | **Checkout con worker** | El botón *Checkout* tenía un `/* todo */`. | Crea el pedido como *Enviando* y encola el `CheckoutWorker`, que lo envía al servicio REST en segundo plano. Al confirmarse lanza una notificación. | `work/CheckoutWorker.kt`, `work/WorkScheduler.kt`, `ui/home/cart/Cart.kt`, `ui/home/cart/CartViewModel.kt` |
| 3 | **Historial de pedidos en Perfil** | La pantalla decía "work in progress". | Lista los pedidos con fecha, productos, total y estado (*Enviando*, *Confirmado* o *Falló*), y muestra la última sincronización del catálogo con un botón *Actualizar ahora*. | `ui/home/Profile.kt`, `data/OrderRepository.kt` |
| 4 | **Detalle del snack con datos del servicio REST** | Descripción e ingredientes eran texto fijo. | Calorías, valoración, descripción e ingredientes vienen de mockapi.io. Muestra estado de carga, de error y un texto alterno si el snack no existe en el servidor. | `ui/snackdetail/SnackDetail.kt`, `data/SnackInfoRepository.kt`, `data/remote/MockApi.kt` |

---

## Workers (WorkManager)

| Worker | Tipo | Restricciones | Reintentos | Qué hace |
| --- | --- | --- | --- | --- |
| `CheckoutWorker` | Trabajo único (`OneTimeWorkRequest`) | Requiere conexión a internet | Backoff exponencial desde 10 s, hasta 5 intentos | Lee el pedido guardado, lo envía con `POST /orders`, actualiza su estado a *Confirmado* (o *Falló* si no se puede) y muestra una notificación. |
| `SnackSyncWorker` | Trabajo **periódico** cada 15 min (el mínimo de WorkManager) y también manual | Requiere conexión a internet | Sin red reintenta; ante otros errores reintenta hasta 3 veces | Descarga `GET /snacks` y guarda una copia local para que el detalle funcione también sin conexión. |

- Si el usuario paga **sin internet**, el pedido queda en *Enviando* y WorkManager lo envía automáticamente cuando vuelve la conexión.
- El worker periódico se programa al abrir la app (`MainActivity`) con política `KEEP`, para no duplicarlo.
- Toda la programación está centralizada en `work/WorkScheduler.kt`.

### Flujo del checkout

```
Pagar (Cart.kt)
   └─> CartViewModel.checkout()
         ├─ OrderRepository.create()  -> pedido en estado "Enviando"
         ├─ WorkScheduler.enqueueCheckout()
         └─ vacía el carrito y muestra mensaje
                    │
                    ▼
        CheckoutWorker (en segundo plano, con red)
         ├─ POST /orders  (Retrofit)
         ├─ OrderRepository.update()  -> "Confirmado" + referencia del servidor
         └─ Notificación "Pedido confirmado"
                    │
                    ▼
        Perfil muestra el pedido y su estado
```

---

## Servicio REST (mockapi.io)

- **URL base:** `https://6aff8fd47fc363ccff0941b8.mockapi.io/`
- Se usa **Retrofit** con **Gson**. Los modelos y la interfaz están en `data/remote/MockApi.kt`.

| Recurso | Método | Uso | Campos |
| --- | --- | --- | --- |
| `snacks` | `GET /snacks` | Complementa los datos de cada snack | `name`, `description`, `ingredients`, `calories`, `rating` |
| `orders` | `POST /orders` | Guarda el pedido del checkout | `items`, `itemCount`, `total`, `status` |

Los 28 snacks de Jetsnack están cargados en el recurso `snacks` y se asocian por **nombre** (por ejemplo, `Cupcake`, `Donut`, `Oreo`).

---

## Archivos nuevos

Dentro de `Lab2-Services/src/main/java/co/edu/udea/compumovil/gr07_20262/lab2/`:

- `data/remote/MockApi.kt`: modelos, interfaz Retrofit y cliente (`ApiClient`).
- `data/SnackInfoRepository.kt`: descarga y guarda localmente los datos de los snacks.
- `data/OrderRepository.kt`: historial de pedidos y su estado.
- `data/CartStore.kt`: carrito compartido entre pantallas.
- `work/CheckoutWorker.kt`: worker del checkout.
- `work/SnackSyncWorker.kt`: worker periódico de sincronización.
- `work/WorkScheduler.kt`: programación de los workers.

## Archivos modificados

- `ui/home/cart/Cart.kt`: el botón *Pagar* llama a `onCheckout`.
- `ui/home/cart/CartViewModel.kt`: función `checkout()` y carrito compartido.
- `ui/home/Profile.kt`: historial de pedidos y estado de la sincronización.
- `ui/snackdetail/SnackDetail.kt`: botón *ADD TO CART* y datos del servicio REST.
- `ui/MainActivity.kt`: permiso de notificaciones y worker periódico.
- `AndroidManifest.xml`: permiso `POST_NOTIFICATIONS`.
- `build.gradle.kts`: dependencias Retrofit, Gson y WorkManager.
- `proguard-rules.pro`: reglas para que el APK release lea bien el JSON.
- `res/values/strings.xml` y `res/values-es/strings.xml`: textos en inglés y español.

---

## Idiomas

La aplicación está en **inglés** (`values/`) y **español** (`values-es/`) y cambia según el idioma del dispositivo. Los textos que llegan del servicio REST están en español.

---

## Cómo probarlo

1. Abre la app y acepta el permiso de notificaciones.
2. Abre un snack: se cargan las calorías, la valoración, la descripción y los ingredientes desde el servidor.
3. Cambia la cantidad y pulsa *ADD TO CART*. Aparece un mensaje de confirmación.
4. Abre el carrito (puedes agregar varios snacks) y pulsa *Pagar*.
5. Entra a *Perfil*: el pedido aparece como *Enviando* y luego pasa a *Confirmado*. Llega la notificación "Pedido confirmado", y el registro aparece en el recurso `orders` de mockapi.
6. **Prueba sin internet:** activa el modo avión, paga y revisa *Perfil*. El pedido queda en *Enviando* y se confirma solo al volver la conexión.
7. En *Perfil*, pulsa *Actualizar ahora* para ejecutar el worker de sincronización y ver cambiar la fecha.
8. Cambia el idioma del dispositivo a español o inglés y revisa que toda la interfaz cambie.

---