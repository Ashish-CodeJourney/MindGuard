# Publishing MindGuard on F-Droid

F-Droid submissions go through a merge request against their `fdroiddata`
repo on **GitLab** (not GitHub), reviewed by volunteer maintainers under
your identity — that part has to happen from your own GitLab account, I
can't do it on your behalf. Everything else is ready.

## Already done

- Public repo, MIT-licensed, real source (not a placeholder) — https://github.com/Ashish-CodeJourney/MindGuard
- No proprietary dependencies (checked `gradle/libs.versions.toml` — only AndroidX, Koin, SQLDelight, all FOSS; no Firebase/GMS)
- Release commits are tagged matching `versionName` (`v0.1.0`, `v0.2.0`) — F-Droid requires this
- Fastlane store metadata at `fastlane/metadata/android/en-US/` (title, short + full description)
- `.fdroid.yml` at the repo root — a complete build recipe (license, categories, description, both release builds) ready to be copied into the fdroiddata submission

## What's still missing (optional, but recommended)

- **Screenshots** — `fastlane/metadata/android/en-US/images/phoneScreenshots/*.png`. Not required for acceptance, but it's what shows on your F-Droid store page. You have screenshots already in `docs/screenshots/` — want me to copy/resize a few into the fastlane path?
- **Per-version changelogs** — `fastlane/metadata/android/en-US/changelogs/1.txt`, `2.txt`, etc. (one file per versionCode). Also optional.

Say the word and I'll do either of these — they're just files in this repo, no external account needed.

## What you need to do (requires your own GitLab account)

1. **Create a GitLab.com account** if you don't have one already.

2. **Fork** https://gitlab.com/fdroid/fdroiddata into your account.

3. Clone your fork and add the metadata file:
   ```bash
   git clone git@gitlab.com:<your-username>/fdroiddata.git
   cd fdroiddata
   git checkout -b com.mindguard
   ```
   Copy this repo's `.fdroid.yml` to `metadata/com.mindguard.yml` in the fdroiddata fork (the content is already in the right format — descriptive header + `Builds:` list).

4. **Validate locally** (optional but catches problems before review; needs `fdroidserver` installed, ~5GB disk, and a while to run):
   ```bash
   pip install fdroidserver
   fdroid lint com.mindguard
   fdroid build com.mindguard    # actually builds the APK from source in a container
   ```

5. Commit and push:
   ```bash
   git add metadata/com.mindguard.yml
   git commit -m "New App: com.mindguard"
   git push origin com.mindguard
   ```

6. Open a **merge request** from your fork's `com.mindguard` branch into `fdroid/fdroiddata:master`. Title it `New App: MindGuard` (or similar) and briefly describe what the app does.

7. Wait for review. Maintainers may comment on the MR asking for changes (e.g. tweaks to `AutoUpdateMode`, categories, reproducibility). Respond there as needed. Typical turnaround for a first review is anywhere from days to a few weeks depending on queue length — this is a volunteer team.

8. Once the metadata MR is merged, F-Droid's build server builds and signs your app with F-Droid's own key. It usually takes 24–48 hours after merge for the app to actually appear in the F-Droid client.

## One thing worth knowing before you submit

Because F-Droid signs the APK with **its own key**, not your `mindguard.jks`, an install from F-Droid and an install of your GitHub-released APK are technically different signatures — a user can't have both installed as "the same app," and can't upgrade a sideloaded copy with an F-Droid one (or vice versa) without uninstalling first. This is normal and unavoidable — every F-Droid app works this way — just worth setting expectations for existing users of your GitHub release.
