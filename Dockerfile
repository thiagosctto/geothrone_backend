# 1. Estágio de Build (Usa o JDK 25 oficial para compilar)
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copia os arquivos do Maven Wrapper e o código fonte
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src ./src

# Dá permissão de execução ao script e compila o projeto
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# 2. Estágio de Produção (Usa o JRE 25 oficial para rodar, sendo mais leve)
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copia o arquivo .jar gerado no passo anterior
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta que o Render vai usar
EXPOSE 8080

# Comando para iniciar o Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]