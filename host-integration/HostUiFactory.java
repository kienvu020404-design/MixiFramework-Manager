package mixi.host.ui;

import android.app.Activity;
import android.app.AppComponentFactory;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import dalvik.system.DexClassLoader;
import java.io.*;
import java.security.MessageDigest;
import java.util.zip.ZipFile;

/** Keep UI dependencies out of the original Host/HMC classloader. */
public final class HostUiFactory extends AppComponentFactory {
    private ApplicationInfo info;
    private ClassLoader ui;
    @Override public ClassLoader instantiateClassLoader(ClassLoader loader, ApplicationInfo info) {
        this.info = info;
        return loader;
    }
    private static String digest(byte[] bytes) throws Exception {
        StringBuilder out = new StringBuilder();
        for (byte b : MessageDigest.getInstance("SHA-256").digest(bytes)) out.append(String.format(java.util.Locale.ROOT,"%02x", b & 255));
        return out.toString();
    }
    private synchronized ClassLoader uiLoader() throws Exception {
        if (ui != null) return ui;
        if (info == null) throw new IllegalStateException("Missing Host application info");
        byte[] bytes;
        String expected;
        try (ZipFile apk = new ZipFile(info.sourceDir)) {
            expected = new String(apk.getInputStream(apk.getEntry("assets/mixi-ui/sha256")).readAllBytes(), java.nio.charset.StandardCharsets.US_ASCII).trim();
        }
        File folder = new File(info.dataDir, "code_cache/mixi-ui");
        if (!folder.isDirectory() && !folder.mkdirs()) throw new IOException("UI cache directory");
        File target = new File(folder, "module.apk");
        if (!target.isFile() || !digest(java.nio.file.Files.readAllBytes(target.toPath())).equals(expected)) {
            try (ZipFile apk = new ZipFile(info.sourceDir)) {
                bytes = apk.getInputStream(apk.getEntry("assets/mixi-ui/module.apk")).readAllBytes();
            }
            if (!digest(bytes).equals(expected)) throw new SecurityException("UI module checksum");
            File temporary = new File(folder, "module.tmp");
            java.nio.file.Files.deleteIfExists(temporary.toPath());
            try (FileOutputStream output = new FileOutputStream(temporary)) {
                if (!temporary.setReadOnly()) throw new IOException("UI code must be read-only");
                output.write(bytes);
                output.getFD().sync();
            }
            if (!temporary.renameTo(target)) throw new IOException("UI cache replacement");
        }
        if (!target.setReadOnly()) throw new IOException("UI cache must be read-only");
        System.setProperty("mixi.ui.module.path", target.getAbsolutePath());
        ui = new DexClassLoader(target.getAbsolutePath(), null, null, ClassLoader.getSystemClassLoader().getParent());
        return ui;
    }
    @Override public Activity instantiateActivity(ClassLoader loader, String name, Intent intent)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (!name.equals("com.mixi.manager.MainActivity")) return super.instantiateActivity(loader,name,intent);
        try { return super.instantiateActivity(uiLoader(),name,intent); }
        catch (Exception failure) { throw new ClassNotFoundException("Host UI module unavailable",failure); }
    }
}
