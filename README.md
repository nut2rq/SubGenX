# SubGenX

**Simple UI, Smart AI** — an Android-first audio transcription and subtitle translation studio.

## Product goals

- Import audio files (MP3, WAV, FLAC).
- Detect the source language automatically where supported.
- Transcribe speech before translation, keeping original text intact.
- Translate into a user-selected target language.
- Preserve subtitle timestamps and allow users to edit text and timing.
- Export original, translated, or bilingual subtitles as SRT.
- Keep the normal workflow simple: **Choose audio → Choose target language → Start → Review → Export SRT**.
- Prefer on-device processing where practical; any online translation option must be clearly disclosed.

## UX principles

- Few visible choices; model and engine selection stay automatic by default.
- Clear progress and honest status messages; never fake transcription percentage.
- Original and translated text are shown separately.
- Advanced settings stay out of the main workflow.

## Current build status

- Native Android Kotlin project and basic audio-file selection UI are in place.
- GitHub Actions builds a debug APK and publishes it as the `SubGenX-debug-apk` artifact.
- **Important:** audio transcription, translation, subtitle review/editing, and SRT export are not implemented yet. The current APK tests the initial app shell and file-selection flow only.
- A successful CI build does not prove transcription quality or runtime behavior on a device.

## Planned milestones

1. Establish an Android build baseline and minimal, accessible UI. (Current)
2. Integrate and benchmark transcription engines on representative audio.
3. Add translation as a separate stage, preserving original text and timestamps.
4. Implement robust subtitle editing and SRT round-trip tests.
5. Add automated tests for the subtitle pipeline.
6. Validate speed, memory use, thermal behavior, and output quality on real devices.
