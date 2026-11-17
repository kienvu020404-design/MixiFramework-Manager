"""Execute the production Apply method with fake Android transport, never on a phone."""
from pathlib import Path
import subprocess
manager=Path(__file__).resolve().parents[1];root=manager.parents[1]
source=(manager/'host-integration/ManagerBridge.java').read_text(encoding='utf-8')
start=source.index('    private static void applyExtensions(')
end=source.index('    private static Bundle previewAssets(',start)
method=source[start:end].replace('android.content.SharedPreferences','SharedPreferences').replace('validate("change",','ManagerBridge.validate("change",')
fixture='''package mixi.settings;
import java.util.*;import java.util.concurrent.*;
public class ApplyTest {
 static class SystemClock {static volatile long offset;static volatile CountDownLatch firstRead;static long elapsedRealtime(){long value=System.nanoTime()/1000000+offset;if(firstRead!=null){CountDownLatch latch=firstRead;firstRead=null;latch.countDown();}return value;}}
 static class Bundle {Map<String,Object> map=new HashMap<>();void putString(String k,String v){map.put(k,v);}boolean getBoolean(String k){return Boolean.TRUE.equals(map.get(k));}}
 static class Uri {static Uri parse(String v){return new Uri();}}
 static class Intent {static final int FLAG_RECEIVER_FOREGROUND=1;String target,action;Intent(String a){action=a;}Intent setPackage(String p){target=p;return this;}Intent addFlags(int f){return this;}}
 static class BroadcastOptions {static BroadcastOptions makeBasic(){return new BroadcastOptions();}void setShareIdentityEnabled(boolean b){}Bundle toBundle(){return new Bundle();}}
 static class SharedPreferences {Map<String,String> values=new ConcurrentHashMap<>();boolean contains(String k){return values.containsKey(k);}String getString(String k,String d){return values.getOrDefault(k,d);}}
 static class Context {List<String> events=new CopyOnWriteArrayList<>();boolean fail;Context getContentResolver(){return this;}Bundle call(Uri u,String m,String arg,Bundle b){events.add("save:"+b.map.get("key")+"="+b.map.get("value"));Bundle reply=new Bundle();reply.map.put("saved",!fail);return reply;}void sendBroadcast(Intent i,String permission,Bundle options){if(!"com.mixi.host.MASTER_RELOAD".equals(i.action))throw new AssertionError();events.add("reload:"+i.target);}}
 static class EnhancedProtocol {static ExecutorService worker=Executors.newSingleThreadExecutor();}
 __METHOD__
 public static void main(String[] args)throws Exception {
  ExecutorService caller=Executors.newSingleThreadExecutor();
  try {
   SharedPreferences prefs=new SharedPreferences();prefs.values.put("appearance.solidBattery","false");prefs.values.put("appearance.ccBatteryPercent","true");Context app=new Context();
   CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);
   EnhancedProtocol.worker.submit(()->{entered.countDown();try{release.await();}catch(InterruptedException e){throw new RuntimeException(e);}});entered.await();
   Future<?> apply=caller.submit(()->{try{applyExtensions(app,prefs,SystemClock.elapsedRealtime()+7500);}catch(Exception e){throw new RuntimeException(e);}});
   Thread.sleep(100);if(!app.events.isEmpty())throw new AssertionError("Applied before earlier drain");
   prefs.values.put("appearance.solidBattery","true");release.countDown();apply.get(3,TimeUnit.SECONDS);
   if(!app.events.equals(List.of("save:solidBattery=true","save:ccBatteryPercent=true","reload:com.android.systemui")))throw new AssertionError(app.events);
   Context failure=new Context();failure.fail=true;
   try{applyExtensions(failure,prefs,SystemClock.elapsedRealtime()+7500);throw new AssertionError("Expected failed save");}catch(ExecutionException expected){}
   if(failure.events.stream().anyMatch(e->e.startsWith("reload:")))throw new AssertionError("Reload after failed save");
   Context expired=new Context();CountDownLatch blocked=new CountDownLatch(1),resume=new CountDownLatch(1);
   EnhancedProtocol.worker.submit(()->{blocked.countDown();try{resume.await();}catch(Exception e){throw new RuntimeException(e);}});blocked.await();
   SystemClock.firstRead=new CountDownLatch(1);CountDownLatch clockRead=SystemClock.firstRead;
   Future<?> expiry=caller.submit(()->{try{applyExtensions(expired,prefs,SystemClock.elapsedRealtime()+7500);}catch(Exception expected){return;}throw new AssertionError("Expected expiry");});
   if(!clockRead.await(2,TimeUnit.SECONDS))throw new AssertionError("Apply did not start");SystemClock.offset=10000;resume.countDown();expiry.get(3,TimeUnit.SECONDS);
   if(!expired.events.isEmpty())throw new AssertionError("Expired Apply still wrote/reloaded");
   System.out.println("SOURCE_EXTRACTED_APPLY_SERIALIZATION_SAVE_FAILURE_EXPIRY_PASS");
  } finally {caller.shutdownNow();EnhancedProtocol.worker.shutdownNow();}
 }
}'''.replace('__METHOD__',method)
out=root/'out/manager-host/apply-test';out.mkdir(exist_ok=True)
path=out/'ApplyTest.java';path.write_text(fixture,encoding='utf-8')
java=root/'tools/jdk/jdk-21.0.13+9/bin';android=root/'tools/sdk/platforms/android-37.0/android.jar'
cp=str(root/'out/manager-host/settings-classes')+';'+str(android)
subprocess.run([str(java/'javac.exe'),'-cp',cp,'-d',str(out),str(path)],check=True)
subprocess.run([str(java/'java.exe'),'-cp',str(out)+';'+cp,'mixi.settings.ApplyTest'],check=True)
