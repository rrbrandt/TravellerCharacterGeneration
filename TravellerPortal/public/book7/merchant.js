import { lines, departments, merchantCareer } from "./merchant-rules.js";
import { byId, credits, showList, archiveDesk, downloadRecord } from "./desk.js";

const form = byId("career-form");
let generator, record;
lines.forEach((name,index) => byId("lines").add(new Option(name,index)));
byId("lines").value = "4";
departments.slice(0,5).forEach((name,index) => byId("departments").add(new Option(name,index)));

function render(step, saved = false) {
  record = step.done ? step.value : step.value.record;
  const decision = !step.done && step.value.decision;
  byId("name").textContent = record.name;
  const status = decision ? "Term complete. Continue service or muster out." :
    !step.done && !record.years ? "Hired. Continue career to roll the first posting." : record.status;
  byId("status").textContent = status + " · Age " + record.age;
  byId("upp").textContent = "UPP " + record.upp + " / Starting " + record.initialUpp;
  byId("posting").textContent = [record.line,record.department,record.rank,
    record.years + " years served",record.terms + " completed terms"].filter(Boolean).join(" · ");
  byId("history").textContent = record.history.length ? "Lines served · " + record.history.join(" → ") : "";
  showList(byId("skills"),Object.keys(record.skills).sort().map(skill => skill + " " + record.skills[skill]));
  showList(byId("benefits"),[...(record.credits ? [credits(record.credits)] : []),...record.benefits]);
  byId("sheet").hidden = false;
  byId("career-log").hidden = false;
  byId("next").hidden = step.done;
  byId("next").textContent = decision ? "Re-enlist" : "Continue career";
  byId("leave").hidden = !decision;
  for (const id of ["save","download","print"]) byId(id).hidden = !step.done;
  byId("save").disabled = saved;
  byId("notice").textContent = "";
  const log = byId("log");
  log.replaceChildren(...record.log.map((entry,index) => {
    const details=document.createElement("details");
    const summary=document.createElement("summary");
    const events=document.createElement("ul");
    summary.textContent=entry.title;
    showList(events,entry.notes);
    details.open=index===record.log.length-1;
    details.append(summary,events);
    return details;
  }));
}
const save = archiveDesk("classic-traveller-merchants-v1","CT-MERCHANT-1985-CAREER", saved => {
  render({done:true,value:saved},true);
  byId("notice").textContent="Opened saved merchant.";
  byId("name").focus();
});
form.addEventListener("submit", event => {
  event.preventDefault();
  const options=Object.fromEntries(new FormData(form));
  options.name=options.name.trim();
  for (const key of ["line","terms","cashRolls","academyDepartment"]) options[key]=Number(options[key]);
  generator=merchantCareer(options);
  let step=generator.next();
  if (event.submitter?.value!=="step") {
    while (!step.done) step=generator.next(step.value.defaultStay);
  }
  render(step);
  byId("name").focus();
});
byId("next").addEventListener("click",()=>render(generator.next(true)));
byId("leave").addEventListener("click",()=>render(generator.next(false)));
byId("save").addEventListener("click",()=>{
  try {
    record=save(record);
    byId("save").disabled=true;
    byId("notice").textContent="Merchant saved in this browser.";
  } catch {
    byId("notice").textContent="Browser storage is unavailable. Download the JSON to keep your record.";
  }
});
byId("download").addEventListener("click",()=>downloadRecord(record,"merchant-career.json"));
byId("print").addEventListener("click",()=>window.print());
let printDetails=[];
window.addEventListener("beforeprint",()=>{
  printDetails=[...byId("log").querySelectorAll("details:not([open])")];
  printDetails.forEach(detail=>{detail.open=true;});
});
window.addEventListener("afterprint",()=>printDetails.forEach(detail=>{detail.open=false;}));
form.querySelectorAll("button[name=mode]").forEach(button=>{button.disabled=false;});
