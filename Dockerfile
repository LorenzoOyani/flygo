## ---------- Build stage ----------
#FROM eclipse-temurin:21-jdk AS builder
#
#WORKDIR /app
#
#COPY mvnw .
#COPY .mvn .mvn
#COPY pom.xml .
#
#RUN chmod +x mvnw
#
## Cache Maven dependencies across builds
#RUN --mount=type=cache,target=/root/.m2 \
#    ./mvnw dependency:go-offline -B
#
#COPY src src
#
#RUN --mount=type=cache,target=/root/.m2 \
#    ./mvnw clean package -DskipTests
#
#
## ---------- Runtime stage ----------
#FROM eclipse-temurin:21-jre
#
#WORKDIR /app
#
#COPY --from=builder /app/target/*.jar app.jar
#
#EXPOSE 8080
#
#ENTRYPOINT ["java", "-jar", "app.jar"]