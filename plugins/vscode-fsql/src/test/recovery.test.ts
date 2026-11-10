import { strict as assert } from 'node:assert';
import { test } from 'node:test';
import { parseFsql } from '../validate.ts';

const run = (text: string) => parseFsql(text);

test('a missing equals reports once and still finds the next procedure', () => {
    const { procedures, diagnostics } = run('broken {\nSELECT 1\n}\n\ngood = {\nSELECT 2\n}');

    assert.equal(diagnostics.length, 1);
    assert.match(diagnostics[0].message, /Expected '=' and a '\{ \.\.\. \}' body after 'broken'/);
    assert.deepEqual(procedures.map(p => p.name), ['good']);
});

test('a missing opening brace reports once', () => {
    const { diagnostics, procedures } = run('broken =\nSELECT 1\n\ngood = {\nSELECT 2\n}');

    assert.equal(diagnostics.length, 1);
    assert.match(diagnostics[0].message, /Expected '\{' to open the body of 'broken'/);
    assert.deepEqual(procedures.map(p => p.name), ['good']);
});

test('spam between procedures reports once, not once per character', () => {
    const { diagnostics, procedures } = run('a = {\nSELECT 1\n}\n\n!!! &&& ???\n\nb = {\nSELECT 2\n}');

    assert.equal(diagnostics.length, 1);
    assert.match(diagnostics[0].message, /Unexpected text outside a procedure/);
    assert.deepEqual(procedures.map(p => p.name), ['a', 'b']);
});

test('a missing closing brace warns about the swallowed procedure', () => {
    const { diagnostics } = run('a = {\nSELECT 1\n\nb = {\nSELECT 2\n}');

    const warning = diagnostics.find(d => d.severity === 'warning');
    assert.ok(warning, `expected a warning, got ${JSON.stringify(diagnostics)}`);
    assert.match(warning!.message, /'b' is inside the body of 'a'/);
    assert.match(warning!.message, /missing its closing '\}'/);
});

test('a stray closing brace reports once', () => {
    const { diagnostics, procedures } = run('a = {\nSELECT 1\n}\n\n}\n\nb = {\nSELECT 2\n}');

    assert.equal(diagnostics.length, 1);
    assert.deepEqual(procedures.map(p => p.name), ['a', 'b']);
});

test('an unterminated final body reports once and stops', () => {
    const { diagnostics } = run('a = {\nSELECT 1\n}\n\nb = {\nSELECT 2\n');

    assert.equal(diagnostics.length, 1);
    assert.match(diagnostics[0].message, /Unterminated body for 'b'/);
});

test('garbage never produces a cascade', () => {
    const { diagnostics } = run('%%%%%%%%%%\n@@@@@@@@@@\n##\n$$$$$$$$$$');

    assert.ok(diagnostics.length <= 2, `got ${diagnostics.length} diagnostics`);
});

test('an empty file reports nothing', () => {
    assert.deepEqual(run('').diagnostics, []);
    assert.deepEqual(run('\n\n   \n').diagnostics, []);
});

test('a comment-only file reports nothing', () => {
    assert.deepEqual(run('# just a note\n# and another\n').diagnostics, []);
});

test('a header split across lines is still recognised', () => {
    const { procedures, diagnostics } = run('spread\n  =\n  {\nSELECT 1\n}');

    assert.deepEqual(procedures.map(p => p.name), ['spread']);
    assert.deepEqual(diagnostics, []);
});

test('SQL containing a braced string is not mistaken for a header', () => {
    const { diagnostics } = run("a = {\nSELECT * FROM t WHERE tags = '{1,2}'\n}");

    assert.equal(diagnostics.filter(d => d.severity === 'warning').length, 0);
});
