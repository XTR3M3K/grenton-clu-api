## Grenton CLU - Java API

Biblioteka do komunikacji z CLU systemu Grenton.

CLU systemu Grenton komunikuje się z Object Managerze po RPC, gdzie OM po prostu wysyła do CLU informację jaką metodę ma wykonać, a CLU zwraca odpowiedź.

Wewnętrzne API CLU jest zawsze wystawiane na porcie 1234/udp.

## Przed uruchomieniem

Do połączenia się z CLU systemu Grenton potrzebujemy:
- adresu IP CLU
- klucza szyfrowania (SecretKey)
- wektora inicjującego (IV)

Klucze szyfrujące znajdują się w folderze projektu Object Manager w pliku **properties.xml**.

```xml
<projectCipherKey id="2">
  <keyBytes id="3"> -- SecretKey -- </keyBytes>
  <ivBytes id="4"> -- IV -- </ivBytes>
</projectCipherKey>
```

## Jak używać

Poniżej przykład jak używać biblioteki.

```java
GrentonCLU clu = new GrentonCLU(
        " -- Adres IP CLU -- ",
        1234,
        " -- SecretKey -- ",
        " -- IV -- "
);

clu.connect();

clu.sendCommand("DOU8791:get(0)").get()
```

## Propozycje zmian / pull requests

Wszelkie propozycje zmian są mile widziane. Jeśli chcesz coś zmienić, stwórz pull requesta.