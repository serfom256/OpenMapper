import * as vscode from 'vscode';
import { formatFsql } from './format';
import { crossFileDuplicates, parseFsql, type FsqlDiagnostic, type Procedure } from './validate';

const procedures = new Map<string, Procedure[]>();
const fileDiagnostics = new Map<string, FsqlDiagnostic[]>();

export function activate(context: vscode.ExtensionContext): void {
    const collection = vscode.languages.createDiagnosticCollection('fsql');
    context.subscriptions.push(collection);

    context.subscriptions.push(vscode.languages.registerDocumentFormattingEditProvider('fsql', {
        provideDocumentFormattingEdits(
            document: vscode.TextDocument,
            options: vscode.FormattingOptions
        ): vscode.TextEdit[] {
            const text = document.getText();
            const formatted = formatFsql(text, { indentSize: options.tabSize });
            if (formatted === text) {
                return [];
            }
            const whole = new vscode.Range(document.positionAt(0), document.positionAt(text.length));
            return [vscode.TextEdit.replace(whole, formatted)];
        }
    }));

    const record = (uri: vscode.Uri, text: string) => {
        const result = parseFsql(text);
        procedures.set(uri.toString(), result.procedures);
        fileDiagnostics.set(uri.toString(), result.diagnostics);
    };

    const forget = (uri: vscode.Uri) => {
        procedures.delete(uri.toString());
        fileDiagnostics.delete(uri.toString());
        collection.delete(uri);
    };

    const publish = () => {
        const across = crossFileDuplicates(procedures, shortName);

        for (const [uri, own] of fileDiagnostics) {
            const extra = across.get(uri) ?? [];
            collection.set(vscode.Uri.parse(uri), [...own, ...extra].map(toVsCode));
        }
    };

    const scanWorkspace = async () => {
        const exclude = vscode.workspace
            .getConfiguration('openmapperFsql')
            .get<string>('exclude', '**/{node_modules,target,target-release,build,out,dist,bin,.git}/**');
        const files = await vscode.workspace.findFiles('**/*.fsql', exclude);
        for (const known of [...procedures.keys()]) {
            if (!files.some(file => file.toString() === known)) {
                procedures.delete(known);
                fileDiagnostics.delete(known);
            }
        }
        for (const file of files) {
            try {
                record(file, Buffer.from(await vscode.workspace.fs.readFile(file)).toString('utf8'));
            } catch {
                forget(file);
            }
        }
        publish();
    };

    context.subscriptions.push(
        vscode.workspace.onDidOpenTextDocument(document => {
            if (document.languageId === 'fsql') {
                record(document.uri, document.getText());
                publish();
            }
        }),
        vscode.workspace.onDidChangeTextDocument(event => {
            if (event.document.languageId === 'fsql') {
                record(event.document.uri, event.document.getText());
                publish();
            }
        }),
        vscode.workspace.onDidChangeConfiguration(event => {
            if (event.affectsConfiguration('openmapperFsql.exclude')) {
                collection.clear();
                void scanWorkspace();
            }
        }),
        vscode.workspace.onDidDeleteFiles(event => {
            for (const file of event.files) {
                forget(file);
            }
            publish();
        })
    );

    const watcher = vscode.workspace.createFileSystemWatcher('**/*.fsql');
    context.subscriptions.push(
        watcher,
        watcher.onDidCreate(() => void scanWorkspace()),
        watcher.onDidChange(() => void scanWorkspace()),
        watcher.onDidDelete(uri => {
            forget(uri);
            publish();
        })
    );

    for (const document of vscode.workspace.textDocuments) {
        if (document.languageId === 'fsql') {
            record(document.uri, document.getText());
        }
    }
    void scanWorkspace();
}

export function deactivate(): void {
    procedures.clear();
    fileDiagnostics.clear();
}

function shortName(uri: string): string {
    return uri.split('/').pop() ?? uri;
}

function toVsCode(diagnostic: FsqlDiagnostic): vscode.Diagnostic {
    const range = new vscode.Range(
        diagnostic.range.line,
        diagnostic.range.character,
        diagnostic.range.endLine,
        diagnostic.range.endCharacter
    );
    const severity = diagnostic.severity === 'error'
        ? vscode.DiagnosticSeverity.Error
        : vscode.DiagnosticSeverity.Warning;
    const result = new vscode.Diagnostic(range, diagnostic.message, severity);
    result.source = 'fsql';
    return result;
}
