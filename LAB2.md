# Laboratorio 2: Workers y consumo de servicios REST

**Computación Móvil · Universidad de Antioquia · Grupo 07 · 2026-2**

Aplicación Android construida sobre **Jetsnack** (`android/compose-samples`). Se completaron funcionalidades que el ejemplo original dejaba sin terminar. Para eso se usan **WorkManager** (`CoroutineWorker`) y un **servicio REST** (mockapi.io) consumido con **Retrofit**.

| | |
| --- | --- |
| **Repositorio** | https://github.com/DanielJimenez0429/LabsCM20262-Gr07 |
| **Módulo** | `Lab2-Services` |
| **Paquete** | `co.edu.udea.compumovil.gr07_20262.lab2` |
| **Idiomas** | Inglés y español |

---

## Integrantes

| Nombre completo | Usuario de GitHub |
| --- | --- |
| Daniel Jimenez | [@DanielJimenez0429](https://github.com/DanielJimenez0429) |
| Sebastian Berrio | [@BerrioS-Eng](https://github.com/BerrioS-Eng) |
| Brayan Jaraba | [@JARABRAMA](https://github.com/JARABRAMA) |

---

## Contenido

1. [Cumplimiento de la evaluación](#cumplimiento-de-la-evaluación)
2. [Tecnologías](#tecnologías)
3. [Funcionalidades implementadas](#funcionalidades-implementadas)
4. [Workers (WorkManager)](#workers-workmanager)
5. [Servicio REST (mockapi.io)](#servicio-rest-mockapiio)
6. [Internacionalización](#internacionalización)
7. [Estructura del código](#estructura-del-código)
8. [Compilación y APK firmado](#compilación-y-apk-firmado)
9. [Cómo probarlo](#cómo-probarlo)

---

## Cumplimiento de la evaluación

| Criterio | Peso | Cómo se cumple |
| --- | :---: | --- |
| Uso de workers y/o coroutine workers | 30 % | Dos `CoroutineWorker`. `SnackSyncWorker` es una tarea **recurrente** (`PeriodicWorkRequest` cada 15 min con `enqueueUniquePeriodicWork`) y `CheckoutWorker` envía el pedido con reintentos. |
| Al menos 2 funcionalidades de Jetsnack | 40 % | **4 funcionalidades**: agregar al carrito, checkout, historial de pedidos en Perfil y detalle del snack con datos remotos. |
| Consumo de un servicio web REST | 20 % | **Retrofit + Gson** contra mockapi.io (`GET /snacks` y `POST /orders`), llamado desde los workers. |
| UI y navegación | 10 % | Jetpack Compose, Material 3 y **Navigation Compose** (inicio, búsqueda, carrito, perfil y detalle). |

---

## Tecnologías

| Uso | Librería | Versión |
| --- | --- | --- |
| UI | Jetpack Compose (BOM) + Material 3 | 2026.09.00 |
| Navegación | `androidx.navigation:navigation-compose` | 2.10.1 |
| Tareas en segundo plano | `androidx.work:work-runtime-ktx` | 2.10.1 |
| Cliente HTTP | `com.squareup.retrofit2:retrofit` | 3.0.0 |
| Conversión JSON | `com.squareup.retrofit2:converter-gson` | 3.0.0 |
| Imágenes | Coil (`coil-compose`) | 2.7.0 |

---

## Funcionalidades implementadas

| # | Funcionalidad | Cómo estaba en Jetsnack | Qué se hizo |
| :---: | --- | --- | --- |
| 1 | **Agregar al carrito** | El botón *ADD TO CART* tenía un `/* todo */` y el carrito mostraba productos fijos. | El carrito es compartido entre el detalle y la pantalla del carrito. Agrega el snack con la cantidad elegida, suma si ya existía y muestra un mensaje de confirmación. |
| 2 | **Checkout con worker** | El botón *Checkout* tenía un `/* todo */`. | Crea el pedido en estado *Enviando* y encola el `CheckoutWorker`, que lo envía al servicio REST en segundo plano. Al confirmarse lanza una notificación. |
| 3 | **Historial de pedidos en Perfil** | La pantalla mostraba "work in progress". | Lista los pedidos con fecha, productos, total y estado (*Enviando*, *Confirmado* o *Falló*). También muestra la última sincronización del catálogo y un botón *Actualizar ahora*. |
| 4 | **Detalle del snack con datos REST** | La descripción y los ingredientes eran texto fijo. | Las calorías, la valoración, la descripción y los ingredientes vienen de mockapi.io. Hay estados de carga, de error y de "no encontrado". |

---

## Workers (WorkManager)

| Worker | Tipo | Restricciones | Reintentos | Qué hace |
| --- | --- | --- | --- | --- |
| `SnackSyncWorker` | **Periódico** cada 15 min (el mínimo de WorkManager) y también manual | Requiere red | Sin red reintenta; ante otros errores, hasta 3 intentos | Descarga `GET /snacks` y guarda una copia local para que el detalle funcione sin conexión. |
| `CheckoutWorker` | Único (`OneTimeWorkRequest`) | Requiere red | Backoff exponencial desde 10 s, hasta 5 intentos | Envía el pedido con `POST /orders`, lo marca como *Confirmado* (o *Falló*) y muestra una notificación. |

- El worker periódico se programa al abrir la app (`MainActivity`) con la política `KEEP`, así que no se duplica.
- Si el usuario paga **sin internet**, el pedido queda en *Enviando* y WorkManager lo envía solo cuando vuelve la conexión.
- Toda la programación de los workers está en `work/WorkScheduler.kt`.

### Flujo del checkout

```
Pagar (Cart.kt)
   └─> CartViewModel.checkout()
         ├─ OrderRepository.create()      -> pedido en estado "Enviando"
         ├─ WorkScheduler.enqueueCheckout()
         └─ vacía el carrito y muestra un mensaje
                    │
                    ▼
        CheckoutWorker (en segundo plano, con red)
         ├─ POST /orders  (Retrofit)
         ├─ OrderRepository.update()      -> "Confirmado" + referencia del servidor
         └─ Notificación "Pedido confirmado"
                    │
                    ▼
        Perfil muestra el pedido y su estado
```

---

## Servicio REST (mockapi.io)

- **URL base:** `https://6aff8fd47fc363ccff0941b8.mockapi.io/`
- Los modelos (DTO), la interfaz Retrofit y el cliente (`ApiClient`) están en `data/remote/MockApi.kt`.

| Recurso | Método | Uso | Campos |
| --- | --- | --- | --- |
| `snacks` | `GET /snacks` | Complementa los datos de cada snack | `name`, `description`, `ingredients`, `calories`, `rating` |
| `orders` | `POST /orders` | Guarda el pedido del checkout | `items`, `itemCount`, `total`, `status` |

Los 28 snacks de Jetsnack están cargados en el recurso `snacks` y se asocian por **nombre** (por ejemplo `Cupcake`, `Donut` u `Oreo`).

---

## Internacionalización

- La app tiene textos en **inglés** (`res/values/strings.xml`) y **español** (`res/values-es/strings.xml`), con las mismas claves en ambos archivos. El idioma cambia según la configuración del dispositivo.
- Los datos de ejemplo de Jetsnack también están traducidos: colecciones del inicio, filtros, categorías, encabezados de búsqueda y dirección de entrega. Cada modelo (`Filter`, `SnackCollection`, `SearchCategory`, `SearchCategoryCollection`, `SearchSuggestionGroup`) tiene un `@StringRes` que la UI muestra con `stringResource`. El campo `name` se conserva como clave interna.
- Los textos que llegan del servicio REST están en español.

---

## Estructura del código

Todas las rutas son relativas a `Lab2-Services/src/main/java/co/edu/udea/compumovil/gr07_20262/lab2/`.

### Archivos nuevos

| Archivo | Responsabilidad |
| --- | --- |
| `data/remote/MockApi.kt` | DTO, interfaz Retrofit y cliente `ApiClient` |
| `data/SnackInfoRepository.kt` | Descarga y guarda localmente los datos de los snacks |
| `data/OrderRepository.kt` | Historial de pedidos y su estado |
| `data/CartStore.kt` | Carrito compartido entre pantallas |
| `work/SnackSyncWorker.kt` | Worker periódico de sincronización |
| `work/CheckoutWorker.kt` | Worker del checkout |
| `work/WorkScheduler.kt` | Programación de los workers |

### Archivos modificados

| Archivo | Cambio |
| --- | --- |
| `ui/MainActivity.kt` | Permiso de notificaciones y programación del worker periódico |
| `ui/home/cart/Cart.kt`, `CartViewModel.kt` | Checkout y carrito compartido |
| `ui/home/Profile.kt` | Historial de pedidos y estado de la sincronización |
| `ui/snackdetail/SnackDetail.kt` | Botón *ADD TO CART* y datos del servicio REST |
| `model/*.kt`, `ui/components/*`, `ui/home/search/*`, `ui/home/DestinationBar.kt`, `ui/home/FilterScreen.kt` | Textos traducibles (`@StringRes`) |
| `AndroidManifest.xml` | Permiso `POST_NOTIFICATIONS` |
| `Lab2-Services/build.gradle.kts` | Dependencias de Retrofit, Gson y WorkManager, y firma del APK |
| `proguard-rules.pro` | Reglas para que el APK release lea bien el JSON |
| `res/values/strings.xml`, `res/values-es/strings.xml` | Textos en inglés y español |

---

## Compilación y APK firmado

```bash
./gradlew :Lab2-Services:assembleDebug     # APK de depuración
./gradlew :Lab2-Services:assembleRelease   # APK firmado (release)
```

**APK firmado entregado:** [`Lab2-Services/release/Lab2-Services-release.apk`](Lab2-Services/release/Lab2-Services-release.apk), firmado con el keystore del grupo (esquema APK Signature v2).

- Certificado: `CN=Grupo 07 CM 2026-2, OU=Computacion Movil, O=Universidad de Antioquia, L=Medellin, C=CO`
- SHA-256: `99:1B:D2:EF:39:29:35:A5:EE:F4:39:0D:85:54:A1:6D:95:47:11:D3:3D:A2:29:71:1F:C3:55:42:A2:53:6B:EE`

Al compilar, el APK release queda en `Lab2-Services/build/outputs/apk/release/` y se firma con `signingConfigs.release`:

1. Copia `keystore.properties.example` como `keystore.properties` en la raíz y completa la ruta y las credenciales del keystore. Ese archivo y los `*.jks` están en `.gitignore`.
2. Si no existe `keystore.properties`, el APK se firma con el keystore debug del usuario (`~/.android/debug.keystore`).

---

## Cómo probarlo

1. Abre la app y acepta el permiso de notificaciones.
2. Abre un snack: las calorías, la valoración, la descripción y los ingredientes se cargan desde el servidor.
3. Cambia la cantidad y pulsa *ADD TO CART*. Aparece un mensaje de confirmación.
4. Abre el carrito (puedes agregar varios snacks) y pulsa *Pagar*.
5. Entra a *Perfil*: el pedido aparece como *Enviando* y luego pasa a *Confirmado*. Llega la notificación "Pedido confirmado" y el registro aparece en el recurso `orders` de mockapi.
6. **Sin internet:** activa el modo avión, paga y revisa *Perfil*. El pedido queda en *Enviando* y se confirma solo al volver la conexión.
7. En *Perfil*, pulsa *Actualizar ahora* para ejecutar el worker de sincronización y ver cómo cambia la fecha.
8. Cambia el idioma del dispositivo entre español e inglés y verifica que toda la interfaz cambie.
