ARG RUNDECK_IMAGE
FROM ${RUNDECK_IMAGE}

RUN sudo apt-get update
RUN sudo apt-get install -y -qq --no-install-recommends wget git curl jq iputils-ping sysstat

COPY plugins/. /home/rundeck/libext
