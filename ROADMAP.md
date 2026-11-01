# SubGenX — phased workflow plan

The project is not considered complete just because the APK compiles. Work is split into testable phases. Do not start unrelated features while the active phase is incomplete.

## Phase 1 — Make the current pipeline observable and results retrievable
- [x] Show explicit stages: prepare audio, transcribe, translate, complete/fail.
- [x] Tell the user which translation provider/model is selected for the current run.
- [x] Enable saving the completed transcript and translation as TXT via Android's save picker.
- [ ] Verify on-device: success state, failure state, TXT file contents, and selected save location.
- [ ] Ensure the API test button label follows the selected provider.

Exit criteria: user can tell which step is running, see whether it succeeded, and save/reopen the result.

## Phase 2 — Reliable transcription output
- [ ] Validate audio input and MIME types.
- [ ] Handle provider/API errors with useful messages (401/403, quota/rate limit, unsupported model, timeout, empty response).
- [ ] Keep original transcript separate from translation.
- [ ] Verify that transcription output is non-empty and correctly displayed.
- [ ] Do not claim timed subtitles until timing data is actually returned and parsed.

Exit criteria: supported audio can be transcribed reliably; failures are actionable and never presented as success.

## Phase 3 — Real subtitle timing and SRT
- [ ] Parse word/segment timestamps from a provider that actually returns them.
- [ ] Build valid SRT cues with sequential numbering and HH:MM:SS,mmm timestamps.
- [ ] Provide subtitle preview/editing.
- [ ] Export SRT using Android's save picker.
- [ ] Validate exported SRT by reopening it and checking cue timing.

Exit criteria: generated SRT contains valid timestamps and can be used with a video player.

## Phase 4 — Provider/model controls
- [ ] Test Gemini and OpenRouter independently.
- [ ] Test selected OpenRouter model with a real translation request, not only /models.
- [ ] Show provider, model, and connection status separately.
- [ ] Do not silently switch providers or incur paid usage.
- [ ] Explain that a saved key is not proof of a valid key or usable model.

Exit criteria: user can confirm exactly which provider/model is being used and why a request failed.

## Phase 5 — End-to-end acceptance test
Test at least one short audio file and one longer supported file:
1. Select file.
2. Transcribe.
3. Translate using the chosen provider/model.
4. Review/edit.
5. Export SRT and TXT.
6. Reopen both saved files.
7. Test invalid key, quota/rate limit, no network, unsupported audio, and retry.

Only after a phase's exit criteria are met should it be marked complete. A successful Gradle build is a build check, not proof that the workflow works.
