# Guide: как устроено приложение «Онлайн Кино»

Этот документ написан для разработчика, который получил проект и знает Android
пока поверхностно. Здесь объясняется не только «что лежит в папках», но и
**как всё работает по шагам**, зачем нужен каждый файл и что делать, если
что-то сломалось.

Если коротко: это простое Android-приложение на Java, которое ищет фильмы по
названию, показывает список с постерами и открывает страницу фильма во
встроенном браузере (`WebView`).

---

## 1. Что видит пользователь

1. Открывается тёмный экран с заголовком **«Онлайн Кино»**.
2. Пользователь вводит название фильма в поисковую строку.
3. Нажимает кнопку поиска на клавиатуре.
4. Приложение уходит в Интернет, получает список фильмов и показывает карточки:
   постер + название.
5. Пользователь нажимает на карточку.
6. Открывается второй экран — `PlayActivity` с `WebView`, где загружается
   страница выбранного фильма.

Приложение **не хранит видео у себя** и **не скачивает фильмы**. Оно получает
только метаданные (название, постер, id) и открывает внешний сайт.

---

## 2. Технологии и термины простыми словами

| Термин | Что это значит в этом проекте |
|---|---|
| **Android SDK** | Набор инструментов Android, которым собирается приложение. |
| **Gradle** | «Сборщик». Читает конфигурацию, скачивает библиотеки и собирает APK. |
| **Gradle Wrapper** | Файлы `gradlew` / `gradlew.bat`, которые запускают нужную версию Gradle автоматически. |
| **JDK 17** | Java Development Kit. Нужен, чтобы компилировать Java-код. |
| **APK** | Установочный файл Android-приложения. |
| **Activity** | Один экран приложения. У нас их два: `MainActivity` и `PlayActivity`. |
| **XML-разметка** | «Чертёж» экрана: где какие кнопки, тексты и отступы. |
| **`R`** | Автоматически сгенерированный класс со ссылками на ресурсы: `R.layout.activity_main`, `R.id.recyclerView`, `R.string.app_name`. Руками его не редактируют. |
| **Intent** | «Конверт» для перехода между экранами и передачи данных. |
| **RecyclerView** | Прокручиваемый список, который переиспользует карточки, а не создаёт их все сразу. |
| **Adapter** | «Переводчик» между списком данных и `RecyclerView`. |
| **ViewHolder** | «Держатель» ссылок на элементы одной карточки, чтобы не искать их заново при прокрутке. |
| **JSON** | Текстовый формат данных, который приходит от API. |
| **API** | Внешний сервис, к которому приложение делает запросы. |
| **Gson** | Библиотека, которая превращает JSON в Java-объекты и обратно. |
| **Glide** | Библиотека, которая скачивает картинки по URL, кэширует их и показывает в `ImageView`. |
| **`WebView`** | Встроенный в приложение браузер. |
| **`ExecutorService`** | «Фоновый работник», который выполняет сетевой запрос не в главном потоке. |
| **`Handler`** | «Курьер», который возвращает результат из фонового потока в главный, чтобы обновить экран. |
| **Main thread** | Главный поток. На нём работает интерфейс. Долгие операции здесь запрещены. |
| **Lifecycle** | Этапы жизни экрана: создание (`onCreate`), показ, сворачивание, уничтожение (`onDestroy`). |
| **`dp` / `sp`** | Единицы размера: `dp` — для отступов и картинок, `sp` — для текста (учитывает системный размер шрифта). |

---

## 3. Структура проекта

Дерево верхнего уровня:

```text
D:\Projects\AndroidAPP\
├── app\
│   ├── build.gradle.kts          ← настройки самого приложения и библиотек
│   ├── proguard-rules.pro        ← правила для release-сборки
│   └── src\
│       ├── main\
│       │   ├── AndroidManifest.xml
│       │   ├── java\com\example\onlinecinema\
│       │   │   ├── MainActivity.java
│       │   │   ├── PlayActivity.java
│       │   │   ├── Encoder.java
│       │   │   ├── Parser.java
│       │   │   ├── Preparer.java
│       │   │   ├── Film.java
│       │   │   └── FilmAdapter.java
│       │   └── res\
│       │       ├── layout\
│       │       │   ├── activity_main.xml
│       │       │   ├── activity_play.xml
│       │       │   └── film_item.xml
│       │       ├── values\
│       │       │   ├── strings.xml
│       │       │   ├── colors.xml
│       │       │   └── themes.xml
│       │       ├── drawable\
│       │       │   ├── search_background.xml
│       │       │   ├── poster_placeholder.xml
│       │       │   ├── ic_film_placeholder.xml
│       │       │   └── ic_launcher_foreground.xml
│       │       └── mipmap-anydpi-v26\
│       │           ├── ic_launcher.xml
│       │           └── ic_launcher_round.xml
│       └── test\java\com\example\onlinecinema\
│           ├── EncoderTest.java
│           └── PreparerTest.java
├── gradle\wrapper\
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
├── build.gradle.kts              ← общие настройки проекта
├── settings.gradle.kts           ← какие модули входят в проект
├── gradle.properties             ← параметры Gradle
├── local.properties              ← путь к Android SDK (не коммитится)
├── gradlew / gradlew.bat         ← запуск Gradle
├── build.ps1                     ← удобный скрипт сборки для Windows
├── .toolchain\jdk17\             ← портативный JDK 17
├── README.md                     ← краткое описание проекта
└── guide.md                      ← этот документ
```

Папки `.gradle`, `build`, `app/build` — это **служебные** папки, которые
создаются при сборке. Их не нужно редактировать.

### 3.1. Шпаргалка: файл → за что отвечает

