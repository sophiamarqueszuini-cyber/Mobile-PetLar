# PetLar 🐾

Aplicativo Android do **PetLar**, com o design do site PetLar Sanctuary.
Tem vitrine de produtos, carrinho, cadastro e login de clientes, perfil com endereços e um painel administrativo para a equipe.

Os dados (usuários, carrinho e pedidos) ficam salvos no próprio aparelho. Não é preciso configurar servidor nem banco de dados.

## Conta administrativa

| Login | Senha |
|---|---|
| `admin@petlar.com` | `admin123` |

Para entrar como **cliente**, crie uma conta pela tela de cadastro do app.

---

## Opção 1: Só usar o app (sem instalar nada no computador)

O APK pronto está em [`apk/PetLar.apk`](apk/PetLar.apk).
Para baixar, abra o arquivo no GitHub e clique em **Download raw file** (ícone ⬇).

### No navegador, com o Appetize.io
1. Acesse [appetize.io/upload](https://appetize.io/upload) e crie uma conta gratuita.
2. Arraste o `PetLar.apk` para a área de upload.
3. Clique em **Tap to play** e espere o celular virtual carregar.

O plano gratuito tem limite de minutos por mês.

### No celular Android
1. Envie o `PetLar.apk` para o celular (WhatsApp, Google Drive, e-mail ou cabo USB).
2. Abra o arquivo no celular.
3. Se aparecer um aviso de segurança, toque em **Configurações** e ative **Permitir desta fonte**.
4. Toque em **Instalar**. Se o Play Protect avisar, toque em **Mais detalhes → Instalar mesmo assim**. O aviso é normal para APK de teste.

Requer Android 7.0 ou superior.

---

## Opção 2: Abrir e rodar o código-fonte

### Requisitos
- [Android Studio](https://developer.android.com/studio) **atualizado**. O projeto usa Android Gradle Plugin 9.3 e SDK 37, então versões antigas não conseguem abrir.
- Cerca de **15 GB livres** no disco, para o SDK, as dependências e o emulador.
- Internet na primeira abertura, para baixar as dependências.

### Passo a passo
1. **Baixe o projeto** por um destes caminhos:
   - no GitHub, **Code → Download ZIP**, e depois extraia o arquivo;
   - ou com Git:
     ```bash
     git clone https://github.com/sophiamarqueszuini-cyber/Mobile-PetLar.git
     ```
2. No Android Studio, vá em **File → Open** e selecione a pasta **Mobile-PetLar**.
3. Espere o **Gradle Sync** terminar. Na primeira vez leva alguns minutos.
   Se ele pedir para instalar o SDK 37 ou aceitar licenças, aceite.
4. Escolha onde rodar:
   - **Emulador:** em **Device Manager → Create Virtual Device**, crie um aparelho com Android 7.0 (API 24) ou superior.
   - **Celular físico:** ative as **Opções do desenvolvedor** (toque 7 vezes em *Número da versão*), ligue a **Depuração USB** e conecte o cabo.
5. Clique em **▶ Run**.

O arquivo `local.properties` não vai para o GitHub porque guarda o caminho do SDK de cada computador.
O Android Studio cria esse arquivo sozinho na primeira abertura.

### Gerar o APK pelo terminal
```bash
# Windows
gradlew.bat assembleDebug

# Linux / macOS
./gradlew assembleDebug
```
O APK é gerado em `app/build/outputs/apk/debug/app-debug.apk`.

---

## Problemas comuns

| Problema | Solução |
|---|---|
| "SDK location not found" | Abra o projeto pelo Android Studio (ele cria o `local.properties`) ou crie o arquivo com `sdk.dir=C:/Users/SEU_USUARIO/AppData/Local/Android/Sdk` |
| Emulador diz "Not enough space" | O emulador precisa de uns 8 GB livres. Libere espaço, use um celular físico ou o Appetize.io |
| Gradle Sync falha por versão | Atualize o Android Studio para a versão mais recente |
| Celular não aparece no Android Studio | Use um cabo USB de dados (não só de carga) e toque em **Permitir** no aviso de depuração USB |
