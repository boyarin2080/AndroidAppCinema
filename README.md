# Онлайн Кино

Android-приложение «Онлайн Кино», собранное по учебно-методическому пособию
«Разработка мобильных приложений» (ТИ НИЯУ МИФИ).

## Что умеет приложение

- Поиск фильмов по названию через `SearchView`.
- Загрузка результатов в `RecyclerView` с постерами (Glide).
- Переход на экран просмотра `PlayActivity` с `WebView` при нажатии на фильм.
- Поиск выполняется в фоновом потоке, интерфейс не блокируется.
- Обрабатываются пустой запрос, отсутствие результатов и сетевые ошибки.

## Документация

Подробный разбор архитектуры, каждого файла и типовых задач —
в [`guide.md`](guide.md). Этот README — только краткая шпаргалка по сборке.

## Соответствие инструкции

| Элемент из пособия | Реализация |
|---|---|
| Шаблон Empty Views Activity, Java, minSdk 28 | `app/build.gradle.kts`, `MainActivity.java` |
| `Encoder.java` | кодирование запроса через `URLEncoder` |
| `Parser.java` | `HttpURLConnection` + Gson |
| `Preparer.java` | выборка `id`, `title`, `poster` |
| `Film.java` | модель фильма |
| `FilmAdapter.java` | RecyclerView + Glide + Intent на `PlayActivity` |
| `film_item.xml` | `RelativeLayout` с `ImageView` и `TextView` |
| `activity_main.xml` | заголовок, `SearchView`, `RecyclerView` |
| `activity_play.xml` | горизонтальный `LinearLayout` с `WebView` |
| `MainActivity.java` | `ExecutorService`, `Handler`, поиск в фоне |
| `AndroidManifest.xml` | разрешение `android.permission.INTERNET` |

## Важное примечание об источниках данных

В пособии используется адрес `https://api4.rhserv.vu/search/`, однако на момент
сборки этот домен уже не существует (DNS возвращает NXDOMAIN). Чтобы приложение
оставалось рабочим, `Parser.java` сначала пытается обратиться к адресу из
пособия, а при ошибке автоматически переключается на открытый
Kinopoisk API Unofficial и приводит его ответ к тому же виду
(`id` / `title` / `poster`). В коде используется демонстрационный API-ключ,
опубликованный в документации Kinopoisk API; при исчерпании лимита его можно
заменить в константе `KINOPOISK_API_KEY`.

Сайт плеера из пособия (`reyohoho.github.io/reyohoho`) также закрыт. `PlayActivity`
пробует его, затем актуальный адрес каталога, а в случае неудачи открывает
страницу фильма на Кинопоиске по тому же `id`.

## Структура проекта

```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/onlinecinema/
│   ├── MainActivity.java
│   ├── PlayActivity.java
│   ├── Film.java
│   ├── FilmAdapter.java
│   ├── Encoder.java
│   ├── Parser.java
│   └── Preparer.java
└── res/
    ├── layout/activity_main.xml
    ├── layout/activity_play.xml
    ├── layout/film_item.xml
    ├── drawable/...
    └── values/...
```

## Сборка

Требования: JDK 17, Android SDK с платформой API 36.

В каталоге проекта уже лежит портативный JDK 17 (`.toolchain/jdk17`), поэтому
сборку можно запустить одной командой:

```powershell
.\build.ps1
```

Либо стандартно, если JDK 17 уже настроен:

```bash
./gradlew assembleDebug
```

Готовый APK: `app/build/outputs/apk/debug/app-debug.apk`.

Для сборки из Android Studio достаточно открыть каталог проекта и нажать
`Run`. Эмулятор или устройство должны иметь доступ в Интернет.
