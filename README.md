# RMG — DFRoot S918N

Independent experimental Android rooting app, renamed **RMG**.

## Visual identity
Six themes are selectable from **Settings** and apply instantly across the app:
**Obsidian**, **Aurora**, **Glacier**, **Ember**, **Matrix**, and **RMG Heritage**.
RMG Heritage draws its warm ivory and amber palette from the Root My Galaxy visual references. This is a design inspiration, not a software dependency.

## SU Managers and offline installation
Choose **KernelSU Next** or **KernelSU Classic (Samsung)** in Settings. Selection is persistent, changes which Manager app is detected, opened, or installed, and works entirely offline on the phone. Official pinned APKs are bundled during the GitHub Actions build and their SHA-256 digests are verified both by CI and before Android install.

- **Next v3.4.2** — official KernelSU-Next release, SHA-256 `774a5511f7034a4930e881114e202319cbdc34c5aaaa5c7c6b6d2902364e4c58`.
- **Classic Samsung v2.2** — diabl0w KernelSU fork, SHA-256 `761dffad54ae080a4977971db7b04e2fc65c2d9b5e2336accca4a85661290a69`.

Android's install confirmation and per-app permission to install APKs are required.

**Important:** The bundled kernel bootstrap and ksud are **Next-specific**. The manager selector is real for install/open operations, but the **exploit backend is not dynamically swappable**. For Classic, root actions are blocked to avoid a misleading or unsafe kernel launch. The native exploit sources are unchanged.

## GitHub release workflow
Main branch builds release `RMG-S918N.apk` and its checksum automatically to [Releases](https://github.com/Ibhamada0/DFRoot-S918N/releases). These releases are experimental prereleases, debug-key-signed rather than production signed.

Firmware/device root compatibility remains unverified; DFRoot may affect kernel and SELinux protections. Back up before using.

Credit: [soumarcelino/DFRoot](https://github.com/soumarcelino/DFRoot) and [diabl0w/DFRoot](https://github.com/diabl0w/DFRoot). Check upstream licensing terms before redistribution.
