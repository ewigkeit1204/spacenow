FROM docker.io/library/node:latest AS builder_vue
WORKDIR /src
ADD ./src/web/ /src
RUN set -x \
 && npm install \
 && npm run build

FROM docker.io/library/maven:3-openjdk-17 AS builder_mvn
WORKDIR /src
ADD . /src
COPY --from=builder_vue /src/dist /src/src/main/resources/static
RUN set -x \
 && mvn clean package

FROM docker.io/library/openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder_mvn /src/target/spacenow.jar .
RUN set -x \
 && apt-get update \
 && apt-get install -y locales \
 && localedef -i ja_JP -f UTF-8 -A /usr/share/locale/locale.alias ja_JP.UTF-8 \
 && rm -rf /var/lib/apt/lists/*
CMD [ "java", "-jar", "/app/spacenow.jar" ]

