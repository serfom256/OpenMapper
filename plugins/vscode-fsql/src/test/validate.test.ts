import { strict as assert } from 'node:assert';
import { test } from 'node:test';
import { readFileSync, readdirSync, statSync } from 'node:fs';
import { join } from 'node:path';
import { parseFsql, crossFileDuplicates, type Procedure } from '../validate.ts';

const messages = (text: string) => parseFsql(text).diagnostics.map(d => d.message);

test('clean file reports nothing', () => {
    const { procedures, diagnostics } = parseFsql('a = {\n SELECT 1 WHERE x = [id]\n}\n\nb = {\n SELECT 2\n}');

    assert.deepEqual(procedures.map(p => p.name), ['a', 'b']);
    assert.deepEqual(diagnostics, []);
});

test('duplicate name in one file is an error on the second declaration', () => {
    const { diagnostics } = parseFsql('a = {\nSELECT 1\n}\n\na = {\nSELECT 2\n}');

    assert.equal(diagnostics.length, 1);
    assert.match(diagnostics[0].message, /Duplicate procedure 'a'/);
    assert.equal(diagnostics[0].range.line, 4);
});

test('missing equals is reported', () => {
    assert.match(messages('a {\nSELECT 1\n}')[0], /Expected '=' and a '\{ \.\.\. \}' body after 'a'/);
});

test('missing opening brace is reported', () => {
    assert.match(messages('a = SELECT 1')[0], /Expected '\{' to open the body of 'a'/);
});

test('unterminated body is reported', () => {
    assert.match(messages('a = {\nSELECT 1\n')[0], /Unterminated body for 'a'/);
});

test('unterminated block comment is reported', () => {
    assert.match(messages('a = {\n/* nope\nSELECT 1\n}')[0], /Unterminated block comment/);
});

test('malformed placeholder is reported', () => {
    assert.match(messages('a = {\nSELECT 1 WHERE x = [a.b]\n}')[0], /Malformed placeholder/);
});

test('placeholders inside comments are ignored', () => {
    assert.deepEqual(messages('a = {\n-- [a.b] is fine here\nSELECT 1\n}'), []);
});

test('positions point at the offending token', () => {
    const { diagnostics } = parseFsql('ok = {\nSELECT 1\n}\n\nbad = {\nSELECT [x.y]\n}');

    assert.equal(diagnostics.length, 1);
    assert.equal(diagnostics[0].range.line, 5);
    assert.equal(diagnostics[0].range.character, 7);
});

test('cross-file duplicates are reported in both files', () => {
    const one = parseFsql('shared = {\nSELECT 1\n}').procedures;
    const two = parseFsql('shared = {\nSELECT 2\n}').procedures;
    const files = new Map<string, Procedure[]>([['a.fsql', one], ['b.fsql', two]]);

    const result = crossFileDuplicates(files);

    assert.deepEqual([...result.keys()].sort(), ['a.fsql', 'b.fsql']);
    assert.match(result.get('a.fsql')![0].message, /also declared in b\.fsql/);
});

test('the same name once per file is not a cross-file duplicate', () => {
    const files = new Map<string, Procedure[]>([
        ['a.fsql', parseFsql('x = {\nSELECT 1\n}').procedures],
        ['b.fsql', parseFsql('y = {\nSELECT 2\n}').procedures]
    ]);

    assert.equal(crossFileDuplicates(files).size, 0);
});

const ROOT = join(import.meta.dirname, '..', '..', '..', '..', 'src', 'test', 'resources');
function fsqlFiles(dir: string): string[] {
    return readdirSync(dir).flatMap(entry => {
        const path = join(dir, entry);
        return statSync(path).isDirectory() ? fsqlFiles(path) : path.endsWith('.fsql') ? [path] : [];
    });
}

test('the real project fsql files are clean', () => {
    for (const file of fsqlFiles(ROOT)) {
        const { diagnostics } = parseFsql(readFileSync(file, 'utf8'));
        assert.deepEqual(diagnostics, [], `${file} reported ${JSON.stringify(diagnostics)}`);
    }
});

test('files sharing a basename are still compared separately', () => {
    const files = new Map<string, Procedure[]>([
        ['file:///a/item.fsql', parseFsql('shared = {\nSELECT 1\n}').procedures],
        ['file:///b/item.fsql', parseFsql('shared = {\nSELECT 2\n}').procedures]
    ]);

    const result = crossFileDuplicates(files, uri => uri.split('/').pop()!);

    assert.equal(result.size, 2);
    assert.match(result.get('file:///a/item.fsql')![0].message, /also declared in item\.fsql/);
});
