FROM eclipse-temurin:17.0.16_8-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends python3 ca-certificates \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /work
