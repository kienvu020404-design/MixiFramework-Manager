"""Local boundary/acknowledgement checks, not a device acceptance substitute."""
from pathlib import Path
import json, subprocess, zipfile

manager=Path(__file__).resolve().parents[1];root=manager.parents[1]
out=root/'out/manager-host';java=root/'tools/jdk/jdk-21.0.13+9/bin'
android=root/'tools/sdk/platforms/android-37.0/android.jar'
def run(*args):subprocess.run(list(map(str,args)),check=True)
classes=out/'test-classes';classes.mkdir(exist_ok=True)
run(java/'javac.exe','-cp',str(out/'settings-classes')+';'+str(android),'-d',classes,manager/'host-integration/BridgeValidationTest.java')
run(java/'java.exe','-cp',';'.join(map(str,[classes,out/'settings-classes',android])),'mixi.settings.BridgeValidationTest')

# Execute the exact generated acknowledgement block, with a newer edit arriving
# while the old provider operation is blocked. No Android method is simulated.
protocol=(out/'EnhancedProtocol.java').read_text(encoding='utf-8')
start=protocol.index('                    synchronized (EnhancedProtocol.class)',protocol.index('notifyGlass(context);'))
end=protocol.index('\n                    continue;',start)
ack=protocol[start:end]
fixture='''import java.util.*;import java.util.concurrent.*;
public class EnhancedProtocol {
 static final Map<String,String> pending=new HashMap<>();
 static final Pref preferences=new Pref();
 static class Pref { volatile String value="20"; volatile boolean marked=true;
  String getString(String key,String fallback){return value;}
  Pref edit(){return this;} Pref remove(String key){marked=false;return this;} void apply(){}
 }
 static void ack(String key,String value){__ACK_BLOCK__}
 public static void main(String[] args)throws Exception{
  String key="appearance.blur";pending.put(key,"20");
  CountDownLatch applying=new CountDownLatch(1),release=new CountDownLatch(1);
  Thread first=new Thread(()->{applying.countDown();try{release.await();}catch(Exception e){throw new RuntimeException(e);}ack(key,"20");});
  first.start();applying.await();synchronized(EnhancedProtocol.class){preferences.value="35";pending.put(key,"35");}
  release.countDown();first.join();
  if(!"35".equals(pending.get(key))||!preferences.marked)throw new AssertionError("New edit lost");
  ack(key,"35");if(!pending.isEmpty()||preferences.marked)throw new AssertionError("Current edit not acknowledged");
  pending.put(key,"35");preferences.value="40";preferences.marked=true;ack(key,"35");
  if(!preferences.marked)throw new AssertionError("New persisted edit lost its retry marker");
  System.out.println("SOURCE_EXTRACTED_APPEARANCE_ACK_RACE_PASS");
 }
}'''.replace('__ACK_BLOCK__',ack)
source=out/'ack-test/EnhancedProtocol.java';source.parent.mkdir(exist_ok=True);source.write_text(fixture,encoding='utf-8')
run(java/'javac.exe','-d',source.parent,source)
run(java/'java.exe','-cp',source.parent,'EnhancedProtocol')

bridge=(manager/'host-integration/ManagerBridge.java').read_text(encoding='utf-8')
assert 'getSentFromUid() != hostUid' in bridge and 'getSentFromPackage()' in bridge
assert 'ArrayBlockingQueue<>(8)' in bridge and 'main.postDelayed(timeout,8000)' in bridge
assert 'finished.compareAndSet(false,true)' in bridge and 'requests.execute(' in bridge
assert 'EnhancedProtocol.worker.execute(' not in bridge
factory=(manager/'host-integration/HostUiFactory.java').read_text(encoding='utf-8')
assert factory.index('Files.deleteIfExists(temporary.toPath())')<factory.index('new FileOutputStream(temporary)')
assert factory.index('temporary.setReadOnly()')<factory.index('output.write(bytes)')
assert 'ClassLoader.getSystemClassLoader().getParent()' in factory
print('AUTH_DEADLINE_EXTRACTION_SOURCE_CONTRACT_PASS (not Android runtime proof)')

manifest=(out/'AndroidManifest.xml').read_text(encoding='utf-8')
assert 'android:process=":manager"' in manifest and 'android:appComponentFactory="mixi.host.ui.HostUiFactory"' in manifest
assert 'category.LAUNCHER' not in manifest and 'BOOT_COMPLETED' not in manifest
with zipfile.ZipFile(out/'MixiHost-UI-Test.apk') as host:
 assert host.testzip() is None
 with zipfile.ZipFile(__import__('io').BytesIO(host.read('assets/mixi-ui/module.apk'))) as ui:assert ui.testzip() is None
