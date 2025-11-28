# 🏹 DevHunters

**DevHunters** é uma plataforma gamificada de "caça a recompensas" (bounties) voltada para desenvolvedores. O sistema conecta empresas ou gestores que precisam resolver *issues* técnicas complexas com desenvolvedores dispostos a solucioná-las em troca de recompensas (XP, dinheiro ou reputação).

> **Projeto Final - Desenvolvimento Fullstack**
> **Curso:** JAVA

---

## 👥 Integrantes do Grupo

* **Davi Augusto Voelz Tonn**
* **Kaio Levi Pollhein**

---

## 🛠 Tecnologias Utilizadas

### Back-end
* **Java & Spring Boot:** Framework principal para construção da API REST.
* **RabbitMQ:** Broker de mensageria para comunicação assíncrona entre serviços.
* **Banco de Dados:** Relacional (PostgreSQL/MySQL) para persistência dos dados.
* **Maven:** Gerenciamento de dependências.

### Front-end
* **Angular:** Framework utilizado para a construção da interface do usuário (SPA).
* **TypeScript, HTML, CSS:** Tecnologias base da interface.

### Infraestrutura & DevOps
* **Docker:** Containerização dos serviços.
* **Docker Compose:** Orquestração dos containers (Banco, RabbitMQ, API e Front).

---

## 🏗 Arquitetura e Mensageria

O sistema adota uma arquitetura de microsserviços simplificada, dividida em domínios de responsabilidade:

1.  **bounty-service (Producer):**
    * Responsável pela lógica principal: autenticação de usuários, criação de missões (bounties), listagem e submissão de soluções.
    * **Fluxo de Mensageria:** Quando um desenvolvedor "Reivindica" ou "Soluciona" uma bounty, este serviço publica uma mensagem na fila do RabbitMQ informando o evento.

2.  **email-service (Consumer):**
    * Serviço responsável por notificações.
    * **Fluxo de Mensageria:** Escuta a fila do RabbitMQ. Ao receber a mensagem de que uma bounty foi atualizada ou reivindicada, ele processa essa informação (simulando o envio de um e-mail de confirmação para o usuário).

---

## 🚀 Como Rodar o Projeto

A maneira mais simples de rodar todo o ecossistema é utilizando o **Docker Compose**, que subirá o Banco de Dados, o RabbitMQ, os Back-ends e o Front-end simultaneamente.

### Pré-requisitos
* Docker e Docker Compose instalados.

### Passo a Passo

1.  Clone o repositório e entre na pasta raiz:
    ```bash
    git clone [https://github.com/davitonn2/devhunters.git](https://github.com/davitonn2/devhunters.git)
    cd devhunters
    ```

2.  Execute o Docker Compose:
    ```bash
    docker-compose up --build
    ```
    *Isso irá baixar as dependências, compilar o Java (Maven), compilar o Angular e subir os containers.*

3.  Acesse a aplicação:
    * **Front-end (DevHunters):** `http://localhost:8081` (ou a porta definida no seu docker-compose)
    * **API (Bounty Service):** `http://localhost:8082`
    * **RabbitMQ Management:** `http://localhost:15672` (Login padrão: `guest` / `guest`)

---

## 🔌 Endpoints da API

A API segue o padrão REST. Abaixo, os principais endpoints disponíveis:

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| **POST** | `/auth/register` | Cria um novo usuário (Caçador ou Criador). |
| **POST** | `/auth/login` | Autentica o usuário e retorna Token. |
| **GET** | `/bounties` | Lista todas as missões disponíveis. |
| **POST** | `/bounties` | Cria uma nova missão (bounty). |
| **PUT** | `/bounties/{id}` | Atualiza status ou reivindica uma missão (Gera evento RabbitMQ). |
| **DELETE**| `/bounties/{id}` | Remove uma missão. |

---

## 📸 Defesa do Tema (Criatividade)

Escolhemos o tema **DevHunters** para fugir do CRUD tradicional de produtos. O sistema simula uma "Guilda de Desenvolvedores":

* **A "Máquina" de Processos:** O sistema funciona como um quadro de missões de RPG, mas adaptado para o mundo corporativo de TI.
* **Interação Real:** A mensageria não é apenas técnica; ela representa o "carteiro" da guilda notificando que uma missão foi aceita, garantindo que o fluxo de informação seja desacoplado e resiliente.
