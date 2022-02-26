FROM docker.io/library/maven:3-openjdk-17 AS builder
WORKDIR /src
ADD . /src
RUN set -x \
 && mvn clean package

FROM docker.io/library/openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /src/target/spacenow.jar .
RUN set -x \
 && apt-get update \
 && apt-get install -y locales \
 && localedef -i ja_JP -f UTF-8 -A /usr/share/locale/locale.alias ja_JP.UTF-8 \
 && rm -rf /var/lib/apt/lists/*
CMD [ "java", "-jar", "/app/spacenow.jar" ]

