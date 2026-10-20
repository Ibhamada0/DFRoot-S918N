# DFRoot S918N

Standalone Android DFRoot project for Samsung Galaxy S23 Ultra SM-S918N testing, based on [soumarcelino/DFRoot](https://github.com/soumarcelino/DFRoot).

Features:
- Existing DFRoot app / root-launch flow preserved from upstream source.
- Additional **Install KernelSU Next Manager** button: opens the official APK download in the browser; installation requires user consent.
- Builds from **this repository's own source** after its one-time initial upstream import.
- GitHub Actions exports an artifact called **DFRoot-S918N-APK** containing **DFRoot-S918N.apk**.

## First import and build

The first push of the workflow invokes `tools/import_dfroot.py` to copy a pinned DFRoot upstream revision into this independent repository and apply the UI modifications; the workflow commits the complete Android source here before building it. Later builds use local repository source (not another app's source).

Go to **Actions → Build DFRoot S918N APK**, then download the ZIP from **Artifacts**. GitHub may require enabling Actions on a new repository or allowing workflows to write repository contents.

## Technical notes

- Original Java package/application ID stays `df.root` for compatibility with existing DFRoot code.
- Android launcher and toolbar name: **DFRoot S918N**.
- The native exploit/kernel module code is not intentionally changed.
- Installer link points to the pinned official KernelSU Next v3.4.2 release file.
- **Build success does not establish that a particular device/firmware can be rooted.** DFRoot modifies security-sensitive kernel state, may crash or bootloop, and can reduce SELinux protection. Backup first.
- Provenance: [upstream fork](https://github.com/soumarcelino/DFRoot), itself forked from [diabl0w/DFRoot](https://github.com/diabl0w/DFRoot). Upstream does not specify a top-level license; verify redistribution rights prior to wider distribution.
- This repository is separate from Root My Galaxy and has no dependency on it.
