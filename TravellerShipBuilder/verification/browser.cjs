const assert=require('node:assert/strict');
const {chromium}=require('C:/Users/Richard/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright');
(async()=>{const b=await chromium.launch({channel:'msedge',headless:true});try{
const base=process.argv[2]||'http://127.0.0.1:18083';const c=await b.newContext();const p=await c.newPage({viewport:{width:1440,height:1000}});
await p.goto(base+'/?preset=scout');await p.getByText('READY FOR THE SHIPYARD',{exact:true}).waitFor();assert.ok((await p.locator('.price').innerText()).includes('29.430'));assert.ok((await p.locator('.metrics').innerText()).includes('3.0'));
await p.screenshot({path:'C:/gitsrc/TravellerShipBuilder/verification/desktop.png',fullPage:true});
await p.goto(base+'/?preset=trader');assert.ok((await p.locator('.price').innerText()).includes('37.080'));assert.ok((await p.locator('.metrics').innerText()).includes('82.0'));
await p.locator('[name=fuel]').fill('1');await p.locator('.errors').filter({hasText:'Fuel is below'}).waitFor();await p.getByRole('button',{name:'Save ship',exact:true}).click();assert.ok(!p.url().includes('id='));await p.locator('.errors').filter({hasText:'Fuel is below'}).waitFor();
await p.locator('[name=fuel]').fill('-1');await p.getByText('READY FOR THE SHIPYARD',{exact:true}).waitFor();
const name='Ship verification '+Date.now();await p.locator('[name=name]').fill(name);await p.getByRole('button',{name:'Save ship',exact:true}).click();await p.getByText('Ship saved. Further saves update this design.',{exact:true}).waitFor();const id=new URL(p.url()).searchParams.get('id');assert.ok(id);
await p.locator('[name=staterooms]').fill('11');await p.getByRole('button',{name:'Save ship',exact:true}).click();await p.getByText('Ship saved. Further saves update this design.',{exact:true}).waitFor();assert.equal(new URL(p.url()).searchParams.get('id'),id);
await p.getByRole('link',{name:'Saved ships',exact:true}).click();assert.equal(await p.getByRole('heading',{name,exact:true}).count(),1);
const fresh=await b.newContext();const q=await fresh.newPage();await q.goto(base+'/?id='+id);assert.equal(await q.locator('[name=staterooms]').inputValue(),'11');assert.equal(await q.locator('[name=name]').inputValue(),name);
const download=await fresh.request.get(base+'/ships/'+id+'.json');assert.equal(download.status(),200);const saved=await download.json();assert.equal(saved.design.name,name);assert.equal(saved.schemaVersion,1);
const invalid=await fresh.request.get(base+'/?id=../../etc/passwd');assert.equal(invalid.status(),404);
await p.goto(base+'/?preset=scout');await p.setViewportSize({width:390,height:844});assert.equal(await p.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false);await p.screenshot({path:'C:/gitsrc/TravellerShipBuilder/verification/mobile.png',fullPage:true});
await p.getByRole('link',{name:'Saved ships',exact:true}).click();assert.equal(await p.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false);
console.log(JSON.stringify({result:'PASS',id,name,checks:'published Scout and Trader outputs, live calculation, invalid-save rejection, save/update/library/fresh-session restore/JSON export, invalid IDs, mobile layout'}));
}finally{await b.close();}})().catch(e=>{console.error(e);process.exit(1)});
