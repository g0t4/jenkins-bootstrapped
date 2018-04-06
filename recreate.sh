#!/bin/bash

# remove containers, volumes (named & anonymous, not external) & orphan containers
# add --rmi local/all to remove images too
docker-compose down --volumes --remove-orphans

# force pull images, then build
docker-compose build --pull

docker-compose up -d

# docker-compose logs # to troubleshoot