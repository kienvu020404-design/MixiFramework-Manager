"""Validate the shipped UI contract and the UI-only permission boundary."""
from pathlib import Path
import json,xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
fields=json.loads((root/'app/src/main/assets/catalog.json').read_text(encoding='utf-8'))
assert len(fields)==92
assert len({f['key'] for f in fields})==len(fields)
assert {f['page'] for f in fields}=={'cover','media.notification','media.island','lyrics','dictionaries','island','extras','immersive','appearance','extensions'}
for f in fields:
 assert f['kind'] in {'bool','range','choice','text','time'}
 if f['kind']=='bool':assert f['initial'] in ('true','false')
 if f['kind'] in ('range','time'):assert f['min']<=int(f['initial'])<=f['max']
 if f['kind']=='choice':assert len(f['labels'])==len(f['values']) and f['initial'] in f['values']
manifest=ET.parse(root/'app/src/main/AndroidManifest.xml').getroot()
assert not manifest.findall('uses-permission')
assert not manifest.findall('.//service') and not manifest.findall('.//provider') and not manifest.findall('.//receiver')
print('MANAGER_UI_CONTRACT_PASS 92 fields, 10 pages, no privileged permissions or background components')
