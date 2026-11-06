## v0.7 changes

Multi-skill tracking separates reading, listening and production; only reading inherits older stages. New practice tasks are audio-first multiple choice; the tone lab uses HSK word pairs with matched segmental pinyin and different tones. Strict pinyin tone grading is optional. Example rotation broadens contextual encounters. Notifications are an optional gentle nudge, not evidence of learning progress.

# Pedagogy and learning limitations — HSK Companion v0.7.0

This is a research-informed **prototype**, not a clinically/experimentally validated learning intervention. The evidence supports the core practices in many language-learning settings, but does not prove this exact app increases a particular person's learning speed or retention by a specific amount.

## Improvements in v0.6.0

**Reviews before novelty.** The scheduler still uses due dates; learning sessions now reduce or pause new-word exposure when review debt accumulates (4+ due words: at most one new item; 8+ due: none). Learners can deliberately choose a pure due-only session. This is a *design heuristic* intended to reduce interference and overload, not a validated universal threshold.

**One promotion per day.** Several successful prompts about one word on the same date are practice, not evidence of memory maintained across days. Each word can advance its stage at most once per local study day. Missed retrievals still reset stage. Previously stored stages are *not* altered by the migration, so legacy progress should be read as an approximation.

**Mistake-aware prioritization.** The due pool is ordered by capped lifetime misses, lower stage and overdue interval. This simple rule does not diagnose causes of errors, guarantee speedups, or compare directly with modern probabilistic spaced repetition systems.

**Transfer in short dialogues.** Ten original, manually written two-line scenarios have single-answer comprehension questions, hidden English/pinyin until feedback, and optional TTS playback. In audio mode, tapping play unlocks the question; this is not a formal score of listening ability and can be guessed. TTS has a single device-selected voice for both lines and is not human conversational speech. Reading mode is clearly distinguished in performance tracking.

## Research translated to interactions

| Principle | Research anchor | Implemented in app |
|---|---|---|
| Distributed practice and recall | Karatas et al., *Language Teaching Research* (2025), DOI 10.1177/13621688211053525. In their classroom/laboratory trial, a mixed retrieval + feedback + spacing training arm improved vocabulary accuracy 18 percentage points from pre-test, versus 8 in the comparator. These were different adult learners and a different target language. | Scheduled 10-minute → 1/3/7/14/30/60-day reviews, no overdue count for unstudied words; words automatically return. |
| Retrieval and corrective feedback | Vujovic et al., *Frontiers in Psychology* (2021), 'Spacing, Feedback, and Testing Boost Vocabulary Learning in a Web Application', https://pmc.ncbi.nlm.nih.gov/articles/PMC8638698/ | Hide meanings before self-testing; show correct Hanzi, pinyin, English gloss and contextual example after an attempted answer. |
| Multi-aspect L2 vocabulary learning | *Studies in Second Language Acquisition* (2020), 'A Review of Laboratory Studies of Adult Second Language Vocabulary Training', DOI 10.1017/S0272263119000500 | Chinese→English retrieval; English→pinyin production; listening→meaning; reading-context cloze; aloud imitation. |
| Distributing study across days | *The Distributed Practice Effect on Classroom Learning*, systematic meta-analysis, https://pmc.ncbi.nlm.nih.gov/articles/PMC12189222/ | No cramming-driven streak punishment, adjustable 4/8-minute missions, gentle return-to-review experience. |
| Autonomy and competence | Alamer et al., 'Self-Determination Theory and Language Learning: A Multilevel Meta-Analysis', *Educational Psychology Review* (2025), https://doi.org/10.1007/s10648-025-10038-y | Choice of session length and level, visible strong-word milestones, gentle activity count instead of loss-aversion streaks, clear session endpoint. |
| Avoiding distracting gamification | 'How Duolingo users perceive the use of game elements in their learning motivation' (2025–26), https://repository.eduhk.hk/en/publications/how-duolingo-users-perceive-the-use-of-game-elements-in-their-lea/ | No coins, ads, ranking competition, failure punishment, forced notifications, or endless loops. |

### Exact review heuristic

Ratings:
- **Not yet**: stage 0; return after 10 minutes (and one retry late in the current mission).
- **Almost**: keep stage or promote new item to stage 1; next due in 8 hours for stage 1, else use previous stage interval. Optionally retry late in the current mission.
- **I recalled it**: promote one stage, then use next interval.
- **Effortless**: promote two stages (max stage 6).

Stage 0 → 10 minutes; stage 1 → 1 day; stage 2 → 3 days; stage 3 → 7 days; stage 4 → 14 days; stage 5 → 30 days; stage 6 → 60 days. The interval is a developer-chosen heuristic; it is **not** an estimate of memory stability and is not scientifically optimized per user. Missed-word retries within a session do not replace reviews in later sessions.

Lessons introduce at most 2 new words (quick) or 4 (deep) when review debt is low (one new item for 4–7 due; none for 8+ due), prioritize due reviews, then interleave listening, production, shadowing and recall. New-word retrieval follows introduction, never precedes it. Up to two cloze questions incorporate example sentences. An unsuccessful task gets one more recall attempt towards the end of the mission, not an endless penalty loop. When a review backlog is empty, the app offers familiar words for optional reinforcement rather than inventing overdue work.

### What the exercise actually assesses

- **Self-rated reveal** is dependent on honesty; it cannot verify answers objectively.
- **Listening recognition** depends on the device's TTS voice. It is not native-speaker recorded speech; without TTS it is transparently replaced by a reading task.
- **Typed pinyin** is auto-graded without tones, because this is an introductory production exercise. A separate tone-identification activity and tone-aware scoring would be required to check lexical tone knowledge.
- **Shadowing** offers a pronunciation model but does *not* evaluate speech quality, since no recognition/phoneme alignment system is included.
- **Contextual cloze** gives practice using a word in a curated sentence. Some examples contain words beyond the learner's current level and should be made more strictly comprehensible in future revisions.
- **Quizzes** are vocabulary drills rather than realistic full-length HSK examination papers.

## Roadmap for a more complete acquisition system

Priority next: corpus-audited graded dialogues that use mostly already-known words; minimal-pair tone perception and explicit tone production feedback; delayed transfer checks in addition to flashcard familiarity; item-level skill dimensions (recognition/production/listening) rather than a single word stage; and alignment to official 2025 HSK exam lists with properly attributed sources. An optional, fully offline performance-based placement test would let advanced beginners skip obvious material while checking recall rather than trusting only multiple-choice recognition.


## v0.8.0 additions

Learning paths group grammar, contextual examples, and short conversations around everyday objectives. Each TTS dialogue is a two-turn, audio-first listening MCQ; A and B are synthesized separately using installed voices when possible. Learners only answer after completing the audio, can replay, and can switch explicitly to reading if Mandarin TTS fails. Scores reflect **device-synthesized** speech comprehension only; they are not equivalent to understanding spontaneous native dialogue. Source editors must review dialogues for tone sandhi and appropriate HSK level before claiming exam alignment.
