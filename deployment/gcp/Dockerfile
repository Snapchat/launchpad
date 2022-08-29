FROM gcr.io/cloudshell-images/cloudshell:latest

RUN apt-get update
RUN apt-get install -y gnupg software-properties-common
RUN wget -O- https://apt.releases.hashicorp.com/gpg | \
        gpg --dearmor | \
        tee /usr/share/keyrings/hashicorp-archive-keyring.gpg
RUN gpg --no-default-keyring \
        --keyring /usr/share/keyrings/hashicorp-archive-keyring.gpg \
        --fingerprint
RUN echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] \
        https://apt.releases.hashicorp.com $(lsb_release -cs) main" | \
        tee /etc/apt/sources.list.d/hashicorp.list
RUN apt-get update
RUN apt-get install terraform

ARG LAUNCHPAD_MODE
ENV LAUNCHPAD_MODE=$LAUNCHPAD_MODE
RUN test -n "${LAUNCHPAD_MODE}"

RUN mkdir -p /terraform
RUN chmod 777 /terraform
COPY "${LAUNCHPAD_MODE}"/main.tf /terraform/main.tf
COPY cloudrun-deploy.sh /bin/cloudshell_open