| Файл | За что отвечает | Когда трогать |
|---|---|---|
| `settings.gradle.kts` | Список модулей и репозитории зависимостей. | При добавлении нового модуля. |
| `build.gradle.kts` (корень) | Версия Android Gradle Plugin. | При обновлении AGP. |
| `gradle.properties` | Память Gradle, флаги AndroidX. | При проблемах сборки/памяти. |
| `local.properties` | Путь к Android SDK на компьютере. | Если SDK не найден. |
| `gradlew` / `gradlew.bat` | Запуск нужной версии Gradle. | Обычно не трогать. |
| `gradle/wrapper/*` | Настройки Gradle Wrapper. | При смене версии Gradle. |
| `build.ps1` | Удобная сборка debug APK в Windows. | Если нужно добавить шаги сборки. |
| `app/build.gradle.kts` | Настройки приложения и список библиотек. | При добавлении зависимостей, смене SDK. |
| `app/proguard-rules.pro` | Правила оптимизации release-сборки. | При включении minify. |
| `AndroidManifest.xml` | Разрешения, Activity, тема, иконка. | При добавлении экрана/разрешения. |
| `MainActivity.java` | Главный экран: поиск, список, фоновые задачи. | При изменении логики поиска. |
| `Encoder.java` | Кодирование поискового запроса для URL. | При смене API. |
| `Parser.java` | Скачивание JSON и его разбор, резервный API. | При смене API/формата данных. |
| `Preparer.java` | Выборка `id`, `title`, `poster` из сырых данных. | При добавлении полей фильма. |
| `Film.java` | Модель одного фильма. | При добавлении полей. |
| `FilmAdapter.java` | Создание карточек, Glide, клик по фильму. | При изменении карточки/навигации. |
| `PlayActivity.java` | Экран WebView, настройки плеера, fallback-адреса. | При смене плеера/URL. |
| `activity_main.xml` | Внешний вид главного экрана. | При изменении вёрстки. |
| `film_item.xml` | Внешний вид одной карточки фильма. | При изменении карточки. |
| `activity_play.xml` | Внешний вид экрана просмотра. | При изменении WebView/прогресса. |
| `strings.xml` | Все тексты приложения. | При смене текстов/локализации. |
| `colors.xml` | Палитра приложения. | При смене стиля. |
| `themes.xml` | Базовая тема Material3. | При смене светлой/тёмной темы. |
| `drawable/*` | Фоны, заглушки, иконки. | При изменении графики. |
| `mipmap-anydpi-v26/*` | Иконка приложения. | При смене логотипа. |
| `src/test/*` | Юнит-тесты чистой логики. | При изменении `Encoder`/`Preparer`. |
| `README.md` | Краткая инструкция. | При изменении сборки/возможностей. |
| `guide.md` | Подробное объяснение (этот файл). | При изменении архитектуры. |

---

## 4. Мысленная модель: на какие слои делится приложение

Проект намеренно простой, без MVVM, Room, Retrofit и навигационных графов.
Логика разделена на четыре слоя:

```text
┌───────────────────────────────┐
│           UI-слой              │
│  MainActivity + XML-разметка   │
│  PlayActivity + XML-разметка   │
└──────────────┬────────────────┘
               │ вызывает
┌──────────────▼────────────────┐
│        Слой данных             │
│  Encoder → Parser → Preparer   │
│  Film (модель)                 │
└──────────────┬────────────────┘
               │ результат
┌──────────────▼────────────────┐
│      Мост между UI и данными   │
│  FilmAdapter + FilmViewHolder  │
└───────────────────────────────┘
               │
┌──────────────▼────────────────┐
│        Внешний мир             │
│  Kinopoisk API / WebView       │
└───────────────────────────────┘
```

Главное правило: **Activity не должна сама парсить JSON и ходить в сеть**.
Она вызывает готовые методы `Encoder`, `Parser`, `Preparer`, получает список
`Film` и отдаёт его адаптеру. Так код проще читать и менять.

---

## 5. Полный путь одного поиска

Ниже — сценарий «пользователь ввёл “матрица” и нажал поиск».

```text
1. SearchView (экран)
        │  пользователь нажал «Поиск» на клавиатуре
        ▼
2. MainActivity.onQueryTextSubmit("матрица")
        │  проверяет, что строка не пустая
        ▼
3. executor.execute(...)
        │  задача уходит в фоновый поток
        ▼
4. Encoder.encode("матрица")
        │  получается:
        │  https://api4.rhserv.vu/search/%D0%BC%D0%B0%D1%82%D1%80%D0%B8%D1%86%D0%B0
        ▼
5. Parser.parse(url)
        │  пробует скачать JSON по адресу из инструкции
        │  если адрес недоступен — переключается на Kinopoisk API
        ▼
6. Preparer.prepare(data)
        │  из «сырых» Map достаёт только id, title, poster
        ▼
7. MainActivity создаёт List<Film>
        │
        ▼
8. mainHandler.post(...)
        │  возвращает выполнение в главный поток
        ▼
9. FilmAdapter.notifyDataSetChanged()
        │
        ▼
10. RecyclerView показывает карточки
        │
        ▼
11. Пользователь нажимает на карточку
        │
        ▼
12. Intent → PlayActivity
        │  передаются title, posterUrl, id
        ▼
13. PlayActivity загружает сайт в WebView
```

### Почему поиск идёт в фоне

Android запрещает сетевые запросы в главном потоке. Если сделать запрос
напрямую в `onCreate` или в обработчике кнопки, приложение «зависнет» или
упадёт с `NetworkOnMainThreadException`. Поэтому используется
`ExecutorService`.

### Почему результат возвращается через Handler

Фоновый поток **не должен** менять интерфейс. Он только получает данные.
Затем `mainHandler.post(...)` кладёт задачу в очередь главного потока, и уже
там обновляются `filmList`, адаптер и видимость текста.

---

## 6. Разбор файлов верхнего уровня

### `settings.gradle.kts`

Определяет, какие модули входят в проект:

```kotlin
rootProject.name = "OnlineCinema"
include(":app")
```

Также здесь указаны репозитории, откуда Gradle скачивает библиотеки:
`google()` и `mavenCentral()`.

**Когда менять:** если добавляете новый модуль (например, `:core` или `:data`).
Для обычной работы трогать не нужно.

### `build.gradle.kts` (в корне)

Подключает Android Gradle Plugin:

```kotlin
plugins {
    id("com.android.application") version "8.13.2" apply false
}
```

`apply false` означает: «скачай плагин, но применяй его не ко всему проекту,
а только там, где указано» — то есть в `app/build.gradle.kts`.

**Когда менять:** при обновлении версии Android Gradle Plugin. Делать это
нужно осторожно и вместе с обновлением Gradle/JDK.

### `gradle.properties`

Параметры сборки:

- `org.gradle.jvmargs=-Xmx2048m` — сколько памяти дать Gradle;
- `android.useAndroidX=true` — использовать современные AndroidX-библиотеки;
- `android.nonTransitiveRClass=true` — каждый модуль получает свой `R`.

