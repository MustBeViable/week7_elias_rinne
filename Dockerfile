FROM eclipse-temurin:21-jdk

RUN apt-get update && \
    apt-get install -y maven wget unzip libgtk-3-0 libx11-6 libxtst6 libxrender1 libxi6 libgl1 && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

RUN wget https://download2.gluonhq.com/openjfx/21/openjfx-21_linux-aarch64_bin-sdk.zip -O /tmp/openjfx.zip && \
    unzip /tmp/openjfx.zip -d /opt && \
    rm /tmp/openjfx.zip

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q clean package -DskipTests

CMD ["java", "--module-path", "/opt/javafx-sdk-21/lib", "--add-modules", "javafx.controls,javafx.fxml", "-jar", "/app/target/sum-product_fx-1.0-SNAPSHOT.jar"]