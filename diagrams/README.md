# ZoneZap — Architecture Diagrams

This folder contains **HLD**, **LLD**, and **ML** architecture diagrams for the ZoneZap project.

## File

- **ARCHITECTURE-DIAGRAMS.md** — All diagrams in Mermaid format. Open in:
  - GitHub / GitLab (renders Mermaid automatically)
  - VS Code (with “Mermaid” or “Markdown Preview Mermaid Support” extension)
  - [Mermaid Live Editor](https://mermaid.live/) (paste diagram code blocks)

## Contents

| Section | Description |
|--------|--------------|
| **HLD — System context** | Users, Guardian, and ZoneZap system boundary |
| **HLD — Container** | Android app, Firebase (Auth, Firestore, Functions, FCM), AI Engine |
| **HLD — Data flow** | Flow for alerts and movement_logs |
| **LLD — Android** | Activities, services, data classes, config |
| **LLD — Backend** | Cloud Functions, triggers, helpers |
| **LLD — Sequences** | Panic alert, wandering detection, overdue reminders |
| **LLD — Firestore** | Collections and entity relationship |
| **ML — Training** | Firestore/CSV → features → Isolation Forest → model.pkl |
| **ML — Prediction** | model.pkl + location → score |
| **ML — Features** | Raw location → engineered features → model input |

## Exporting to images (PNG/SVG)

For thesis or presentation slides you can export Mermaid to images:

1. **Mermaid Live Editor**: Paste a code block from `ARCHITECTURE-DIAGRAMS.md` → export PNG/SVG.
2. **CLI** (if you use [@mermaid-js/mermaid-cli](https://github.com/mermaid-js/mermaid-cli)):
   - Install: `npm install -g @mermaid-js/mermaid-cli`
   - Create a `.mmd` file with one diagram per file, then: `mmdc -i diagram.mmd -o diagram.png`

Then place the image in `thesis/figures/` (e.g. `architecture.png`) or in your presentation assets.
