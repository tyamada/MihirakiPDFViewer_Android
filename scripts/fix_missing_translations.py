#!/usr/bin/env python3
import xml.etree.ElementTree as ET
import pathlib

def main():
    res_dir = pathlib.Path("app/src/main/res")
    default_strings_path = res_dir / "values" / "strings.xml"
    if not default_strings_path.exists():
        print("Default strings.xml not found.")
        return

    # Parse default strings.xml preserving structure
    default_tree = ET.parse(default_strings_path)
    default_root = default_tree.getroot()
    default_strings_dict = {item.get('name'): item for item in default_root.findall('string') if item.get('name')}

    for child in res_dir.iterdir():
        if child.is_dir() and child.name.startswith("values-") and child.name != "values-ja":
            lang_strings_path = child / "strings.xml"
            if lang_strings_path.exists():
                tree = ET.parse(lang_strings_path)
                root = tree.getroot()
                existing_names = {item.get('name') for item in root.findall('string') if item.get('name')}

                added_count = 0
                for name, elem in default_strings_dict.items():
                    if name not in existing_names:
                        root.append(elem)
                        added_count += 1

                if added_count > 0:
                    tree.write(lang_strings_path, encoding="utf-8", xml_declaration=True)
                    print(f"Added {added_count} missing strings to {lang_strings_path.relative_to(res_dir.parent.parent)}")

    print("[SUCCESS] All translation files synchronized and missing strings filled from default strings.xml!")

if __name__ == "__main__":
    main()
