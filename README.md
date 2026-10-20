# DFRoot S918N

Independent experimental DFRoot Android project for Samsung Galaxy S23 Ultra SM-S918N.

## Premium control center

- Premium dark theme with custom shield icon, live root status, firmware and kernel information.
- Official KernelSU Next Manager button, launch and boot toggles, and copyable live logs.
- The native DFRoot exploit code is unchanged from the upstream import.

## Download from Releases

Every successful main-branch build publishes `DFRoot-S918N.apk` and its SHA-256 checksum
on [GitHub Releases](https://github.com/Ibhamada0/DFRoot-S918N/releases) as a prerelease.
GitHub Actions also keeps a downloadable artifact.

**Warning:** Experimental and not hardware-tested on SM-S918N firmware.
DFRoot may change kernel security or SELinux state. Back up before trying.
APK signing uses the upstream project's Android debug key (not production signing).

## Credits

[soumarcelino/DFRoot](https://github.com/soumarcelino/DFRoot),
[diabl0w/DFRoot](https://github.com/diabl0w/DFRoot).
Verify upstream redistribution licensing. No dependency on Root My Galaxy.

## Settings: Manager choice and root backend

The settings screen supports selecting, installing and opening either **KernelSU Next** or **KernelSU Classic (diabl0w's Samsung fork)**. Your selection is preserved in direct-boot-aware storage and controls the manager button.

**Important backend limitation:** this DFRoot S918N fork bundles a Next-specific `ksud` and the kernel module hardcodes the Next package. Choosing the classic Manager does not modify the exploit backend. Therefore root launch and automatic boot-time root are intentionally blocked while Classic is selected. Next remains the only currently supported launch backend, and firmware compatibility is unverified.

The upstream [diabl0w/DFRoot](https://github.com/diabl0w/DFRoot) has a different manager-agnostic bootstrap that consumes `libksud.so` from supported Manager apps. It is not interchangeable with the Next-specific kernel module in this fork without a separate compatible backend implementation and validation.
