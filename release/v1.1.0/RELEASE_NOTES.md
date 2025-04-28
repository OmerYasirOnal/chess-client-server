# Chess Game v1.1.0 Release Notes

Bu sürüm, satranç oyun projemizin ilk kararlı sürümü olan 1.0.0'dan sonraki ilk güncellemedir.

## Yenilikler ve İyileştirmeler

* **Kod Temizliği**: Tüm kod tabanında genel temizlik ve optimizasyon yapıldı
* **Kullanıcı Arayüzü İyileştirmeleri**: Arayüz bileşenlerinde düzenlemeler yapıldı
* **Performans İyileştirmeleri**: Oyun motoru ve ağ iletişimi hızlandırıldı
* **Belgelendirme Güncellemeleri**: README ve ChangeLog dosyaları güncel bilgilerle güncellendi

## Yükleme Talimatları

### Sunucu
```bash
java -jar chess-server-1.1.0.jar [port]
```
Varsayılan port belirtilmezse 5000'dir.

### İstemci
```bash
java -jar chess-client-1.1.0.jar [server-address] [port]
```
Varsayılan sunucu adresi localhost ve varsayılan port 5000'dir.

## Gelecek Sürüm Planları

1.2.0 sürümünde aşağıdaki özelliklerin eklenmesi planlanmaktadır:
- Alınan taşların gösterilmesi
- Sürükle-bırak taş hareketi
- Hamle geçmişi kaydı ve gösterimi
- Tahta boyutu ve koordinat etiketi iyileştirmeleri
- Satranç saati eklentisi

Detaylı bilgi için ChangeLog.txt dosyasına bakabilirsiniz. 