# ============================================================
# STAGE 1: BUILD — Maven + JDK 17
# ============================================================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copia apenas o pom.xml primeiro (cache de dependências)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o restante do código-fonte
COPY src ./src

# Compila e gera o JAR (sem testes)
RUN mvn clean package -DskipTests -B

# ============================================================
# STAGE 2: RUNTIME — JRE Alpine (leve)
# ============================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Cria usuário não-root por segurança
RUN addgroup -S spring && adduser -S spring -G spring

# Copia o JAR gerado no estágio de build
COPY --from=build /app/target/saude-ocupacional-1.0.0.jar app.jar

# Ajusta permissões
RUN chown spring:spring app.jar

USER spring

# Expõe a porta da aplicação
EXPOSE 8080

# Inicia a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