**Когда менять:** если сборка падает из-за нехватки памяти или нужно
включить/выключить экспериментальные флаги Gradle.

### `local.properties`

Содержит путь к Android SDK **на конкретном компьютере**:

```properties
sdk.dir=C\:\\Users\\Home\\AppData\\Local\\Android\\Sdk
```

Этот файл не коммитится в Git, потому что у каждого разработчика путь свой.
Android Studio обычно создаёт его автоматически.

**Если проект не собирается с ошибкой “SDK location not found”** — проверьте
этот файл или откройте проект в Android Studio, чтобы она пересоздала путь.

### `gradlew`, `gradlew.bat`, `gradle/wrapper/*`

Это Gradle Wrapper. Он позволяет не устанавливать Gradle вручную:

```powershell
.\gradlew.bat :app:assembleDebug
```

`gradle-wrapper.properties` указывает версию Gradle (`8.13`). Не меняйте её
без причины: версия Gradle должна быть совместима с Android Gradle Plugin.

### `build.ps1`

Удобный скрипт для Windows. Он:

1. Находит портативный JDK 17 в `.toolchain/jdk17`;
2. Подставляет `JAVA_HOME`;
3. Подставляет `ANDROID_HOME`, если он не задан;
4. Запускает `gradlew.bat :app:assembleDebug`.

Запуск:

```powershell
.\build.ps1
```

Внутри используется `--no-daemon`, чтобы процесс Gradle завершался и не
оставлял висеть фоновый демон.

### `README.md`

Краткая инструкция: что умеет приложение, как собрать, где лежит APK и почему
появился резервный API.

### `guide.md`

Этот подробный документ.

---

## 7. Модуль `app`: сборка и зависимости

### `app/build.gradle.kts`

Главный файл настроек приложения.

```kotlin
android {
    namespace = "com.example.onlinecinema"
    compileSdk = 36
    ...
}
```

- **`namespace`** — Java-пакет, в котором лежат классы и генерируется `R`.
- **`applicationId`** — уникальный идентификатор приложения на устройстве и
  в магазине. Сейчас `com.example.onlinecinema`.
- **`compileSdk = 36`** — какой версией Android SDK компилировать.
- **`minSdk = 28`** — минимальная поддерживаемая версия (Android 9).
- **`targetSdk = 36`** — под какую версию Android приложение оптимизировано
  (Android 16).
- **`versionCode` / `versionName`** — внутренний номер и красивая версия.

Блок `dependencies` подключает библиотеки:

| Библиотека | Зачем |
|---|---|
| `androidx.appcompat` | базовые `AppCompatActivity`, темы, совместимость. |
| `androidx.activity` | `EdgeToEdge`, `OnBackPressedDispatcher`. |
| `androidx.constraintlayout` | `ConstraintLayout` для главного экрана. |
| `androidx.recyclerview` | список фильмов. |
| `com.google.android.material` | Material-темы и стили. |
| `gson` | разбор JSON. |
| `glide` + `glide:compiler` | загрузка и кэширование постеров. |
| `junit` | юнит-тесты. |
| `androidx.test.*` | задел под инструментальные тесты. |

**Когда менять:** при добавлении новой библиотеки. После изменения обязательно
нажмите **Sync Now** в Android Studio.

### `app/proguard-rules.pro`

Правила для release-сборки. Сейчас minify выключен, поэтому файл почти пустой.
Он пригодится, если включать `isMinifyEnabled = true`.

---

## 8. `AndroidManifest.xml`: паспорт приложения

Файл `app/src/main/AndroidManifest.xml` сообщает Android:

1. Какие разрешения нужны.
2. Какие Activity есть в приложении.
3. Какая Activity запускается первой.
4. Какая тема и иконка используются.

Ключевые строки:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Без этого разрешения приложение не сможет делать сетевые запросы.

```xml
<activity android:name=".PlayActivity" android:exported="false" />
```

`PlayActivity` — внутренний экран. `exported="false"` значит, что другие
приложения не могут запустить его напрямую.

```xml
<activity android:name=".MainActivity" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

Это главный экран и иконка в меню приложений.

`android:usesCleartextTraffic="false"` запрещает обычный HTTP. Все адреса
должны быть `https://`. Это правильно с точки зрения безопасности.

**Важно:** если добавляете новую Activity, её нужно прописать здесь, иначе
приложение упадёт при попытке открыть экран.

---

## 9. Java-классы: кто за что отвечает

Все классы лежат в:

```text
app/src/main/java/com/example/onlinecinema/
```

### 9.1. `MainActivity.java` — главный экран

Это «мозг» главного экрана. Он:

- включает режим edge-to-edge;
- подключает XML-разметку `activity_main.xml`;
- добавляет отступы под статус-бар и панель навигации;
- создаёт `RecyclerView` и `FilmAdapter`;
- находит `SearchView`;
- запускает поиск в фоне;
- обновляет список в главном потоке;
- показывает сообщения об ошибках и пустом результате.

Ключевые поля:

```java
private RecyclerView recyclerView;      // список
private FilmAdapter adapter;           // мост между списком и данными
private List<Film> filmList;           // сами фильмы
private SearchView searchView;         // строка поиска
private TextView emptyText;            // подсказка, когда список пуст

private final ExecutorService executor =
        Executors.newSingleThreadExecutor();          // фоновый поток
private final Handler mainHandler =
        new Handler(Looper.getMainLooper());          // возврат в UI-поток
```

#### `onCreate`

Вызывается один раз при создании экрана.

```java
EdgeToEdge.enable(this);
setContentView(R.layout.activity_main);
```

`setContentView` «надувает» XML: превращает описание из `activity_main.xml`
в реальные View-объекты.

```java
ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), ...);
```

На современных Android интерфейс рисуется под статус-баром. Этот код добавляет
внутренние отступы, чтобы заголовок и поиск не уезжали под часы и батарею.
`R.id.main` — это `android:id="@+id/main"` у корневого `ConstraintLayout`.

```java
filmList = new ArrayList<>();
adapter = new FilmAdapter(this.getLayoutInflater(), filmList);

recyclerView = findViewById(R.id.recyclerView);
recyclerView.setLayoutManager(new LinearLayoutManager(this));
recyclerView.setAdapter(adapter);
```

Здесь:

- создаётся пустой список;
- создаётся адаптер, который смотрит на этот же список;
- `RecyclerView` получает менеджер компоновки (вертикальный список);
- `RecyclerView` получает адаптер.

