# 🐯 Jogo do Tigrinho - API de Slot Machine

[![Java Version](https://img.shields.io/badge/java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/daniel-castilho/tigrinho)
[![Coverage](https://img.shields.io/badge/coverage-95%25-blue.svg)](https://github.com/daniel-castilho/tigrinho)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Uma API REST para um jogo de slot machine, construído com uma arquitetura moderna e robusta utilizando Java 21 e Spring Boot. O projeto implementa conceitos avançados como "Hot/Cold Wallet" para performance e um sistema "Provably Fair" para garantir a transparência e justiça de cada jogada.

---

## ✨ Features Principais

-   👤 **Gestão de Jogadores**: Criação de novos jogadores com saldo inicial e gerenciamento de dados.
-   ⚡ **Sistema de Carteira (Hot/Cold Wallet)**:
    -   **Hot Wallet (Redis)**: Operações de jogo (apostas e prêmios) ocorrem em um cache Redis de alta velocidade para garantir performance máxima.
    -   **Cold Wallet (MongoDB)**: O saldo principal e os dados do jogador são armazenados de forma persistente no MongoDB.
    -   **Sincronização Assíncrona**: Um evento é disparado via **RabbitMQ** para atualizar o MongoDB em segundo plano, garantindo que a resposta da API para o jogador seja instantânea.
-   🎲 **Jogo "Provably Fair" (Comprovadamente Justo)**:
    -   O resultado de cada giro é **determinístico**, baseado na combinação de uma `serverSeed` (secreta), uma `clientSeed` (do jogador) e um `nonce` (contador).
    -   O sistema usa HMAC-SHA256 para gerar um resultado único e verificável, permitindo que o jogador valide a justiça de cada jogada.
-   🏆 **Regras de Premiação com Strategy Pattern**:
    -   A lógica para calcular prêmios foi implementada usando o **Design Pattern Strategy**. Cada regra de prêmio é uma classe separada, tornando o sistema fácil de estender com novas regras sem modificar o código existente.
-   📚 **Documentação de API**: A API é documentada com SpringDoc (Swagger) para fácil exploração e teste dos endpoints.

---

## 🛠️ Pilha de Tecnologias (Tech Stack)

| Categoria              | Tecnologia                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             - **Backend**: `Java 21`, `Spring Boot 3.5.7`
| **Banco de Dados**: `MongoDB` (Persistência), `Redis` (Cache / Hot Wallet)
| **Mensageria**: `RabbitMQ` (Sincronização Assíncrona)
| **Build**: `Maven`
| **Testes**: `JUnit 5`, `Mockito`, `Testcontainers`, `RestAssured`, `Awaitility`
| **Qualidade**: `JaCoCo` (Code Coverage), `Lombok`

---

## 🚀 Como Executar

### Pré-requisitos

-   Java 21+
-   Maven 3.8+
-   Docker e Docker Compose

### 1. Iniciar o Ambiente

O projeto utiliza Docker Compose para orquestrar os serviços de infraestrutura.

```bash
# Inicia os contêineres do MongoDB, Redis e RabbitMQ em segundo plano
docker-compose up -d
```

### 2. Executar a Aplicação

Você pode executar a aplicação Spring Boot de duas maneiras:

```bash
# Opção 1: Usando o plugin do Maven
mvn spring-boot:run

# Opção 2: Empacotando e executando o .jar (recomendado para produção)
mvn clean install
java -jar target/tigrinho-0.0.1-SNAPSHOT.jar
```

### 3. Acessar a Documentação da API

Com a aplicação rodando, a documentação interativa da API (Swagger UI) estará disponível em:

[**http://localhost:8080/swagger-ui.html**](http://localhost:8080/swagger-ui.html)

---

## 🧪 Testes e Qualidade de Código

O projeto é configurado com uma suíte de testes robusta para garantir a qualidade e a estabilidade.

### Rodar Todos os Testes

Este comando executa todos os testes unitários e de integração.

```bash
mvn clean install
```

### Gerar Relatório de Cobertura (JaCoCo)

Para gerar o relatório de cobertura de testes, ative o perfil `ci` do Maven.

```bash
# Executa os testes e gera o relatório
mvn clean verify -Pci
```

O relatório estará disponível em `target/site/jacoco/index.html`.

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.
