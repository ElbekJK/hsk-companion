# Vocabulary data provenance and licensing

## Bundled starter dataset

`app/src/main/assets/vocabulary.json` ships with an independently assembled, short-gloss beginner starter pack of **293 entries** (147 classic HSK 1 and 146 classic HSK 2). This seed was manually drafted for this example and has NOT been checked entry-for-entry against the official standard. The source is independently reusable under the project's MIT licence. It is automatically replaced with the downloaded full datasets in the recommended Debian build workflow when network access succeeds.

## Downloaded full vocabulary

`scripts/fetch_vocab.sh` downloads four TSV datasets from:

- https://nihaolearn.com/resources
- https://nihaolearn.com/licenses
- https://nihaolearn.com/downloads/hsk-2.0-level-1.tsv
- https://nihaolearn.com/downloads/hsk-2.0-level-2.tsv

Vocabulary word/level data is derived by NihaoLearn from Complete HSK Vocabulary by Yanis Zafirópulos and the HSK 2.0 standard. The word-list dataset is made available under the MIT licence, with permission notice at https://nihaolearn.com/licenses (and upstream https://github.com/drkameleon/complete-hsk-vocabulary/blob/main/LICENSE).

English definitions are adapted from the CC-CEDICT project (https://www.mdbg.net/chinese/dictionary?page=cc-cedict), licensed under the **Creative Commons Attribution-ShareAlike 4.0 International** licence (https://creativecommons.org/licenses/by-sa/4.0/). Copyright belongs to the original contributors. Changes in this app: the downloadable TSV columns are transformed into JSON, the part-of-speech column is omitted, and records are organized by HSK version and level. These redistributed adapted English definitions remain subject to CC BY-SA 4.0; the word/pinyin/level fields are covered by the separate MIT vocabulary notice. No CC-CEDICT audio is distributed.

Original NihaoLearn data attribution: **NihaoLearn — Free HSK word lists** (https://nihaolearn.com/resources). Original definitions attribution: **CC-CEDICT contributors / MDBG** (https://www.mdbg.net/chinese/dictionary?page=cc-cedict). Modified structure by HSK Companion, November 2026.

For the full third-party licence texts and upstream copyright attributions, read https://nihaolearn.com/licenses . Redistribution of the derived vocabulary files must preserve the attribution, licence notice, links, and share-alike conditions as applicable. The MIT licence for the application itself does not supersede these obligations.

No proprietary NihaoLearn user interface, original lessons, audio recordings, artwork, or grammar material has been copied.

## v0.6 original dialogue exercise text

The ten two-person sample dialogues in `app/src/main/assets/dialogues.json` were authored for this prototype. They are fictional study prompts; neither their script nor the Android TTS playback should be described as original field recordings or native-speaker interviews. No third-party video or audio is bundled.
