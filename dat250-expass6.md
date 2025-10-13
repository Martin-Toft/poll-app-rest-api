## Expass 6
Messaging with RabbitMQ

## What i did
Added RabbitMQ topic exchange and consumer queue.
On poll creation, decleared per-poll queue bound by ID.
Built publisher and listener that applies votes via DomainManager.

## Technical problems
When testing, i sent votes with userId = null.
Since there was no user with this Id lookups failed and threw errors.
I temporarily fixed this by creating a new user when userID is missing or unknown to store the vote.


## Pending issues
I havent made proper error handling yet, so when creating a vote if the userId is not recogniced it just creates a new user to store the vote.