# 1. Estágio de Build (Baixa o Maven e o Java para compilar o código)
FROM maven:3.9.6-eclipse-temurin-25 AS build
WORKDIR /app

# Copia os arquivos de dependência e o código fonte
COPY pom.xml .
COPY src ./src

# Compila o projeto ignorando os testes para ser mais rápido
RUN mvn clean package -DskipTests

# 2. Estágio de Produção (Cria uma imagem leve apenas com o Java para rodar o app)
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copia o arquivo .jar gerado no passo anterior
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta que o Render vai usar
EXPOSE 8080

# Comando para iniciar o Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]