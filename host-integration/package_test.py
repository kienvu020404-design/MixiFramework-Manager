"""Create a reversible module test; never install it or change the accepted ZIP."""
from pathlib import Path
import hashlib,json,zipfile
manager=Path(__file__).resolve().parents[1];root=manager.parents[1]
out=root/'out/manager-host';sha=lambda b:hashlib.sha256(b).hexdigest()
contract=json.loads((manager/'host-integration/bridge-contract.json').read_text(encoding='utf-8'))
baseline=root/contract['baselineModule']
assert sha(baseline.read_bytes())==contract['baselineModuleSha256']
for name,digest in contract['sourceSha256'].items():
 assert sha((root/name).read_bytes())==digest,'Bridge source changed; rebuild and verify: '+name
candidate=(root/contract['candidateBridge']).read_bytes()
assert sha(candidate)==contract['candidateBridgeSha256'],'Unverified bridge build'
verification=json.loads((out/'verification.json').read_text())
assert 'localTests' in verification,'Run test_integration.py first'
apk=(out/'MixiHost-UI-Test.apk').read_bytes()
assert sha(apk)==verification['hostSha256']
with zipfile.ZipFile(baseline) as z:parts={n:z.read(n) for n in z.namelist()}
bridge='system/framework/mixi-hypermodifier.jar'
assert sha(parts[bridge])==contract['baselineBridgeSha256']
host='system/system_ext/priv-app/MixiHost/MixiHost.apk';old=parts.copy();parts[host]=apk;parts[bridge]=candidate
parts['module.prop']=parts['module.prop'].decode().replace('version=1.0.6','version=1.0.6-SixFix-Test').replace('name=Mixi Framework 1.0.6 - Final Test','name=Mixi Framework 1.0.6 - Six Fix Test').encode()
parts['HUONG-DAN.txt']=('Mixi 1.0.6 / Manager1.0.0 - SixFix Test\n'
 'Sửa sáu mục: đóng đúng bridge đã kiểm thử; deadline request; ViewModel giữ hàng đợi qua xoay Activity; draft Glass được xóa khi thả; đổi đơn vị thời gian giữ giây; gesture thiếu API/cài lỗi tắt trọn nhóm.\n'
 'Cài ZIP qua quản lý module rồi reboot thủ công. Không cài riêng Host, không xóa data.\n'
 'Đã kiểm tra ART cô lập normal/missing/partial/protection và callback rollback với HMC. Cần nghiệm thu UI xoay máy, lưu/timeout và game→Home thật sau cài.\n'
 'Gesture không fallback sang monitor thiếu bảo vệ. Kiểm tra trong game thanh ẩn thì monitor dừng và về Home khôi phục.\n'
 'Rollback: Mixi-v1.0.6-Final-Test.zip. Giữ bản ROM1.0.6 đã phát hành đến khi nghiệm thu.\n').encode()
parts['SHA256SUMS']=''.join(sha(b)+'  '+n+'\n' for n,b in parts.items() if n.startswith('system/')).encode()
artifact=root/'out/Mixi-v1.0.6-SixFix-Test.zip'
with zipfile.ZipFile(artifact,'w',zipfile.ZIP_DEFLATED) as z:
 for n,b in parts.items():z.writestr(n,b)
with zipfile.ZipFile(artifact) as z:
 assert z.testzip() is None
 assert 'version=1.0.6' in z.read('module.prop').decode()
 assert 'versionCode=100600' in z.read('module.prop').decode()
 assert z.read('customize.sh')==old['customize.sh']
 assert sha(z.read(bridge))==contract['candidateBridgeSha256']
 for n,b in old.items():
  if n.startswith('system/') and n not in (host,bridge):assert z.read(n)==b,n
 for line in z.read('SHA256SUMS').decode().splitlines():
  expected,name=line.split('  ',1);assert sha(z.read(name))==expected
verification.update(artifact=artifact.name,moduleSha256=sha(artifact.read_bytes()),moduleChecks='CRC/SHA256SUMS/unchanged installer/unchanged external native/verified fail-closed gesture bridge PASS',noAutomaticInstall=True)
(out/'verification.json').write_text(json.dumps(verification,indent=2)+'\n',encoding='utf-8')
(manager/'host-integration/VERIFICATION.json').write_text(json.dumps(verification,indent=2)+'\n',encoding='utf-8')
print('HOST_UI_TEST_MODULE_PASS',verification['moduleSha256'])
