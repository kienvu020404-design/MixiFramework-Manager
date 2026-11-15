"""Package the UI inside the accepted Host; never rebuild HMC/native runtime."""
from pathlib import Path
import hashlib, json, os, re, shutil, subprocess, zipfile

manager = Path(__file__).resolve().parents[1]
root = manager.parents[1]
out = root/'out/manager-host'; out.mkdir(parents=True, exist_ok=True)
java = root/'tools/jdk/jdk-21.0.13+9/bin'
sdk = root/'tools/sdk/build-tools/37.0.0'
android = root/'tools/sdk/platforms/android-37.0/android.jar'
dexlib = root/'tools/jadx/lib/jadx-1.5.6-all.jar'
os.environ['JAVA_HOME'] = str(java.parent)
sha = lambda b: hashlib.sha256(b).hexdigest()
def run(*args): subprocess.run(list(map(str,args)),cwd=root,check=True)
def compile(name,sources,extra=()):
    classes=out/(name+'-classes')
    assert classes.resolve().parent==out.resolve()
    if classes.exists():shutil.rmtree(classes)
    classes.mkdir()
    run(java/'javac.exe','-encoding','UTF-8','-source','8','-target','8','-cp',';'.join(map(str,[android,*extra])),'-d',classes,*sources)
    jar=out/(name+'-classes.jar'); run(java/'jar.exe','cf',jar,'-C',classes,'.')
    dex=out/(name+'.jar'); run(java/'java.exe','-cp',sdk/'lib/d8.jar','com.android.tools.r8.D8','--min-api','35','--lib',android,'--output',dex,jar)
    return dex

baseline=root/'out/v104/MixiHost.apk'
assert sha(baseline.read_bytes())=='fc2f2f5701dfa41e95a95ab182d13aece279331e5f52d6082982b5055187e8bc','Accepted Host mismatch'
for name,expected in json.loads((manager/'host-integration/framework-inputs.json').read_text()).items():
    assert sha((root/name).read_bytes())==expected,'Framework source input changed: '+name
ui=manager/'app/build/outputs/apk/release/app-release.apk'
badging=subprocess.check_output([str(sdk/'aapt2.exe'),'dump','badging',str(ui)],text=True,encoding='utf-8')
assert "name='com.mixi.host'" in badging and "versionName='0.9.3-host-ui'" in badging,'Build with -PhostUi=true first'
resources=subprocess.check_output([str(sdk/'aapt2.exe'),'dump','resources',str(ui)],text=True,encoding='utf-8')
assert 'id=80' in resources or 'id=0x80' in resources,'UI resource IDs must be isolated from original 0x7f'
run(java/'java.exe','-jar',sdk/'lib/apksigner.jar','verify',ui)

sources=[p for p in (root/'rom-integration/v102/settings').rglob('*.java') if p.name not in ('SettingsPage.java','EnhancedCatalog.java')]
protocol=root/'rom-integration/v102/settings/mixi/settings/EnhancedProtocol.java'
sources.remove(protocol)
protocol_text=protocol.read_text(encoding='utf-8')
old_ack='synchronized (EnhancedProtocol.class) { pending.remove(key); preferences.edit().remove("pending." + key).apply(); }'
new_ack='''synchronized (EnhancedProtocol.class) {
                        if (value.equals(pending.get(key))) {
                            pending.remove(key);
                            if (value.equals(preferences.getString(key, null))) preferences.edit().remove("pending." + key).apply();
                        }
                    }'''
assert protocol_text.count(old_ack)==1
adapted_protocol=out/'EnhancedProtocol.java'
adapted_protocol.write_text(protocol_text.replace(old_ack,new_ack),encoding='utf-8')
sources.append(adapted_protocol)
sources+=list((root/'rom-integration/v103-apk/settings').rglob('*.java'))
sources+=[root/('settings-extension/java/mixi/settings/'+n+'.java') for n in ('MixiSettingsFragment','MixiInfoPreference','Host','RomInfo')]
sources+=[manager/'host-integration/ManagerBridge.java']
settings=compile('settings',sources,[root/'out/v09/reference-r2/settings-stubs'])

# Reuse current extension; change only our header's destination and attach the relay lazily.
source=(root/'rom-integration/v102/SettingsExtension.java').read_text(encoding='utf-8')
old='''   installNavigation(host,runtime);
   panel.invoke(call.receiver,HeaderPlacement.FRAGMENT,new Bundle(),0,"Mixi Framework",null,0);call.returnValue(null);'''
new='''   ensureFragment(host);
   extensionLoader.loadClass("mixi.settings.ManagerBridge").getMethod("attach",Context.class).invoke(null,app);
   ((Context)call.receiver).startActivity(new android.content.Intent().setComponent(new android.content.ComponentName("com.mixi.host","com.mixi.manager.MainActivity")));
   call.returnValue(null);'''
assert source.count(old)==1
source=source.replace(old,new)
legacy='  Method open=null;for(Method m:activity.getMethods())if(m.getName().equals("startPreferencePanel")&&m.getParameterTypes().length==6){open=m;break;}\n  if(open==null)throw new NoSuchMethodException("Settings startPreferencePanel");final Method panel=open;'
assert source.count(legacy)==1
source=source.replace(legacy,"")
source=source.replace('SettingsRuntimeEntry.report("HEADER_"+result,"Original Settings header list; size="+list.size());','if(!"ALREADY_PLACED".equals(result))SettingsRuntimeEntry.report("HEADER_"+result,"Original Settings header list; size="+list.size());')
generated=out/'SettingsExtension.java';generated.write_text(source,encoding='utf-8')
patch=compile('core-patch',[generated],[root/'build/v06/core-classes.jar',root/'out/v09/v097/core-async-classes',root/'out/v09/reference-r2/core-extra-classes'])
factory=compile('ui-factory',[manager/'host-integration/HostUiFactory.java'])

