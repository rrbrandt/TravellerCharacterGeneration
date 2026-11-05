(() => {
 const form=document.querySelector('#ship-form'),status=document.querySelector('#update-status');
 if(!form)return;
 function updateCraftReferences(){
  let selected=0;
  for(const card of document.querySelectorAll('[data-craft-id]')){
   const input=form.elements.namedItem('vehicles['+card.dataset.craftId+']');
   const quantity=Number(input?.value || 0);
   const active=Number.isInteger(quantity)&&quantity>0;
   card.hidden=!active;if(active)selected++;
   card.querySelector('[data-craft-quantity]').textContent=quantity+' selected';
  }
  const empty=document.querySelector('#craft-reference-empty');if(empty)empty.hidden=selected>0;
 }
 updateCraftReferences();form.addEventListener('input',updateCraftReferences);
 let timer,controller,revision=0;
 const narrow=window.matchMedia('(max-width:850px)');
 function compactReport(){if(narrow.matches)document.querySelectorAll('.report details').forEach(d=>d.open=false);}
 compactReport();narrow.addEventListener('change',compactReport);
 let printDetails=[];
 window.addEventListener('beforeprint',()=>{printDetails=[...document.querySelectorAll('details')].map(d=>[d,d.open]);for(const [d]of printDetails)d.open=true;});
 window.addEventListener('afterprint',()=>{for(const [d,open]of printDetails)d.open=open;});
 async function calculate(){
  const current=++revision;controller?.abort();controller=new AbortController();status.textContent='Updating design…';
  try{
   const response=await fetch(form.action,{method:'POST',body:new URLSearchParams(new FormData(form)),signal:controller.signal});
   if(!response.ok)throw Error('Calculation unavailable');
   const doc=new DOMParser().parseFromString(await response.text(),'text/html'),report=doc.querySelector('#report-content');
   if(!report)throw Error('Calculation unavailable');
   if(current!==revision)return;
   document.querySelector('#report-content').replaceWith(document.importNode(report,true));compactReport();status.textContent='Design updated.';
  }catch(error){if(error.name!=='AbortError')status.textContent='Live calculation is unavailable. Use Recalculate design to try again.';}
 }
 form.addEventListener('input',()=>{clearTimeout(timer);status.textContent='Changes pending…';timer=setTimeout(calculate,700);});
 form.addEventListener('change',()=>{clearTimeout(timer);timer=setTimeout(calculate,150);});
 form.addEventListener('submit',()=>{clearTimeout(timer);controller?.abort();});
})();
