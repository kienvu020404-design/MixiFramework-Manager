"""Execute the production expiry guard with a blocked request queue."""
from pathlib import Path
import subprocess

manager = Path(__file__).resolve().parents[1]
root = manager.parents[1]
source = (manager / 'host-integration/ManagerBridge.java').read_text(encoding='utf-8')
start = source.index('    private static void requireTime(')
end = source.index('    private static void applyExtensions(', start)
method = source[start:end]
fixture = '''import java.util.concurrent.*;
public class RequestDeadlineTest {
 static class SystemClock {static volatile long now;static long elapsedRealtime(){return now;}}
 __METHOD__
 public static void main(String[]args)throws Exception {
  ExecutorService queue=Executors.newSingleThreadExecutor();
  try {
   CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);
   queue.submit(()->{entered.countDown();try{release.await();}catch(InterruptedException e){throw new RuntimeException(e);}});
   entered.await();java.util.concurrent.atomic.AtomicInteger writes=new java.util.concurrent.atomic.AtomicInteger();
   Future<?> expired=queue.submit(()->{requireTime(7500);writes.incrementAndGet();});
   SystemClock.now=8000;release.countDown();
   try{expired.get();throw new AssertionError("Expired write ran");}
   catch(ExecutionException expected){if(!(expected.getCause() instanceof IllegalStateException))throw expected;}
   if(writes.get()!=0)throw new AssertionError("Late write");
   SystemClock.now=9000;queue.submit(()->{requireTime(16500);writes.incrementAndGet();}).get();
   if(writes.get()!=1)throw new AssertionError("Valid write rejected");
   System.out.println("REQUEST_QUEUE_EXPIRED_WRITE_REJECTED_VALID_WRITE_PASS");
  } finally {queue.shutdownNow();}
 }
}'''.replace('__METHOD__', method)
out = root / 'out/manager-host/deadline-test'
out.mkdir(parents=True, exist_ok=True)
path = out / 'RequestDeadlineTest.java'
path.write_text(fixture, encoding='utf-8')
java = root / 'tools/jdk/jdk-21.0.13+9/bin'
subprocess.run([str(java / 'javac.exe'), '-d', str(out), str(path)], check=True)
subprocess.run([str(java / 'java.exe'), '-cp', str(out), 'RequestDeadlineTest'], check=True)
