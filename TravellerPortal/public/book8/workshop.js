import { VERSION,chassis,frames,plants,movement,appendages,equipment,weapons,logic,commands,programs,
  presets,calculateRobot,sizeBrain } from "./robot-rules.js";
import { byId,credits,showList,archiveDesk,downloadRecord } from "../book7/desk.js";

const form=byId("design");
const control=name=>form.elements.namedItem(name);
const number=value=>new Intl.NumberFormat(undefined,{maximumFractionDigits:2}).format(value);
const numeric=["tl","budget","size","frame","head","plant","fuel","battery","move","units","legs","batteryDrive",
  "payload","arm","arms","tentacle","tentacles","weapon","law","logic","command","linear","parallel","synaptic","storage","synStorage"];
const flags=["reflective","sealed","circuit"];
const options=(name,rows,offset=0)=>rows.forEach((label,index)=>control(name).add(new Option(label,index+offset)));
options("size",chassis.map(row=>row.name));
for (const [name,rows] of Object.entries({frame:frames,plant:plants,move:movement,weapon:weapons,logic,command:commands})) {
  options(name,rows.map(row=>row[0]));
}
options("arm",appendages.slice(0,4).map(row=>row[0]));
options("tentacle",appendages.slice(4).map(row=>row[0]),4);
byId("version").textContent="VERSION "+VERSION;
let designId, record;

for (const [key,[name]] of Object.entries(equipment)) {
  const label=document.createElement("label");
  label.className="check";
  const input=document.createElement("input");
  input.name="gear-"+key;input.type="checkbox";
  label.append(input,name);
  byId("gear").append(label);
}
for (const [key,[name]] of Object.entries(programs)) {
  const row=document.createElement("div");
  row.className="program";
  const level=document.createElement("label"), resident=document.createElement("label");
  level.textContent=name+" level";
  const select=document.createElement("select");
  select.name="level-"+key;
  for (let value=0;value<=(key==="emotion"?1:4);value++) select.add(new Option(String(value),value));
  level.append(select);
  resident.className="check";
  const checkbox=document.createElement("input");
  checkbox.type="checkbox";checkbox.name="cpu-"+key;
  checkbox.setAttribute("aria-label",name+" resident in CPU");
  resident.append(checkbox,"CPU");
  row.append(level,resident);
  byId("programs").append(row);
}

function apply(design,id) {
  designId=id;
  for (const name of [...numeric,"name","purpose","armor"]) control(name).value=design[name];
  for (const name of flags) control(name).checked=design[name];
  for (const key of Object.keys(equipment)) control("gear-"+key).checked=design.gear.includes(key);
  for (const key of Object.keys(programs)) {
    control("level-"+key).value=design.apps[key]?.level ?? 0;
    control("cpu-"+key).checked=Boolean(design.apps[key]?.resident);
  }
  render();
}
function readDesign() {
  const design=Object.fromEntries([...numeric,"name","purpose","armor"].map(name=>[name,
    numeric.includes(name)?Number(control(name).value):control(name).value.trim()]));
  for (const name of flags) design[name]=control(name).checked;
  design.name ||= "Unnamed robot";
  design.gear=Object.keys(equipment).filter(key=>control("gear-"+key).checked);
  design.apps={};
  for (const key of Object.keys(programs)) {
    const level=Number(control("level-"+key).value);
    if (level) design.apps[key]={level,resident:control("cpu-"+key).checked};
  }
  return design;
}

