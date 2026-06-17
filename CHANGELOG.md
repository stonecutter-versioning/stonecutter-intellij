### Features
- Added code wrapping actions:
  1. Wrap in `? if ...` – surround selection with condition comments.
  2. Wrap in `?} else ...` – extend the previous condition with selection.
  3. Wrap in `~ if ... '' -> ''` – surround selection with a local replacement.
- Added custom live templates:
  1. After `? `:
     - `il` — `? if ...`
     - `ic` – `? if ... {`
     - `is` – `? if ... >> '_'`
     - `el` – `?} else ...`
     - `ec` – `?} else ... {`
     - `es` – `?} else ... >> '_'`
  2. After `$ `:
     - `sl` — `$ _`
     - `sc` — `$ _ {`
     - `ss` — `$ _ >> '_'`
  3. After `~ `:
     - `rl` — `~ if ... '_' -> '_'`
     - `rc` — `~ if ... '_' -> '_' {`
     - `rs` — `~ if ... '_' -> '_' >> '_'`
     - `rn` — `~ if ... '_' -> '_' as _`

### Changes
- Updated compatible version to IJ 2026.2.*

### Known issues
- Autocompleting incomplete template keys sometimes inserts a wrong template.
- Templates require a space after `?`/`$`/`~`.
