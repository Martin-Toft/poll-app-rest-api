## Expass 5


In the beginning i had some problems downloading redis for windows,
i had to use it with docker, which i was not too fimiliar with, 
but i read up on it, and got it running in not too long.
It also took some time adding the dependency, for some reason whenever i add
dependencies in the build file and save the file, it usually doesnt want to work right away.
So when i added: implementation("redis.clients:jedis:6.2.0") to the build file it gave me an error
saying something about missing init.gradle path.
I fixed the problem by reloading the java project and gradle project a couple of times in the VSCode IDE.
Other than that it went fine, i added the file VoteCountCache.java for the cache, and also
optimised PollController to return on HIT and creat and store on MISS, i also made some changes to
VoteController upsert function to invalidate the cache to make sure it stays up to date.