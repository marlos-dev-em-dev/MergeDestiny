# SelfUpdate

Base inicial de atualizador Android em Java, com módulo `selfupdate` separado do app de teste.

## Configuração obrigatória

Abra `app/src/main/java/com/marlos/selfupdatetest/MainActivity.java` e substitua:

`https://raw.githubusercontent.com/SEU_USUARIO/SEU_REPO/main/version/update.json`

pela URL HTTPS real do manifesto JSON.

O arquivo de exemplo está em `version/update.example.json`. Publique-o como `update.json` no repositório de atualizações.

## O que esta primeira base faz

- Consulta o manifesto remoto por HTTPS.
- Lê `version`, `versionCode`, `downloadUrl`, `sha256` e `changelog`.
- Compara versões `MAJOR.MINOR.PATCH`.
- Identifica atualização disponível ou app atualizado.
- Exibe o tipo PATCH, MINOR ou MAJOR na tela temporária.

## Ainda não implementado nesta etapa

O download do APK, validação SHA-256, validação de package/assinatura e abertura do instalador Android serão adicionados e testados nas próximas etapas. A primeira entrega prioriza uma base que possamos compilar e validar antes de avançar.

## Regra de versão

- PATCH: correções, opcional.
- MINOR: melhorias/QoL, opcional.
- MAJOR: atualização obrigatória (a política de bloqueio será implementada na etapa de fluxo/UI).

`versionCode` é um inteiro crescente e não substitui `versionName`.

## Projeto

- `app`: aplicativo temporário para testar o atualizador.
- `selfupdate`: biblioteca reutilizável.
- `version`: exemplo de manifesto.
- `docs`: documentação de integração.
