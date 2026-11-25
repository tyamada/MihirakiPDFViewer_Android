# Verification list maintenance rules

- Write the results table and usage cautions in `VERIFICATION_LIST.md` in English. Preserve official manufacturer and model names.
- Include only the results table and a usage-related Notes section in `VERIFICATION_LIST.md`. Keep result labels concise; table note cells may reference usage cautions. Do not include test procedures, detailed results, verification timelines, evidence links, settings-restoration reports, maintenance rules, or internal memos in the list. Store those details in a separate verification memo or maintenance document.
- Keep operational rules and internal maintenance notes in this separate file or another dedicated maintenance document, not in `VERIFICATION_LIST.md`.
- Keep exactly one row per device model, showing its latest verification result. Identify the model by manufacturer and model name/number; spacing differences do not create separate models.
- When a new verification is completed, replace that model's existing row instead of adding another. Use the verification date and confirmed installed app version; the highest version number alone does not establish the latest verification.
- Preserve earlier results in detailed verification records and Git history. Keep unresolved issues and untested scope explicit; a newer limited check does not automatically resolve earlier issues or validate every feature.
- Notes must describe points users should be aware of when using the app, not testing history. Preserve test scope, untested items, unresolved issues, evidence references, and restored settings in the separate verification memo; do not silently discard them when shortening a row. Keep the table result accurate (for example, Basic checks passed does not mean all features were tested).
- Maintain existing data, unrelated Git differences, and pending verification items. Restore temporarily changed device settings and record that restoration in the supporting device report.

## Internal maintenance notes

The low-RAM page-cache caution remains in the public Notes section. The version-53 source disables page caching for devices classified as low RAM or with less than 4 GiB total RAM; the implementation was checked during this documentation cleanup. This is not an additional runtime cache test.

Detailed November 25 results and the pre-cleanup table snapshot are retained in `docs/DEVICE_ENGLISH_BASIC_CHECK_2026-11-25.ja.md`. This includes test scope, earlier findings, corrections, settings restoration, and outstanding items. The previous Chromebook window/full-screen statement is retained in that memo rather than treated as a usage caution.

Supporting memos may use Japanese. The English-only requirement applies to `VERIFICATION_LIST.md`. Update the separate memo whenever replacing a model's latest table result.
