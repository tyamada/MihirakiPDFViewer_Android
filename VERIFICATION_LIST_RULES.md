# Verification list maintenance rules

- Write verification results in `VERIFICATION_LIST.md` in English, including result labels, scope, limitations, and notes. Preserve official manufacturer and model names.
- Keep operational rules and internal maintenance notes in this separate file or another dedicated maintenance document, not in `VERIFICATION_LIST.md`.
- Keep exactly one row per device model, showing its latest verification result. Identify the model by manufacturer and model name/number; spacing differences do not create separate models.
- When a new verification is completed, replace that model's existing row instead of adding another. Use the verification date and confirmed installed app version; the highest version number alone does not establish the latest verification.
- Preserve earlier results in detailed verification records and Git history. Keep unresolved issues and untested scope explicit; a newer limited check does not automatically resolve earlier issues or validate every feature.
- Limit table notes to observed results, relevant limitations, and links to supporting records. Put investigation timelines, raw evidence references, and maintenance procedures in separate documents. Do not publish account identifiers, device serial numbers, purchase tokens, or private local paths in the table.
- Maintain existing data, unrelated Git differences, and pending verification items. Restore temporarily changed device settings and record that restoration in the supporting device report.

## Internal maintenance notes

The previous general note about automatic page-cache disabling on low-RAM devices has been moved here from the list. It is not proof that the cache policy was reverified in the latest device session. Check the applicable implementation and supporting records before assigning it to a current result.

The previous Chromebook note about window mode and tablet/full-screen mode is an existing device result, so it remains in English in that model's table row instead of as a numbered general note.

Supporting device history for the November 25 sessions is in `docs/DEVICE_ENGLISH_BASIC_CHECK_2026-11-25.ja.md`; its historical findings, unresolved scope, and later corrections remain intact. Supporting records may use Japanese; the English-only requirement applies to the public results list.
