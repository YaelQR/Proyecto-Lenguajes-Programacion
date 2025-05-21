# 📱 Finanzi

Este repositorio contiene el proyecto de la materia *Lenguajes de Programación*, desarrollado en **Kotlin** utilizando **Android Studio**.

**Finanzi** es una aplicación móvil diseñada para permitir a los usuarios llevar un control detallado de sus ingresos y gastos personales. La app incorpora autenticación mediante Firebase, almacenamiento de datos en Firestore, y visualización de estadísticas a través de gráficos dinámicos.

## 🎯 Características

- Interfaz intuitiva y amigable
- Registro y login de usuarios
- Agregado, edición y eliminación de ingresos y gastos
- Visualización de estadísticas con gráficas tipo dona y de barras
- Filtro por fechas y categorías según selección del usuario
- Categorías personalizables

## 🛠️ Tecnologías y Herramientas Utilizadas

- **Kotlin** – Lenguaje de programación principal
- **Android Studio** – Entorno de desarrollo (IDE)
- **XML** – Lenguaje de marcado para definir interfaces y recursos
- **Firebase Auth y Firestore** – Servicios backend para autenticación y base de datos en la nube
- **MPAndroidChart** – Biblioteca para gráficos estadísticos
- **Material Design** – Estándar de diseño visual para apps Android

## 📦 Instalación y Ejecución

La aplicación se puede ejecutar de dos maneras: clonando el repositorio desde GitHub usando Android Studio o instalando directamente el archivo APK generado.

### 🔧 Opción 1: Clonar el repositorio y ejecutar con Android Studio

#### 1. Instalar Android Studio
Descarga e instala el IDE desde el sitio oficial: [https://developer.android.com/studio](https://developer.android.com/studio)

#### 2. Clonar el repositorio desde Android Studio
Una vez abierto Android Studio, en la **pantalla de bienvenida** verás la siguiente interfaz:

![Pantalla de inicio de Android Studio](https://github.com/user-attachments/assets/7825b83e-c8ce-4e55-97ee-25bfede9bdc1)

Haz clic en el botón **Clone Repository** (Clonar Repositorio).

#### 3. Pega la URL del repositorio

![image](https://github.com/user-attachments/assets/31591e91-b1f1-4940-b44e-e99c0596554c)

En el cuadro de diálogo que aparece, ingresa esta URL: https://github.com/YaelQR/Proyecto-Lenguajes-Programacion.git

Luego haz clic en **Clone**.

#### 4. Abrir el proyecto

Una vez clonado, Android Studio abrirá el proyecto automáticamente. 

> ⚠️ Si no se sincroniza de forma automática, Android Studio mostrará una barra amarilla en la parte superior con un botón **“Sync now”**. Haz clic en él para iniciar la sincronización manual.
> ![image](https://github.com/user-attachments/assets/0d16e149-7929-46b1-83f7-4005f65a913b)

También puedes hacerlo desde el menú:

File > Sync Project with Gradle Files

![image](https://github.com/user-attachments/assets/d01aba54-5317-4ea8-aa68-9ef89bf850f7)


Este paso es esencial para descargar las dependencias necesarias y que el proyecto compile correctamente.

Espera a que sincronice con Gradle y asegúrate de que el entorno esté correctamente configurado (SDK, dependencias, etc.).

#### 5. Ejecutar la app

- Conecta un dispositivo Android (Este debe tener la depuracion por usb activada) o inicia un emulador.
- Haz clic en **Run ▶️** o presiona `Shift + F10`.

> ✅ La app debería compilarse y ejecutarse en tu dispositivo/emulador.

### 📲 Opción 2: Instalar el APK directamente

