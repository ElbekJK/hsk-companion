# HSK Companion 1.1 · Offline Azure Mandarin audio

## What this release actually provides

- Android automatically prefers packaged **MP3 recordings** to device TTS for words, sentence examples, grammar models and two-speaker dialogues.
- Female voice A: Azure `zh-CN-XiaoxiaoNeural`. Male voice B: `zh-CN-YunxiNeural`.
- Two in-app speaker preview buttons under Settings → Offline Azure voices.
- Playback is offline on Android. A network connection **is required only during audio generation on Debian**. Azure keys are **never included** in Android code or the APK.
- If no MP3 is packaged, Android TTS is used as the fallback. No blank soundtracks or deceptive AI audio are shipped in this ZIP.
- Audio-first dialogue MCQs remain locked until the two lines have completed; an alternate reading mode is available.

## Start on Debian

Install `ffmpeg` (provides `ffprobe`) before generating audio:

```bash
sudo apt update
sudo apt install -y python3 ffmpeg
cd ~/hsk-companion-v110/HSKCompanion
bash scripts/fetch_vocab.sh          # optional, downloads classic HSK 1–2 words
bash prepare_azure_audio.sh --dry-run # plan, count and estimate; no API key needed
```

Create your own **Azure Speech resource** in the Azure portal. Choose a supported region, then copy its **key** and **region identifier** from *Keys and Endpoint*. Check that the resource's pricing tier and quota are what you expect. For security, never paste your key into ChatGPT, Git, the project ZIP, an Android app, a web link, or a screenshot. Enter it locally in your Debian terminal:

```bash
read -r -p 'Azure Speech region (e.g. eastus): ' AZURE_SPEECH_REGION
read -rs -p 'Azure Speech API key (hidden): ' AZURE_SPEECH_KEY; echo
export AZURE_SPEECH_REGION AZURE_SPEECH_KEY
bash prepare_azure_audio.sh --preview
```

The preview option generates **six recordings**: two distinct speaker voice previews and the two lines of each of the first two sample dialogues. It writes into `app/src/main/assets/audio/`, then generates a checksum/duration manifest and human-review CSV. Preview the recordings on Debian, or build and listen to them inside HSK Companion:

```bash
find app/src/main/assets/audio/a -name '*.mp3' -print -quit | xargs -r ffplay -autoexit -nodisp
# Run the source/test suite and build the APK (install in the emulator):
bash scripts/test_project.sh
bash run_desktop.sh --offline
```

To create the complete audio set (reuses existing recordings and preserves already approved reviews when checksums match):

```bash
bash prepare_azure_audio.sh --all
python3 scripts/audit_audio.py --require-complete
bash build_phone_apk.sh --offline
```

The resulting private-test APK should be in `~/Downloads/HSK_Companion_v1.1.0.apk`. The APK is built **only on your Debian PC**, not in this release archive. Make sure this terminal is the same login that previously signed your existing debug APK; do not uninstall the existing phone app or you may lose learning progress.

### What the commands do and don't do

`--preview` and `--all` make actual paid-or-quota Azure requests; `--dry-run` does not. A free-tier account may cover all content, but the script cannot verify your Azure subscription, quota, billing or terms. It prints the estimated number of billable character units before making any call. `--limit N` optionally restricts the number of *new* API calls while experimenting. Synthesis can be resumed without regenerating already valid files.

## Quality checks (important for language teaching)

After synthesis, review `app/src/main/assets/audio/audio_review.csv`. Each row contains a script, voice, file name, SHA-256 hash, and a `review_status` (`pending` by default). Listen to every file, ideally with a native Mandarin speaker, and change status to `approved` only after confirming:

1. **Exact wording:** the recording matches the Chinese text; no missing or extra words.
2. **Lexical tones:** verify 1–4 and neutral tones, including third-tone sandhi, `一` / `不` sandhi, and polyphonic characters.
3. **Speech pacing:** natural, HSK 1–2 appropriate, no clipping, abnormal silence or wrong Mandarin accent.
4. **Speaker identity:** A is the female prebuilt voice, B the male prebuilt voice; verify dialogue order and natural pauses.
5. **Pedagogy:** listening questions can actually be answered from the audio alone, before showing characters/transcripts.

Run automatic integrity checks:

```bash
python3 scripts/audit_audio.py --inspect
python3 scripts/audit_audio.py --require-complete
```

Checks include MP3 decoding metadata via `ffprobe`, duration range, content-hash integrity and full source-content coverage. They **cannot** certify linguistic accuracy; a human must do that. If a generated MP3 changes, previous human approval is invalidated automatically on the next `--all` manifest refresh.

## Licensing and disclosure before Google Play

This project intentionally **does not assert that generated speech is licensed for redistribution**. Microsoft documents how prebuilt neural voices can be used in customer applications, but your Azure agreement, any subscription terms, voice restrictions, and your publishing territory must be checked for the specific use. Record a release-approval note in `app/src/main/assets/audio/REDISTRIBUTION_RIGHTS_CONFIRMED.txt` containing the literal `reviewed-and-confirmed`, the date, the Azure service/resource tier, and the reviewed terms or supporting agreement. This note should only be created when you have performed the real review; it is not a license by itself.

To block public distribution unless all conditions pass:

```bash
bash scripts/check_publishable_audio.sh
```

Microsoft recommends disclosing that speech is AI generated. The app identifies the bundled voices as synthesized audio in Settings and the source documents. Neither the recordings nor the app impersonate a real speaker. All dialogue scripts in this project are original HSK teaching content. For authoritative guidance, see:

- Microsoft Azure Speech REST: https://learn.microsoft.com/en-us/azure/ai-services/speech-service/rest-text-to-speech
- Azure voice catalog: https://learn.microsoft.com/en-us/azure/ai-services/speech-service/language-support?tabs=tts
- Current pricing: https://azure.microsoft.com/en-us/pricing/details/speech/
- Microsoft synthetic voice disclosure: https://learn.microsoft.com/en-us/azure/foundry/responsible-ai/speech-service/text-to-speech/concepts-disclosure-guidelines
- Azure prebuilt voice responsible use: https://learn.microsoft.com/en-us/azure/foundry/responsible-ai/speech-service/text-to-speech/transparency-note

## Security / data persistence

The speech generator executes only on Debian using your local environment variables. Its API key is passed as a private HTTP header to Microsoft's Azure Speech endpoint, never written to a file. The app's package identifier is `com.example.hskcompanion` and progress remains in the existing `hsk_progress_v1` storage. The audio pack only adds read-only Android assets; it never migrates or clears the learner database. Back up progress using the existing Settings → Export progress backup before changing phones, keystores or installing beta versions.

## v1.2 upgrade without re-generating Azure audio

If the v1.1 generator is running, **do not stop it**. After all 758 source recordings are complete, extract the v1.2 ZIP into `~/hsk-companion-v120/HSKCompanion`, then from that folder run:

```bash
python3 scripts/import_audio_from_v110.py ~/hsk-companion-v110/HSKCompanion --dry-run
python3 scripts/import_audio_from_v110.py ~/hsk-companion-v110/HSKCompanion
python3 scripts/audit_audio.py --require-complete
bash build_phone_apk.sh --offline
```

This copies audio **rather than synthesizing again**. Your v1.1 download had 297 words and 758 planned recordings; v1.2's raw starter contains 293 until the vocabulary is synchronized. The import handles this by copying the matching downloaded vocabulary too. The Azure key is never copied or required for importing. The Android app does not contain the key or make any Azure calls.
