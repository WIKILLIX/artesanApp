# ArtesanApp - Documentación Técnica

## Tabla de Contenidos

1. [Descripción General](#descripción-general)
2. [Arquitectura del Proyecto](#arquitectura-del-proyecto)
3. [Base de Datos](#base-de-datos)
4. [Componentes Principales](#componentes-principales)
5. [Autenticación y Autorización](#autenticación-y-autorización)
6. [Gestión de Usuarios](#gestión-de-usuarios)
7. [Gestión de Productos](#gestión-de-productos)
8. [Carrito de Compras](#carrito-de-compras)
9. [Integración de Mapas](#integración-de-mapas)
10. [Flujos de Trabajo](#flujos-de-trabajo)
11. [Configuración y Construcción](#configuración-y-construcción)

---

## Descripción General

**ArtesanApp** es una aplicación Android nativa desarrollada en Kotlin que proporciona una plataforma de marketplace para productos artesanales. La aplicación implementa un sistema completo de gestión con roles de usuario (Admin y User), carrito de compras, gestión de productos con imágenes, y visualización de ubicación de tienda mediante Google Maps.

### Información Técnica

- **Lenguaje**: Kotlin
- **SDK Mínimo**: Android 12 (API 31)
- **SDK Target**: Android 14 (API 36)
- **SDK Compilación**: 36
- **Versión de Java**: 11
- **Package**: `com.artesan.artesanapp`

### Características Principales

- ✅ Sistema de autenticación con roles (Admin/User)
- ✅ CRUD completo de productos (solo Admin)
- ✅ CRUD completo de usuarios (solo Admin)
- ✅ Carrito de compras con gestión de cantidades
- ✅ Carga y compresión de imágenes
- ✅ Integración con Google Maps
- ✅ Persistencia de datos con SQLite
- ✅ Búsqueda en tiempo real de usuarios
- ✅ Interfaz Material Design 3

---

## Arquitectura del Proyecto

### Patrón de Arquitectura

El proyecto sigue una arquitectura de capas con separación de responsabilidades:

```
┌─────────────────────────────────────┐
│         Activities/Fragments        │  ← Capa de Presentación
├─────────────────────────────────────┤
│       Services/Repositories         │  ← Capa de Lógica de Negocio
├─────────────────────────────────────┤
│        Storage Manager              │  ← Capa de Abstracción de Datos
├─────────────────────────────────────┤
│            DAOs                      │  ← Capa de Acceso a Datos
├─────────────────────────────────────┤
│        SQLite Database              │  ← Capa de Persistencia
└─────────────────────────────────────┘
```

### Estructura de Paquetes

```
com.artesan.artesanapp/
├── activities/           # Actividades principales
│   ├── WelcomeActivity
│   ├── LoginActivity
│   ├── RegisterActivity
│   └── HomeActivity
├── fragments/           # Fragmentos de la aplicación
│   ├── ProductListFragment
│   ├── CartFragment
│   ├── CreateProductFragment
│   ├── ProductDetailFragment
│   ├── MapFragment
│   ├── UserManagementFragment
│   ├── CreateUserFragment
│   └── EditUserFragment
├── models/             # Modelos de datos
│   ├── User
│   ├── Product
│   └── CartItem
├── adapters/           # Adaptadores de RecyclerView
│   ├── ProductAdapter
│   ├── CartAdapter
│   └── UserAdapter
├── services/           # Servicios de lógica de negocio
│   ├── AuthService
│   └── CartService
├── repositories/       # Repositorios de datos
│   └── ProductRepository
├── database/          # Capa de base de datos
│   ├── DatabaseHelper
│   ├── UserDao
│   ├── ProductDao
│   ├── CartDao
│   └── SessionDao
├── storage/           # Gestión de almacenamiento
│   └── StorageManager
└── utils/             # Utilidades
    └── ImageHelper
```

---

## Base de Datos

### Esquema de Base de Datos

La aplicación utiliza SQLite con las siguientes tablas:

#### Tabla: `users`

```sql
CREATE TABLE users (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role TEXT NOT NULL
)
```

**Campos:**
- `id`: UUID único del usuario
- `name`: Nombre completo del usuario
- `email`: Correo electrónico (único)
- `username`: Nombre de usuario (único, generado automáticamente desde email)
- `password`: Contraseña en texto plano (⚠️ no recomendado para producción)
- `role`: Rol del usuario (ADMIN o USER)

#### Tabla: `products`

```sql
CREATE TABLE products (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    price REAL NOT NULL,
    stock INTEGER NOT NULL,
    image_base64 TEXT
)
```

**Campos:**
- `id`: UUID único del producto
- `name`: Nombre del producto
- `description`: Descripción del producto
- `price`: Precio del producto
- `stock`: Cantidad en inventario
- `image_base64`: Imagen codificada en Base64 (max 800px, 80% JPEG)

#### Tabla: `cart`

```sql
CREATE TABLE cart (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE
)
```

**Campos:**
- `id`: ID autoincremental
- `product_id`: Referencia al producto
- `quantity`: Cantidad de productos en el carrito

#### Tabla: `session`

```sql
CREATE TABLE session (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    user_id TEXT,
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
)
```

**Campos:**
- `id`: Siempre 1 (solo una sesión activa)
- `user_id`: ID del usuario actualmente logueado

### Versiones de Base de Datos

- **Versión 1**: Esquema inicial sin campos name y email en users
- **Versión 2**: Agregados campos name y email a la tabla users

---

## Componentes Principales

### Activities

#### WelcomeActivity
- **Propósito**: Pantalla de bienvenida y punto de entrada
- **Ruta**: Launcher activity
- **Navegación**: Login o Register

#### LoginActivity
- **Propósito**: Autenticación de usuarios
- **Funcionalidad**:
  - Validación de credenciales
  - Inicio de sesión
  - Navegación a HomeActivity en caso exitoso
- **Usuario por defecto**:
  - Username: `admin`
  - Password: `admin123`
  - Email: `admin@artesanapp.com`

#### RegisterActivity
- **Propósito**: Registro de nuevos usuarios
- **Campos**:
  - Nombre completo
  - Email
  - Contraseña
- **Funcionalidad**:
  - Genera username automáticamente desde email
  - Crea usuarios con rol USER por defecto
  - Validaciones de email y contraseña

#### HomeActivity
- **Propósito**: Contenedor principal de la aplicación
- **Componentes**:
  - MaterialToolbar con título
  - BottomNavigationView para navegación
  - FragmentContainer para contenido dinámico
- **Navegación**:
  - Home (ProductListFragment)
  - Carrito (CartFragment)
  - Mapa (MapFragment)
  - Usuarios (UserManagementFragment - solo Admin)
  - Producto (CreateProductFragment - solo Admin)

### Fragments

#### ProductListFragment
- **Propósito**: Lista de productos disponibles
- **Funcionalidad**:
  - RecyclerView con ProductAdapter
  - Botón "Agregar al Carrito"
  - Editar/Eliminar productos (solo Admin)
  - Navegación a ProductDetailFragment

#### CartFragment
- **Propósito**: Gestión del carrito de compras
- **Funcionalidad**:
  - Lista de productos en el carrito
  - Incrementar/Decrementar cantidad
  - Eliminar productos del carrito
  - Calcular total
  - Botón "Comprar" (checkout)

#### CreateProductFragment
- **Propósito**: Crear nuevos productos (Admin)
- **Campos**:
  - Nombre
  - Descripción
  - Precio
  - Stock
  - Imagen (desde galería)
- **Funcionalidad**:
  - Selección de imagen con ActivityResultContracts
  - Compresión automática de imagen (800px max, 80% JPEG)
  - Preview de imagen seleccionada

#### MapFragment
- **Propósito**: Mostrar ubicación de la tienda
- **Funcionalidad**:
  - Google Maps integrado
  - Marcador en ubicación de tienda (Vito's: 4.71047, -74.111894)
  - Botón "Mi Ubicación" para mostrar ubicación del usuario
  - Solicitud de permisos de ubicación

#### UserManagementFragment (Admin)
- **Propósito**: Gestión de usuarios del sistema
- **Funcionalidad**:
  - Lista de usuarios con RecyclerView
  - Búsqueda en tiempo real por nombre o email
  - Botón flotante para crear usuarios
  - Editar/Eliminar usuarios
  - Restricción: No permite eliminar usuario logueado

#### CreateUserFragment (Admin)
- **Propósito**: Crear nuevos usuarios
- **Campos**:
  - Nombre completo
  - Email
  - Contraseña
  - Rol (USER/ADMIN)
- **Funcionalidad**:
  - Validaciones completas
  - Generación automática de username desde email
  - Verificación de email y username únicos

#### EditUserFragment (Admin)
- **Propósito**: Editar información de usuarios
- **Funcionalidad**:
  - Editar nombre, email y rol
  - Botón "Reset Password" con dialog
  - Botón "Delete User" con confirmación
  - Restricción: No permite eliminar usuario logueado

### Adapters

#### ProductAdapter
- **Propósito**: Adapter para lista de productos
- **ViewHolder**: Muestra nombre, descripción, precio, stock e imagen
- **Acciones**:
  - Agregar al carrito
  - Editar (solo Admin)
  - Eliminar (solo Admin)

#### CartAdapter
- **Propósito**: Adapter para carrito de compras
- **ViewHolder**: Producto con cantidad y controles
- **Acciones**:
  - Incrementar cantidad
  - Decrementar cantidad
  - Eliminar del carrito

#### UserAdapter
- **Propósito**: Adapter para lista de usuarios
- **ViewHolder**: Avatar, nombre, email, rol
- **Acciones**:
  - Editar usuario
  - Eliminar usuario
- **Colores de rol**:
  - ADMIN: Naranja (#FF5722)
  - USER: Gris (#757575)

---

## Autenticación y Autorización

### AuthService

**Ubicación**: `com.artesan.artesanapp.services.AuthService`

#### Métodos Principales

```kotlin
fun register(
    name: String,
    email: String,
    username: String,
    password: String,
    role: UserRole = UserRole.USER
): Result<User>
```
- Registra un nuevo usuario
- Validaciones: nombre, email, username, contraseña (min 6 caracteres)
- Verifica unicidad de email y username

```kotlin
fun login(username: String, password: String): Result<User>
```
- Autentica usuario
- Guarda sesión en tabla session
- Retorna usuario autenticado

```kotlin
fun logout()
```
- Cierra sesión actual
- Limpia tabla session

```kotlin
fun isLoggedIn(): Boolean
```
- Verifica si hay usuario logueado

```kotlin
fun isAdmin(): Boolean
```
- Verifica si el usuario actual es Admin

### Sistema de Sesiones

- **Tabla session**: Almacena ID del usuario logueado
- **Persistencia**: La sesión persiste entre reinicios de la app
- **Verificación**: HomeActivity verifica sesión en onCreate()

### Roles de Usuario

```kotlin
enum class UserRole {
    ADMIN,  // Acceso completo
    USER    // Acceso limitado (solo lectura y carrito)
}
```

**Permisos por Rol:**

| Funcionalidad | ADMIN | USER |
|--------------|-------|------|
| Ver productos | ✅ | ✅ |
| Carrito de compras | ✅ | ✅ |
| Crear productos | ✅ | ❌ |
| Editar productos | ✅ | ❌ |
| Eliminar productos | ✅ | ❌ |
| Ver mapa | ✅ | ✅ |
| Gestionar usuarios | ✅ | ❌ |

---

## Gestión de Usuarios

### Modelo User

```kotlin
data class User(
    val id: String,           // UUID
    val name: String,         // Nombre completo
    val email: String,        // Email único
    val username: String,     // Username único
    val password: String,     // Contraseña
    val role: UserRole        // ADMIN o USER
)
```

### UserDao

**Ubicación**: `com.artesan.artesanapp.database.UserDao`

#### Operaciones CRUD

```kotlin
fun insert(user: User): Long
fun update(user: User): Int
fun delete(id: String): Int
fun getAll(): List<User>
fun findById(id: String): User?
fun findByUsername(username: String): User?
```

#### Operaciones Especiales

```kotlin
fun searchByNameOrEmail(query: String): List<User>
```
- Búsqueda por nombre o email usando LIKE
- Ordenado por nombre ascendente

```kotlin
fun usernameExists(username: String): Boolean
fun emailExists(email: String): Boolean
```
- Validación de unicidad

### Flujo de Gestión de Usuarios (Admin)

1. **Listar Usuarios**
   - UserManagementFragment carga lista completa
   - SearchBar filtra en tiempo real
   - Muestra: Avatar, Nombre, Email, Rol

2. **Crear Usuario**
   - Click en FAB → CreateUserFragment
   - Validaciones de campos
   - Username generado desde email
   - Guardar → Regresa a lista

3. **Editar Usuario**
   - Click en botón editar → EditUserFragment
   - Cargar datos del usuario
   - Modificar campos
   - "Reset Password" → Dialog de nueva contraseña
   - "Save Changes" → Actualizar en BD

4. **Eliminar Usuario**
   - Click en botón eliminar
   - Verificación: No es usuario logueado
   - Dialog de confirmación
   - Eliminar de BD (CASCADE elimina sesión si existe)

---

## Gestión de Productos

### Modelo Product

```kotlin
data class Product(
    val id: String,              // UUID
    val name: String,            // Nombre
    val description: String,     // Descripción
    val price: Double,           // Precio
    val stock: Int,              // Inventario
    val imageBase64: String?     // Imagen en Base64
)
```

### ProductRepository

**Ubicación**: `com.artesan.artesanapp.repositories.ProductRepository`

#### Operaciones CRUD

```kotlin
fun insert(product: Product): Boolean
fun update(product: Product): Boolean
fun delete(id: String): Boolean
fun getAll(): List<Product>
fun getById(id: String): Product?
```

### ImageHelper

**Ubicación**: `com.artesan.artesanapp.utils.ImageHelper`

#### Funcionalidades

```kotlin
fun compressImage(uri: Uri, context: Context): String?
```
- Carga imagen desde URI
- Redimensiona a máximo 800px (mantiene aspect ratio)
- Comprime a JPEG 80%
- Codifica a Base64
- Maneja rotación EXIF

```kotlin
fun decodeBase64(base64: String): Bitmap?
```
- Decodifica string Base64 a Bitmap

### Flujo de Productos

1. **Crear Producto (Admin)**
   - Click en "Producto" → CreateProductFragment
   - Llenar formulario
   - Click en "Seleccionar Imagen"
   - ActivityResultContracts.GetContent() abre galería
   - ImageHelper comprime imagen
   - Preview de imagen
   - Guardar → ProductRepository.insert()

2. **Editar Producto (Admin)**
   - Click en editar desde ProductListFragment
   - Dialog con campos prellenados
   - Modificar campos
   - Opcionalmente cambiar imagen
   - Guardar → ProductRepository.update()

3. **Eliminar Producto (Admin)**
   - Click en eliminar
   - Dialog de confirmación
   - ProductRepository.delete()
   - Actualizar lista

---

## Carrito de Compras

### Modelo CartItem

```kotlin
data class CartItem(
    val product: Product,    // Producto en carrito
    val quantity: Int        // Cantidad
)
```

### CartService

**Ubicación**: `com.artesan.artesanapp.services.CartService`

#### Operaciones

```kotlin
fun addToCart(productId: String, quantity: Int = 1): Boolean
```
- Agrega producto al carrito
- Si ya existe, incrementa cantidad

```kotlin
fun updateQuantity(productId: String, quantity: Int): Boolean
```
- Actualiza cantidad de producto en carrito

```kotlin
fun removeFromCart(productId: String): Boolean
```
- Elimina producto del carrito

```kotlin
fun getCart(): List<CartItem>
```
- Obtiene todos los items del carrito con detalles del producto

```kotlin
fun clearCart()
```
- Vacía el carrito completamente

```kotlin
fun getTotal(): Double
```
- Calcula total del carrito (precio × cantidad)

### Flujo del Carrito

1. **Agregar al Carrito**
   - ProductListFragment → Click "Agregar al Carrito"
   - CartService.addToCart(productId)
   - Toast de confirmación

2. **Ver Carrito**
   - Click en "Carrito" → CartFragment
   - Carga CartService.getCart()
   - Muestra RecyclerView con CartAdapter

3. **Modificar Cantidad**
   - Click en + → incrementar
   - Click en - → decrementar (min 1)
   - CartService.updateQuantity()
   - Actualizar total

4. **Checkout**
   - Click en "Comprar"
   - Validar stock disponible
   - ProductRepository.update() para decrementar stock
   - CartService.clearCart()
   - Toast de confirmación

---

## Integración de Mapas

### Google Maps API

**API Key**: `YOUR_API_KEY_HERE`

**Configuración en AndroidManifest.xml**:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY_HERE" />
```

### MapFragment

**Ubicación**: `com.artesan.artesanapp.fragments.MapFragment`

#### Funcionalidades

1. **Ubicación de Tienda**
   - Coordenadas: LatLng(4.71047, -74.111894)
   - Marcador: "Mochilas artesanales"
   - Zoom inicial: 17
   - Se muestra automáticamente al abrir el mapa

2. **Ubicación del Usuario**
   - Botón "Mi Ubicación" (top-right)
   - Solicita permisos: ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION
   - FusedLocationProviderClient obtiene ubicación
   - Agrega marcador "Tu Ubicación"
   - Mueve cámara a ubicación del usuario

#### Permisos Requeridos

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```

#### Controles del Mapa

- Zoom controls: Habilitados
- Compass: Habilitado
- My Location button: Deshabilitado (se usa botón personalizado)

---

## Flujos de Trabajo

### Flujo de Autenticación

```
┌─────────────┐
│  Welcome    │
└──────┬──────┘
       │
   ┌───┴───┐
   │       │
   ▼       ▼
┌──────┐ ┌──────────┐
│Login │ │ Register │
└───┬──┘ └─────┬────┘
    │          │
    │    ┌─────┘
    │    │
    ▼    ▼
┌──────────────┐
│ AuthService  │
│ .login()     │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ Save Session │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ HomeActivity │
└──────────────┘
```

### Flujo de Navegación (Usuario Normal)

```
┌──────────────┐
│ HomeActivity │
└──────┬───────┘
       │
   ┌───┴────────────────┐
   │ BottomNavigation   │
   └───┬────────────────┘
       │
   ┌───┴────┬──────┬──────┐
   │        │      │      │
   ▼        ▼      ▼      ▼
┌─────┐ ┌─────┐ ┌────┐ ┌────┐
│Prod │ │Cart │ │Map │ │... │
└─────┘ └─────┘ └────┘ └────┘
```

### Flujo de Navegación (Admin)

```
┌──────────────┐
│ HomeActivity │
└──────┬───────┘
       │
   ┌───┴────────────────┐
   │ BottomNavigation   │
   └───┬────────────────┘
       │
   ┌───┴────┬──────┬──────┬──────┬────────┐
   │        │      │      │      │        │
   ▼        ▼      ▼      ▼      ▼        ▼
┌─────┐ ┌─────┐ ┌────┐ ┌─────┐ ┌────────┐
│Prod │ │Cart │ │Map │ │User │ │Create  │
│List │ │     │ │    │ │Mgmt │ │Product │
└─────┘ └─────┘ └────┘ └─────┘ └────────┘
```

### Flujo de Compra

```
┌──────────────┐
│ Product List │
└──────┬───────┘
       │ Click "Add to Cart"
       ▼
┌──────────────┐
│ CartService  │
│ .addToCart() │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ Navigate to  │
│ CartFragment │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ Modify Qty   │
│ +/-          │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ Click "Buy"  │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ Validate     │
│ Stock        │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ Update Stock │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ Clear Cart   │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ Show Success │
└──────────────┘
```

---

## Configuración y Construcción

### Dependencias

**build.gradle.kts (app)**:

```kotlin
dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Gson para JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // ExifInterface para metadatos de imágenes
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // Activity/Fragment KTX
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```

### Comandos de Construcción

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on device/emulator
./gradlew installDebug

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Clean build
./gradlew clean

# Lint check
./gradlew lint
```

### Configuración de Google Maps

1. **Obtener API Key**:
   - Google Cloud Console
   - Habilitar Maps SDK for Android
   - Crear credenciales (API Key)

2. **Agregar a AndroidManifest.xml**:
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="YOUR_API_KEY_HERE" />
   ```

3. **Permisos necesarios**:
   ```xml
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
   <uses-permission android:name="android.permission.INTERNET" />
   ```

---

## Consideraciones de Seguridad

### ⚠️ Advertencias de Seguridad

1. **Contraseñas en Texto Plano**
   - **Problema**: Las contraseñas se almacenan sin hash
   - **Recomendación**: Implementar BCrypt o similar para producción

2. **API Key en Código**
   - **Problema**: API key de Google Maps en el código fuente
   - **Recomendación**: Usar local.properties o Build Secrets

3. **SQL Injection**
   - **Estado**: Protegido mediante consultas parametrizadas
   - ✅ Todos los DAOs usan placeholders `?`

4. **Permisos de Ubicación**
   - ✅ Runtime permissions implementados correctamente
   - ✅ Solicitud solo cuando se necesita

### Mejoras Recomendadas para Producción

1. **Seguridad de Contraseñas**
   ```kotlin
   // Implementar hash de contraseñas
   fun hashPassword(password: String): String {
       return BCrypt.hashpw(password, BCrypt.gensalt())
   }

   fun verifyPassword(password: String, hash: String): Boolean {
       return BCrypt.checkpw(password, hash)
   }
   ```

2. **Tokens de Sesión**
   - Implementar JWT en lugar de ID directo
   - Expiración de sesiones
   - Refresh tokens

3. **Validación de Imágenes**
   - Validar tamaño máximo
   - Validar formato de archivo
   - Sanitizar nombres de archivo

4. **Rate Limiting**
   - Limitar intentos de login
   - Prevenir fuerza bruta

5. **Cifrado de Base de Datos**
   - SQLCipher para cifrar SQLite
   - Proteger datos sensibles

---

## Testing

### Testing Unitario

**Ubicación**: `app/src/test/java/`

Ejemplo de test:
```kotlin
class AuthServiceTest {
    @Test
    fun register_withValidData_returnsSuccess() {
        val authService = AuthService(context)
        val result = authService.register(
            name = "Test User",
            email = "test@example.com",
            username = "testuser",
            password = "password123"
        )
        assertTrue(result.isSuccess)
    }
}
```

### Testing Instrumentado

**Ubicación**: `app/src/androidTest/java/`

Ejemplo de test:
```kotlin
class LoginActivityTest {
    @Test
    fun login_withValidCredentials_navigatesToHome() {
        onView(withId(R.id.editTextUsername))
            .perform(typeText("admin"))
        onView(withId(R.id.editTextPassword))
            .perform(typeText("admin123"))
        onView(withId(R.id.buttonLogin))
            .perform(click())
        onView(withId(R.id.fragmentContainer))
            .check(matches(isDisplayed()))
    }
}
```

---

## Troubleshooting

### Problemas Comunes

1. **Error de compilación con Google Maps**
   - Verificar que play-services-maps esté en dependencies
   - Sync Gradle files
   - Invalidate caches y restart

2. **Imágenes no se cargan**
   - Verificar permisos de lectura de almacenamiento
   - Comprobar que ImageHelper.compressImage() no devuelva null
   - Verificar tamaño de imagen en base64 (puede ser muy grande)

3. **Usuario admin no existe**
   - Borrar datos de app
   - AuthService crea admin automáticamente en primer inicio

4. **Mapa no se muestra**
   - Verificar API Key en AndroidManifest.xml
   - Verificar que Maps SDK esté habilitado en Google Cloud
   - Verificar permisos de internet

5. **Sesión no persiste**
   - Verificar que SessionDao esté funcionando
   - Comprobar foreign keys habilitados
   - Revisar DatabaseHelper.onConfigure()

---

## Glosario

- **DAO**: Data Access Object - Patrón de acceso a datos
- **DTO**: Data Transfer Object - Objeto de transferencia de datos
- **UUID**: Universally Unique Identifier - Identificador único universal
- **CRUD**: Create, Read, Update, Delete - Operaciones básicas de BD
- **FAB**: Floating Action Button - Botón de acción flotante
- **RecyclerView**: Componente de Android para listas eficientes
- **Fragment**: Componente reutilizable de UI en Android
- **Activity**: Pantalla completa en Android
- **Material Design**: Sistema de diseño de Google
- **SQLite**: Base de datos embebida de Android

---

## Contacto y Recursos

### Enlaces Útiles

- [Documentación Android](https://developer.android.com/docs)
- [Kotlin Language](https://kotlinlang.org/)
- [Material Design 3](https://m3.material.io/)
- [Google Maps Android](https://developers.google.com/maps/documentation/android-sdk)

### Información del Proyecto

- **Versión**: 1.0
- **Fecha de Creación**: 2025
- **Última Actualización**: 2025

---

**Nota**: Esta documentación está sujeta a cambios según evolucione el proyecto.
