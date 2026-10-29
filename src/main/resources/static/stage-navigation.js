(() => {
  const contextPath = (document.body.dataset.contextPath || '/').split(';')[0];
  const form = document.querySelector('.panel form');
  const tabs = [...document.querySelectorAll('.steps li')];
  const current = tabs.findIndex(t => t.classList.contains('active'));
  const activeStage = Number(form.dataset.activeStage || 0);
  const readOnly = form.dataset.readOnly === 'true';
  document.querySelector('.character-save-form')?.addEventListener('submit', event => {
    const saveForm = event.currentTarget;
    saveForm.querySelectorAll('[data-save-value]').forEach(input => input.remove());
    const values = {fromStage: current};
    if (!readOnly && current === activeStage) {
      for (const [name,value] of new FormData(form)) {
        if ((current === 0 && ['name','age'].includes(name)) ||
            (current === 1 && name.startsWith('characteristics.'))) values[name] = value;
      }
    }
    for (const [name,value] of Object.entries(values)) {
      const input=document.createElement('input'); input.type='hidden'; input.name=name; input.value=value;
      input.dataset.saveValue='true'; saveForm.append(input);
    }
  });
  const key = 'traveller-character-draft';
  let saved = {};
  try { saved = JSON.parse(sessionStorage.getItem(key) || '{}'); } catch {}
  const isField = name => ['name','age','service','rank'].includes(name) || name.startsWith('characteristics.');
  // The server response owns rendered values (including freshly rolled characteristics).
  function capture() {
    for (const [name,value] of new FormData(form)) if (isField(name)) saved[name] = value;
    try { sessionStorage.setItem(key, JSON.stringify(saved)); } catch {}
  }
  function carry() {
    capture();
    for (const [name,value] of Object.entries(saved)) {
      if (!isField(name) || form.elements.namedItem(name)) continue;
      const input = document.createElement('input'); input.type='hidden'; input.name=name; input.value=value; form.append(input);
    }
  }
  function go(stage) {
    const navigation = document.createElement('form');
    navigation.method='post'; navigation.action=contextPath+'stage';
    const values = {stage, fromStage: current};
    if (!readOnly && current === activeStage) {
      for (const [name,value] of new FormData(form)) {
        if ((current === 0 && ['name','age'].includes(name)) ||
            (current === 1 && name.startsWith('characteristics.'))) values[name] = value;
      }
    }
    for (const [name,value] of Object.entries(values)) {
      const input=document.createElement('input'); input.type='hidden'; input.name=name; input.value=value; navigation.append(input);
    }
    document.body.append(navigation); navigation.submit();
  }
  tabs.forEach((tab, i) => {
    const button=document.createElement('button'); button.type='button'; button.innerHTML=tab.innerHTML;
    button.setAttribute('aria-label', ['Identity','Characteristics','Service','Skills','Muster Out Benefits','Final Character'][i]);
    if (i===current) button.setAttribute('aria-current','step');
    button.addEventListener('click',()=>go(i)); tab.replaceChildren(button);
  });
  const nav=document.createElement('nav'); nav.className='stage-arrows'; nav.setAttribute('aria-label','Stage navigation');
  for(const [label,offset] of [['← Previous',-1],['Next →',1]]) {
    const button=document.createElement('button'); button.type='button'; button.textContent=label;
    button.disabled=current+offset<0 || current+offset>=tabs.length;
    button.addEventListener('click',()=>go(current+offset)); nav.append(button);
  }
  document.querySelector('.steps').after(nav);
  form.addEventListener('submit', event=>{
    if(readOnly) { event.preventDefault(); return; }
    carry();
    if(current===0 || (current===1 && event.submitter?.name!=='ReRoll')) {event.preventDefault(); go(current+1);}
  });
  if (readOnly) {
    const notice=document.createElement('p'); notice.className='stage-lock-notice'; notice.setAttribute('role','status');
    notice.textContent='Completed stage — read only. Use the tabs to review your character or return to your current stage.';
    form.prepend(notice);
    form.querySelectorAll('input,select,textarea,button').forEach(control=>{
      if(control.matches('input[type=text],input[type=number],textarea')) control.readOnly=true;
      else control.disabled=true;
      if(control.matches('button,input[type=submit],input[type=reset]')) control.hidden=true;
    });
  }
  document.querySelectorAll('a').forEach(a=>{if(new URL(a.href).pathname===contextPath+'newcharacter')a.addEventListener('click',()=>sessionStorage.removeItem(key));});
  // Hide repeated editing fields outside their owning stage; retain their form values.
  document.querySelectorAll('.field').forEach(label=>{
    const name=label.querySelector('input')?.name || '';
    if ((current>0 && ['name','age'].includes(name)) || (current>1 && name.startsWith('characteristics.'))) label.hidden=true;
  });
  document.querySelectorAll('.panel h4').forEach(h=>{if(/^(Character:|Characteristics:)$/i.test(h.textContent.trim())) h.hidden=true;});
  capture();
  if (current >= 2) {
    const stats = [['strength','STR','Strength'],['dexterity','DEX','Dexterity'],['endurance','END','Endurance'],['intelligence','INT','Intelligence'],['education','EDU','Education'],['socialStanding','SOC','Social Standing']];
    const strip=document.createElement('section'); strip.className='character-strip'; strip.setAttribute('aria-label','Character characteristics and UPP');
    const list=document.createElement('dl'); list.className='character-stats';
    const digits=[];
    for (const [key,short,label] of stats) {
      const raw=form.elements.namedItem('characteristics.'+key)?.value ?? saved['characteristics.'+key];
      const value=raw==null || String(raw).trim()==='' ? NaN : Number(raw);
      const valid=Number.isInteger(value) && value>=0 && value<=15;
      digits.push(valid ? value.toString(16).toUpperCase() : '?');
      const item=document.createElement('div'), term=document.createElement('dt'), detail=document.createElement('dd');
      term.textContent=short; term.title=label; detail.textContent=Number.isFinite(value) ? String(value) : '—';
      item.append(term,detail); list.append(item);
    }
    const profile=document.createElement('div'); profile.className='upp';
    const label=document.createElement('span'); label.textContent='UPP';
    const value=document.createElement('code'); value.textContent=digits.join('');
    profile.title='Universal Personality Profile: STR DEX END INT EDU SOC. A=10, B=11, C=12, D=13, E=14, F=15. ? = missing or outside 0–15.';
    profile.append(label,value); strip.append(list,profile); form.prepend(strip);
  }
})();
