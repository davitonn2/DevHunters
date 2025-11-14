# 🎯 DevHunter - Plataforma Gamificada de Bug Bounty

O DevHunter é uma plataforma interna de "Bug Bounty" gamificada para times de desenvolvimento, onde Mestres criam recompensas e Caçadores as resolvem para ganhar XP.

## 🏗️ Arquitetura

O projeto consiste em:

- **bounty-service**: Microserviço Spring Boot que gerencia bounties e usuários, e produz mensagens no RabbitMQ
- **review-service**: Microserviço Spring Boot que consome mensagens do RabbitMQ para processar revisões
- **frontend-angular**: Interface Angular 20 para interação com a plataforma
- **MySQL**: Banco de dados relacional
- **RabbitMQ**: Broker de mensageria para comunicação assíncrona

## 🚀 Como Executar

### Pré-requisitos

- Docker Desktop instalado e rodando
- Docker Compose instalado

### Executando o Projeto

1. Clone o repositório (se ainda não tiver feito)

2. Na raiz do projeto, execute:

```bash
docker-compose up --build
```

Este comando irá:
- Construir as imagens Docker de todos os serviços
- Subir o MySQL na porta 3306
- Subir o RabbitMQ na porta 5672 (serviço) e 15672 (interface de gerenciamento)
- Subir o bounty-service na porta 8080
- Subir o review-service (sem porta exposta, apenas consome mensagens)
- Subir o frontend na porta 80

### Acessando a Aplicação

- **Frontend**: http://localhost
- **API REST (bounty-service)**: http://localhost:8080/bounties
- **RabbitMQ Management UI**: http://localhost:15672 (usuário: `guest`, senha: `guest`)

## 📋 Endpoints da API

### Bounties

- `GET /bounties` - Lista todas as bounties abertas
- `POST /bounties` - Cria uma nova bounty
  ```json
  {
    "title": "Corrigir bug no login",
    "description": "O botão de login não está funcionando",
    "rewardXp": 100
  }
  ```
- `PUT /bounties/{id}/claim` - Reivindica uma bounty
  ```json
  {
    "hunterId": 1
  }
  ```
- `POST /bounties/{id}/submit` - Entrega uma bounty para revisão
  ```json
  {
    "hunterId": 1
  }
  ```
- `DELETE /bounties/{id}` - Deleta uma bounty

## 🔄 Fluxo de Negócio

1. **Mestre cria uma bounty** através do frontend ou API
2. **Caçador reivindica** a bounty (status muda para `EM_ANDAMENTO`)
3. **Caçador entrega** a bounty (status muda para `EM_REVISAO`)
4. **bounty-service** envia mensagem para o RabbitMQ
5. **review-service** consome a mensagem e processa a revisão (simulado com log no console)

## 🛠️ Tecnologias Utilizadas

### Backend
- Java 17
- Spring Boot 3.2.5
- Spring Data JPA
- Spring AMQP (RabbitMQ)
- MySQL
- Lombok
- Maven

### Frontend
- Angular 20
- TypeScript
- RxJS
- Nginx (para servir arquivos estáticos)

### Infraestrutura
- Docker
- Docker Compose
- RabbitMQ
- MySQL 8.0

## 📁 Estrutura do Projeto

```
projeto-rannyer/
├── bounty-service/          # Microserviço principal
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   ├── Dockerfile
│   └── pom.xml
├── review-service/          # Microserviço consumidor
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   ├── Dockerfile
│   └── pom.xml
├── Front/
│   └── front-end/           # Frontend Angular
│       ├── src/
│       ├── Dockerfile
│       ├── nginx.conf
│       └── package.json
└── docker-compose.yml       # Orquestração de todos os serviços
```

## 🔍 Verificando os Logs

Para ver os logs de um serviço específico:

```bash
docker-compose logs -f bounty-service
docker-compose logs -f review-service
docker-compose logs -f frontend
```

## 🛑 Parando os Serviços

```bash
docker-compose down
```

Para remover também os volumes (incluindo dados do banco):

```bash
docker-compose down -v
```

## 📝 Notas

- O `hunterId` está hardcoded como `1` no frontend. Em produção, isso viria de um sistema de autenticação.
- O `review-service` apenas loga a mensagem recebida. Em produção, implementaria testes, validações, notificações, etc.
- As bounties são persistidas no MySQL. Os dados são mantidos em um volume Docker.

## 🎓 Conceitos Demonstrados

- Arquitetura de microserviços
- Comunicação assíncrona com RabbitMQ
- API REST com Spring Boot
- Frontend SPA com Angular
- Containerização com Docker
- Orquestração com Docker Compose
- Padrão Producer-Consumer

