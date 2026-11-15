package mixi.settings;

/** Pure-JVM boundary checks; does not claim live Binder/broadcast acceptance. */
public final class BridgeValidationTest {
    private static void rejected(String op,String key,String value) {
        try { ManagerBridge.validate(op,key,value); }
        catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Accepted invalid configuration: "+key);
    }
    public static void main(String[] args) {
        int checked=0;
        for(EnhancedCatalog.Field field:EnhancedCatalog.FIELDS) {
            ManagerBridge.validate("change",field.key,field.initial);
            if(field.kind.equals("bool"))rejected("change",field.key,"1");
            if(field.kind.equals("range")||field.kind.equals("time")){
                rejected("change",field.key,Integer.toString(field.min-1));
                rejected("change",field.key,Integer.toString(field.max+1));
                rejected("change",field.key,"NaN");
            }
            if(field.kind.equals("choice"))rejected("change",field.key,"INVALID_CHOICE");
            checked++;
        }
        ManagerBridge.validate("query",null,null);
        ManagerBridge.validate("preview",null,null);
        ManagerBridge.validate("change","enhanced.master","true");
        rejected("change","enhanced.master","1");
        rejected("change","appearance.untrusted","10");
        rejected("change","immersive.idleSeconds","1");
        rejected("change",null,"true");
        rejected("change","clockSize",null);
        rejected("arbitraryCommand",null,null);
        System.out.println("BRIDGE_BOUNDARY_VALIDATION_PASS "+checked+" fields");
    }
}
