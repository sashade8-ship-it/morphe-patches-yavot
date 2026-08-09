/*
 * Copyright (C) 2026 YaVoT maintainers (sashade8-ship-it)
 *
 * This file is part of YaVoT, an independent GPLv3 add-on compatible with
 * Morphe Patches.
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Mutual-exclusion glue between the Yandex VoT add-on and Morphe's built-in
 * voice-over translation. This file deliberately uses only the public host API.
 */

package app.morphe.extension.youtube.patches.yandexvot;

import java.util.concurrent.atomic.AtomicBoolean;

import app.morphe.extension.youtube.patches.voiceovertranslation.VoiceOverTranslationPatch;

/** Keeps the two translated audio sessions mutually exclusive without a custom AddOnApi. */
@SuppressWarnings("unused")
public final class YandexVotCoordinator {
    private static final AtomicBoolean callbackRegistered = new AtomicBoolean(false);

    /** Registers the official state callback once when the add-on is loaded. */
    public static void register() {
        if (!callbackRegistered.compareAndSet(false, true)) return;
        VoiceOverTranslationPatch.addOnTranslationStateChangeCallback(
                YandexVotCoordinator::onOfficialStateChanged);
    }

    /** Called immediately before Yandex starts its own translated-audio session. */
    static void deactivateOfficialBeforeStarting() {
        VoiceOverTranslationPatch.deactivateTranslation();
    }

    private static void onOfficialStateChanged() {
        if (VoiceOverTranslationPatch.isSessionEnabled()
                && YandexVoiceOverTranslationPatch.isTranslationActive()) {
            YandexVoiceOverTranslationPatch.cancelTranslation();
        }
    }

    private YandexVotCoordinator() {
    }
}
