# ThreatWatch

## Introduction

## Moving Parts
- React Frontend
- Springboot Java app 
- GitHub repo & actions
- Redis database
- NVD backend APIs

## Technologies Involved
- Java
- Docker
- RedisDB
- GitHub

## Workflow
1. User clones and runs docker containers in VM
2. User opens the frontend and:
   1. Inputs email addresses for notification sending
   2. Adds filters for products
   3. Sets the interval of the scheduler, because it affects the performance of the VM
3. The configuration is sent to an API of the java backend and saves the configuration in the Redis database
4. The scheduler runs every X amount of time and:
   1. Queries NVD with a lookback window of Y, where Y >= X * 2 to account for delays
   2. Queries Redis to find previous run's CVEs that were sent
   3. Filters out duplicate records from the current run's query
   4. Sends the email with the new CVEs
   5. Saves the new CVEs to Redis
6. Redis clears old CVEs based on Z time window TTL, where Z > Y to account for delays in runs,

## Springboot Java app
- 1 API talking with frontend
- 