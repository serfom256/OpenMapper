import { strict as assert } from 'node:assert';
import { test } from 'node:test';
import { readFileSync, readdirSync, statSync } from 'node:fs';
import { join } from 'node:path';
import { formatFsql } from '../format.ts';

const ROOT = join(import.meta.dirname, '..', '..', '..', '..', 'src', 'test', 'resources');

function fsqlFiles(dir: string): string[] {
    return readdirSync(dir).flatMap(entry => {
        const path = join(dir, entry);
        if (statSync(path).isDirectory()) {
            return fsqlFiles(path);
        }
        return path.endsWith('.fsql') ? [path] : [];
    });
}

function procedures(text: string): Map<string, string> {
    const found = new Map<string, string>();
    const pattern = /([A-Za-z0-9_.\-]+)\s*=\s*\{([^}]*)\}/g;
    let match: RegExpExecArray | null;
    while ((match = pattern.exec(text)) !== null) {
        found.set(match[1], match[2].replace(/\s+/g, ' ').trim());
    }
    return found;
}

const files = fsqlFiles(ROOT);

test('the project ships fsql files to check against', () => {
    assert.ok(files.length > 0, `no .fsql files found under ${ROOT}`);
});

for (const file of files) {
    test(`formatting preserves every procedure in ${file.split('/').pop()}`, () => {
        const original = readFileSync(file, 'utf8');
        const formatted = formatFsql(original);

        const before = procedures(original);
        const after = procedures(formatted);

        assert.deepEqual([...after.keys()], [...before.keys()]);
        for (const [name, body] of before) {
            assert.equal(after.get(name), body, `body of ${name} changed`);
        }
    });

    test(`formatting is idempotent for ${file.split('/').pop()}`, () => {
        const once = formatFsql(readFileSync(file, 'utf8'));

        assert.equal(formatFsql(once), once);
    });
}
