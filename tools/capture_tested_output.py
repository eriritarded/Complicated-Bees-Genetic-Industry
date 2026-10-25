"""Capture the production classes/resources from a successfully tested development runtime."""
import argparse
import datetime
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODULES = ('complicated_bees', 'genetic_industry')

def digest(data):
    return hashlib.sha256(data).hexdigest()

def capture(target):
    evidence = ROOT / 'evidence' / target
    evidence.mkdir(parents=True, exist_ok=True)
    reports = {}
    for kind, name in [('server', 'validation-report.txt'), ('client', 'client-report.txt')]:
        source = ROOT / 'genetic_industry' / 'run' / target / kind / name
        report = source.read_text(encoding='utf-8')
        if not report.startswith('PASS '):
            raise SystemExit(f'{source} did not pass')
        (evidence / name).write_text(report, encoding='utf-8')
        reports[kind] = report.splitlines()[0]
    screenshots = ROOT / 'genetic_industry' / 'run' / target / 'client' / 'screenshots'
    for source in screenshots.glob('*.png'):
        (evidence / source.name).write_bytes(source.read_bytes())
    result = {'target': target, 'captured_utc': datetime.datetime.now(datetime.timezone.utc).isoformat(),
              'runtime': 'ModDevGradle development output; connected dedicated server and client',
              'reports': reports, 'modules': {}}
    for module in MODULES:
        entries = {}
        for suffix in ('classes/java/main', 'resources/main'):
            output = ROOT / module / 'build' / target / suffix
            if not output.is_dir():
                raise SystemExit(f'Missing {output}')
            for file in sorted(output.rglob('*')):
                if file.is_file():
                    key = file.relative_to(output).as_posix()
                    if key in entries:
                        raise SystemExit(f'Duplicate production output: {key}')
                    entries[key] = digest(file.read_bytes())
        result['modules'][module] = entries
    (evidence / 'tested-output.json').write_text(json.dumps(result, indent=2) + '\n', encoding='utf-8')
    print(f'{target}: captured {reports}, {sum(map(len, result["modules"].values()))} production entries')

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('target', choices=('26.1.2', '26.2'))
    capture(parser.parse_args().target)
