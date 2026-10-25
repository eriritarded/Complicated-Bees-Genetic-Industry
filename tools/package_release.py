"""Verify production bytes against tested output, then create the local release bundles."""
import datetime
import hashlib
import json
import os
from pathlib import Path
import shutil
import struct
import tomllib
import zipfile
import zlib

ROOT = Path(__file__).resolve().parents[1]
DIST = ROOT / 'dist'
TARGETS = {'26.1.2': '26.1.2.122', '26.2': '26.2.0.93'}
MODS = {'complicated_bees': ('complicated_bees', '3.6.2'), 'genetic_industry': ('genindustry_bees', '1.0.0')}
PORTS = {'complicated_bees': 1, 'genetic_industry': 2}

def sha(data):
    return hashlib.sha256(data).hexdigest()

def png_check(data):
    assert data[:8] == b'\x89PNG\r\n\x1a\n'
    offset, compressed, ended = 8, bytearray(), False
    while offset < len(data):
        length = struct.unpack_from('>I', data, offset)[0]
        kind = data[offset+4:offset+8]
        payload = data[offset+8:offset+8+length]
        crc = struct.unpack_from('>I', data, offset+8+length)[0]
        assert zlib.crc32(kind+payload) & 0xffffffff == crc, 'PNG CRC mismatch'
        if kind == b'IDAT': compressed.extend(payload)
        if kind == b'IEND': ended = True
        offset += length + 12
    assert ended and offset == len(data) and zlib.decompress(compressed)

def verify_jar(path, module, target, tested):
    modid, upstream = MODS[module]
    counts = {'classes': 0, 'json': 0, 'png': 0, 'tested_entries': 0}
    with zipfile.ZipFile(path) as jar:
        files = [i.filename for i in jar.infolist() if not i.is_dir()]
        assert len(files) == len(set(files)), 'Duplicate archive entries'
        assert not any('/validation/' in name.lower() or 'bees_port_validation' in name or 'reference-mc' in name for name in files)
        metadata = tomllib.loads(jar.read('META-INF/neoforge.mods.toml').decode('utf-8'))
        assert metadata['mods'][0]['modId'] == modid
        assert metadata['mods'][0]['version'] == f'{upstream}+{target}-port.{PORTS[module]}'
        dependencies = {entry['modId']: entry for entry in metadata['dependencies'][modid]}
        assert dependencies['minecraft']['versionRange'] == f'[{target}]'
        assert dependencies['neoforge']['versionRange'] == f'[{TARGETS[target]},)'
        if module == 'genetic_industry':
            assert dependencies['complicated_bees']['type'] == 'required'
        assert 'LICENSE.txt' in files
        for name, digest in tested.items():
            assert name in files, f'Tested output absent from JAR: {name}'
            assert sha(jar.read(name)) == digest, f'Production byte mismatch: {name}'
            counts['tested_entries'] += 1
        allowed = set(tested) | {'META-INF/MANIFEST.MF', 'LICENSE.txt'}
        assert set(files) <= allowed, f'Untested production entries: {set(files)-allowed}'
        for name in files:
            data = jar.read(name)
            if name.endswith('.class'):
                assert data[:4] == b'\xca\xfe\xba\xbe' and int.from_bytes(data[6:8], 'big') == 69, name
                counts['classes'] += 1
            elif name.endswith('.json'):
                json.loads(data.decode('utf-8'))
                counts['json'] += 1
            elif name.endswith('.png'):
                png_check(data)
                counts['png'] += 1
    return counts

def artifact(path):
    data = path.read_bytes()
    return {'file': path.relative_to(DIST).as_posix(), 'bytes': len(data), 'sha256': sha(data)}

