# Verification list maintenance rules

- Write the results table and Notes in `VERIFICATION_LIST.md` in English. Preserve official manufacturer and model names.
- Include only the results table and a Notes section in `VERIFICATION_LIST.md`. Each Result cell must contain exactly one of four labels: No issues, Limited, Unusable, or Untested. Do not add explanations, test-item lists, or investigation narratives to Result cells.
- Put result qualifications, explanatory details, and usage cautions in numbered Notes outside the table. The table Notes column should contain only the corresponding note number(s), or a dash when no note is needed.
- Keep test procedures, full test results, chronological investigation logs, raw evidence references, settings-restoration reports, maintenance rules, and internal memos in a separate verification memo or maintenance document. Notes outside the table summarize the result for readers; they do not replace the full verification record.
- Keep operational rules and internal maintenance notes in this separate file or another dedicated maintenance document, not in `VERIFICATION_LIST.md`.
- Keep exactly one row per device model, showing its latest verification result. Identify the model by manufacturer and model name/number; spacing differences do not create separate models.
- When a new verification is completed, replace that model's existing row instead of adding another. Use the verification date and confirmed installed app version; the highest version number alone does not establish the latest verification.
- Preserve earlier results in detailed verification records and Git history. Keep unresolved issues and untested scope explicit; a newer limited check does not automatically resolve earlier issues or validate every feature.
- Notes outside the table should explain the reported result and points users should be aware of, including relevant limitations or unresolved issues, without reproducing the testing history. Preserve test scope, untested items, unresolved issues, evidence references, and restored settings in the separate verification memo; do not silently discard them when shortening a row. Keep the table result accurate: No issues applies only to the checked scope and does not certify untested features. A partial check is not automatically Untested if it supports a basic-operation result; disclose its scope in Notes and the separate memo.
- Maintain existing data, unrelated Git differences, and pending verification items. Restore temporarily changed device settings and record that restoration in the supporting device report.

## Result categories

| Result label | Requested category | Criteria |
|---|---|---|
| No issues | 問題なし | The checked scope works, with no known device-specific usage restriction in that scope. |
| Limited | 制限あり | The app remains usable, but a verified restriction, unavailable feature, or necessary workaround affects use. Intentional device-specific restrictions also belong here. |
| Unusable | 使用不可 | A confirmed issue prevents core operation on the device. |
| Untested | 未検証 | No completed verification, or insufficient evidence to assess basic operation for the reported environment. |

Use only these labels in the table. Explain limitations, workarounds, evidence qualifications, and scope in numbered Notes outside the table, referenced by the Notes column. Preserve full procedures and evidence in the separate memo. Missing coverage for one feature does not establish that the whole app is unusable. Reclassify only from confirmed evidence; a temporary setup or package-update interruption is not automatically an app defect.

## Internal maintenance notes

The low-RAM page-cache caution remains in the public Notes section. The version-53 source disables page caching for devices classified as low RAM or with less than 4 GiB total RAM; the implementation was checked during this documentation cleanup. This is not an additional runtime cache test.

Detailed November 25 results and the pre-cleanup table snapshot are retained in `docs/DEVICE_ENGLISH_BASIC_CHECK_2026-11-25.ja.md`. This includes test scope, earlier findings, corrections, settings restoration, and outstanding items. The previous Chromebook window/full-screen statement is retained in that memo rather than treated as a usage caution.

Supporting memos may use Japanese. The English-only requirement applies to `VERIFICATION_LIST.md`. Update the separate memo whenever replacing a model's latest table result.

The November 25 table was normalized to the four labels without rerunning tests. Galaxy Tab A SM-T510 is Limited because of its recorded low-RAM page-cache restriction; its successful retest and the resolved package-update interruption remain in the detailed memo. Other current basic-operation passes are No issues within their documented checked scope. Chromebook evidence remains explicitly user-reported.
