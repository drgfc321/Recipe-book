FROM eclipse-temurin:21-jdk AS build

WORKDIR /app
COPY . .
RUN chmod +x mvnw

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN mkdir -p /deployments/uploads/images

COPY --from=build /app/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build /app/target/quarkus-app/app/ /deployments/app/
COPY --from=build /app/target/quarkus-app/quarkus/ /deployments/quarkus/
COPY --from=build /app/target/quarkus-app/quarkus-run.jar /deployments/quarkus-run.jar

WORKDIR /deployments
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/deployments/quarkus-run.jar"]
