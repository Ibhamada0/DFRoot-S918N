#!/usr/bin/env python3
"""One-time import of DFRoot source into this independent repository."""
from pathlib import Path
import subprocess
import tempfile
import shutil
import sys

REV = "7443558c407e0d92c9034d497582e4d2b1babeb3"
APK = "https://github.com/KernelSU-Next/KernelSU-Next/releases/download/v3.4.2/KernelSU_Next_v3.4.2_33355-release.apk"

def patch(path, old, new):
    p = Path(path)
    s = p.read_text(encoding="utf-8")
    if s.count(old) != 1:
        raise RuntimeError("Unexpected upstream source: " + str(p))
    p.write_text(s.replace(old, new, 1), encoding="utf-8")

def main():
    root = Path.cwd()
    if (root / "app").exists():
        print("app/ already exists, skipping initial import")
        return
    with tempfile.TemporaryDirectory() as tmp:
        src = Path(tmp) / "upstream"
        subprocess.run(["git", "clone", "--depth=1", "https://github.com/soumarcelino/DFRoot.git", str(src)], check=True)
        sha = subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=src, text=True).strip()
        if sha != REV:
            subprocess.run(["git", "fetch", "--depth=1", "origin", REV], cwd=src, check=True)
            subprocess.run(["git", "checkout", "--detach", REV], cwd=src, check=True)
        main = src / "app/src/main/java/df/root/MainActivity.java"
        layout = src / "app/src/main/res/layout/activity_main.xml"
        patch(main, "import android.os.Bundle;", "import android.os.Bundle;\nimport android.content.Intent;\nimport android.net.Uri;")
        patch(main, "        setSupportActionBar(binding.toolbar);",
              '        setSupportActionBar(binding.toolbar);\n'
              '        binding.btnManager.setOnClickListener(v ->\n'
              '            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("' + APK + '"))));')
        patch(layout, '                android:text="Auto Soft Reboot" />\n\n'
              '        </LinearLayout>\n\n'
              '    </com.google.android.material.card.MaterialCardView>\n\n'
              '    <ScrollView',
              '                android:text="Auto Soft Reboot" />\n\n'
              '            <Button\n'
              '                android:id="@+id/btnManager"\n'
              '                android:layout_width="match_parent"\n'
              '                android:layout_height="wrap_content"\n'
              '                android:text="Install KernelSU Next Manager" />\n\n'
              '        </LinearLayout>\n\n'
              '    </com.google.android.material.card.MaterialCardView>\n\n'
              '    <ScrollView')
        patch(layout, 'android:title="DFRoot"', 'android:title="DFRoot S918N"')
        patch(src / "app/src/main/AndroidManifest.xml", 'android:label="DFRoot"', 'android:label="DFRoot S918N"')
        patch(src / "gradle/wrapper/gradle-wrapper.properties",
              'distributionUrl=https\\://services.gradle.org/distributions/gradle-9.7.1-bin.zip',
              'distributionUrl=https\\://services.gradle.org/distributions/gradle-8.9-bin.zip')
        for item in src.iterdir():
            if item.name in {".git", ".gitignore", ".github", "README.md"}:
                continue
            dest = root / item.name
            if dest.exists():
                raise RuntimeError("Target path already exists: " + str(dest))
            if item.is_dir():
                shutil.copytree(item, dest)
            else:
                shutil.copy2(item, dest)
    print("Complete standalone DFRoot S918N sources imported.")

if __name__ == "__main__":
    main()
