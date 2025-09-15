## What i added
- small svelte single-page poll app
- possability to create a poll with any number of options
- possability to vote on an existing poll and see total votes per option
- a simple open by poll-ID flow

## technical problems
- it was for the most part handling data (400 bad requests), giving and receiving data on the "expected" form took a while, but
with a bit of trying and failing i made it work :)
- also had a bit of CORS problems in the beginning, I dont remember exactly what i had in the @crossorigin on the controllers
i just know that i had to try a couple different restrictions before it started working.
- not really a problem, but really time consuming to make the page look "nice" so spent a long time, messing around with styling, moving stuff around,
colors and so on xd


## pending issues
- sometimes when restarting, when looking on the browser consol while creating a poll, i get a 500 error
that i am not sure how to fix, i suspect it has something to do with the way i create demo users since i have not implemented
a way of using the SPA to register users

link to the .svelte file:
https://github.com/Martin-Toft/poll-app-rest-api/blob/main/frontend/src/App.svelte
