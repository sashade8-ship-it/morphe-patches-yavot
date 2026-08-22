# YaVoT

YaVoT is an independent, unofficial Yandex voice-over translation add-on that is compatible with [Morphe Patches](https://github.com/MorpheApp/morphe-patches). It is not a Morphe product and must be loaded alongside an official Morphe bundle.

Version `1.1.0` is built and tested against the immutable official Morphe Patches `v1.40.0-dev.1` commit `0b2ac378feb6b64fb7f1125abdccb0ff7d9125ff`; see [COMPATIBILITY.md](COMPATIBILITY.md).

This bundle ships the `Voice Over Translation (Yandex)` patch for YouTube. It is meant to be loaded **alongside** the official base bundle in Morphe Manager, not as a replacement. All classes, resources, preference keys and extension descriptors are renamed with a `yandex_vot` / `YandexVot*` prefix so nothing collides with the built-in Google-TTS-based VoT patch.

## Install

### Morphe Manager

1. Install/keep the base [morphe-patches](https://github.com/MorpheApp/morphe-patches) bundle in [Morphe Manager](https://github.com/MorpheApp/morphe-manager).
2. Add this bundle as an additional source in Morphe Manager (`Patch sources → Add`).
3. When patching YouTube, both patches are available:
   - `Voice Over Translation` (base, Google/OpenRouter/MyMemory backends)
   - `Voice Over Translation (Yandex)` (this bundle)
   
### Morphe Desktop
Patch with two mpp bundles: Morphe official bundle (morphe.mpp) and this yavot addon bundle (yavot.mpp):

`java -Xms1024m -jar morphe-desktop*-all.jar patch --patches morphe.mpp --patches yavot.mpp youtube_original.apk --out youtube_patched.apk`

## What is renamed vs upstream yavot

Everything that would get baked into the patched YouTube APK uses a distinct namespace:

- Kotlin patch package: `app.morphe.patches.youtube.video.yandexvot`
- Extension package: `app.morphe.extension.youtube.patches.yandexvot`
- Player button class: `YandexVotButton` (was `VoiceOverTranslationButton`)
- OAuth preference class: `YandexVotOAuthPreference`
- Settings constants: `YANDEX_VOT_ENABLED`, `YANDEX_VOT_SOURCE_LANGUAGE`, ...
- SharedPreferences keys: `morphe_yandex_vot_*` (was `morphe_vot_*`)
- Drawables: `morphe_yt_yandex_vot(_activated).xml`
- Settings sit directly next to the built in `Voice over translation` entry on the `Video` screen, under the key `morphe_vot_screen_yandex`, and are titled the same with `(Yandex)` appended.

## How the add-on attaches to the base bundle

Morphe Manager loads every patch bundle in its own class loader, so this bundle cannot reference
any patch of morphe-patches. Everything goes through the patched app instead, using the hooks the
`Add-on support` patch of morphe-patches provides:

- The patch adds a call to `YandexVotAddOn.register()` to `AddOnManager.registerAddOns()` of the
  base extension, in a finalize block.
- `register()` subscribes to `AddOnApi`: player overlay buttons, legacy player controls, new video,
  video id and video time.
- The player button uses `PlayerOverlayButton.addButton()`, and in the old player layout one of the
  legacy button slots the base bundle reserves for add-ons.
- Preferences are declared in `morphe_addon_prefs.xml` with `after="morphe_vot_screen"`, so the
  settings patch places them right next to the built in voice over translation entry. Strings,
  arrays and drawables are written into the app resources by this bundle itself.

Only the `AudioTrack.setVolume` hook that ducks the original audio is patched directly, using a
fingerprint of this bundle.

## Compatibility gate

YaVoT `1.1.0` is compatible with official Morphe Patches `v1.40.0-dev.1` at immutable commit `0b2ac378feb6b64fb7f1125abdccb0ff7d9125ff`. Before it changes an APK,
the patch verifies the exact public static `AddOnManager`/generic `AddOnApi` hooks it uses and the
official `VoiceOverTranslationPatch` callbacks it invokes. It fails closed before adding YaVoT
registration when any required signature is absent.

YaVoT coordinates directly with the official translation implementation: immediately before
Yandex starts it deactivates the official session, and the one-time official state callback cancels
Yandex when the official session becomes active. This requires no custom AddOnApi version or
engine-owner API.

## Build

Requires a sibling checkout of `morphe-patcher`, `morphe-patches-library`, and — for the extension
code only — `morphe-patches`:

```
StudioProjects/
├── morphe-patcher
├── morphe-patches
├── morphe-patches-library
└── morphe-patches-yavot  ← this repo
```

The patch code (`patches/`) depends on `morphe-patcher` and `morphe-patches-library` only. The
extension code compiles against the compiled extension classes of morphe-patches with
`compileOnly`, since all extensions end up in the same patched app. Build the base extension first:

```
../morphe-patches/gradlew :extensions:youtube:compileReleaseKotlin :extensions:youtube:compileReleaseJavaWithJavac

# Or use another built compatible host checkout without changing this repository:
./gradlew :extensions:youtube:compileReleaseJavaWithJavac -PbaseExtensionsDir=../morphe-official-1.40-test/extensions
```

## What is bundled

- 1 Kotlin patch plus its add-on support helpers (`YandexVoiceOverTranslationPatch.kt`, `AddOn.kt`)
- 9 Java extension classes (`YandexVot*`, including the add-on entry point `YandexVotAddOn`)
- `YandexVotSettings.java` (9 setting fields, isolated from base's `Settings.java`)
- `yandexvotbutton/` drawables
- Filtered `strings.xml` and `arrays.xml` with only `morphe_yandex_vot_*` keys (en + ru + uk)

## Credits

Yandex VoT implementation:
- [MarcaDian](https://github.com/MarcaDian) — original YaVoT add-on repository and v1.0.4 baseline
- [Jav1x](https://github.com/Jav1x) — original author of the patch, Morphe port
- [anddea](https://github.com/anddea) — revanced-patches port
- Dual VoT maintainers — timing v3, diagnostic hardening, and official/YaVoT coordination adaptations
- YaVoT maintainers: [sashade8-ship-it](https://github.com/sashade8-ship-it)

Compatibility dependencies: [Morphe Patches](https://github.com/MorpheApp/morphe-patches), [Morphe Manager](https://github.com/MorpheApp/morphe-manager), [Morphe Patcher](https://github.com/MorpheApp/morphe-patcher).

## Release and source distribution

Each YaVoT release contains a uniquely named `patches-<version>.mpp` and a matching `patches-<version>.mpp.sha256`. The checksum file is generated from that release MPP immediately after its clean build and can be verified with `sha256sum --check patches-<version>.mpp.sha256`. The MPP itself includes `META-INF/LICENSE` and `META-INF/NOTICE`; the same files and complete corresponding source are available in this repository. Preserve the notices when redistributing YaVoT.

## Patches

<!-- PATCHES_START EXPANDED -->
> **[v1.1.3](https://github.com/sashade8-ship-it/morphe-patches-yavot/releases/tag/v1.1.3)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;1 patches total
<details open>
<summary>📦 YouTube&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Voice Over Translation (Yandex)](#voice-over-translation-yandex) | Adds an option to enable Yandex voice-over translation of video audio tracks. Requires a Morphe Patches version with add-on support. |  |

</details>

<!-- PATCHES_END -->

## License

GNU General Public License v3.0
