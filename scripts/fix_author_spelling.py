#!/usr/bin/env python3
import pathlib

def main():
    root = pathlib.Path(__file__).resolve().parent.parent
    target_extensions = {".md", ".html", ".kt", ".txt"}
    exclude_dirs = {".git", ".idea", "build", ".gradle", ".artifacts", ".venv-play-console"}

    count = 0
    for path in root.rglob("*"):
        if any(ex in path.parts for ex in exclude_dirs):
            continue
        if path.is_file() and path.suffix in target_extensions:
            try:
                text = path.read_text(encoding="utf-8")
                if "Komairo Biyori" in text:
                    new_text = text.replace("Komairo Biyori", "Komairo Hiyori")
                    path.write_text(new_text, encoding="utf-8")
                    print(f"Updated Komairo Biyori -> Komairo Hiyori in {path.relative_to(root)}")
                    count += 1
            except Exception as e:
                print(f"Error reading {path}: {e}")

    print(f"[SUCCESS] Updated {count} files to use 'Komairo Hiyori'.")

if __name__ == "__main__":
    main()
