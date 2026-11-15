#!/usr/bin/env python3
"""Check or explicitly update asset hashes after editing the bundled example."""
import argparse
import hashlib
import json
from pathlib import Path


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--write", action="store_true", help="Update the manifest instead of checking it")
    options = parser.parse_args()
    resources = Path(__file__).resolve().parents[1] / "examples/bundled-svg/src/main/resources"
    target = resources / "META-INF/openlap/visualization.json"
    manifest = json.loads(target.read_text(encoding="utf-8"))
    changed = []
    for asset in manifest["assets"]:
        path = Path(asset["path"])
        if path.is_absolute() or path.parts[0] != "web" or ".." in path.parts:
            raise SystemExit("Asset paths must stay under web/.")
        checksum = hashlib.sha256((resources / path).read_bytes()).hexdigest()
        if checksum != asset["sha256"]:
            changed.append(asset["id"])
            asset["sha256"] = checksum
    if changed and not options.write:
        raise SystemExit("Changed assets: " + ", ".join(changed) + ". Run with --write to record the reviewed bytes.")
    if options.write:
        target.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    print("Example asset hashes match." if not changed else "Updated hashes: " + ", ".join(changed))


if __name__ == "__main__":
    main()
