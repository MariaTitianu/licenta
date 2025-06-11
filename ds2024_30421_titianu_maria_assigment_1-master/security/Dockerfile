FROM eclipse-temurin:21-jdk-alpine as build
WORKDIR /ds2024/securityms

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY lombok.config .
RUN ./mvnw dependency:resolve
RUN ./mvnw dependency:resolve-plugins
COPY src src
RUN ./mvnw install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp_securityms
ARG DEPENDENCY=/ds2024/securityms/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","dsrl.mariatitianu.security.SecurityApplication"]