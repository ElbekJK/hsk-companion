# HSK Companion v0.9 — Information architecture and review checklist

## Primary goal

Reduce decision fatigue and navigation duplication while keeping learning the central action. No badges, streak threats, or artificially endless flows.

## Navigation

| Destination | Purpose | What belongs here |
| --- | --- | --- |
| Today | One sensible next action | Current mission, reviews due, minimal progress pulse |
| Learn | New knowledge and reference | Structured units, grammar, word lookup, dialogues |
| Practice | Retrieval & exam preparation | Flashcards, MCQ listening/reading, tone lab, exam-length practice |
| Progress | Evidence of learning | Skill-specific due counts, delayed retention, exam history |
| Settings | User preferences | HSK level, pace, tones, voices, reminders and attribution |

Deep screens inherit their parent's highlighted tab. Android Back from a detail screen returns to a sensible parent, while the bottom navigation remains available for quick top-level travel. A compact HSK level badge routes to Settings; changing the level does not clear earlier progress.

## Visual system

- Native Java Android Views (no new runtime dependencies)
- Warm off-white background `#F7F9F7`, deep green primary `#205C4F`, neutral text `#1F2C2A` and restrained pale green surfaces
- Clear hierarchy: 23-25sp page/feature headings, 16-17sp rows, 13-14sp descriptions, 11-12sp navigation/eyebrows; Chinese example text remains larger
- Padded single-column layout, no crowded dashboard catalog
- Minimum 48dp button and tab targets, visible textual labels, status colors paired with words
- No excessive animation and no new network calls or tracking

## GitHub material reviewed

1. `nextlevelbuilder/ui-ux-pro-max-skill`: applied principles around appropriate hierarchy, readable text, responsive narrow-width designs and anti-pattern avoidance. No source code copied.
2. `rorkai/app-store-connect-cli-skills`: this automates Apple's App Store Connect/TestFlight/iOS workflows, **not** Google Play or native Android development, so it was not installed or incorporated.
3. `taracodlabs/aiden`: general autonomous agent and test-verification philosophy. Not a UI library; AGPL-3.0 runtime was not bundled or copied.

## Manual device test checklist

1. Open app on OnePlus 13; ensure five tabs fit with readable labels at normal and enlarged text settings.
2. Confirm Today displays only the mission, optional due review, and learning pulse; no large menu.
3. Open a Learn unit, a grammar explanation and a dialogue. Verify Back and active tab.
4. Open Practice → MCQs/Tone lab/Exam, and confirm audio-first behavior.
5. Complete an exam-length practice; compare section performance and inspect suggested follow-up.
6. Open Settings, change HSK level, select independent dialogue voices and test audio.
7. Confirm existing starred vocabulary, due reviews, study history and settings survive update.
8. Rotate device or increase text size: verify no clipped cards/buttons; report screenshots of any issue.

Do not infer visual or audio tests passed from Java compilation alone.
