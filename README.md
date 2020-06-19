# Overview
This repository is part of a URL Shortener application for Skillshare, more specifically,
it exposes the backend service that is in charge of handling URL requests.

Relevant notes:
- The service is containerized using Docker
- This application currently can only be run locally out of the box
- The service is implemented in Kotlin

# Dependencies
The only dependencies needed to run this application locally are:
- [Docker](https://docs.docker.com/get-docker/)
- [GNU make tool](https://www.gnu.org/software/make/manual/make.html)

# Usage
In order to build and expose the backend service, run the following command:
```shell script
make docker-image-run
```

The above command will build the Docker image that contains the URL Shortener Service and it will then run it inside a
local container. Once the command finishes running you will have access to the event log stream from the containerized
application.