function render() {
  const move=Number(control("move").value);
  byId("units").hidden=move<4;
  byId("legs").hidden=move!==1;
  byId("drive").hidden=Number(control("plant").value)!==-1||move<1||move>3;
  byId("notice").textContent="";
  if (!form.checkValidity()) {
    record=undefined;
    byId("status").textContent="Complete the fields within their listed ranges to recalculate.";
    byId("status").className="error";
    for (const id of ["save","download","print","auto-brain"]) byId(id).disabled=true;
    return;
  }
  const design=readDesign();
  const report=calculateRobot(design);
  record={schemaVersion:1,version:VERSION,ruleset:"CT-ROBOTS-1986",id:designId,name:design.name,design,report};
  byId("robot-name").textContent=design.name;
  byId("purpose").textContent=design.purpose+" · TL "+design.tl;
  byId("status").textContent=report.valid?"Design passes worksheet checks":"Design needs adjustment";
  byId("status").className=report.valid?"":"error";
  byId("urp").textContent="URP "+report.urp;
  const metrics=[
    ["Price",credits(report.price)],["Volume",number(report.used)+" / "+number(report.capacity)+" L"],
    ["Power",number(report.draw)+(report.output?" / "+number(report.output):"")+" kW"],
    ["Loaded mass",number(report.mass)+" kg"],["Fuel endurance",report.fuelHours?number(report.fuelHours)+" h":"—"],
    ["Battery endurance",report.energy?number(report.batteryHours)+" h":"—"],
    ["Lift thrust",report.thrust?number(report.thrust)+" kg":"—"],
    ["Annual maintenance",credits(report.maintenance)],
  ];
  byId("meters").replaceChildren(...metrics.map(([label,value])=>{
    const box=document.createElement("div");
    const description=document.createElement("small"),reading=document.createElement("strong");
    description.textContent=label;reading.textContent=value;
    box.append(description,reading);
    return box;
  }));
  byId("brain").textContent="CPU "+report.cpu+" / "+report.need.cpu+" required · Storage "+report.storage+
    " / "+report.need.storage+" required · "+report.need.levels+" total skill levels";
  byId("attributes").textContent="STR "+report.strength+" · DEX "+report.dexterity+" · INT "+report.intelligence+
    " · EDU "+report.education+(report.educationPotential>report.education?" (potential "+report.educationPotential+")":"")+
    (report.maneuver?" · "+number(report.maneuver)+" maneuver G":"");
  byId("installed-programs").textContent=report.need.installed.map(app=>
    app.name+(app.key==="emotion"?"":"-"+app.level)+(app.resident?" (CPU)":" (storage)")).join(" · ");
  for (const key of ["errors","warnings"]) {
    byId(key).hidden=!report[key].length;
    showList(byId(key),report[key]);
  }
  byId("ledger").replaceChildren(...report.rows.map(row=>{
    const tr=document.createElement("tr");
    for (const value of [row.name,row.liters,row.mass,row.power,row.cost]) {
      const cell=document.createElement("td");
      cell.textContent=typeof value==="number"?number(value):value;
      tr.append(cell);
    }
    return tr;
  }));
  byId("save").disabled=!report.valid;
  for (const id of ["download","print","auto-brain"]) byId(id).disabled=false;
}
form.addEventListener("input",render);
form.addEventListener("submit",event=>event.preventDefault());
document.querySelectorAll("[data-preset]").forEach(button=>{
  button.disabled=false;
  button.addEventListener("click",()=>{apply(presets[button.dataset.preset]);byId("robot-name").focus();});
});
byId("auto-brain").addEventListener("click",()=>{
  for (const [name,value] of Object.entries(sizeBrain(readDesign()))) control(name).value=value;
  render();
});
const save=archiveDesk("classic-traveller-robots-v1","CT-ROBOTS-1986",saved=>{
  apply(saved.design,saved.id);
  byId("notice").textContent="Opened saved robot.";
  byId("robot-name").focus();
});
byId("save").addEventListener("click",()=>{
  if (!record?.report.valid) return;
  try {
    record=save(record);
    designId=record.id;
    byId("notice").textContent="Robot saved in this browser.";
  } catch {
    byId("notice").textContent="Browser storage is unavailable. Download JSON to keep your design.";
  }
});
byId("download").addEventListener("click",()=>downloadRecord(record,"robot-design.json"));
byId("print").addEventListener("click",()=>window.print());
apply(presets.utility);
