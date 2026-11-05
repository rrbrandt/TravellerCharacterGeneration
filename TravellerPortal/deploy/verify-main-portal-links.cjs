const assert=require('node:assert/strict');
const {chromium}=require('C:/Users/Richard/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright');
(async()=>{const b=await chromium.launch({channel:'msedge',headless:true});try{
const c=await b.newContext();let next=0;await c.route('**/*',async r=>{const at=Math.max(Date.now(),next);next=at+650;await new Promise(f=>setTimeout(f,at-Date.now()));await r.continue()});const p=await c.newPage({viewport:{width:1440,height:1000}});const errors=[];p.on('pageerror',e=>errors.push(e.message));
for(const [label,path]of [['characters','/traveller/stage?stage=0'],['character-library','/traveller/characters'],['ships','/shipyard/'],['ship-library','/shipyard/library'],['worlds','/worlds/'],['atlas-library','/worlds/library']]){
 await p.goto('https://traveller.doxylabs.net'+path);const link=p.getByRole('link',{name:'Main portal',exact:true});await link.waitFor();assert.equal(await link.count(),1);assert.equal(await link.getAttribute('href'),'/');
 for(const width of [1440,390]){await p.setViewportSize({width,height:1000});await link.scrollIntoViewIfNeeded();const box=await link.boundingBox();assert.ok(box.x>=0&&box.x+box.width<=width+1,label+' link fits viewport');if(!label.includes('library'))await p.screenshot({path:'C:/gitsrc/TravellerPortal/deploy/'+label+'-portal-'+width+'.png',fullPage:false});}
 await link.click();await p.waitForURL('https://traveller.doxylabs.net/');assert.equal(await p.locator('.tool').count(),3);
}
assert.deepEqual(errors,[]);console.log('PASS: one Main portal link on all three apps and their libraries, desktop/mobile visibility, six return navigations, and no JavaScript errors.');
}finally{await b.close()}})().catch(e=>{console.error(e);process.exit(1)});
