
<script>

  //routing
  let route = window.location.hash;
  window.addEventListener("hashchange", () => {route = window.location.hash})
  $: voteId = route.startsWith("#/vote/") ? route.slice("#/vote/".length) : null;

  const POLL_BASE = "/polls";
  const USERS_URL = "/users";

  // function for making a demo user
  let userId = localStorage.getItem("userId") || null;

  async function ensureUser() {
    if (userId){
      try {
        const r = await fetch(`${USERS_URL}/${userId}`)
        if (r.ok) return;
      }catch (_){}
    }
    try {
      const res = await fetch(USERS_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          username: "demo-" + Math.floor(Math.random() * 1e6),
          email: "demo@example.com"
      })
    });
    const u = await res.json();
    userId = u.id;
    localStorage.setItem("userId", userId);
    } catch (err) {
      console.error("failed", err);
    }
  }

  // function for adding more vote options
  let options = [null, null];
  function addOption() {
    options = [...options, null];
  }
  // function for removing last created vote option
  function removeLastOption() {
    if (options.length <= 2) return;
    options = options.slice(0, -1);
  }
  //function for creating a poll
  let createdPoll = null;

  async function handleCreate(e) {
    e.preventDefault();

    const form = e.currentTarget;
    const fd = new FormData(form);
    await ensureUser();

    const question = (fd.get("question") || "").toString().trim();
    const options  = fd.getAll("option")
      .map(v => v.toString().trim())
      .filter(Boolean);

    const payload = {
      ownerId: userId,
      question,
      publishedAt: null,
      validUntil: null,
      options: options.map((text, i) => ({
        caption: text,
        presentationOrder: i
      }))
    };

    try {
      const res = await fetch(POLL_BASE, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });
    if (!res.ok) {
      console.error("failed", res.status); return;
    }

    createdPoll = null;
    try {
      if ((res.headers.get("content-type") || "").includes("application/json")) {
        createdPoll = await res.json();
      } else {
        await res.text();
      }
    } catch {}
    form.reset()
  } catch (err) {
    console.error("failed",err);
  }
}

  //fetching poll data from backend
  let poll = null;
  let lastLoadedId = null;

  async function loadPoll(id) {
    poll = null;
    try {
      const res = await fetch(`${POLL_BASE}/${id}`);
      if (!res.ok) { console.error("failed", res.status); return;}
      poll = await res.json();
    } catch (err) {
      console.error("failed", err);
    }
  }

  $: if (voteId && voteId !== lastLoadedId) {
    lastLoadedId = voteId;
    loadPoll(voteId);
  }

  //votes
  let voteCounts = {}

  async function loadVotes(id) {
    try {
      const res = await fetch(`${POLL_BASE}/${id}/votes`);
      if (!res.ok) { voteCounts = {}; return; }
      const votes = await res.json();
      const m = {};
      for (const v of votes) m[v.option.id] = (m[v.option.id] || 0) + 1;
      voteCounts = m;
    } catch (err) {
      voteCounts = {};
    }
  }

  $: if (poll && voteId) loadVotes(voteId);

  let lastVote = null;
  async function vote(optionId) {
    await ensureUser();
    lastVote = null;
    try {
      let res = await fetch(`${POLL_BASE}/${voteId}/votes`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId, optionId })
      });

      if (res.status === 500) {
        localStorage.removeItem("userId");
        userId = null;
        await ensureUser();
        res = await fetch(`${POLL_BASE}/${voteId}/votes`, {
          method: "POST",
          headers: {"Content-Type": "application/json" },
          body: JSON.stringify({userId, optionId})
        });
      }

      if (!res.ok) { console.error("failed", res.status); return;}
      lastVote = await res.json().catch(() => null);
      await loadVotes(voteId);
    } catch (err) {
      console.error("failed", err);
    }
  }

  //poll lookup by id
  let lookupId = "";

  async function goToPoll(e) {
    e.preventDefault();
    const id = lookupId.trim();
    if (!id) {return;}
    window.location.hash = `#/vote/${id}`;
    lookupId = "";
  }

</script>


<header>
  <h1>Poll App</h1>
</header>

<nav>
  <a href="#/create">Create a poll</a>
  <a href="#/vote">Vote on a poll</a>
</nav>


<main>
  {#if route === "#/create"}
    <h2> Create a poll:</h2>


  <form on:submit|preventDefault={handleCreate}>
    <label class="question">
      <span>Question:</span>
      <input name="question" type="text" placeholder="Question to vote on:" />
    </label>

    {#each options as _, i}
      <label class="option">
        <span>Option {i + 1}:</span>
        <input name="option" type="text" placeholder="Vote option caption" />
      </label>
    {/each}

    <button type="button" on:click={addOption}>+ Add option</button>
    <button type="button" on:click={removeLastOption} disabled={options.length <= 2}>- Remove option</button>
    <button type="submit">Create poll</button>

    {#if createdPoll?.id}
      <p>Created poll id: <code>{createdPoll.id}</code></p>
      <p><a href={`#/vote/${createdPoll.id}`}>Go vote on this poll</a></p>
    {/if}
  </form>

  {:else if route.startsWith("#/vote")}
    {#if voteId}
      {#if poll}
        <h2 class="voteQuestion">{poll.question}</h2>
        <div class="voteList">
          {#each poll.options as opt}
            <div class="voteRow">
              <div class="voteText">{opt.caption}</div>
              <button class="upButton" type="button" on:click={() => vote(opt.id)}> vote </button>
              <div class="voteCount">{voteCounts[opt.id] ?? 0} Votes </div>
            </div>
          {/each}
        </div>
      {/if}
    {:else}
      <h2>Open a poll</h2>
      <form on:submit|preventDefault={goToPoll}>
        <input type="text" bind:value={lookupId} placeholder="paste poll ID" />
        <button type="submit">Open</button>
      </form>
  {/if}
  {:else}
    <p>Home Page:</p>
  {/if}
</main>


<style>
  :global(body) {
    background: #27798d;
  }

  header {
    background: #bddad9;
    padding: 1rem;
  }

  h1 {
    margin: 0;
  }

  main {
    padding:0.5rem;
  }

  nav a {
    display: block;
  }

  label.question {
    display: block;
    margin-bottom: 1rem;
  }

  label.option {
    display: block;
  }

  input[name="question"] {
    background: #bddad9;
  }

  input[name="option"] {
    background: #bddad9;
  }

  button {
    margin-top: 1rem;
    margin-left: 0.5rem;
    background: #bddad9;
  }

  button[type="submit"]{
    display: block;
    margin-left: 4rem;
  }


  div.voteList{
    border: 1px solid #d9d9d9;
    border-radius: 12px;
    overflow: hidden;
    max-width: 600px;
  }

  div.voteRow{
    display: grid;
    grid-template-columns: 1fr auto auto;
    align-items: center;
    padding: 0.75rem 1rem;
    border-top: 1px solid #eee;
  }
  div.voteRow:first-child{
    border-top: none;
  }
  button.upButton{
    padding: 0.35rem 0.7rem;
    border-radius: 0.4rem;
    margin-bottom: 1rem;
    margin-right: 0.4rem;
  }
  div.voteText{
    font-size: 1rem;
  }

</style>