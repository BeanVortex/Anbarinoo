FROM openjdk:21-jdk
ARG JAR_FILE=*.jar
COPY ${JAR_FILE} application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]
#ENTRYPOINT ["java", "-Djasypt.encryptor.password=ChangeMe", "-jar", "application.war"]