`findViewById` ищет View по id из XML. Если id в XML переименовать, а в Java
не обновить — вернётся `null` и приложение упадёт.

#### Обработчик поиска

```java
searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
    @Override
    public boolean onQueryTextSubmit(String query) {
        ...
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
});
```

- `onQueryTextSubmit` срабатывает, когда пользователь нажал «Поиск» на
  клавиатуре.
- `onQueryTextChange` срабатывает при каждом изменении текста. Сейчас он
  ничего не делает, поэтому поиск идёт только по кнопке.

Внутри `onQueryTextSubmit`:

1. `query.trim()` убирает пробелы по краям.
2. Если строка пустая — показывается Toast «Введите название фильма».
3. `searchView.clearFocus()` снимает фокус (клавиатура обычно скрывается).
4. `executor.execute(...)` отправляет задачу в фоновый поток.

В фоне:

```java
String encoded = Encoder.encode(q);
List<Map<String, Object>> parsed = Parser.parse(encoded);
List<List<String>> ready = Preparer.prepare(parsed);
```

Дальше каждый ряд превращается в `Film`:

```java
for (List<String> row : ready) {
    if (row != null && row.size() >= 3) {
        films.add(new Film(row.get(0), row.get(1), row.get(2)));
    }
}
```

Затем результат возвращается в UI:

```java
mainHandler.post(() -> {
    filmList.clear();
    filmList.addAll(films);
    adapter.notifyDataSetChanged();
    emptyText.setVisibility(films.isEmpty() ? View.VISIBLE : View.GONE);
});
```

`notifyDataSetChanged()` говорит адаптеру: «данные изменились, перерисуй
список». Для небольших списков это нормально. Для больших лучше использовать
`DiffUtil`.

Проверка `isFinishing()` / `isDestroyed()` защищает от обновления экрана,
который уже закрыт.

#### `onDestroy`

```java
executor.shutdown();
```

Когда экран уничтожается, фоновый исполнитель корректно останавливается.

---

### 9.2. `Encoder.java` — подготовка URL

```java
public static String encode(String query) throws UnsupportedEncodingException {
    String url = "https://api4.rhserv.vu/search/";
    String encodedQuery = URLEncoder.encode(query, "UTF-8");
    return url + encodedQuery;
}
```

Зачем кодировать запрос:

- В URL нельзя вставлять пробелы и некоторые символы.
- Русские буквы тоже нужно закодировать.

Пример:

```text
"матрица"
→ %D0%BC%D0%B0%D1%82%D1%80%D0%B8%D1%86%D0%B0
```

Итог:

```text
https://api4.rhserv.vu/search/%D0%BC%D0%B0%D1%82%D1%80%D0%B8%D1%86%D0%B0
```

`static` означает, что метод можно вызывать без создания объекта:
`Encoder.encode(...)`, а не `new Encoder().encode(...)`.

**Важно:** этот же базовый адрес продублирован в `Parser.MANUAL_SEARCH_URL`.
Если будете менять API, обновите оба места.

---

### 9.3. `Parser.java` — сеть и JSON

Самый сложный класс. Он делает две вещи:

1. Скачивает JSON по URL.
2. Превращает JSON в `List<Map<String, Object>>`.

#### Почему есть резервный API

Адрес из методички `https://api4.rhserv.vu/search/` перестал существовать.
Поэтому `Parser.parse` сначала пробует его, а при ошибке автоматически
переключается на Kinopoisk API Unofficial:

```java
public static List<Map<String, Object>> parse(String urlString) throws IOException {
    try {
        return parseJson(fetch(urlString, null));
    } catch (Exception primaryError) {
        String query = extractQuery(urlString);
        if (query.isEmpty()) {
            throw asIOException(primaryError);
        }

        try {
            return searchKinopoisk(query);
        } catch (Exception fallbackError) {
            fallbackError.addSuppressed(primaryError);
            throw asIOException(fallbackError);
        }
    }
}
```

Что происходит:

- `fetch(...)` скачивает текст.
- `parseJson(...)` разбирает его Gson-ом.
- Если что-то пошло не так — `extractQuery` достаёт исходный запрос из URL.
- `searchKinopoisk(query)` делает запрос к резервному API.
- Если и он не сработал — выбрасывается исключение, которое `MainActivity`
  показывает в Toast.

#### `extractQuery`

```java
URLDecoder.decode(urlString.substring(MANUAL_SEARCH_URL.length()), "UTF-8")
```

Берёт всё после `https://api4.rhserv.vu/search/` и раскодирует обратно в
`матрица`. Это нужно, чтобы передать запрос в резервный API.

#### `fetch`

```java
URL url = new URL(urlString);
HttpURLConnection connection = (HttpURLConnection) url.openConnection();
connection.setRequestMethod("GET");
```

Дальше:

- `setConnectTimeout(15000)` — сколько ждать соединения;
- `setReadTimeout(20000)` — сколько ждать ответ;
- заголовок `Accept: application/json`;
- заголовок `User-Agent`;
- если передан ключ — заголовок `X-API-KEY`.

Проверка ответа:

```java
int responseCode = connection.getResponseCode();
if (responseCode < 200 || responseCode >= 300) {
    ...
    throw new IOException("HTTP " + responseCode + ...);
}
```

То есть 200–299 — успех, всё остальное — ошибка.

`readAll` читает ответ построчно:

```java
BufferedReader in = new BufferedReader(
        new InputStreamReader(stream, StandardCharsets.UTF_8)
);
String inputLine;
StringBuilder content = new StringBuilder();
while ((inputLine = in.readLine()) != null) {
    content.append(inputLine);
}
```

Важно: указана кодировка **UTF-8**, иначе русские названия превратятся в
«кракозябры».

#### `parseJson`

```java
Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
List<Map<String, Object>> list = gson.fromJson(content, listType);
```

Gson превращает JSON-массив в список словарей. Каждый словарь — один фильм:
ключ (`"title"`) → значение (`"Матрица"`).

#### `searchKinopoisk`

Kinopoisk возвращает не массив, а объект с полем `films`:

```json
{
  "keyword": "матрица",
  "pagesCount": 2,
  "films": [
    {
      "filmId": 301,
      "nameRu": "Матрица",
      "nameEn": "The Matrix",
      "year": "1999",
      "posterUrl": "https://...",
      "posterUrlPreview": "https://..."
    }
  ]
}
```