def main():
    DIST.mkdir(exist_ok=True)
    artifacts, checks = [], {}
    now = datetime.datetime.now(datetime.timezone.utc).isoformat()
    for target, neo in TARGETS.items():
        tested = json.loads((ROOT / 'evidence' / target / 'tested-output.json').read_text(encoding='utf-8'))
        assert all(result.startswith('PASS ') for result in tested['reports'].values())
        folder = DIST / f'neoforge-{target}'
        folder.mkdir(exist_ok=True)
        checks[target] = {'reports': tested['reports'], 'modules': {}}
        jars = []
        for module, (modid, version) in MODS.items():
            filename = f'{modid}-neoforge-{version}+{target}-port.{PORTS[module]}.jar'
            source = ROOT / module / 'build' / target / 'libs' / filename
            checks[target]['modules'][module] = verify_jar(source, module, target, tested['modules'][module])
            dest = folder / filename
            shutil.copy2(source, dest)
            jars.append(dest)
            artifacts.append(artifact(dest))
        install = (f'Unofficial Complicated Bees + Genetic Industry port\n\n'
                   f'Minecraft Java {target}; NeoForge {neo}; Java 25.\n'
                   'Copy both files from mods/ into the matching instance mods directory.\n'
                   'Use the same pair on the dedicated server and its clients. Do not mix Minecraft targets.\n'
                   'No additional required mod libraries. Optional JEI and Jade are separate downloads.\n'
                   'EMI integration is unavailable. Patchouli guidebook resources are retained but unverified; no compatible 26.2 release was found.\n'
                   'This is an unofficial port. Original authors: ACCBDD and TheSmartNoob880.\n'
                   'Source, complete coverage notes and validation evidence are supplied alongside these bundles.\n')
        bundle = DIST / f'complicated-bees-and-genetic-industry-{target}-neoforge-port.2.zip'
        with zipfile.ZipFile(bundle, 'w', compression=zipfile.ZIP_DEFLATED) as archive:
            for jar in jars: archive.write(jar, f'mods/{jar.name}')
            archive.writestr('INSTALL.txt', install)
            for module in MODS: archive.write(ROOT / module / 'LICENSE.txt', f'licenses/{module}.txt')
            archive.write(ROOT / 'PORT_STATUS.md', 'PORT_STATUS.md')
        artifacts.append(artifact(bundle))
    report = DIST / 'package-validation.json'
    report.write_text(json.dumps({'verified_utc': now, 'result': 'PASS', 'targets': checks,
                                 'note': 'Official-name production JAR entries match tested development classes/resources byte for byte. No launcher installation was tested.'}, indent=2)+'\n', encoding='utf-8')
    artifacts.append(artifact(report))
    source = DIST / 'complicated-bees-ports-source-port.2.zip'
    excluded = {'.git', '.gradle', '.gradle-daemons', 'reference', 'reference-mc', 'logs', 'build', 'run', 'dist', '__pycache__'}
    with zipfile.ZipFile(source, 'w', compression=zipfile.ZIP_DEFLATED) as archive:
        for directory, subdirs, filenames in os.walk(ROOT):
            subdirs[:] = sorted(name for name in subdirs if name not in excluded)
            for filename in sorted(filenames):
                file = Path(directory) / filename
                relative = file.relative_to(ROOT)
                if not filename.endswith('.log'):
                    archive.write(file, f'complicated-bees-ports/{relative.as_posix()}')
    artifacts.append(artifact(source))
    manifest = DIST / 'manifest.json'
    manifest.write_text(json.dumps({'created_utc': now, 'unofficial_port': True, 'java': 25,
        'targets': TARGETS, 'gradle': '9.2.1', 'moddevgradle': '2.0.148',
        'upstream': {
            'complicated_bees': {'url': 'https://github.com/ACCBDD/complicated_bees', 'revision': '68d1299ef688ff9a844c5e16b37876c5122712b5', 'license': 'MIT'},
            'genetic_industry': {'url': 'https://github.com/TheSmartNoob880/Complicated-Bees-Genetic-Industry', 'revision': '68fceb01e926f18de1a76591b8691bdb75353bd9', 'license': 'GPL-3.0-only (upstream LICENSE; metadata disagrees)'}},
        'artifacts': artifacts}, indent=2)+'\n', encoding='utf-8')
    artifacts.append(artifact(manifest))
    (DIST / 'SHA256SUMS.txt').write_text(''.join(f'{entry["sha256"]}  {entry["file"]}\n' for entry in artifacts), encoding='utf-8')
    print(json.dumps(checks, indent=2))
    print(f'Packaged {len(artifacts)} canonical artifacts; checksums in dist/SHA256SUMS.txt')

if __name__ == '__main__':
    main()
