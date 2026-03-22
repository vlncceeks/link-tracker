FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /build

COPY pom.xml .
COPY bot/pom.xml bot/
COPY scrapper/pom.xml scrapper/
COPY ai-agent/pom.xml ai-agent/
COPY build-report-aggregate/pom.xml build-report-aggregate/

RUN mvn -B -q -e -DskipTests dependency:go-offline

COPY . .

RUN mvn -B -q -Drevision=22.0.0 -DskipTests package

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=builder /build/bot/target/*.jar bot.jar
COPY --from=builder /build/scrapper/target/*.jar scrapper.jar

