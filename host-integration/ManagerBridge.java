package mixi.settings;

import android.app.BroadcastOptions;
import android.content.*;
import android.net.Uri;
import android.os.*;

/** Settings remains the authenticated owner of existing configuration protocols. */
public final class ManagerBridge {
    public static final String ACTION = "com.mixi.host.SETTINGS_REQUEST";
    private static boolean attached;
    private static final java.util.concurrent.ThreadPoolExecutor requests = new java.util.concurrent.ThreadPoolExecutor(
        0,1,15,java.util.concurrent.TimeUnit.SECONDS,new java.util.concurrent.ArrayBlockingQueue<>(8),task -> new Thread(task,"MixiManagerRequests"));
    public static synchronized void attach(Context context) {
        if (attached) return;
        Context app = context.getApplicationContext();
        app.registerReceiver(new BroadcastReceiver() {
            @Override public void onReceive(Context ignored, Intent intent) {
                try {
                    int hostUid = app.getPackageManager().getApplicationInfo("com.mixi.host",0).uid;
                    if (getSentFromUid() != hostUid || !"com.mixi.host".equals(getSentFromPackage())) return;
                    String op = intent.getStringExtra("op");
                    String key = intent.getStringExtra("key"), value = intent.getStringExtra("value");
                    validate(op,key,value);
                    PendingResult result = goAsync();
                    java.util.concurrent.atomic.AtomicBoolean finished = new java.util.concurrent.atomic.AtomicBoolean();
                    Handler main = new Handler(Looper.getMainLooper());
                    Runnable timeout = () -> { if(finished.compareAndSet(false,true)){result.setResultCode(-1);result.finish();} };
                    main.postDelayed(timeout,8000);
                    try { requests.execute(() -> {
                        Bundle reply; int code;
                        try {
                            android.content.SharedPreferences saved = app.getSharedPreferences("mixi_enhanced_r1",Context.MODE_PRIVATE);
                            if ("previewAssets".equals(op)) {
                                reply = previewAssets(app); code = 1;
                            } else {
                            if ("change".equals(op)) {
                                if (key.equals("enhanced.master")) master(app,Boolean.parseBoolean(value));
                                else {
                                    if (!saved.edit().putString(key,value).commit()) throw new IllegalStateException("Configuration not saved");
                                    EnhancedProtocol.changed(app,saved,key,value);
                                }
                            } else if ("applyExtensions".equals(op)) applyExtensions(app,saved);
                            else if ("preview".equals(op)) ImmersiveClient.call(app,"preview",null);
                            else if ("query".equals(op)) EnhancedProtocol.replay(app,saved);
                            reply = snapshot(app,saved,"immersiveQuery".equals(op)); code = 1;
                            }
                        } catch (Exception failure) {
                            reply = new Bundle(); reply.putString("error",failure.getClass().getSimpleName()); code = -1;
                        }
                        main.removeCallbacks(timeout);
                        if(finished.compareAndSet(false,true)){result.setResultCode(code);result.setResultExtras(reply);result.finish();}
                    }); } catch (java.util.concurrent.RejectedExecutionException busy) {
                        main.removeCallbacks(timeout); timeout.run();
                    }
                } catch (Exception rejected) { setResultCode(-1); }
            }
        }, new IntentFilter(ACTION),Context.RECEIVER_EXPORTED);
        attached = true;
    }
    static void validate(String op,String key,String value) {
        if ("query".equals(op) || "preview".equals(op) || "previewAssets".equals(op) || "immersiveQuery".equals(op) || "applyExtensions".equals(op)) return;
        if (!"change".equals(op) || key == null || value == null) throw new IllegalArgumentException("Invalid request");
        if (key.equals("enhanced.master") && (value.equals("true") || value.equals("false"))) return;
        for (EnhancedCatalog.Field field : EnhancedCatalog.FIELDS)
            if (field.key.equals(key) && field.accepts(value)) return;
        throw new IllegalArgumentException("Unknown setting/value");
    }
    private static void applyExtensions(Context app,android.content.SharedPreferences saved) throws Exception {
        // Run after the existing drain, including its failure path, before reloading SystemUI.
        final long deadline=SystemClock.elapsedRealtime()+5500;
        java.util.concurrent.Future<?> action=EnhancedProtocol.worker.submit(() -> {
            for(String key:new String[]{"appearance.solidBattery","appearance.ccBatteryPercent"}) {
                if(SystemClock.elapsedRealtime()>=deadline)throw new IllegalStateException("Apply expired");
                if(!saved.contains(key))continue;
                String value=saved.getString(key,"false");
                validate("change",key,value);
                Bundle edit=new Bundle();edit.putString("key",key.substring(11));edit.putString("value",value);
                Bundle reply=app.getContentResolver().call(Uri.parse("content://com.mixi.host.master"),"appearance",null,edit);
                if(reply==null||!reply.getBoolean("saved"))throw new IllegalStateException("Extension save failed");
            }
            if(SystemClock.elapsedRealtime()>=deadline)throw new IllegalStateException("Apply expired");
            BroadcastOptions options=BroadcastOptions.makeBasic();options.setShareIdentityEnabled(true);
            app.sendBroadcast(new Intent("com.mixi.host.MASTER_RELOAD").setPackage("com.android.systemui")
                .addFlags(Intent.FLAG_RECEIVER_FOREGROUND),null,options.toBundle());
        });
        try{action.get(6,java.util.concurrent.TimeUnit.SECONDS);}
        catch(java.util.concurrent.TimeoutException expired){action.cancel(false);throw expired;}
    }
    private static Bundle previewAssets(Context app) throws Exception {
        Bundle result=new Bundle();
        android.graphics.drawable.Drawable wallpaper=android.app.WallpaperManager.getInstance(app).getDrawable();
        if(wallpaper==null)throw new IllegalStateException("Wallpaper unavailable");
        int width=480,height=Math.max(1,Math.min(1200,wallpaper.getIntrinsicHeight()*width/Math.max(1,wallpaper.getIntrinsicWidth())));
        android.graphics.Bitmap bitmap=android.graphics.Bitmap.createBitmap(width,height,android.graphics.Bitmap.Config.ARGB_8888);
        wallpaper.setBounds(0,0,width,height);wallpaper.draw(new android.graphics.Canvas(bitmap));
        java.io.ByteArrayOutputStream output=new java.io.ByteArrayOutputStream();
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG,75,output);bitmap.recycle();
        if(output.size()>256*1024)throw new IllegalStateException("Wallpaper preview too large");
        result.putByteArray("wallpaper",output.toByteArray());
        String apk=app.getPackageManager().getApplicationInfo("com.aritxonly.myhypermodifier",0).sourceDir;
        ClassLoader loader=new dalvik.system.DexClassLoader(apk,null,null,ClassLoader.getSystemClassLoader().getParent());
        float[] recipe=(float[])loader.loadClass("d52").getMethod("r").invoke(null);
        if(recipe==null||recipe.length!=42)throw new IllegalStateException("Glass recipe mismatch");
        result.putFloatArray("recipe",recipe);return result;
    }
    private static void master(Context app,boolean enabled) {
        Bundle edit = new Bundle(); edit.putBoolean("enabled",enabled);
        Bundle reply = app.getContentResolver().call(Uri.parse("content://com.mixi.host.master"),"set",null,edit);
        if (reply == null || !reply.getBoolean("saved")) throw new IllegalStateException("Master save failed");
        for (String target : new String[]{"com.android.systemui","com.miui.miwallpaper","com.miui.aod"}) {
            BroadcastOptions options = BroadcastOptions.makeBasic(); options.setShareIdentityEnabled(true);
            app.sendBroadcast(new Intent("com.mixi.host.MASTER_RELOAD").setPackage(target)
                .addFlags(Intent.FLAG_RECEIVER_FOREGROUND),null,options.toBundle());
        }
    }
    private static Bundle snapshot(Context app,android.content.SharedPreferences saved,boolean probe) throws Exception {
        Bundle result = new Bundle(), values = new Bundle();
        Bundle master = app.getContentResolver().call(Uri.parse("content://com.mixi.host.master"),"get",null,null);
        if (master == null) throw new IllegalStateException("Host provider unavailable");
        boolean pending = false;
        for (EnhancedCatalog.Field field : EnhancedCatalog.FIELDS) {
            String value = field.key.startsWith("appearance.") && !saved.getBoolean("pending."+field.key,false) ? master.getString(field.key,field.initial) : saved.getString(field.key,field.initial);
            values.putString(field.key,field.accepts(value) ? value : field.initial);
            pending |= saved.getBoolean("pending."+field.key,false);
        }
        values.putString("enhanced.master",Boolean.toString(master.getBoolean("enabled",true)));
        if (probe) try {
            Bundle immersive = ImmersiveClient.call(app,"query",null);
            result.putBundle("immersive",immersive);
            for (EnhancedCatalog.Field field : EnhancedCatalog.FIELDS) if (field.key.startsWith("immersive.") && !saved.getBoolean("pending."+field.key,false)) {
                Object actual = immersive.get(field.key.substring(10));
                if (actual != null && field.accepts(String.valueOf(actual))) values.putString(field.key,String.valueOf(actual));
            }
        } catch (Exception unavailable) { result.putBoolean("immersiveUnavailable",true); }
        result.putBundle("values",values); result.putBoolean("pending",pending);
        return result;
    }
}
