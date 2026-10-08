# MyGandara

Citizen reporting + LGU bulletin board app for Gandara, Samar (similar to MyNaga).

## Stack

- Kotlin + Jetpack Compose (Material 3), Navigation Compose
- Supabase via `supabase-kt`: Auth, Postgrest, Storage, Realtime
- Ktor client engine for Android
- Package: `com.pikacheat.mygandara`, minSdk 24, targetSdk 36, AGP 9.2.1, Kotlin 2.2.10
- Dependencies go in `gradle/libs.versions.toml`, not as inline strings in `app/build.gradle.kts`

## Roles

| Role | Powers |
|---|---|
| `citizen` | Default on signup. Submits reports, reads the bulletin. |
| `staff` | Manages reports, updates their status. |
| `admin` | Staff powers + posts to the bulletin + promotes users. |

- Role lives in a `profiles` table linked to `auth.users` and is enforced with Row Level Security.
- Users never choose their role at signup. A database trigger on `auth.users` insert creates the profile with role `'citizen'`.
- The client UI may hide/show things by role, but RLS is the real enforcement. Never rely on the app alone.

## Data model

Tables:
- `profiles` — id (= auth.users.id), name, role, etc.
- `reports` — citizen-submitted issues (category, description, status, photo, GPS)
- `report_updates` — status timeline entries for a report
- `posts` — bulletin items; `type` is one of `announcement`, `ordinance`, `event`, `emergency`

Storage buckets: `report-photos`, `post-attachments`.

Keep SQL (schema, triggers, RLS policies) in version-controlled files under `supabase/` so it can be reviewed and re-applied.

## Secrets

- Supabase URL and **anon** key go in `local.properties` (gitignored) and are read into `BuildConfig` at build time. Never hardcode or commit them.
- The service role key / JWT secret / DB password must never be in the Android app or this repo. They belong only in Supabase Edge Function secrets or server-side environments.

## Build order

Work one step at a time and make sure the project builds (`./gradlew assembleDebug`) after each step.

1. Supabase setup + SQL schema + ViewModel/repository layer
2. Auth + role-based navigation
3. Real reports with photo + GPS
4. Bulletin board posting
5. Staff tools + admin user management
6. Push notifications (FCM triggered from a Supabase Edge Function) + polish — **push postponed by the user (budget); do not implement until asked**
7. Review RLS policies + privacy notice

## Current state

Steps 1–5 and 7 are implemented in code; step 6 push notifications are skipped. Builds with `./gradlew assembleDebug`
(needs `JAVA_HOME` = Android Studio's `jbr`; compileSdk 37, Kotlin 2.4.21, supabase-kt 3.8.0). Not yet tested against a
live Supabase project.

- SQL: `supabase/migrations/0001_init.sql`, `0002_profile_email_and_hardening.sql`, `0003_cancel_and_reference_numbers.sql`; checks in `supabase/tests/rls_checks.sql`;
  setup + security model in `supabase/README.md`. Add new SQL as new numbered migration files, never edit applied ones.
- Status changes go through `report_updates` (trigger copies status onto `reports`). Roles change only via the admin-only
  `set_user_role()` RPC. Reports are rate-limited to 10/hour per user.
- `data/`: `SupabaseProvider` (lazy client), `AppContainer` (manual DI held by `MyGandaraApp`), DTOs in `data/model/`,
  repositories (Auth, Profile, Report, Post, Realtime).
- `ui/viewmodel/`: one ViewModel per screen, created with `appViewModelFactory`. `Loadable` + `UiStateContent` handle
  loading/error/pull-to-refresh. `SessionViewModel` drives `navigation/NavGraph.kt` (`MyGandaraRoot`): signed-out graph
  (login, sign-up, privacy) vs signed-in graph with role-based bottom tabs (`Tab.forRole`).
- Photos: `util/ImageCompressor` (resize + strip EXIF). GPS: `util/LocationHelper` (fused location, asked on tap).
- Privacy notice text in `ui/screens/privacy/PrivacyNoticeScreen.kt` still has a DPO contact placeholder.
- Branding: Gandara seal at `res/drawable-nodpi/gandara_seal.png` (launcher icon + login); colours in `ui/theme/Color.kt`
  (wreath green / gold / sky blue). Dynamic colour is off on purpose.
- UI text is English wrapped in `t("...")` (`i18n/I18n.kt`); the English text is the key into
  `i18n/FilipinoStrings.kt` and `i18n/WarayStrings.kt` (missing keys fall back to English). When adding UI text,
  wrap it in `t()` and add both translations. Templates use `%s` / `%1$s` (escape `$` in Kotlin). Shared components
  (ConfirmDialog, MessageBox, EmptyListText, ChoiceChipRow, StatusBadge, PillBadge) translate their inputs themselves.
  `TranslationsTest` checks placeholders match. The Waray table is a first draft that needs native-speaker review.
- On-device prefs (`util/LocalStore`): language, seen report versions ("Updated" dot), dismissed emergency banner.
