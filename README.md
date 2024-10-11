# tech-challenger-fiap

Bem-vindo ao repositório do projeto "Parkingmeter", desenvolvido como parte do desafio tecnológico da FIAP. Este projeto visa implementar uma API para gerenciamento de um parquimetro.

Projeto de pós-graduação em arquitetura e desenvolvimento JAVA pela FIAP
ALUNOS 5ADJT

<p>Edson Antonio da Silva Junior</p>
<p>Gabriel Ricardo dos Santos</p>
<p>Luiz Henrique Romão de Carvalho</p>
<p>Marcelo de Souza</p>

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.10-brightgreen?style=flat&logo=spring&logoColor=white)
![Java 17](https://img.shields.io/badge/Java-17-blue?style=flat&logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.8.5-orange?style=flat&logo=apachemaven&logoColor=white)
![MongoDB](https://img.shields.io/badge/MySQL-8.0-blue?style=flat&logo=mysql&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-3.0-brightgreen?style=flat&logo=swagger&logoColor=white)

## Sumário

- [Estrutura do Projeto](#estrutura-do-projeto)
- [Como Executar o Projeto](#como-executar-o-projeto)
- [Endpoints da API](#endpoints-da-api)
- [Configurando as Variáveis de Ambiente](#configurando-as-variáveis-de-ambiente)
- [Contribuindo](#contribuindo)
- [Licença](#licença)

## Estrutura do Projeto

- **/src/main/java**: código-fonte da aplicação.
- **/src/main/resources**: arquivos de configuração e recursos.

## Como Executar o Projeto

Para rodar o projeto localmente, siga os passos abaixo:

1. **Clone o repositório:**

    ```bash
    git clone https://github.com/LuizRomao02/parkingmeter.git
    ```

2. **Navegue até o diretório do projeto:**

    ```bash
    cd parkingmeter
    ```

3. **Colocar as variaveis de ambiente do arquivo que foi anexado com a documentação**
   ```bash
   caso isso nao seja feito o projeto nao ira fazer o build
    ```
4. **Construa o projeto com Maven:**

    ```bash
    mvn clean install
    ```

5. **Inicie a aplicação localmente:**

    ```bash
    mvn spring-boot:run
    ```
## Endpoints da API

A seguir está uma sequência de passos para o projeto:

1. **Cadastrar um estacionamento**  
   Endpoint: `POST http://localhost:8080/parkingmeter/parking-meter`
   Exemplo de corpo da requisição:
   ```json
   {
    "operatingHours": {
        "start": "07:00",
        "end": "15:00"
    },
    "rate": {
        "firstHour": 5.0,
        "additionalHours": 4.0
    },
    "availableSpaces": 7,
    "address": {
        "street": "street",
        "neighborhood": "neighborhood",
        "city": "city",
        "state": "state",
        "zipCode": "00000000",
        "number": "0",
        "complement": "no complet"
    }
   }

2. **Cadastrar um proprietario**  
   Endpoint: `POST http://localhost:8080/parkingmeter/owner`
   Exemplo de corpo da requisição:
   ```json
   {
    "name": "Fulano",
    "cpf": "111.111.111-1",
    "phone": "(11) 91111-1111",
    "email": "fulano@gmail.com",
    "address": {
        "street": "street",
        "neighborhood": "neighborhood",
        "city": "city",
        "state": "state",
        "zipCode": "11111111",
        "number": "3",
        "complement": "no complement"
    }
   }

3. **Cadastrar um veículo**  
   Endpoint: `POST http://localhost:8080/parkingmeter/vehicle`  
   Exemplo de corpo da requisição:
   ```json
   {
    "licensePlate": "AAB-1002",
    "model": "voyage",
    "color": "azul",
    "ownerId": "6705b0b46e091f401842c1e4"
   }
   
4. **Criar um ticket**
   Endpoint: `POST http://localhost:8080/parkingmeter/ticket`  
   Exemplo de corpo da requisição:
   ```json
   {
     "vehicle_id": "6707d1e4f37424582a57146c",
      "parking_meter_id": "66fc44c1efd00169ea025d37"
   }
   
5. **Atualizar situação do ticket para "CANCELLED"**
   Endpoint: `PATCH http://localhost:8080/parkingmeter/ticket/{id}/cancel`  

6. **Atualizar situação do ticket para "PAID"**
   Endpoint: `PATCH http://localhost:8080/parkingmeter/ticket/{id}/payment`

A API pode ser explorada e testada utilizando o Swagger. A documentação está disponível em:
http://localhost:8080/swagger-ui/index.html

## Configurando as Variáveis de Ambiente


1. **Configure a `MONGO_URI`:**
    - Adicione uma `MONGO_URI` a um arquivo .env e utilize ele para rodar o aplicativo.

## Contribuindo

Contribuições são bem-vindas! Para contribuir com o projeto, por favor siga estes passos:

1. Faça um fork do repositório.
2. Crie uma branch para sua feature ou correção (`git checkout -b feature/nova-feature`).
3. Faça commit das suas mudanças (`git commit -am 'Adiciona nova feature'`).
4. Envie suas alterações para o repositório (`git push origin feature/nova-feature`).
5. Abra um pull request.

## Licença

Este projeto está licenciado sob a [MIT License](LICENSE).