Код достаёт `films`, проходит по нему и создаёт **нормализованный** список:

```java
item.put("id", ...);       // filmId
item.put("title", ...);    // nameRu, иначе nameEn, иначе nameOriginal
item.put("poster", ...);   // posterUrl, иначе posterUrlPreview
```

Благодаря этому `Preparer` и остальной код не знают, откуда пришли данные:
из методички или из Kinopoisk. Формат для них одинаковый.

#### Почему id выглядит как `301.0`

Gson разбирает JSON-числа в `Double`, поэтому `filmId: 301` превращается в
`301.0`. В `PlayActivity` есть строка:

```java
movieId = extraId.replace(".0", "");
```

Она убирает `.0` перед подстановкой в URL. Это важная деталь: если будете
менять парсер, не потеряйте её.

#### `firstNonEmpty`

Возвращает первое непустое значение из списка. Например, если `nameRu` пустой,
берётся `nameEn`. Если и он пустой — `nameOriginal`.

#### `asIOException`

Приводит любое исключение к `IOException`, чтобы сигнатура метода оставалась
понятной: `parse(...) throws IOException`.

---

### 9.4. `Preparer.java` — выборка нужных полей

```java
public static List<List<String>> prepare(List<Map<String, Object>> data) {
    List<List<String>> megalist = new ArrayList<>();
    if (data == null) {
        return megalist;
    }

    for (Map<String, Object> elem : data) {
        if (elem == null) {
            continue;
        }

        List<String> row = new ArrayList<>();
        row.add(valueOf(elem.get("id")));
        row.add(valueOf(elem.get("title")));
        row.add(valueOf(elem.get("poster")));
        megalist.add(row);
    }

    return megalist;
}
```

Зачем это нужно:

- `Parser` возвращает «сырые» словари, где может быть много полей.
- UI нужны только три строки: `id`, `title`, `poster`.
- `Preparer` превращает список словарей в список рядов.

`valueOf` защищает от `null`: если поля нет, вернётся пустая строка, а не
падение.

---

### 9.5. `Film.java` — модель фильма

```java
public class Film {
    private final String id;
    private final String title;
    private final String poster;

    public Film(String id, String title, String poster) { ... }

    public String getTitle() { return title; }
    public String getId() { return id; }
    public String getPoster() { return poster; }
}
```

Это простой контейнер данных (POJO). `final` означает, что после создания
поля нельзя заменить. Так безопаснее: фильм не изменится случайно.

**Если нужно добавить год, рейтинг или описание:**

1. Добавьте поле в `Film`.
2. Добавьте его в конструктор и getter.
3. Добавьте извлечение в `Preparer`.
4. Добавьте `TextView` в `film_item.xml`.
5. Найдите его в `FilmViewHolder` и заполните в `onBindViewHolder`.

---

### 9.6. `FilmAdapter.java` — список и карточки

`RecyclerView` сам не знает, как рисовать карточку. Адаптер объясняет ему:

- сколько элементов;
- как создать новую карточку;
- какие данные положить в карточку;
- что делать при нажатии.

#### ViewHolder

```java
static class FilmViewHolder extends RecyclerView.ViewHolder {
    ImageView poster;
    TextView title;

    FilmViewHolder(View itemView) {
        super(itemView);
        poster = itemView.findViewById(R.id.poster);
        title = itemView.findViewById(R.id.title);
    }
}
```

`ViewHolder` хранит ссылки на `ImageView` и `TextView`. Без него при каждой
прокрутке пришлось бы заново искать элементы через `findViewById`, что медленно.

#### onCreateViewHolder

```java
return new FilmViewHolder(inflater.inflate(R.layout.film_item, parent, false));
```

Создаёт новую карточку из `film_item.xml`. `false` означает: «не прикрепляй
сразу к родителю, RecyclerView сделает это сам».

#### onBindViewHolder

```java
Film film = films.get(position);
holder.title.setText(film.getTitle());

Glide.with(holder.itemView.getContext())
        .load(film.getPoster())
        .placeholder(R.drawable.poster_placeholder)
        .error(R.drawable.poster_placeholder)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(holder.poster);
```

- `placeholder` показывается, пока картинка грузится;
- `error` показывается, если загрузка не удалась;
- `DiskCacheStrategy.ALL` кэширует картинку на диске.

Клик:

```java
holder.itemView.setOnClickListener(v -> {
    Intent intent = new Intent(v.getContext(), PlayActivity.class);
    intent.putExtra("title", film.getTitle());
    intent.putExtra("posterUrl", film.getPoster());
    intent.putExtra("id", film.getId());
    v.getContext().startActivity(intent);
});
```

`Intent` — это «конверт». В него кладутся данные через `putExtra`, а
`PlayActivity` достаёт их через `getIntent().getStringExtra("id")`.

#### getItemCount

```java
return films.size();
```

Сообщает списку, сколько карточек рисовать.

---

### 9.7. `PlayActivity.java` — экран просмотра

Это второй экран. Он получает `id` фильма и открывает сайт в `WebView`.

#### Получение id

```java
String extraId = getIntent().getStringExtra("id");
movieId = extraId == null ? "" : extraId.replace(".0", "");
```

Если `id` не передали, будет пустая строка. `.replace(".0", "")` убирает
хвост от Gson.

#### Настройки WebView

```java
WebSettings settings = webView.getSettings();
settings.setJavaScriptEnabled(true);
settings.setDomStorageEnabled(true);
settings.setMediaPlaybackRequiresUserGesture(false);
settings.setAllowContentAccess(true);
settings.setDatabaseEnabled(true);
settings.setLoadWithOverviewMode(true);
settings.setUseWideViewPort(true);
```

Зачем:

- `JavaScriptEnabled` — сайт использует JavaScript;
- `DomStorageEnabled` — `localStorage`/`sessionStorage`;
- `MediaPlaybackRequiresUserGesture(false)` — разрешает автовоспроизведение;
- `AllowContentAccess` / `DatabaseEnabled` — доступ к контенту и базам;
- `LoadWithOverviewMode` / `UseWideViewPort` — нормальное масштабирование
  мобильной страницы.

`@SuppressLint("SetJavaScriptEnabled")` отключает предупреждение линтера:
JavaScript в WebView потенциально опасен, но здесь он нужен для плеера.

