DAT 250 Assignment 2

#Technical problems encountered and resolved
- Lombok
  in the beginning i just made the standard POJO files, but i wanted to try out lombok, so changing to that gave me a little trouble
  i eventually figured it out, and added the correct dependencies for it.
- Test client
  At first my test scenarios were manual, I had to copy the IDs returned from one request and paste them into the next request.
  I resolved this issue by learning to use VS Code REST Client's @name annotation, with .response.body.$.id, so the client goes in and gets the id for me insted.
- Votes not clearing after deleting poll
  At one point after deleting a poll the votes for the deleted poll was still showing.
  I resolved this, by updating my deletePoll in DomainManager to also remove options and votes.

#Pending issues I wouldnt say these are things i did not manage to solve, but rather given more time, i would have implemented
- Error handling / constraints
  I currently have no error handling at all.
  For example a user could make an account with no email, or name.
- JUnit test for fully automatic testing,
  what i currently have is testing through VS Code http client, where each test has to be clicked to run.
