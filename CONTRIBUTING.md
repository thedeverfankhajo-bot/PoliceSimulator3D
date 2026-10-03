# Contributing

## Before changing code

1. Read `README.md`, `SECURITY.md`, and the relevant files under `docs/`.
2. Check the current GitHub Actions status before starting a large change.
3. Keep one logical change per commit where practical.
4. Do not commit secrets, signing credentials, keystores, generated exports, or unrelated local files.

## Godot changes

- Target the Godot version declared by `project.godot`.
- Follow the project's GDScript typing and naming conventions.
- Prefer focused scenes and scripts with explicit dependencies.
- Add or update tests for new gameplay logic.
- Do not claim performance improvements without measurements.

## Assets

Do not add third-party assets with unclear redistribution rights. Record verified source and licensing information in `docs/CITATIONS.md` before committing an external asset.

## Verification

Run the repository's documented headless Godot tests and validation checks relevant to the change. Android export is a separate gate; a Linux headless pass is not evidence of Android-device compatibility.

## Commits

Use Conventional Commits, for example:

- `feat(missions): add red-light violation evidence`
- `fix(save): restore missing career state`
- `test(traffic): cover wrong-way detection`
- `docs(status): update verified project scope`

Keep commit subjects concise and use the imperative form.

## Pull requests

A pull request should explain:

- what changed;
- why it changed;
- tests that were run and their actual results;
- known limitations or follow-up work;
- any asset/license implications.

Do not mark a check as passing unless the underlying test actually passed.