#### WebViewClient

```java
webView.setWebViewClient(new WebViewClient() {
    @Override
    public void onPageStarted(...) { progressBar.setVisibility(View.VISIBLE); }

    @Override
    public void onPageFinished(...) { progressBar.setVisibility(View.GONE); }

    @Override
    public void onReceivedError(...) { loadFallbackPage(); }

    @Override
    public void onReceivedHttpError(...) { loadFallbackPage(); }
});
```

- `onPageStarted` / `onPageFinished` управляют индикатором загрузки.
- `onReceivedError` ловит сетевые ошибки (нет соединения, DNS).
- `onReceivedHttpError` ловит HTTP-ошибки (404, 403, 500).

#### Цепочка резервных адресов

```java
private int fallbackStage;

private void loadFallbackPage() {
    if (fallbackStage == 0) {
        fallbackStage = 1;
        webView.loadUrl("https://reyohoho.com/#/movie/" + movieId);
    } else if (fallbackStage == 1) {
        fallbackStage = 2;
        webView.loadUrl("https://www.kinopoisk.ru/film/" + movieId + "/");
    } else {
        Toast.makeText(this, R.string.player_unavailable, Toast.LENGTH_LONG).show();
    }
}
```

Почему так:

1. Сначала пробуется адрес из методички:
   `https://reyohoho.github.io/reyohoho/#/movie/<id>`.
2. Если он недоступен — актуальный адрес каталога.
3. Если и он не работает — страница фильма на Кинопоиске.
4. Если всё сломалось — Toast «Не удалось открыть страницу просмотра».

#### Кнопка «Назад»

На Android 16+ обычный `onBackPressed()` больше не вызывается для
предиктивного жеста «назад». Поэтому используется современный API:

```java
getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
    @Override
    public void handleOnBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }
});
```

Логика:

- если внутри WebView есть история — идём на предыдущую страницу;
- если истории нет — закрываем экран.

#### `onDestroy`

```java
if (webView != null) {
    webView.destroy();
}
```

`WebView` — тяжёлый объект. Его нужно уничтожать, чтобы не было утечек памяти.

---

## 10. XML-разметка: как читать экраны

### 10.1. `activity_main.xml` — главный экран

Корень — `ConstraintLayout`. Он позволяет привязывать элементы друг к другу:

```xml
app:layout_constraintTop_toBottomOf="@id/searcharea"
```

Это значит: «верх элемента должен быть под нижней границей SearchView».

Основные элементы:

| ID | Что это |
|---|---|
| `main` | корневой контейнер. Нужен для отступов edge-to-edge. |
| `text` | заголовок «Онлайн Кино». |
| `subtitle` | подпись «Поиск фильмов онлайн». |
| `searcharea` | `SearchView`. |
| `recyclerView` | список результатов. |
| `emptyText` | подсказка, когда результатов нет. |

Почему у `RecyclerView` высота `0dp`:

```xml
android:layout_height="0dp"
app:layout_constraintTop_toBottomOf="@id/searcharea"
app:layout_constraintBottom_toBottomOf="parent"
```

В `ConstraintLayout` `0dp` означает «займи всё пространство между верхней и
нижней привязками». То есть список тянется от поиска до низа экрана.

`SearchView`:

```xml
android:iconifiedByDefault="false"
android:imeOptions="actionSearch"
```

- `iconifiedByDefault=false` — строка поиска сразу развёрнута;
- `imeOptions=actionSearch` — на клавиатуре появляется кнопка «Поиск».

### 10.2. `film_item.xml` — одна карточка

Корень — `RelativeLayout`. Внутри:

- `ImageView` с id `poster`;
- `TextView` с id `title`.

Постер:

```xml
android:layout_width="175dp"
android:layout_height="250dp"
android:scaleType="centerCrop"
```

`centerCrop` заполняет весь прямоугольник и обрезает лишнее. Благодаря этому
постеры разного размера выглядят одинаково.

Название:

```xml
android:layout_toEndOf="@id/poster"
android:layout_centerVertical="true"
android:layout_marginStart="16dp"
```

- `toEndOf` — поставить справа от постера;
- `centerVertical` — выровнять по центру по вертикали;
- `marginStart` — отступ от постера.

`android:background="?attr/selectableItemBackground"` добавляет эффект нажатия
(ripple).

### 10.3. `activity_play.xml` — экран просмотра

Корень — `LinearLayout` с горизонтальной ориентацией. Внутри `FrameLayout`,
который занимает всё свободное место (`layout_weight="1"`), а в нём:

- `WebView` — сайт;
- `ProgressBar` — крутилка загрузки поверх WebView.

`ProgressBar` изначально `visibility="gone"`, а `PlayActivity` показывает его
при начале загрузки.

---

## 11. Ресурсы: тексты, цвета, темы, картинки

### `res/values/strings.xml`

Все тексты приложения. Вместо «магических строк» в коде используется
`@string/...` или `R.string....`.

Пример:

```xml
<string name="app_name">Онлайн Кино</string>
```

В Java:

```java
getString(R.string.app_name)
```

**Когда менять:** при изменении названия приложения, подсказок, сообщений.

### `res/values/colors.xml`

Палитра:

| Цвет | Где используется |
|---|---|
| `cinema_red` | акцент, заголовок, прогресс. |
| `cinema_dark` | фон. |
| `cinema_surface` | поверхность. |
| `cinema_surface_light` | поле поиска. |
| `cinema_text` | основной текст. |
| `cinema_muted` | второстепенный текст. |

**Когда менять:** для смены фирменного стиля.

### `res/values/themes.xml`

Тема приложения:

```xml
<style name="Theme.OnlineCinema" parent="Theme.Material3.Dark.NoActionBar">
```

- `Material3` — современный стиль Material;
- `Dark` — тёмная тема;
- `NoActionBar` — без верхней панели, потому что заголовок свой.

Здесь же задаются цвета статус-бара, навигации и фона.

**Когда менять:** если нужна светлая тема или другой базовый стиль.

### `res/drawable/*`

| Файл | Что это |
|---|---|
| `search_background.xml` | скруглённый фон поисковой строки. |
| `poster_placeholder.xml` | заглушка, пока постер не загрузился. |
| `ic_film_placeholder.xml` | иконка плёнки внутри заглушки. |
| `ic_launcher_foreground.xml` | передний план иконки приложения. |

