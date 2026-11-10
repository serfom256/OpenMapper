export interface Range {
    line: number;
    character: number;
    endLine: number;
    endCharacter: number;
}

export interface FsqlDiagnostic {
    range: Range;
    message: string;
    severity: 'error' | 'warning';
}

export interface Procedure {
    name: string;
    range: Range;
}

export interface ParseResult {
    procedures: Procedure[];
    diagnostics: FsqlDiagnostic[];
}

const NAME_CHARACTER = /[A-Za-z0-9_.\-]/;

export function parseFsql(text: string): ParseResult {
    const lineStarts = computeLineStarts(text);
    const at = (from: number, to: number): Range => ({
        ...toPosition(lineStarts, from),
        ...toEndPosition(lineStarts, to)
    });

    const procedures: Procedure[] = [];
    const diagnostics: FsqlDiagnostic[] = [];
    const seen = new Map<string, Procedure>();
    let position = 0;

    while (position < text.length) {
        position = skipTrivia(text, position);
        if (position >= text.length) {
            break;
        }

        const header = matchHeader(text, position);
        if (!header) {
            const resume = nextHeader(text, position);
            const end = resume === -1 ? text.length : resume;
            if (text.slice(position, end).trim().length > 0) {
                diagnostics.push({
                    range: at(position, Math.min(lineEnd(text, position), end)),
                    message: describeStrayText(text, position, end),
                    severity: 'error'
                });
            }
            position = end;
            continue;
        }

        const procedure: Procedure = { name: header.name, range: at(position, position + header.name.length) };
        const duplicate = seen.get(header.name);
        if (duplicate) {
            diagnostics.push({
                range: procedure.range,
                message: `Duplicate procedure '${header.name}': openMapper keeps only the last one, silently`,
                severity: 'error'
            });
        } else {
            seen.set(header.name, procedure);
        }
        procedures.push(procedure);

        const bodyStart = position + header.length;
        const closing = text.indexOf('}', bodyStart);
        if (closing === -1) {
            diagnostics.push({
                range: at(position, Math.min(lineEnd(text, position), text.length)),
                message: `Unterminated body for '${header.name}': no closing '}'`,
                severity: 'error'
            });
            break;
        }

        const swallowed = matchHeaderAnywhere(text, bodyStart, closing);
        if (swallowed) {
            diagnostics.push({
                range: at(swallowed.index, swallowed.index + swallowed.name.length),
                message:
                    `'${swallowed.name}' is inside the body of '${header.name}': ` +
                    `'${header.name}' is probably missing its closing '}'`,
                severity: 'warning'
            });
        }

        diagnostics.push(...checkBody(text, bodyStart, closing, at));
        position = closing + 1;
    }

    return { procedures, diagnostics };
}

const NAME_PATTERN = '[A-Za-z0-9_.\\-]+';

function matchHeader(text: string, from: number): { name: string; length: number } | null {
    const pattern = new RegExp(`^(${NAME_PATTERN})\\s*=\\s*\\{`);
    const match = pattern.exec(text.slice(from));
    return match ? { name: match[1], length: match[0].length } : null;
}

function matchHeaderAnywhere(
    text: string,
    from: number,
    to: number
): { name: string; index: number } | null {
    const pattern = new RegExp(`(?:^|\\n)\\s*(${NAME_PATTERN})\\s*=\\s*\\{`, 'g');
    const slice = text.slice(from, to);
    const match = pattern.exec(slice);
    if (!match) {
        return null;
    }
    return { name: match[1], index: from + match.index + match[0].indexOf(match[1]) };
}

function nextHeader(text: string, from: number): number {
    const pattern = new RegExp(`(?:^|\\n)\\s*${NAME_PATTERN}\\s*=\\s*\\{`, 'g');
    pattern.lastIndex = from;
    const match = pattern.exec(text);
    if (!match) {
        return -1;
    }
    const offset = match[0].startsWith('\n') ? 1 : 0;
    return match.index + offset;
}

