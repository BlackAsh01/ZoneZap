"""
Replace Mermaid code blocks in PROJECT-REPORT.md with rendered diagram images.
Uses Kroki.io API (deflate + base64) - no local CLI required.
Run: py -3 render_diagrams.py
"""
import base64
import re
import zlib
from pathlib import Path
from urllib.parse import quote

BASE = Path(__file__).resolve().parent
MD_PATH = BASE / "PROJECT-REPORT.md"
DIAGRAMS_DIR = BASE / "diagrams"

# Captions for each diagram (in order of appearance)
CAPTIONS = [
    "ZoneZap high-level architecture – Client, Firebase, AI, and end devices.",
    "Component diagram – Mobile app, Firebase services, and external devices.",
    "Process flow – Panic alert (User → App → Firestore → Cloud Function → FCM → Guardians).",
    "Process flow – Wandering detection (movement_logs → analysis → alert → FCM).",
    "Process flow – Overdue reminders (scheduler → Cloud Function → FCM → Users).",
    "Data flow summary – Event types and Cloud Function responses.",
]


def encode_diagram(code: str) -> str:
    """Kroki-style encoding: deflate(9) + base64 url-safe."""
    compressed = zlib.compress(code.encode("utf-8"), 9)
    return base64.urlsafe_b64encode(compressed).decode("ascii").rstrip("=")


def main():
    with open(MD_PATH, "r", encoding="utf-8") as f:
        content = f.read()

    pattern = re.compile(r"```mermaid\n(.*?)```", re.DOTALL)
    blocks = list(pattern.finditer(content))

    if not blocks:
        print("No mermaid blocks found.")
        return

    replacements = []
    for i, m in enumerate(blocks):
        code = m.group(1).strip()
        caption = CAPTIONS[i] if i < len(CAPTIONS) else f"Diagram {i + 1}."
        encoded = encode_diagram(code)
        # Kroki: GET https://kroki.io/<type>/<format>/<encoded>
        url = f"https://kroki.io/mermaid/png/{encoded}"
        replacement = f'\n\n![{caption}]({url})\n\n*{caption}*\n\n'
        replacements.append((m.start(), m.end(), replacement))

    for start, end, repl in reversed(replacements):
        content = content[:start] + repl + content[end:]

    # Remove instructional paragraph before first diagram
    content = re.sub(
        r"\nUse the following Mermaid code in any Mermaid-supported editor[^\n]*\n\n",
        "\n\n",
        content,
        count=1,
    )
    # Remove the note about mermaid.live
    content = re.sub(
        r"\n---\n\n\*To create proper diagrams: copy each Mermaid block[^\*]*\*\.\n\n---",
        "\n",
        content,
        count=1,
    )

    with open(MD_PATH, "w", encoding="utf-8") as f:
        f.write(content)

    print(f"Updated {MD_PATH}: {len(blocks)} diagrams replaced with image links (Kroki.io).")
    print("Images load when viewing the MD in a browser or app that fetches images.")


if __name__ == "__main__":
    main()
