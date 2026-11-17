import com.mixi.manager.StandbyTimeValue;
public class StandbyTimeTest {
 public static void main(String[]args){
  for(int n=10;n<=86400;n++){
   Integer actual=StandbyTimeValue.parse(StandbyTimeValue.display(n,true),true,86400);
   if(actual==null||actual!=n)throw new AssertionError("Roundtrip "+n);
  }
  if(StandbyTimeValue.parse("1.5",true,3600)!=90||StandbyTimeValue.parse("1,5",true,3600)!=90)throw new AssertionError();
  for(String invalid:new String[]{"NaN","-1","1e9","0.1","60.1"})if(StandbyTimeValue.parse(invalid,true,3600)!=null)throw new AssertionError(invalid);
  if(StandbyTimeValue.parse("1.5",false,3600)!=null)throw new AssertionError("Fractional seconds");
  System.out.println("STANDBY_TIME_ALL_SECONDS_ROUNDTRIP_AND_BOUNDS_PASS");
 }
}
