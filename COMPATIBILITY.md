# YaVoT coordinator compatibility

No published upstream base currently satisfies this add-on's public coordinator contract. This
branch is therefore not compatible with stock upstream Morphe Patches.

The add-on's patch-time gate requires an encoded dex `public static final int API_VERSION = 1`
and these exact `public static` signatures:

- `registerVoiceOverEngine(String, Runnable): boolean`
- `activateVoiceOverEngine(String): boolean`
- `deactivateVoiceOverEngine(String): boolean`
- `getActiveVoiceOverEngineId(): String`
- `addVoiceOverEngineListener(Consumer): void`
- `addNewVideoStartedListener(Runnable): void`
- player-overlay, legacy-control, video-id, video-time, and video-state listener registration

It also requires `AddOnManager.registerAddOns(): void`. If any required member is absent, the
patch aborts before inserting YaVoT registration or mutating the target APK. The coordinator is
owned by the base bundle and must never be duplicated in this add-on.

`6d2cb3e30f78be25b29225d509a0e692fb2c8a07`, the current upstream AddOnApi PR head, does not
provide this coordinator contract and is intentionally rejected. Release and host-integrated
build validation are blocked until a minimal coordinator commit rebased on that head is published.
Only then should its full immutable SHA be added as the compatible host pin.
