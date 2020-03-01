# FYI image docs are here: 
#   https://github.com/jenkinsci/docker/blob/master/README.md
ARG jenkins_image_tag=2.107.1-alpine

FROM jenkins/jenkins:${jenkins_image_tag}

USER root
RUN apk -U add tree docker

USER jenkins

RUN install-plugins.sh \
    git \
    dockerhub-notification \
    blueocean

COPY ref /usr/share/jenkins/ref/.




## NOTES

# Jenkins versions as base layer 
    # 2016 jenkins course used jenkins 2.7.1 which you can find at jenkins:lts-alpine-2.7.1
        # I haven't tested if this legacy version works with this repo and not sure I care to since that course is getting updated 
        # FROM jenkins:2.7.1-alpine # UNTESTED
    # 2018 docker registry course used jenkins/jenkins:lts-alpine which "floats" to latest version of jenkins pushed out under lts-alpine tag which turns out to be 2.107.1 as of the last build I pushed to docker hub 
        # FROM jenkins/jenkins:2.107.1-alpine
        # 2.107.1 images versions "search" https://hub.docker.com/r/jenkins/jenkins/tags?page=1&name=2.107.1
        # 2.107.1-alpine tag page: https://hub.docker.com/layers/jenkins/jenkins/2.107.1-alpine/images/sha256-c78fd8e236667551d9269f097a05d73c9ad7582906f7ab0de20c67db2808049e?context=explore