FROM adoptopenjdk/openjdk8:latest

RUN apt-get update && apt-get install -y wget unzip

RUN apt-get clean \
 && rm -rf /var/lib/apt/lists/*

ENV VAULT_VER=0.8.3
ENV UNAME=$(uname -s |  tr '[:upper:]' '[:lower:]')
ENV VAULT_ZIP="vault_${VAULT_VER}_${UNAME}_amd64.zip"

RUN wget "https://releases.hashicorp.com/vault/${VAULT_VER}/${VAULT_ZIP}"
RUN unzip ${VAULT_ZIP}
