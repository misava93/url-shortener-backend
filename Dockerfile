# docker image that exposes the URL Shortener Service as a local server
FROM openjdk:8-jdk AS url-shortener-service

# specify configuration to be passed at image build time
ARG USER=skillshare
ARG HOME=/home/$USER

# set environmental variables for the image
ENV USER=${USER}
ENV HOME=${HOME}
ENV APP_HOST="url-shortener-service"
ENV APP_PORT=8080

# change default shell to bash
SHELL ["/bin/bash", "-c"]

# setup steps that require root access
## setup user that will be used for subsequent non-sudo commands and to run the application
RUN groupadd -r ${USER} && useradd -r -g ${USER} ${USER}
RUN mkdir ${HOME}
RUN chown ${USER} ${HOME}

# setup steps that dont require root access
## change user
USER ${USER}:${USER}
RUN echo "USER: ${USER}"
## setup working directory
WORKDIR ${HOME}
RUN echo "HOME: ${HOME}"

# application-level steps
## copy relevant source code
ENV APP_DIR=${HOME}/skillshare-url-shortener-service
RUN mkdir ${APP_DIR}
WORKDIR ${APP_DIR}
COPY ./build/libs/skillshare-tinyurl.jar ${APP_DIR}/skillshare-tinyurl.jar

CMD ["java", "-server", "-jar", "skillshare-tinyurl.jar"]
