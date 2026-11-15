import com.android.tools.smali.dexlib2.*;
import com.android.tools.smali.dexlib2.iface.*;
import com.android.tools.smali.dexlib2.immutable.*;
import java.util.*;
public final class VerifyHostDex {
    static byte[] canonical(ClassDef c, Opcodes op) throws Exception {
        var store=new com.android.tools.smali.dexlib2.writer.io.MemoryDataStore();
        com.android.tools.smali.dexlib2.writer.pool.DexPool.writeTo(store,new ImmutableDexFile(op,List.of(c)));
        return store.getData();
    }
    public static void main(String[] args) throws Exception {
        var before=DexFileFactory.loadDexFile(args[0],Opcodes.forDexVersion(39));
        var after=DexFileFactory.loadDexFile(args[1],before.getOpcodes());
        Map<String,ClassDef> classes=new HashMap<>();
        for(ClassDef c:after.getClasses())classes.put(c.getType(),ImmutableClassDef.of(c));
        int count=0;
        for(ClassDef c:before.getClasses()) {
            String name=c.getType();
            if(name.equals("Lmixi/settings/SettingsExtension;")||name.startsWith("Lmixi/settings/SettingsExtension$"))continue;
            if(!Arrays.equals(canonical(ImmutableClassDef.of(c),before.getOpcodes()),canonical(classes.get(name),before.getOpcodes())))throw new AssertionError(name);
            count++;
        }
        System.out.println("NON_SETTINGS_EXTENSION_CORE_CLASSES_UNCHANGED "+count);
    }
}
