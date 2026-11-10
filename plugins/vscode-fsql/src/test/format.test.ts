import { strict as assert } from 'node:assert';
import { test } from 'node:test';
import { formatFsql } from '../format.ts';

test('indents a body and puts the brace on its own line', () => {
    const formatted = formatFsql('findById={SELECT id FROM item WHERE id = [id]}');

    assert.equal(formatted, 'findById = {\n    SELECT id FROM item WHERE id = [id]\n}\n');
});

test('preserves relative indentation inside the body', () => {
    const source = [
        'findJoined = {',
        '        SELECT i.id, b.size',
        '        FROM item i',
        '            LEFT JOIN bundle b ON b.item_id = i.id',
        '}'
    ].join('\n');

    assert.equal(formatFsql(source), [
        'findJoined = {',
        '    SELECT i.id, b.size',
        '    FROM item i',
        '        LEFT JOIN bundle b ON b.item_id = i.id',
        '}',
        ''
    ].join('\n'));
});

test('separates procedures with exactly one blank line', () => {
    const source = 'a = {\nSELECT 1\n}\n\n\n\nb = {\nSELECT 2\n}';

    assert.equal(formatFsql(source), 'a = {\n    SELECT 1\n}\n\nb = {\n    SELECT 2\n}\n');
});

test('keeps a comment attached to the procedure below it', () => {
    const source = '# counts rows\ncountAll = {\nSELECT count(*) FROM item\n}';

    assert.equal(formatFsql(source), '# counts rows\ncountAll = {\n    SELECT count(*) FROM item\n}\n');
});

test('never rewrites characters inside a line', () => {
    const source = "find = {\n   SELECT * FROM t WHERE name = 'a    b'\n}";

    assert.ok(formatFsql(source).includes("name = 'a    b'"));
});

test('honours the requested indent size', () => {
    const formatted = formatFsql('a = {\nSELECT 1\n}', { indentSize: 2 });

    assert.equal(formatted, 'a = {\n  SELECT 1\n}\n');
});

test('is idempotent', () => {
    const source = 'a = {\n   SELECT 1\n}\n\nb = {\n  SELECT 2\n}';
    const once = formatFsql(source);

    assert.equal(formatFsql(once), once);
});

test('closes an unterminated block', () => {
    const formatted = formatFsql('broken = {\nSELECT 1\n');

    assert.equal(formatted, 'broken = {\n    SELECT 1\n}\n');
});
