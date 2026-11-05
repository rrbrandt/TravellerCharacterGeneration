'use strict';
(()=>{
const $=id=>document.getElementById(id),data=$('atlas-data');if(!data)return;
let atlas=JSON.parse(data.textContent),selected=atlas.worlds[0]||null,dirty=false;
const csrf=document.querySelector('meta[name=csrf-token]').content,base=new URL($('generate-form').action).pathname.replace(/;[^/]*/g,'').replace(/\/generate$/,'');
const NS='http://www.w3.org/2000/svg',saveMessage=document.body.dataset.publicHosting==='true'?'Atlas saved on DoxyLabs. Further saves update this shared atlas.':'Atlas saved on this computer. Further saves update this atlas.';
function el(tag,attrs={},text){const e=document.createElementNS(NS,tag);for(const[k,v]of Object.entries(attrs))e.setAttribute(k,v);if(text!==undefined)e.textContent=text;return e;}
function status(text,error=false){$('status').hidden=false;$('status').textContent=text;$('status').classList.toggle('error',error);}
function changed(){dirty=true;$('save').textContent='Save changes';}
window.addEventListener('beforeunload',e=>{if(dirty){e.preventDefault();e.returnValue='';}});
$('generate-form').addEventListener('submit',e=>{if(dirty&&!window.confirm('Generate a new survey and discard unsaved changes?'))e.preventDefault();else dirty=false;});
function downloads(){for(const[k,extension]of [['download-json','json'],['download-text','txt']]){$(k).hidden=!atlas.id;if(atlas.id)$(k).href=base+'/atlases/'+encodeURIComponent(atlas.id)+'.'+extension;}}
function renderMap(){
const svg=$('subsector-map');svg.replaceChildren();svg.append(el('title',{},atlas.name+' · subsector chart'));
svg.append(el('text',{x:38,y:30,fill:'#a4b2c2','font-family':'Consolas, monospace','font-size':10},'COREWARD'));
svg.append(el('text',{x:640,y:978,fill:'#a4b2c2','font-family':'Consolas, monospace','font-size':10,'text-anchor':'end'},'RIMWARD'));
const worlds=new Map(atlas.worlds.map(w=>[w.hex,w]));
for(let col=1;col<=8;col++)for(let row=1;row<=10;row++){
 const hex=String(col).padStart(2,'0')+String(row).padStart(2,'0'),w=worlds.get(hex),x=65+(col-1)*80,y=80+(row-1)*88+((col-1)%2)*44;
 const group=el('g',{'class':'hex'+(w?' has-world':'')+(w&&selected&&w.hex===selected.hex?' selected':''),'data-hex':hex});
 let points=[];for(let i=0;i<6;i++){const a=i*Math.PI/3;points.push((x+52*Math.cos(a)).toFixed(1)+','+(y+44*Math.sin(a)).toFixed(1));}
 group.append(el('polygon',{points:points.join(' ')}));group.append(el('text',{x,y:y-26,'class':'hex-label-map'},hex));
 if(w){
  group.setAttribute('tabindex','0');group.setAttribute('role','button');group.setAttribute('aria-label',hex+' '+w.name+' '+w.uwp);
  group.append(el('title',{},hex+' · '+w.name+' · '+w.uwp));
  group.append(el('circle',{cx:x,cy:y-8,r:w.size===0?4:7,fill:w.hydrographics>0?'#80c7d3':'#edb66a',stroke:'#f1f0ea','stroke-width':.6}));
  if(w.size===0)group.append(el('circle',{cx:x-6,cy:y-4,r:2,fill:'#edb66a'}));
  group.append(el('text',{x:x+12,y:y-5,'class':'port-map'},w.starport));
  if(w.gasGiant){group.append(el('circle',{cx:x-23,cy:y-8,r:3,fill:'#a4b2c2'}));group.append(el('ellipse',{cx:x-23,cy:y-8,rx:6,ry:2,stroke:'#a4b2c2',fill:'none'}));}
  group.append(el('text',{x:x-36,y:y+5,'class':'base-map'},(w.naval?'N':'')+(w.scout?'S':'')));
  const name=w.name.replace(/ \d{4}$/,'');group.append(el('text',{x,y:y+13,'class':'world-name-map'},name.length>13?name.slice(0,12)+'…':name));
  group.append(el('text',{x,y:y+28,'class':'uwp-map'},w.uwp));
  group.addEventListener('click',()=>select(w));group.addEventListener('keydown',e=>{if(e.key==='Enter'||e.key===' '){e.preventDefault();select(w);}});
 }svg.append(group);
}
}
function select(w){selected=w;renderMap();renderDossier();}
function renderDossier(){
 $('dossier').hidden=!selected;$('selection-empty').hidden=!!selected;if(!selected)return;
 const w=selected;$('selected-hex').textContent='HEX '+w.hex;$('selected-name').textContent=w.name;$('selected-uwp').textContent=w.uwp;
 $('profile-details').replaceChildren();for(const[k,v]of Object.entries(w.details)){const row=document.createElement('div'),dt=document.createElement('dt'),dd=document.createElement('dd');dt.textContent=k;dd.textContent=v;row.append(dt,dd);$('profile-details').append(row);}
 $('world-tags').replaceChildren();for(const tag of w.tradeCodes){const s=document.createElement('span');const titles={Ag:'Agricultural',Na:'Non-agricultural',In:'Industrial',Ni:'Non-industrial',Ri:'Rich',Po:'Poor'};s.textContent=tag+' · '+titles[tag];$('world-tags').append(s);}
 $('planet').className='planet'+(w.size===0?' belt':w.hydrographics===0?' dry':'');$('selected-bases').textContent='Bases · '+w.bases;$('selected-gas').textContent='Gas giant · '+(w.gasGiant?'Yes':'No');
 $('edit-name').value=w.name;$('edit-uwp').value=w.uwp;$('edit-notes').value=w.notes;$('edit-naval').checked=w.naval;$('edit-scout').checked=w.scout;$('edit-gas').checked=w.gasGiant;
 $('rolls').replaceChildren();for(const roll of w.rolls){const li=document.createElement('li'),strong=document.createElement('strong'),code=document.createElement('code'),small=document.createElement('small');strong.textContent=roll.label+' → '+roll.result+' ';code.textContent=roll.dice;small.textContent=roll.formula;li.append(strong,code,small);$('rolls').append(li);}
}
function renderTable(){
 const q=$('filter').value.toLowerCase();$('world-list').replaceChildren();for(const w of atlas.worlds){if(![w.hex,w.name,w.uwp,...w.tradeCodes].join(' ').toLowerCase().includes(q))continue;const row=document.createElement('tr');for(const [i,value]of [w.hex,w.name,w.uwp,w.bases,w.gasGiant?'G':'—',w.tradeCodes.join(' ')].entries()){const cell=document.createElement('td');if(i===1){const b=document.createElement('button');b.textContent=value;b.addEventListener('click',()=>{select(w);if(innerWidth<760)$('dossier').scrollIntoView({behavior:'smooth',block:'start'});});cell.append(b);}else cell.textContent=value;row.append(cell);}$('world-list').append(row);}
}
function render(){ $('atlas-title').textContent=atlas.name;$('world-count').textContent=atlas.worlds.length+' world'+(atlas.worlds.length===1?'':'s')+' · seed '+atlas.seed+(atlas.kind==='subsector'?' · '+atlas.density+'+ presence':' · single-world survey');$('empty').hidden=atlas.worlds.length>0;downloads();renderMap();renderDossier();renderTable();}
async function post(path,body){const r=await fetch(base+path,{method:'POST',headers:{'Content-Type':'application/json','X-CSRF-Token':csrf},body:JSON.stringify(body)});const data=await r.json();if(!r.ok)throw Error(data.error||'The request could not be completed.');return data;}
$('edit-form').addEventListener('submit',async e=>{e.preventDefault();if(!selected)return;try{const old=selected,next=await post('/decode',{uwp:$('edit-uwp').value,name:$('edit-name').value,hex:old.hex});next.naval=$('edit-naval').checked;next.scout=$('edit-scout').checked;next.gasGiant=$('edit-gas').checked;next.bases=next.naval&&next.scout?'Naval + Scout':next.naval?'Naval':next.scout?'Scout':'None';next.notes=$('edit-notes').value;atlas.worlds[atlas.worlds.indexOf(old)]=next;selected=next;changed();render();status('World updated. Save the atlas to keep these changes.');}catch(ex){status(ex.message,true);}});
$('save').addEventListener('click',async()=>{const b=$('save');b.disabled=true;try{atlas.name=document.querySelector('[name=name]').value.trim();const hex=selected?.hex;atlas=await post('/save',atlas);selected=atlas.worlds.find(w=>w.hex===hex)||atlas.worlds[0]||null;dirty=false;history.replaceState(null,'',base+'/?id='+encodeURIComponent(atlas.id));b.textContent='Save atlas';render();status(saveMessage);}catch(ex){status(ex.message,true);}finally{b.disabled=false;}});
$('filter').addEventListener('input',renderTable);
function view(table){$('map-view').hidden=table;$('table-view').hidden=!table;for(const[id,on]of [['table-tab',table],['map-tab',!table]]){$(id).classList.toggle('active',on);$(id).setAttribute('aria-pressed',String(on));}}
$('map-tab').addEventListener('click',()=>view(false));$('table-tab').addEventListener('click',()=>view(true));$('print').addEventListener('click',()=>window.print());render();
})();
