"""Validate the shipped UI contract and the UI-only permission boundary."""
from pathlib import Path
import json,xml.etree.ElementTree as ET
import subprocess,sys
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

generated=root/'app/src/main/kotlin/com/mixi/manager/UiCatalog.kt'
before=generated.read_bytes()
subprocess.run([sys.executable,str(root/'scripts/generate_catalog.py')],check=True)
assert generated.read_bytes()==before, 'Compiled catalog was stale; rebuild after regeneration'
print('COMPILED_CATALOG_MATCHES_SOURCE_PASS')

for variant in ('values','values-night'):
 theme=ET.parse(root/f'app/src/main/res/{variant}/theme.xml').getroot()
 attributes={item.attrib['name']:item.text for item in theme.findall('.//item')}
 assert attributes['android:windowDisablePreview']=='true'
 assert attributes['android:windowSplashScreenAnimatedIcon']=='@drawable/empty_startup_icon'
 assert attributes['android:windowSplashScreenAnimationDuration']=='0'
print('NO_BRANDED_STARTING_WINDOW_THEME_PASS day/night')

popup=(root/'app/src/main/kotlin/com/mixi/manager/GlassDropdownPreference.kt').read_text(encoding='utf-8')
assert '.padding(8.dp).verticalScroll(' in popup
assert 'isFirst=false,isLast=false' in popup
assert 'propagateMinConstraints=true' in popup
assert 'enableWindowDim=false' in popup and 'noiseCoefficient=0f' in popup
print('CONSISTENT_POPUP_INSETS_AND_ROW_POLICY_PASS')

import hashlib
for filename,expected in {
 'island_demo_coloros.webp':'7b1691ac0d17379dfcb3eb860658b456578373d339039f9a30a2a8caddc6e091',
 'island_demo_hyperos.webp':'95990cf1192644abe51f4f9714a7fc589e205e515040fc4093dd97b470d2d9fc',
}.items():
 assert hashlib.sha256((root/'app/src/main/res/raw'/filename).read_bytes()).hexdigest()==expected
print('ORIGINAL_ENHANCED_ISLAND_ASSETS_PASS')
