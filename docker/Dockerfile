FROM openjdk
ARG JAR_FILE=*.war
COPY ${JAR_FILE} application.war
ENTRYPOINT ["java", "-Djasypt.encryptor.password=ChangeMe", "-jar", "application.war"]