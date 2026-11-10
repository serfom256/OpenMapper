# OpenMapper fsql - official VS Code extension

Syntax highlighting, embedded SQL and formatting for the `.fsql` files OpenMapper reads.

## Features

- **Highlighting** for procedure names, the `=` separator, the `{ }` block, `[param]`
  placeholders and `#`, `--` and `/* */` comments
- **Embedded SQL**: the block body is scoped as SQL, so SQL keywords, strings and functions
  highlight inside it
- **Formatting**: `Format Document` puts each header on one line as `name = {`, re-bases the
  body to the editor's indent while keeping its relative structure, closes with `}` at column
  zero, and leaves one blank line between procedures

- **Diagnostics**: problems are underlined as you type

The formatter only ever changes leading and trailing whitespace on a line. It never rewrites
characters inside a line, so string literals and spacing within a statement survive untouched.

## Grammar it implements

Taken from `DefaultParserImpl` and `SourceMapper`:

| Rule | Value |
| ---- | ----- |
| Procedure name | `A-Z a-z 0-9 _ . -` |
| Separator | `=` |
| Body | `{` to the **first** `}`, no nesting and no escaping |
| Placeholder | `[name]`, where name is a single `\w+` token |
| Comments | `#` anywhere; `--` and `/* */` inside a body |