function describeStrayText(text: string, from: number, to: number): string {
    const slice = text.slice(from, to);
    const named = new RegExp(`^(${NAME_PATTERN})\\s*=`).exec(slice);
    if (named) {
        return `Expected '{' to open the body of '${named[1]}'`;
    }
    const bare = new RegExp(`^(${NAME_PATTERN})`).exec(slice);
    if (bare) {
        return `Expected '=' and a '{ ... }' body after '${bare[1]}'`;
    }
    return `Unexpected text outside a procedure: ${JSON.stringify(slice.trim().slice(0, 30))}`;
}

function skipTrivia(text: string, from: number): number {
    let position = from;
    for (;;) {
        while (position < text.length && /\s/.test(text[position])) {
            position++;
        }
        if (text[position] === '#') {
            position = lineEnd(text, position);
            continue;
        }
        return position;
    }
}

function checkBody(
    text: string,
    from: number,
    to: number,
    at: (offset: number, end: number) => Range
): FsqlDiagnostic[] {
    const diagnostics: FsqlDiagnostic[] = [];
    let position = from;

    while (position < to) {
        const character = text[position];

        if (character === '-' && text[position + 1] === '-') {
            position = lineEnd(text, position);
            continue;
        }
        if (character === '#') {
            position = lineEnd(text, position);
            continue;
        }
        if (character === '/' && text[position + 1] === '*') {
            const end = text.indexOf('*/', position + 2);
            if (end === -1 || end > to) {
                diagnostics.push({
                    range: at(position, position + 2),
                    message: 'Unterminated block comment',
                    severity: 'error'
                });
                return diagnostics;
            }
            position = end + 2;
            continue;
        }
        if (character === '[') {
            const match = /^\[(\w+)\]/.exec(text.slice(position, to));
            if (!match) {
                diagnostics.push({
                    range: at(position, Math.min(position + 2, to)),
                    message: "Malformed placeholder: expected '[name]' with a single word",
                    severity: 'error'
                });
            } else {
                position += match[0].length;
                continue;
            }
        }
        position++;
    }

    return diagnostics;
}

export function crossFileDuplicates(
    files: ReadonlyMap<string, Procedure[]>,
    label: (key: string) => string = key => key
): Map<string, FsqlDiagnostic[]> {
    const owners = new Map<string, { uri: string; procedure: Procedure }[]>();
    for (const [uri, procedures] of files) {
        for (const procedure of procedures) {
            const list = owners.get(procedure.name) ?? [];
            list.push({ uri, procedure });
            owners.set(procedure.name, list);
        }
    }

    const result = new Map<string, FsqlDiagnostic[]>();
    for (const [name, declarations] of owners) {
        const distinct = new Set(declarations.map(declaration => declaration.uri));
        if (declarations.length < 2 || distinct.size < 2) {
            continue;
        }
        for (const { uri, procedure } of declarations) {
            const others = [...distinct].filter(other => other !== uri).map(label);
            const list = result.get(uri) ?? [];
            list.push({
                range: procedure.range,
                message:
                    `Procedure '${name}' is also declared in ${others.join(', ')}: ` +
                    'openMapper keeps only the last one loaded',
                severity: 'error'
            });
            result.set(uri, list);
        }
    }
    return result;
}

function lineEnd(text: string, from: number): number {
    const end = text.indexOf('\n', from);
    return end === -1 ? text.length : end;
}

function computeLineStarts(text: string): number[] {
    const starts = [0];
    for (let i = 0; i < text.length; i++) {
        if (text[i] === '\n') {
            starts.push(i + 1);
        }
    }
    return starts;
}

function lineOf(lineStarts: number[], offset: number): number {
    let low = 0;
    let high = lineStarts.length - 1;
    while (low < high) {
        const middle = Math.ceil((low + high) / 2);
        if (lineStarts[middle] <= offset) {
            low = middle;
        } else {
            high = middle - 1;
        }
    }
    return low;
}

function toPosition(lineStarts: number[], offset: number): { line: number; character: number } {
    const line = lineOf(lineStarts, offset);
    return { line, character: offset - lineStarts[line] };
}

function toEndPosition(
    lineStarts: number[],
    offset: number
): { endLine: number; endCharacter: number } {
    const { line, character } = toPosition(lineStarts, offset);
    return { endLine: line, endCharacter: character };
}
