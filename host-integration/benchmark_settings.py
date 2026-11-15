"""Measure stock Settings launch states. No install, force-stop, data or ART changes."""
from pathlib import Path
import argparse,json,re,statistics,subprocess,time
parser=argparse.ArgumentParser();parser.add_argument('--label',required=True);parser.add_argument('--runs',type=int,default=5)
args=parser.parse_args();assert 1<=args.runs<=10
assert re.fullmatch(r"[A-Za-z0-9_-]{1,64}",args.label)
root=Path(__file__).resolve().parents[3];adb=root/'tools/sdk/platform-tools/adb.exe'
def shell(command):return subprocess.check_output([str(adb),'shell',command],encoding='utf-8',timeout=30).strip()
rows=[]
host=shell('sha256sum /system_ext/priv-app/MixiHost/MixiHost.apk').split()[0]
for requested in ('cold','warm','hot'):
 for i in range(args.runs):
  shell('input keyevent KEYCODE_HOME');time.sleep(1)
  if requested=='cold':shell('am kill com.android.settings') # cached/background Settings only
  pid_before=shell('pidof com.android.settings || true')
  output=shell('am start -W -n com.android.settings/.MiuiSettings')
  state=re.search(r'LaunchState: (\S+)',output);duration=re.search(r'TotalTime: (\d+)',output)
  actual=state[1] if state else 'UNREPORTED'
  row=dict(requested=requested,actual=actual,pidBefore=pid_before,pidAfter=shell('pidof com.android.settings || true'),ms=int(duration[1]) if duration else None)
  rows.append(row)
  # Back finishes the Settings page for the next warm sample; Home retains it for hot.
  if requested in ('cold','warm'):shell('input keyevent KEYCODE_BACK')
  time.sleep(1)
shell('input keyevent KEYCODE_HOME')
summary={}
for state in ('COLD','WARM','HOT'):
 values=[r['ms'] for r in rows if r['actual']==state and r['ms'] is not None and r['ms']>0]
 summary[state]=dict(samples=values,medianMs=statistics.median(values) if values else None)
result=dict(label=args.label,hostSha256=host,rows=rows,summary=summary,limits='Actual Android states only; no forced stop/cache wipe. Uncontrolled thermal/frequency conditions. Before/after requires same procedure.')
path=root/'out'/('settings-launch-'+args.label+'.json');path.write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps(summary))
