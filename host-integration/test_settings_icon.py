"""Exercise the exact generated callback with fake Views; no Android hooks installed."""
from pathlib import Path
import subprocess
manager=Path(__file__).resolve().parents[1];root=manager.parents[1];out=root/'out/manager-host'
source=(out/'SettingsExtension.java').read_text(encoding='utf-8')
start=source.index('public void before(HookRegistry.Call call)throws Exception{',source.index('final Field holderIcon'))
start=source.index('{',start)+1;end=source.index('\n  }});',start)
body=source[start:end].replace('android.widget.ImageView','Image').replace('android.view.View.VISIBLE','0')
fixture='''import java.lang.reflect.*;
public class SettingsIconTest {
 public static class Header {public String fragment;Header(String f){fragment=f;}}
 public static class Row {public Image icon;Row(Image i){icon=i;}}
 public static class Image {int bitmaps,placeholders,visibility=8;Object bitmap;void setImageBitmap(Object b){bitmaps++;bitmap=b;}void setVisibility(int v){visibility=v;}}
 static class HeaderPlacement {static final String FRAGMENT="mixi.settings.MixiSettingsFragment";}
 static final Object headerIcon=new Object();
 static Field holderIcon,headerFragment;
 static class Call {Object[] values;boolean handled;Call(Object row,Object header){values=new Object[]{row,header};}Object[] arguments(){return values;}void returnValue(Object ignored){handled=true;}}
 static void callback(Call call)throws Exception{__BODY__}
 static void bind(Row row,Header header)throws Exception{Call call=new Call(row,header);callback(call);if(!call.handled&&row!=null&&row.icon!=null)row.icon.placeholders++;}
 public static void main(String[] args)throws Exception{
  holderIcon=Row.class.getDeclaredField("icon");headerFragment=Header.class.getDeclaredField("fragment");
  Image own=new Image();bind(new Row(own),new Header(HeaderPlacement.FRAGMENT));
  if(own.bitmaps!=1||own.placeholders!=0||own.bitmap!=headerIcon||own.visibility!=0)throw new AssertionError("Owned row mismatch");
  Image other=new Image();bind(new Row(other),new Header("com.android.settings.device.MiuiMyDeviceSettings"));
  if(other.bitmaps!=0||other.placeholders!=1)throw new AssertionError("Touched another row");
  bind(null,new Header(HeaderPlacement.FRAGMENT));bind(new Row(null),new Header(HeaderPlacement.FRAGMENT));bind(new Row(new Image()),null);
  System.out.println("SOURCE_EXTRACTED_OWNED_ICON_SKIP_OTHER_ROWS_NULL_GUARDS_PASS");
 }
}'''.replace('__BODY__',body)
folder=out/'settings-icon-test';folder.mkdir(exist_ok=True);path=folder/'SettingsIconTest.java';path.write_text(fixture,encoding='utf-8')
java=root/'tools/jdk/jdk-21.0.13+9/bin'
subprocess.run([str(java/'javac.exe'),'-d',str(folder),str(path)],check=True)
subprocess.run([str(java/'java.exe'),'-cp',str(folder),'SettingsIconTest'],check=True)
assert 'if("OS4.0.4.0.XOBCNXM".equals(android.os.Build.VERSION.INCREMENTAL))' in source
assert 'public void after(HookRegistry.Call call)' in source[source.index('final Field holderIcon'):]
print('VERIFIED_FIRMWARE_GUARD_AND_OEM_FALLBACK_SOURCE_PASS (device speed pending)')
