const {chromium}=require('C:/Users/Richard/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright');
const fs=require('fs'); const testName='Verification Army '+Date.now();
(async()=>{const browser=await chromium.launch({channel:'msedge',headless:true});const page=await browser.newPage({viewport:{width:1440,height:1100}});const errors=[];page.on('pageerror',e=>errors.push(e.message));await page.goto('https://traveller.doxylabs.net/mercenary/');await page.screenshot({path:__dirname+'/public-desktop.png',fullPage:true});
await page.getByRole('button',{name:'Roll characteristics',exact:true}).click();await page.locator('input[name=name]').fill(testName);
await page.getByRole('button',{name:'Enlist & lock recruit'}).click();
let count=0;while(count++<180){const phase=(await page.locator('#decisions>.eyebrow').innerText()).trim();
if(phase==='RECRUIT')throw Error(await page.locator('.notice').innerText());
if(['FINAL','DEAD','OTHER'].includes(phase))break;
if(phase==='TRAIN')await page.getByRole('button',{name:'Complete year 1 training'}).click();
else if(phase==='ASSIGN')await page.getByRole('button',{name:'Roll this year’s posting'}).click();
else if(phase==='SKILL')await page.getByRole('button',{name:'Roll skill & finish year'}).click();
else if(phase==='CHOICE')await page.getByRole('button',{name:'Lock specialty'}).click();
else if(phase==='CROSS')await page.getByRole('button',{name:'Roll cross-training'}).click();
else if(phase==='TERM')await page.getByRole('button',{name:'Resolve service decision'}).click();
else if(phase==='BENEFITS'){await page.locator('select[name=table]').selectOption({label:'Material'});await page.getByRole('button',{name:'Roll mustering-out benefit'}).click();}
else throw Error('Unknown phase '+phase);
}
if(count>=180)throw Error('Career stalled');let result=await page.locator('#decisions>.eyebrow').innerText();const record=await (await page.request.get('https://traveller.doxylabs.net/mercenary/export')).json();if(!record.years&&result!=='OTHER')throw Error('No career progress');
// Replaying a prior revision must not change the character.
const csrf=await page.locator('input[name=csrf]').inputValue();const revision=Number(await page.locator('input[name=revision]').inputValue());await page.request.post('https://traveller.doxylabs.net/mercenary/action',{form:{csrf,revision:String(revision-1),action:'new'}});const unchanged=await(await page.request.get('https://traveller.doxylabs.net/mercenary/export')).json();if(unchanged.id!==record.id)throw Error('Stale decision accepted');
await page.goto('https://traveller.doxylabs.net/mercenary/characters');await page.getByRole('heading',{name:testName,exact:true}).first().waitFor();await page.locator('article').filter({has:page.getByRole('heading',{name:testName,exact:true})}).first().getByRole('button').click();if(!(await page.locator('.metrics').innerText()).includes(testName))throw Error('Resume failed');
await page.screenshot({path:__dirname+'/public-career.png',fullPage:true});await page.setViewportSize({width:390,height:844});await page.screenshot({path:__dirname+'/public-mobile.png',fullPage:true});if(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth))throw Error('Mobile overflow');
await page.getByRole('button',{name:'New recruit',exact:true}).click(); console.log('NEW',page.url(),await page.locator('.notice').innerText(),await page.locator('#decisions>.eyebrow').innerText());await page.getByRole('button',{name:'Roll characteristics',exact:true}).click();console.log('AFTER ROLL',page.url(),(await page.locator('body').innerText()).slice(0,2200)); await page.locator('input[name=name]').fill('Verification Marine');await page.locator('#service').selectOption('Marines');if((await page.locator('#arm').inputValue())!=='Marine Infantry')throw Error('Marine arm options failed');await page.getByRole('button',{name:'Enlist & lock recruit'}).click();if(await page.locator('#decisions>.eyebrow').innerText()==='RECRUIT')throw Error('Marine enlist request failed');
if(errors.length)throw Error(errors.join('\n'));console.log(JSON.stringify({careerResult:result,years:record.years,skills:record.skills,saveResume:true,staleDecisionRejected:true,mobileOverflow:false,marineEnlistment:true}));await browser.close();})().catch(e=>{console.error(e);process.exit(1)});
