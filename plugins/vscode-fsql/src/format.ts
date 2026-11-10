export interface FormatOptions {
    indentSize?: number;
}

const NAME_CHARACTER = /[A-Za-z0-9_.\-]/;

export function formatFsql(text: string, options: FormatOptions = {}): string {
    const indent = ' '.repeat(options.indentSize ?? 4);
    const lines: string[] = [];
    const pendingComments: string[] = [];
    let position = 0;

    const skipWhitespace = () => {
        while (position < text.length && /\s/.test(text[position])) {
            position++;
        }
    };

    const emit = (block: string[]) => {
        if (lines.length > 0) {
            lines.push('');
        }
        lines.push(...pendingComments, ...block);
        pendingComments.length = 0;
    };

    while (position < text.length) {
        skipWhitespace();
        if (position >= text.length) {
            break;
        }

        if (text[position] === '#') {
            const end = lineEnd(text, position);
            pendingComments.push(text.slice(position, end).trimEnd());
            position = end;
            continue;
        }

        const nameStart = position;
        while (position < text.length && NAME_CHARACTER.test(text[position])) {
            position++;
        }
        const name = text.slice(nameStart, position);
        if (name.length === 0) {
            const end = lineEnd(text, position);
            emit([text.slice(position, end).trimEnd()]);
            position = end;
            continue;
        }

        skipWhitespace();
        if (text[position] !== '=') {
            emit([name]);
            continue;
        }
        position++;

        skipWhitespace();
        if (text[position] !== '{') {
            emit([`${name} =`]);
            continue;
        }
        position++;

        const closing = text.indexOf('}', position);
        const body = text.slice(position, closing === -1 ? text.length : closing);
        position = closing === -1 ? text.length : closing + 1;

        emit([`${name} = {`, ...reindent(body, indent), '}']);
    }

    lines.push(...pendingComments);
    return lines.length === 0 ? '' : `${lines.join('\n')}\n`;
}

function lineEnd(text: string, from: number): number {
    const end = text.indexOf('\n', from);
    return end === -1 ? text.length : end;
}

function reindent(body: string, indent: string): string[] {
    const lines = body.split('\n').map(line => line.replace(/\r$/, '').trimEnd());

    while (lines.length > 0 && lines[0].trim() === '') {
        lines.shift();
    }
    while (lines.length > 0 && lines[lines.length - 1].trim() === '') {
        lines.pop();
    }
    if (lines.length === 0) {
        return [];
    }

    const widths = lines
        .filter(line => line.trim() !== '')
        .map(line => line.length - line.trimStart().length);
    const common = widths.length === 0 ? 0 : Math.min(...widths);

    return lines.map(line => (line.trim() === '' ? '' : indent + line.slice(common)));
}