### `res/mipmap-anydpi-v26/*`

Адаптивная иконка приложения. `v26` означает, что она используется на Android
8.0+; у нас `minSdk 28`, поэтому подходит.

`monochrome` — монохромный слой для тематических иконок Android 13+.

---

## 12. Тесты

В проекте есть два простых юнит-теста. Они не ходят в Интернет и проверяют
чистую логику.

### `EncoderTest.java`

Проверяет, что запрос правильно кодируется:

```java
assertEquals(
    "https://api4.rhserv.vu/search/%D0%BC%D0%B0%D1%82%D1%80%D0%B8%D1%86%D0%B0",
    Encoder.encode("матрица")
);
```

Если вы измените базовый URL в `Encoder`, этот тест нужно обновить.

### `PreparerTest.java`

Проверяет, что из словаря фильма достаются `id`, `title`, `poster`, а `null`
обрабатывается без падения.

### Как запустить

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Отчёт:

```text
app/build/reports/tests/testDebugUnitTest/index.html
```

---

## 13. Сборка и запуск

### Требования

- JDK 17;
- Android SDK с платформой API 36;
- Android Studio (желательно) или командная строка.

В проекте уже лежит портативный JDK:

```text
.toolchain/jdk17/jdk-17.0.21+9
```

### Сборка через скрипт

```powershell
.\build.ps1
```

Готовый APK:

```text
app\build\outputs\apk\debug\app-debug.apk
```

### Сборка через Gradle

```powershell
.\gradlew.bat :app:assembleDebug
```

### Сборка в Android Studio

1. Откройте каталог `D:\Projects\AndroidAPP` как проект.
2. Дождитесь Gradle Sync.
3. Нажмите **Run** (зелёный треугольник).

### Установка на устройство

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

`-r` означает «переустановить, сохранив данные».

### Debug и release

- **Debug APK** — подписан отладочным ключом, ставится на устройство, удобен
  для разработки.
- **Release APK** — нужен для публикации. Его нужно отдельно подписывать
  keystore-ом. Сейчас release-подпись не настроена.

---

## 14. Как отлаживать

### Logcat

В Android Studio откройте **Logcat** и отфильтруйте по:

```text
package:com.example.onlinecinema
```

Ищите строки `E/` (error) и stack trace исключений. В `MainActivity` есть
`e.printStackTrace()`, поэтому ошибки парсера будут видны.

### Точки останова

Поставьте breakpoint:

- в `MainActivity.onQueryTextSubmit` — посмотреть введённый запрос;
- после `Parser.parse(...)` — посмотреть, сколько фильмов пришло;
- в `FilmAdapter.onBindViewHolder` — посмотреть, какие данные попали в карточку;
- в `PlayActivity.onCreate` — проверить `movieId`.

### Проверка API вручную

Kinopoisk-запрос можно проверить в браузере или через `curl`:

```powershell
curl.exe -H "X-API-KEY: 8c8e1a50-6322-4135-8875-5d40a5420d86" ^
  "https://kinopoiskapiunofficial.tech/api/v2.1/films/search-by-keyword?keyword=matrix&page=1"
```

Если API отвечает 401 — ключ неверный или закончился.
Если 403 — доступ ограничен.
Если ответ пустой — попробуйте другой запрос.

### Частые проблемы

| Симптом | Вероятная причина | Что делать |
|---|---|---|
| `Unable to resolve host` | Нет Интернета или DNS. | Проверить сеть на устройстве/эмуляторе. |
| `HTTP 401` | Неверный/просроченный API-ключ. | Заменить `KINOPOISK_API_KEY`. |
| `HTTP 403` | API заблокировал запрос. | Проверить ключ, лимиты, регион. |
| Список пуст, Toast «Ничего не найдено» | Запрос не дал результатов. | Попробовать другое название. |
| Постеры серые | Нет доступа к картинкам или URL битый. | Проверить Интернет, открыть URL в браузере. |
| Приложение падает сразу | Не найден `findViewById`, ошибка в XML/теме. | Смотреть Logcat, проверить id и тему. |
| Кнопка «Назад» не работает | Старый `onBackPressed` на Android 16. | Использовать `OnBackPressedDispatcher` (уже сделано). |
| Клавиатура не показывает «Поиск» | Нет `imeOptions="actionSearch"`. | Проверить `SearchView` в XML. |
| После поворота экрана список пропал | Activity пересоздалась, состояние не сохранено. | Это известное ограничение; см. раздел 16. |

---

## 15. Как вносить типовые изменения

### Изменить название приложения

`res/values/strings.xml`:

```xml
<string name="app_name">Новое название</string>
```

Название на главном экране и в лаунчере обновится.

### Изменить цвета

`res/values/colors.xml`. Например, заменить `cinema_red` на другой акцент.

### Изменить набор фильтров/полей карточки

1. `Film.java` — добавить поле.
2. `Preparer.java` — достать поле из Map.
3. `film_item.xml` — добавить View.
4. `FilmAdapter.java` — найти View в `ViewHolder` и заполнить в `onBindViewHolder`.

### Поменять API

Нужно обновить:

1. `Encoder.java` — базовый URL.
2. `Parser.java` — `MANUAL_SEARCH_URL`, логику разбора.
3. `MainActivity.java` — если изменится формат данных.
4. Тесты — если меняется `Encoder`.

Лучше вынести адреса в отдельный класс конфигурации, но в учебном проекте
оставлено просто.

### Поменять ключ Kinopoisk

`Parser.java`:

```java
private static final String KINOPOISK_API_KEY = "...";
```

Для реального проекта ключ не должен лежать в исходниках: лучше получать его
с вашего backend или хранить в защищённой конфигурации.

### Поменять экран плеера

`PlayActivity.java`:

- начальный URL;
- `loadFallbackPage`;
- настройки `WebSettings`.

### Изменить минимальную версию Android

`app/build.gradle.kts`:

```kotlin
minSdk = 28
```

Но перед понижением проверьте, что используемые API доступны на новой версии.

### Переименовать пакет

Лучше делать через Android Studio: **Refactor → Rename**. Нужно обновить:

- `namespace` и `applicationId` в `app/build.gradle.kts`;
- путь папки `java/com/example/onlinecinema`;
- `package` в каждом Java-файле;
- импорты, если они были.

---

## 16. Известные ограничения и что можно улучшить

Это не ошибки, а осознанные упрощения учебного проекта.

