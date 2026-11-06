# HSK Companion v1.2 — Focused Learning visual identity

The existing development name is retained to avoid avoidable migration. The app is not associated with official HSK authorities. Before public launch, independently check brand/name availability.

## Visual approach
Use **deep blue and warm orange**, not an all-over saturated color. The palette aims at comfortable reading and clear hierarchy; no color has a universally established causal benefit for learning. Contrast, predictable navigation, typography, and appropriately sized tap targets matter more than simplistic color psychology.

| Role | Light | Dark | Usage |
|---|---|---|---|
| Background | `#FAF9F6` | `#0F172A` | Calm reading canvas |
| Card | `#FFFFFF` | `#1E293B` | Content panels |
| Text | `#1F2937` | `#F8FAFC` | Hanzi, prompts |
| Secondary text | `#536174` | `#BAC6D8` | Context, captions |
| Primary | `#2F5DA8` | `#7FB3FF` | Navigation, standard buttons |
| Primary soft | `#EAF1FC` | `#1B2A42` | Supporting surfaces |
| Hero background | `#2F5DA8` | `#254A83` | Today focus |
| Accent | `#F4A261` | `#FFB86C` | Main mission CTA only |
| Accent text | `#26252C` | `#172234` | Main CTA lettering |
| Success | `#146B5F` | `#9BE1C9` | Correct feedback only |
| Error | `#A12E40` | `#FFB3BB` | Incorrect feedback only |

Text/panel pairs are checked programmatically at 4.5:1 or better in `scripts/test_theme.py`. Actual screens still require device testing, particularly large Chinese fonts, system text scaling, and launcher masks. Orange is intentionally not used for an entire page or error/success semantics.

## Logo
Original vector of **a speech bubble containing a Mandarin tone contour**. The two strokes in blue and orange hint at auditory contrasts and speaker turns. The icon has a blue background, white speech bubble and small accent, with Android adaptive and monochrome variants. It is not an official HSK mark. Concept preview is `design/brand_preview_v120.png` (concept only, not a phone screenshot).

## Unchanged constraints
- The current five-area navigation stays: Today, Learn, Practice, Progress, Settings.
- Keep character legibility and progress safety ahead of graphic novelty.
- Preserve the same package ID, local preferences and audio clip text hashing.