with zipfile.ZipFile(baseline) as z:
    original={n:z.read(n) for n in z.namelist()}
parts={n:b for n,b in original.items() if not (n=='META-INF/MANIFEST.MF' or n.startswith('META-INF/') and n.endswith(('.SF','.RSA','.DSA','.EC')))}
(out/'base-core.dex').write_bytes(parts['classes3.dex'])
with zipfile.ZipFile(patch) as z:(out/'patch.dex').write_bytes(z.read('classes.dex'))
tools=out/'dex-tools';tools.mkdir(exist_ok=True)
run(java/'javac.exe','-cp',dexlib,'-d',tools,root/'scripts/v09/framework-child/ComposeDex.java',manager/'host-integration/VerifyHostDex.java')
cp=str(tools)+';'+str(dexlib)
run(java/'java.exe','-cp',cp,'ComposeDex',out/'base-core.dex',out/'patch.dex',out/'core.dex')
run(java/'java.exe','-cp',cp,'VerifyHostDex',out/'base-core.dex',out/'core.dex')
parts['classes3.dex']=(out/'core.dex').read_bytes()
with zipfile.ZipFile(factory) as z:
    assert 'classes4.dex' not in parts
    parts['classes4.dex']=z.read('classes.dex')
parts['assets/mixi-ui/module.apk']=ui.read_bytes()
parts['assets/mixi-ui/sha256']=(sha(ui.read_bytes())+'\n').encode('ascii')
parts['META-INF/mixi/modules/settings.jar']=settings.read_bytes()
pin='META-INF/mixi/pins/settings.properties'
metadata=dict(line.split('=',1) for line in parts[pin].decode().splitlines() if '=' in line)
metadata['extensionSha256']=sha(settings.read_bytes())
metadata['buildId']='host-ui-0.9.0-test'
parts[pin]=''.join(k+'='+v+'\n' for k,v in metadata.items()).encode()
manifest=(root/'out/v104/AndroidManifest.xml').read_text(encoding='utf-8')
manifest=manifest.replace('100400','100404').replace('android:versionName="1.0.4"','android:versionName="1.0.4-ui-test"')
manifest=manifest.replace('<application ','<application android:appComponentFactory="mixi.host.ui.HostUiFactory" android:allowBackup="false" ')
manifest=manifest.replace('</application>','<activity android:name="com.mixi.manager.MainActivity" android:exported="true" android:process=":manager" android:theme="@android:style/Theme.Material.NoActionBar" /></application>')
(out/'AndroidManifest.xml').write_text(manifest,encoding='utf-8')
run(sdk/'aapt2.exe','link','-I',android,'-I',baseline,'--manifest',out/'AndroidManifest.xml','-o',out/'manifest.apk')
with zipfile.ZipFile(out/'manifest.apk') as z:parts['AndroidManifest.xml']=z.read('AndroidManifest.xml')
index='META-INF/mixi/index.properties'
parts.pop(index)
parts[index]=('schema=1\nfeature.ids=settings\n'+''.join('sha.'+n+'='+sha(b)+'\n' for n,b in sorted(parts.items()) if n.startswith('META-INF/mixi/'))).encode()
with zipfile.ZipFile(out/'unsigned.apk','w') as z:
    for n,b in parts.items():z.writestr(n,b)
apk=out/'MixiHost-UI-Test.apk'
run(sdk/'zipalign.exe','-P','16','-f','4',out/'unsigned.apk',out/'aligned.apk')
run(java/'java.exe','-jar',sdk/'lib/apksigner.jar','sign','--ks',root/'build/private/manager-preview.keystore','--ks-pass','pass:android','--key-pass','pass:android','--out',apk,out/'aligned.apk')
run(java/'java.exe','-jar',sdk/'lib/apksigner.jar','verify',apk)
run(sdk/'zipalign.exe','-c','-P','16','4',apk)
def cert(path):
    output=subprocess.check_output([str(java/'java.exe'),'-jar',str(sdk/'lib/apksigner.jar'),'verify','--print-certs',str(path)],text=True)
    return re.search(r'certificate SHA-256 digest: (\w+)',output)[1]
assert cert(apk)==cert(baseline),'Host upgrade signing identity changed'
with zipfile.ZipFile(apk) as z:
    assert z.testzip() is None
    for n,b in original.items():
        if n in ('classes.dex','classes2.dex','resources.arsc') or n.startswith(('res/','lib/','assets/')):assert z.read(n)==b,n
result=dict(hostSha256=sha(apk.read_bytes()),uiSha256=sha(ui.read_bytes()),baselineSha256=sha(baseline.read_bytes()),checks='signature identity, CRC, UI resource package 0x80, original API/HMC DEX/resources/assets/libs exact, non-SettingsExtension core classes preserved',device='PENDING: no install/boot/Activity/relay/render acceptance',scope='Settings navigation/config relay + lazy isolated Host UI; no runtime native/HMC changes')
(out/'verification.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8')
print('HOST_UI_PACKAGING_PASS',json.dumps(result))
