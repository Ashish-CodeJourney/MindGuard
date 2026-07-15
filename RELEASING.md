# Releasing MindGuard

How to cut a new release. CI does the signing and publishing — this is what you do locally.

## One-time setup (already done)

These exist and don't need to be repeated unless you rotate the keystore or set up a fresh repo:

- `androidApp/mindguard.jks` — the release signing keystore (gitignored, local only)
- `keystore.properties` at the repo root (gitignored, local only), pointing at that keystore:
  ```
  storeFile=../androidApp/mindguard.jks
  storePassword=...
  keyAlias=mindguard
  keyPassword=...
  ```
- GitHub Actions secrets on the repo (Settings → Secrets and variables → Actions), used by `.github/workflows/ci.yml` to sign release builds in CI:
  - `RELEASE_KEYSTORE_BASE64` — base64 of `androidApp/mindguard.jks`
  - `RELEASE_KEYSTORE_PASSWORD`
  - `RELEASE_KEY_ALIAS`
  - `RELEASE_KEY_PASSWORD`

**Back up `androidApp/mindguard.jks` somewhere safe outside this machine.** If it's lost, you can never publish an update that Android will accept as the same app — every install would need to uninstall the old MindGuard first, and users lose their data.

## Cutting a release

1. Bump the version in `androidApp/build.gradle.kts`:
   ```kotlin
   versionCode = <increment by 1>
   versionName = "X.Y.Z"
   ```
   Commit it (e.g. `chore: bump version to X.Y.Z`) and push to `main`.

2. Tag the commit and push the tag:
   ```
   git tag -a vX.Y.Z -m "MindGuard vX.Y.Z" <commit-sha>
   git push origin vX.Y.Z
   ```
   Tagging is GPG-signed for this repo, so it needs your passphrase — run it yourself in a real terminal, not something Claude can do non-interactively.

3. That's it. The tag push triggers `.github/workflows/ci.yml` → `release` job, which:
   - runs `build`, `coverage`, `lint` first
   - decodes the keystore from secrets and builds `assembleRelease`
   - verifies the signature with `apksigner verify`
   - publishes a GitHub Release named after the tag with `MindGuard-vX.Y.Z.apk` attached, plus auto-generated release notes from commits

4. Check the [Actions tab](https://github.com/Ashish-CodeJourney/MindGuard/actions) for the run, then the [Releases page](https://github.com/Ashish-CodeJourney/MindGuard/releases) once it's published. If you want hand-written release notes instead of the auto-generated ones, edit the release afterward (`gh release edit vX.Y.Z --notes-file ...` or via the GitHub UI).

## If you need to rotate or recreate the keystore secrets

Only needed if the secrets are lost/rotated, or this is set up on a new repo:

```bash
# from repo root, with androidApp/mindguard.jks and keystore.properties present
base64 -w0 androidApp/mindguard.jks | gh secret set RELEASE_KEYSTORE_BASE64
grep '^storePassword=' keystore.properties | cut -d= -f2- | gh secret set RELEASE_KEYSTORE_PASSWORD
grep '^keyAlias=' keystore.properties | cut -d= -f2- | gh secret set RELEASE_KEY_ALIAS
grep '^keyPassword=' keystore.properties | cut -d= -f2- | gh secret set RELEASE_KEY_PASSWORD
```

Requires `gh auth login` first (`gh auth status` to check).

## Manual/local build (fallback, not normally needed)

Only if you need an APK without going through CI — e.g. to sanity-check locally before tagging:

```bash
./gradlew assembleRelease
# output signed via keystore.properties (same key as CI, if configured):
# androidApp/build/outputs/apk/release/androidApp-release.apk

# confirm the signature is valid
$ANDROID_HOME/build-tools/<version>/apksigner verify --verbose \
  androidApp/build/outputs/apk/release/androidApp-release.apk
```
