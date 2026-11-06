# Play Console — Data safety form answers (Pawtiva 1.0.0)

Checked against [Data safety section guidance](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en) on 2026-11-05: every app must complete the form and link a privacy policy, even if it collects no data. "Collected" means data sent off the device; data processed only on the device does not need to be disclosed.

Re-verify these answers against the final build (APK Analyzer: no network library, no analytics/ads SDK) before submitting.

## Data collection and security

| Question | Answer | Why |
| --- | --- | --- |
| Does your app collect or share any of the required user data types? | **No** | All data stays on the device (Room database + DataStore). The app has no server and no network code that sends user data. |
| Is all of the user data collected by your app encrypted in transit? | Not applicable (no data collected) | — |
| Do you provide a way for users to request that their data is deleted? | Not applicable (no data collected). In-app reset deletes all local data. | Settings → Data and privacy → Reset app data |

## Data types

None collected, none shared. Leave every data type unchecked.

## Notes for the reviewer / your own records

- **Android Auto Backup** is enabled (`android:allowBackup="true"`, rules in `app/src/main/res/xml/data_extraction_rules.xml` and `backup_rules.xml`). Backup is performed by Android's system backup service into the user's own Google account; the developer has no access. This is how the privacy policy describes it. If you prefer to avoid any discussion of it, set `allowBackup="false"` and update the privacy policy (users then lose progress when changing phones).
- **Data export** writes a JSON file to a location the user picks through the system file picker. Nothing is sent by the app.
- **Permissions declared:** `POST_NOTIFICATIONS` (optional reminders), `RECEIVE_BOOT_COMPLETED` (reschedule reminders after restart, if added by the reminders implementation). No `INTERNET` permission is required by app code; confirm in the merged manifest (`app/build/intermediates/merged_manifests/release/...`) that no library added it.
- **Ads:** answer "No, my app does not contain ads" in App content → Ads.
- **Privacy policy URL:** host `docs/privacy-policy-en.md` (and optionally the Turkish version) at a public URL.
