#!/usr/bin/env python3
import xml.etree.ElementTree as ET
import pathlib

def parse_strings(xml_path):
    tree = ET.parse(xml_path)
    root = tree.getroot()
    return {item.get('name') for item in root.findall('string') if item.get('name')}

def main():
    res_dir = pathlib.Path("app/src/main/res")
    default_strings = res_dir / "values" / "strings.xml"
    if not default_strings.exists():
        print("Default strings.xml not found.")
        return

    base_keys = parse_strings(default_strings)
    print(f"Base strings.xml (values): {len(base_keys)} keys found.")

    all_ok = True
    for child in res_dir.iterdir():
        if child.is_dir() and child.name.startswith("values-"):
            lang_strings = child / "strings.xml"
            if lang_strings.exists():
                lang_keys = parse_strings(lang_strings)
                missing = base_keys - lang_keys
                if missing:
                    all_ok = False
                    print(f"[{child.name}] Missing {len(missing)} keys: {sorted(missing)}")
                else:
                    print(f"[{child.name}] All {len(lang_keys)} keys present.")

    if all_ok:
        print("\n[SUCCESS] All translation files have complete string keys!")

if __name__ == "__main__":
    main()
