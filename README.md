# 🐯 Jogo do Tigrinho — API de Slot Machine

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.7-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Uma API REST para um jogo de slot machine, construída com **Java 21** e **Spring Boot 3.5.7**.
O projeto implementa dois conceitos avançados: um sistema de carteira **Hot/Cold Wallet**
(Redis + MongoDB com sincronização assíncrona via RabbitMQ) para máxima performance, e um sistema
**Provably Fair** (comprovadamente justo) que garante a transparência e a verificabilidade de cada jogada.

## Tabela de Conteúdos

- [Features Principais](#-features-principais)
- [Arquitetura](#-arquitetura)
- [Pilha de Tecnologias](#-pilha-de-tecnologias-tech-stack)
- [Como Executar](#-como-executar)
- [Uso & Endpoints](#-uso--endpoints)
- [Testes e Qualidade](#-testes-e-qualidade)
- [Estado Atual](#-estado-atual)
- [Contribuição](#-contribuição)
- [Licença](#-licença)

---

## ✨ Features Principais

- 👤 **Gestão de Jogadores** — criação de novos jogadores (username + senha) com saldo inicial.
- ⚡ **Sistema de Carteira (Hot/Cold Wallet)**:
  - **Hot Wallet (Redis)** — as operações de jogo (apostas e prêmios) ocorrem em um cache Redis de
    alta velocidade, garantindo performance máxima.
  - **Cold Wallet (MongoDB)** — o saldo persistente e os dados do jogador ficam no MongoDB.
  - **Sincronização Assíncrona** — cada mutação dispara um evento via **RabbitMQ** que atualiza o
    MongoDB em segundo plano, mantendo a resposta da API instantânea para o jogador.
- 🎲 **Jogo "Provably Fair" (Comprovadamente Justo)**:
  - O resultado de cada giro é **determinístico**, baseado na combinação de uma `serverSeed`
    (secreta), uma `clientSeed` (do jogador) e um `nonce` (contador).
  - O sistema usa **HMAC-SHA256** para gerar um resultado único e verificável, permitindo que o
    jogador valide a justiça de cada jogada.
- 🏆 **Regras de Premiação com Strategy Pattern** — a lógica de prêmios é implementada com o padrão
  **Strategy** (`WinStrategy`), com regras concretas como `ThreeOfAKindWinStrategy` e
  `ThreeSevensWinStrategy`, fáceis de estender sem modificar o código existente.
- 📚 **Documentação de API** — documentada com SpringDoc (Swagger/OpenAPI) para fácil exploração.

---

## 🏗️ Arquitetura

O fluxo de uma jogada atravessa as camadas `controller → service → repository`, com o dinheiro
sempre arbitrado pela carteira e o resultado do giro derivado do sistema Provably Fair:

```
                 ┌────────────────────────────────────────────┐
  jogador ──▶    │ PlayerController  (REST, /api/v1/players)  │
                 └───────────────┬────────────────────────────┘
                                 │
                 ┌───────────────▼────────────────────────────┐
                 │  PlayerService · GameService · WalletService│
                 │  RNGService · CryptoService (HMAC-SHA256)   │
                 └───────┬──────────────────┬─────────────────┘
                         │                  │
                 ┌───────▼────────┐  ┌──────▼───────────┐
                 │  Hot Wallet    │  │  Provably Fair   │
                 │  (Redis)       │  │  serverSeed      │
                 │  saldo em jogo │  │  + clientSeed    │
                 └───────┬────────┘  │  + nonce → HMAC  │
                         │           └──────────────────┘
              (WalletSyncEvent via RabbitMQ)
                         │
                 ┌───────▼────────┐
                 │  Cold Wallet   │
                 │  (MongoDB)     │
                 └────────────────┘
```

- **Hot Wallet (Redis)** — operações de aposta/prêmio são resolvidas na memória, sem tocar o banco.
- **Cold Wallet (MongoDB)** — persistência final via `WalletSyncListener`, que consome o evento
  `WalletSyncEvent` publicado no RabbitMQ e grava o saldo consolidado.
- **Provably Fair** — `RNGService` combina `serverSeed` + `clientSeed` + `nonce` e deriva o
  resultado com `CryptoService` (HMAC-SHA256), permitindo auditoria externa de cada giro.

---

## 🛠️ Pilha de Tecnologias (Tech Stack)

| Categoria        | Tecnologia                                                          |
| ---------------- | ------------------------------------------------------------------- |
| **Backend**      | Java 21, Spring Boot 3.5.7                                          |
| **Banco de Dados** | MongoDB (persistência / Cold Wallet)                               |
| **Cache**        | Redis (Hot Wallet)                                                  |
| **Mensageria**   | RabbitMQ (sincronização assíncrona Hot → Cold)                      |
| **Build**        | Maven (com wrapper `./mvnw`)                                        |
| **Documentação** | SpringDoc OpenAPI (Swagger UI)                                      |
| **Testes**       | JUnit 5, Mockito, Testcontainers, RestAssured, Awaitility           |
| **Qualidade**    | JaCoCo (cobertura), Checkstyle, Sonar, Lombok                       |

---

## 🚀 Como Executar

### Pré-requisitos

- Java 21+
- Maven 3.8+
- Docker e Docker Compose

### 1. Iniciar o Ambiente

O projeto utiliza Docker Compose para orquestrar os serviços de infraestrutura (MongoDB, Redis e RabbitMQ):

```bash
docker-compose up -d
```

### 2. Executar a Aplicação

```bash
# Opção 1: usando o plugin do Maven
mvn spring-boot:run

# Opção 2: empacotando e executando o .jar (recomendado para produção)
mvn clean install
java -jar target/tigrinho-0.0.1-SNAPSHOT.jar
```

### 3. Acessar a Documentação da API

Com a aplicação rodando, a documentação interativa (Swagger UI) estará disponível em:

[**http://localhost:8080/swagger-ui.html**](http://localhost:8080/swagger-ui.html)

---

## 📡 Uso & Endpoints

Todos os endpoints estão sob o prefixo `/api/v1/players`.

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `POST` | `/api/v1/players` | Cria um jogador (username 3–30, senha 6–100). Retorna `201` + header `Location`. |
| `GET` | `/api/v1/players/{playerId}/wallet/balance` | Consulta o saldo da carteira. |
| `POST` | `/api/v1/players/{playerId}/spin` | Realiza um giro com o valor da aposta. `402` se saldo insuficiente. |
| `GET` | `/api/v1/players/{playerId}/provably-fair` | Retorna `clientSeed`, hash da `serverSeed` e `nonce`. |
| `POST` | `/api/v1/players/{playerId}/provably-fair/seeds` | Troca a `clientSeed` (e rearma a `serverSeed`), zerando o `nonce`. |

### Exemplo de fluxo completo

```bash
# 1. Criar um jogador
curl -X POST http://localhost:8080/api/v1/players \
  -H "Content-Type: application/json" \
  -d '{"username":"daniel","password":"segredo123"}'
# → 201 Created, header Location: /api/v1/players/{playerId}

# 2. Consultar o saldo
curl http://localhost:8080/api/v1/players/{playerId}/wallet/balance
# → 100.00

# 3. Realizar um giro (aposta)
curl -X POST http://localhost:8080/api/v1/players/{playerId}/spin \
  -H "Content-Type: application/json" \
  -d '{"betAmount":10.00}'
# → 200 (resultado do giro) · 402 Payment Required se o saldo for insuficiente

# 4. Consultar os dados Provably Fair
curl http://localhost:8080/api/v1/players/{playerId}/provably-fair
# → { "clientSeed": "...", "serverSeedHash": "...", "nonce": 0 }

# 5. Trocar as seeds (reseta o nonce)
curl -X POST http://localhost:8080/api/v1/players/{playerId}/provably-fair/seeds \
  -H "Content-Type: application/json" \
  -d '{"clientSeed":"minha-seed-nova"}'
```

---

## 🧪 Testes e Qualidade

### Rodar Todos os Testes

```bash
mvn clean install
```

### Gerar Relatório de Cobertura (JaCoCo)

```bash
mvn clean verify -Pci
```

O relatório fica disponível em `target/site/jacoco/index.html`.

---

## 📌 Estado Atual

Projeto em desenvolvimento ativo na branch `main` — **sem releases taggeadas ainda**. O que já está
implementado:

- Criação de jogadores e consulta de saldo;
- Giro (`spin`) com apostas e verificação de saldo insuficiente (`402`);
- Hot/Cold Wallet com sincronização assíncrona via RabbitMQ;
- Provably Fair com HMAC-SHA256 e rotação de seeds;
- Regras de premiação via Strategy Pattern (`three of a kind`, `three sevens`);
- Documentação OpenAPI/Swagger e health checks (Actuator + indicador customizado);
- CI/CD (GitHub Actions) e tooling de qualidade (Checkstyle, Sonar, JaCoCo).

---

## 🤝 Contribuição

Projeto desenvolvido de forma individual. Pull requests são bem-vindos — siga o padrão de código
existente (Java 21, Checkstyle, testes com JUnit 5) e mantenha a suíte verde antes de submeter
(`mvn clean install`).

---

## 📄 Licença

Este projeto está sob a licença **MIT**. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.
