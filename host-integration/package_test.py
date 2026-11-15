"""Create a reversible module test; never install it or change the accepted ZIP."""
from pathlib import Path
import hashlib,json,zipfile
manager=Path(__file__).resolve().parents[1];root=manager.parents[1]
out=root/'out/manager-host';sha=lambda b:hashlib.sha256(b).hexdigest()
baseline=root/'out/Mixi-v1.0.4-Native-Test.zip'
assert sha(baseline.read_bytes())=='6e5dcfbac7277d0e7fb9daabfb40563667ee02d333f214f7ede9a964a73dfc1a'
verification=json.loads((out/'verification.json').read_text())
assert 'localTests' in verification,'Run test_integration.py first'
apk=(out/'MixiHost-UI-Test.apk').read_bytes()
assert sha(apk)==verification['hostSha256']
with zipfile.ZipFile(baseline) as z:parts={n:z.read(n) for n in z.namelist()}
host='system/system_ext/priv-app/MixiHost/MixiHost.apk';old=parts.copy();parts[host]=apk
parts['module.prop']=parts['module.prop'].decode().replace('name=Mixi Framework 1.0.4','name=Mixi Framework - Host UI Test').replace('version=1.0.4','version=1.0.4-ui-test').replace('versionCode=100400','versionCode=100403').encode()
parts['HUONG-DAN.txt']=('MixiHost UI test - UI 0.9.2 / runtime 1.0.4\n'
 'Chỉ cài ZIP qua trình quản lý module rồi reboot thủ công. Không cài riêng APK Host.\n'
 'Settings → Mixi Framework sẽ mở UI trong MixiHost; không có APK Manager cài thêm, không có icon launcher mới.\n'
 'Giữ cấu hình thật đang lưu; không nhập cấu hình xem trước Manager. Soft Glass tự gửi khi thả thanh kéo.\n'
 'Kiểm tra: mở từ Settings, giá trị cũ, sửa Glass rồi mở Control Center nhiều lần, reboot kiểm tra giữ cấu hình, đổi một tùy chọn HMC/HyperImmersive.\n'
 'Trạng thái Đã kết nối xác nhận Settings relay, không tự khẳng định mọi hook đang active. Đã lưu/chờ xác nhận không đồng nghĩa runtime đã áp dụng.\n'
 'Nếu Settings bị đóng khỏi bộ nhớ hoặc chưa mở qua Settings, UI sẽ báo chưa kết nối; mở lại đúng entry.\n'
 'Chưa có kiểm thử cài/boot/hiển thị trên điện thoại cho bản tích hợp này. Không dùng làm gói ROM chính thức.\n'
 'Rollback: cài lại Mixi-v1.0.4-Native-Test.zip đã nghiệm thu hoặc tắt module test; reboot thủ công. Không xóa dữ liệu ứng dụng.\n').encode()
parts['SHA256SUMS']=''.join(sha(b)+'  '+n+'\n' for n,b in parts.items() if n.startswith('system/')).encode()
artifact=root/'out/Mixi-v1.0.4-HostUI-0.9.2-Test.zip'
with zipfile.ZipFile(artifact,'w',zipfile.ZIP_DEFLATED) as z:
 for n,b in parts.items():z.writestr(n,b)
with zipfile.ZipFile(artifact) as z:
 assert z.testzip() is None
 assert z.read('customize.sh')==old['customize.sh']
 for n,b in old.items():
  if n.startswith('system/') and n!=host:assert z.read(n)==b,n
 for line in z.read('SHA256SUMS').decode().splitlines():
  expected,name=line.split('  ',1);assert sha(z.read(name))==expected
verification.update(artifact=artifact.name,moduleSha256=sha(artifact.read_bytes()),moduleChecks='CRC/SHA256SUMS/unchanged installer/unchanged external native+API JAR PASS',noAutomaticInstall=True)
(out/'verification.json').write_text(json.dumps(verification,indent=2)+'\n',encoding='utf-8')
(manager/'host-integration/VERIFICATION.json').write_text(json.dumps(verification,indent=2)+'\n',encoding='utf-8')
print('HOST_UI_TEST_MODULE_PASS',verification['moduleSha256'])