1. **Нет пагинации.** Kinopoisk-запрос всегда просит `page=1`. Если фильмов
   много, показываются только первые 20.
2. **Нет индикатора загрузки на главном экране.** Пользователь не видит, что
   поиск идёт; просто через некоторое время появляется список.
3. **Нет сохранения состояния.** При повороте экрана Activity пересоздаётся,
   и результаты поиска теряются. Можно сохранять запрос и список в
   `onSaveInstanceState` или использовать `ViewModel`.
4. **Поиск только по кнопке.** `onQueryTextChange` пустой. Можно добавить
   debounce и поиск по мере ввода.
5. **`notifyDataSetChanged()` перерисовывает весь список.** Для больших
   списков лучше `DiffUtil`.
6. **API-ключ в коде.** Для production нужен backend/proxy и хранение секретов
   вне репозитория.
7. **Плеер зависит от внешнего сайта.** Если сайт закрыт или заблокирован,
   приложение откроет страницу Кинопоиска, но не сам просмотр.
8. **Нет release-подписи.** Debug APK нельзя опубликовать в магазине.
9. **Только русские тексты.** Для локализации нужно добавить
   `values-en/strings.xml` и переводить строки.
10. **Длинные названия могут обрезаться.** В карточке `maxLines=4` и
    `ellipsize=end`; при желании можно увеличить карточку или уменьшить шрифт.
11. **Нет обработки повторных быстрых нажатий.** Несколько поисков встанут в
    очередь `singleThreadExecutor` и выполнятся по очереди.
12. **Нет тестов на `Parser`.** Сетевой код можно тестировать через
    MockWebServer, но это уже следующий уровень.

---

## 17. Чек-лист перед передачей проекта

- [ ] Установлен JDK 17.
- [ ] Установлен Android SDK с платформой API 36.
- [ ] `local.properties` содержит правильный путь к SDK.
- [ ] Проект открывается в Android Studio и Gradle Sync проходит.
- [ ] `.\build.ps1` завершается с `BUILD SUCCESSFUL`.
- [ ] `.\gradlew.bat :app:testDebugUnitTest` проходит.
- [ ] `.\gradlew.bat :app:lintDebug` не содержит ошибок.
- [ ] APK устанавливается на устройство/эмулятор.
- [ ] Поиск возвращает результаты (или понятную ошибку).
- [ ] Постеры загружаются.
- [ ] Нажатие на карточку открывает `PlayActivity`.
- [ ] Кнопка «Назад» возвращает из WebView или закрывает экран.
- [ ] API-ключ заменён, если демонстрационный лимит исчерпан.
- [ ] Проверены актуальность адресов API и плеера.

---

## 18. Мини-глоссарий

**Activity** — экран приложения. В проекте две Activity.

**Layout** — XML-файл с описанием экрана.

**View** — любой элемент интерфейса: текст, картинка, кнопка, список.

**ViewGroup** — контейнер для других View (`LinearLayout`, `RelativeLayout`,
`ConstraintLayout`).

**`findViewById`** — поиск View по id.

**`R.id.*`** — сгенерированный id из XML.

**`R.layout.*`** — сгенерированная ссылка на XML-разметку.

**`R.string.*`** — сгенерированная ссылка на строку.

**Inflate** — превратить XML в реальные объекты View.

**Intent** — запрос на действие, например «открой PlayActivity и передай id».

**`putExtra` / `getStringExtra`** — положить/достать данные из Intent.

**Adapter** — поставщик данных и карточек для RecyclerView.

**ViewHolder** — кэш ссылок на View внутри одной карточки.

**`notifyDataSetChanged`** — сообщить списку, что данные изменились.

**Main thread** — поток интерфейса.

**Background thread** — фоновый поток для сети/тяжёлых операций.

**`ExecutorService`** — пул фоновых потоков.

**`Handler`** — механизм отправить задачу в другой поток.

**`Looper`** — цикл обработки сообщений потока.

**`WebView`** — встроенный браузер.

**`WebViewClient`** — обработчик событий WebView (загрузка, ошибки).

**`WebChromeClient`** — обработчик JavaScript-диалогов и видео.

**JSON** — текстовый формат обмена данными.

**Gson** — библиотека JSON ↔ Java.

**Glide** — библиотека загрузки картинок.

**API** — внешний сервис.

**URL encoding** — кодирование символов для безопасной передачи в URL.

**`dp`** — density-independent pixels, размеры элементов.

**`sp`** — scale-independent pixels, размеры текста.

**Gradle** — система сборки.

**Gradle Wrapper** — способ запускать нужную версию Gradle без ручной установки.

**AGP** — Android Gradle Plugin, плагин сборки Android.

**APK** — установочный пакет Android.

---

## 19. Быстрый ответ на вопрос «с чего начать читать код»

1. Откройте `MainActivity.java` — это точка входа.
2. Найдите `onCreate` — там создаётся экран.
3. Найдите `onQueryTextSubmit` — там начинается поиск.
4. Перейдите по вызову в `Encoder.encode`.
5. Затем в `Parser.parse`.
6. Затем в `Preparer.prepare`.
7. Вернитесь в `MainActivity` и посмотрите, как создаются `Film`.
8. Откройте `FilmAdapter` — там карточки.
9. Откройте `PlayActivity` — там второй экран.
10. Дальше смотрите XML-файлы и ресурсы.

Такой маршрут повторяет реальный путь данных и позволяет быстро понять проект
целиком.

---

## 20. Короткое резюме

- Приложение состоит из двух экранов: поиск и просмотр.
- Главный экран: `MainActivity` + `activity_main.xml`.
- Список: `RecyclerView` + `FilmAdapter` + `film_item.xml`.
- Данные: `Encoder` → `Parser` → `Preparer` → `Film`.
- Сеть: `HttpURLConnection`, JSON через Gson.
- Картинки: Glide.
- Второй экран: `PlayActivity` + `activity_play.xml` + `WebView`.
- Фоновая работа: `ExecutorService`, возврат в UI через `Handler`.
- Основной API из методички умер, поэтому есть резервный Kinopoisk API.
- Сборка: `.\build.ps1`, APK в `app/build/outputs/apk/debug/`.
- Главное, что нужно помнить: **UI не ходит в сеть напрямую; сеть не трогает
  UI напрямую**. Между ними — `ExecutorService` и `Handler`.
