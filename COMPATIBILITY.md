# YaVoT compatibility

This add-on targets official Morphe Patches `v1.40.0-dev.1`
(`0b2ac378feb6b64fb7f1125abdccb0ff7d9125ff`). It does not require an unpublished AddOnApi
version or a custom engine coordinator.

Before it inserts YaVoT registration, the patch checks for exact public static methods:

- `AddOnManager.registerAddOns(): void`;
- the AddOnApi hooks it calls: player-overlay, legacy-control, new-video, video-id, video-time,
  and video-state listener registration;
- `VoiceOverTranslationPatch.addOnTranslationStateChangeCallback(Runnable): void`;
- `VoiceOverTranslationPatch.isSessionEnabled(): boolean`;
- `VoiceOverTranslationPatch.deactivateTranslation(): void`;
- the public static player-overlay button factory YaVoT invokes.

If a method is absent or not public static, patching aborts before YaVoT registration is inserted.
At runtime YaVoT directly deactivates the official translation before Yandex starts; the official
state callback cancels Yandex if the official session becomes active. No URLs, video identifiers,
credentials, or network responses are logged by this coordination layer.
