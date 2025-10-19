## Expass 7
Docker

## What i did
Containerized the spring boot rest API by using a multi stage dockerfile.
Added a docker compose file with services: api, rabbit and redis and wired the API to them.
Built and ran with docker compose up --build and verified logs, api came up on 8080 and successfully connected to rabbit and redis.

## Technical problems
Redis host was hard coded to localhost.
Fixed this by changing VoteCountCache to read REDIS_HOST and REDIS_PORT from env variables.

## Pending issues 
Currently when running 'docker compose up --build' in the beginning the API log will show "connection refused" from AMQP.
This happens because the APIs rabbit listener start before rabbit is fully ready.
It will keep trying to connect until Rabbit is up, so it still works.