verification=json.loads((out/'verification.json').read_text())
verification['localTests']='92 catalog defaults/invalid values; source-extracted acknowledgement race; auth/deadline/extraction source contracts; nested APK CRC PASS'
(out/'verification.json').write_text(json.dumps(verification,indent=2)+'\n',encoding='utf-8')
preview=(manager/'app/src/main/kotlin/com/mixi/manager/SoftGlassPreview.kt').read_text(encoding='utf-8')
assert 'textureBlur(' not in preview and 'setMiGlass' in preview
assert 'loadPreview()' in preview and 'previewAssets' in bridge
print('HOST_UI_LOCAL_CHECKS_PASS')

assert factory.index("if (!target.isFile()") < factory.index('getEntry("assets/mixi-ui/module.apk")')
print("CACHE_HIT_SKIPS_EMBEDDED_MODULE_READ_SOURCE_PASS")

assert 'getMethod("setMiViewMaterialType"' in preview
assert 'getMethod("setPassWindowBlurEnabled"' in preview
assert "invoke(panel,blur.toInt(),(blur*2f).toInt())" in preview
assert "invoke(panel,(80f*background/100f).toInt())" in preview
print("OEM_MATERIAL_INITIALIZATION_SOURCE_CONTRACT_PASS (visual parity pending)")

assert "if(image.tag !== bitmap)" in preview
assert "val active=enabled&&visible&&foreground" in preview
assert "panel.tag==configuration" in preview and "panel.tag=configuration" in preview
assert "lifecycle.removeObserver(observer)" in preview
main=(manager/"app/src/main/kotlin/com/mixi/manager/MainActivity.kt").read_text(encoding="utf-8")
assert ".padding(end=16.dp).size(128.dp)" in main
assert "Color(0xFFE0F1E4)" in main and "color=content" in main
extension=(out/"SettingsExtension.java").read_text(encoding="utf-8")
assert 'Method open=null;' not in extension
assert 'if(!"ALREADY_PLACED".equals(result))' in extension
print("PREVIEW_LIFECYCLE_IDENTITY_THEME_ENTRY_SOURCE_CONTRACT_PASS (device acceptance pending)")

assert 'title="Mã nguồn Manager"' not in main and 'Intent.ACTION_VIEW' not in main
assert 'var message by' not in main and 'hmcAlive' not in main
assert 'HmcProtocol.send(app,"query"' not in bridge
assert 'snapshot(app,saved,"immersiveQuery".equals(op))' in bridge
assert 'title=if(store.applyingExtensions)' in main and 'store.applyExtensions()' in main
assert 'action.cancel(false)' in bridge and 'Apply expired' in bridge
island=(manager/"app/src/main/kotlin/com/mixi/manager/IslandDemo.kt").read_text(encoding="utf-8")
assert 'if(visible&&lifecycle.currentState.isAtLeast' in island
assert 'if(it.drawable !== drawable)' in island
assert 'suspend fun loadPreview()' in main and 'fun stopReads()' in main
assert 'repeatOnLifecycle(Lifecycle.State.RESUMED)' in preview
print("NO_SOURCE_LINK_NO_UNUSED_PROBE_APPLY_AND_READ_LIFECYCLE_SOURCE_PASS")

assert 'title="Framework",summary=frameworkVersion' in main
assert 'getPackageInfo("com.mixi.host",0).versionName' in main
assert 'withContext(Dispatchers.IO)' in main
assert 'versionName="1.0.7"' in manifest and 'versionCode="100700"' in manifest
with zipfile.ZipFile(out/'MixiHost-UI-Test.apk') as host:
 pins=dict(line.split('=',1) for line in host.read('META-INF/mixi/pins/settings.properties').decode().splitlines() if '=' in line)
 assert pins['frameworkVersion']=='1.0.7'
 assert 'public void before(HookRegistry.Call call)' not in extension[extension.index('"mixi-header-icon"'):extension.index('Method click=')]
print('MANAGER_FRAMEWORK_VERSION_PAIRING_ACCEPTED_ICON_PATH_PASS')

assert 'Bật chức năng này có thể tăng nhẹ điện năng tiêu thụ' in main

assert 'ViewModelProvider(this' in main and 'ManagerState(applicationContext)' in main
assert 'UiStore(context.applicationContext,viewModelScope)' in main
assert 'drafts.remove(key)' in preview and 'onPreviewChange?.invoke(option.key,null)' in main
assert 'requireTime(requestDeadline)' in bridge and 'Math.min(requestDeadline' in bridge
print('SIXFIX_VIEWMODEL_PREVIEW_AND_REQUEST_DEADLINE_SOURCE_CONTRACT_PASS')
