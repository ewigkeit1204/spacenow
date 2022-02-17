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
ENV TWITTER_CONSUMER_KEY ""
ENV TWITTER_CONSUMER_SECRET ""
CMD ["java", "-jar", "/app/spacenow.jar", "--spacenow.twitter.consumerKey=$TWITTER_CONSUMER_KEY", "--spacenow.twitter.consumerSecret=$TWITTER_CONSUMER_SECRET"]

