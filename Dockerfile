# ===== Etape 1 : compilation (image de build, jamais livree) =====
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build

# Copie du pom.xml seul d'abord : Docker met cette couche en cache tant que le
# pom.xml ne change pas, donc les dependances ne sont re-telechargees que si une
# dependance est ajoutee/retiree (pas a chaque changement de code Java).
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Copie du code source, puis compilation + packaging (tests deja executes
# manuellement via Postman, voir Documentation_Technique_Backend_BioConversion.docx).
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ===== Etape 2 : image d'execution (legere, sans Maven ni code source) =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Nom du jar derive de pom.xml (artifactId + version) -- le wildcard evite de
# casser le build si la version change un jour dans pom.xml.
COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
