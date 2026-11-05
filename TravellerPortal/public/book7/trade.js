import { marketWorld, quoteTrade, rollFreight, settleVoyage, brokerMaximum } from "./trade-rules.js";
import { byId, credits, showList, archiveDesk, downloadRecord } from "./desk.js";

const voyage = byId("voyage"), loadout = byId("loadout");
let current, record;
const worlds = new Map();

function atlasWorlds(data) {
  if (data.ruleset === "CT-SCOUTS-1983-SURVEY") {
    const main = data.stars?.flatMap(star => star.bodies.flatMap(body => [body,...body.moons]))
      .find(body => body.id === data.mainId);
    return main ? [main] : [];
  }
  return data.atlas?.worlds ?? data.worlds ?? [];
}

function addWorlds(data) {
  for (const entry of atlasWorlds(data).slice(0,80)) {
    try {
      const world = marketWorld(entry.name,entry.uwp ?? entry.upp);
      const label = world.name + " · " + world.upp;
      if (!worlds.has(label)) byId("worlds").add(new Option(label,label));
      worlds.set(label,world);
    } catch {
      // Entries without a valid mainworld UWP cannot act as market worlds.
    }
  }
  for (const id of ["set-source","set-market"]) byId(id).disabled = !worlds.size;
}
try {
  const saved = JSON.parse(localStorage.getItem("classic-traveller-surveys-v1") || "[]");
  if (Array.isArray(saved)) saved.forEach(addWorlds);
} catch {
  // Atlas uploads remain available.
}
for (const side of ["source","market"]) {
  byId("set-" + side).addEventListener("click",() => {
    const world=worlds.get(byId("worlds").value);
    voyage.elements[side+"Name"].value=world.name;
    voyage.elements[side+"Uwp"].value=world.upp;
    resetQuote();
  });
}
async function uploaded(event) {
  const file=event.target.files[0];
  if (!file || file.size > 5_000_000) throw new Error("Select a JSON export under 5 MB.");
  return JSON.parse(await file.text());
}
byId("atlas").addEventListener("change",async event=>{
  try {
    const data=await uploaded(event);
    if (!atlasWorlds(data).length) throw new Error("No worlds found in that export.");
    addWorlds(data);
    byId("import-status").textContent=worlds.size+" worlds ready to select.";
  } catch (error) {byId("import-status").textContent=error.message;}
});
byId("ship").addEventListener("change",async event=>{
  try {
    const {design}=await uploaded(event);
    if (!design || typeof design.name!=="string") throw new Error("Select a saved Book 2 or High Guard ship export.");
    loadout.elements.shipName.value=design.name.slice(0,60);
    const cargo=design.cargo;
    if (typeof cargo!=="number" || cargo<0 || cargo>1000000) {
      byId("import-status").textContent="Ship name loaded. Enter available cargo from its design worksheet.";
      return;
    }
    loadout.elements.hold.value=cargo;
    byId("import-status").textContent="Declared cargo loaded. Check available tonnage against the ship worksheet.";
    updateCapacity();
  } catch (error) {byId("import-status").textContent=error.message;}
});
byId("example").addEventListener("click",()=>{
  for (const [key,value] of Object.entries({sourceName:"Rule",sourceUwp:"C776977-7",marketName:"Regina",marketUwp:"A788899-A"})) {
    voyage.elements[key].value=value;
  }
  loadout.elements.tons.value=10;
  loadout.elements.broker.value=4;
  resetQuote();
  voyage.elements.sourceUwp.focus();
});

function resetQuote() {
  current=undefined;
  for (const id of ["market-quote","offers","loadout"]) byId(id).hidden=true;
  byId("quote-error").hidden=true;
}
voyage.addEventListener("input",resetQuote);

function selectedLots() {
  return [...byId("lots").querySelectorAll("input:checked")].map(input=>current.lots[Number(input.value)]);
}
function updateCapacity() {
  if (!current) return;
  const freight=selectedLots().reduce((sum,lot)=>sum+lot.tons,0);
  const total=freight+Number(loadout.elements.tons.value);
  const hold=Number(loadout.elements.hold.value);
  byId("capacity").textContent=total+" / "+hold+" tons used · "+freight+" freight tons"+
    (total>hold ? " · over capacity" : "");
  byId("capacity").classList.toggle("error",total>hold);
}

voyage.addEventListener("submit",event=>{
  event.preventDefault();
  byId("quote-error").hidden=true;
  try {
    const source=marketWorld(voyage.elements.sourceName.value,voyage.elements.sourceUwp.value);
    const market=marketWorld(voyage.elements.marketName.value,voyage.elements.marketUwp.value);
    const quote=quoteTrade(source,market,Number(voyage.elements.accelerate.value));
    const lots=rollFreight(source,market,voyage.elements.zone.value,Number(voyage.elements.liaison.value));
    current={source,market,quote,lots};
    byId("route").textContent=source.name+" → "+market.name;
    byId("world-codes").textContent=source.upp+" "+source.codes.join(" ")+" → "+market.upp+" "+market.codes.join(" ");
    byId("purchase-price").textContent=credits(quote.purchasePrice);
    byId("market-price").textContent=quote.marketPrice?credits(quote.marketPrice):"No speculative market";
    byId("modifiers").textContent="Trade class adjustment "+credits(quote.tradeModifier)+
      " · Source minus market TL "+quote.techDifference+" · Local brokers up to "+brokerMaximum[market.port];
    showList(byId("pairs"),quote.pairs.map(pair=>pair.from+" → "+pair.to+" · "+credits(pair.modifier*1000)));
    byId("lots").replaceChildren(...lots.map((lot,index)=>{
      const label=document.createElement("label");
      label.className="check";
      const input=document.createElement("input");
      input.type="checkbox";input.value=index;
      label.append(input,lot.kind+" lot "+lot.id+" · "+lot.tons+" tons · "+credits(lot.fee));
      return label;
    }));
    if (!lots.length) byId("lots").textContent="No freight lots are available.";
    byId("settled").hidden=true;
    for (const id of ["market-quote","offers","loadout"]) byId(id).hidden=false;
    byId("sale-error").hidden=true;
    loadout.elements.broker.max=brokerMaximum[market.port];
    loadout.elements.broker.value=Math.min(Number(loadout.elements.broker.value),brokerMaximum[market.port]);
    updateCapacity();
    byId("route").focus();
  } catch (error) {
    byId("quote-error").textContent=error.message;
    byId("quote-error").hidden=false;
  }
});
byId("lots").addEventListener("change",updateCapacity);
loadout.addEventListener("input",updateCapacity);

function showRecord(saved) {
  record=saved;
  byId("ledger-title").textContent=saved.name;
  byId("manifest").textContent=saved.shipName+" · "+saved.freightTons+" freight tons + "+saved.tons+
    " speculative tons / "+saved.hold+" tons available";
  byId("sale-roll").textContent=saved.tons ? "Actual value · "+saved.dice.join(" + ")+" + Broker "+
    saved.broker+" = "+saved.result+" · "+saved.percentage+"% of base sale price" : "Freight-only voyage";
  const lines=[
    ["Freight revenue",saved.freightRevenue],["Speculative sale",saved.sales],["Purchase cost",-saved.purchase],
    ["Broker commission",-saved.commission],["Voyage expenses",-saved.expenses],["Net voyage result",saved.net],
  ];
  byId("ledger").replaceChildren(...lines.flatMap(([label,amount])=>{
    const term=document.createElement("dt"),value=document.createElement("dd");
    term.textContent=label;
    value.textContent=credits(amount);
    return [term,value];
  }));
  for (const id of ["offers","loadout"]) byId(id).hidden=true;
  byId("settled").hidden=false;
  byId("save-status").textContent="";
  byId("ledger-title").focus();
}
loadout.addEventListener("submit",event=>{
  event.preventDefault();
  byId("sale-error").hidden=true;
  try {
    const settings={};
    for (const key of ["hold","tons","broker","expenses"]) settings[key]=Number(loadout.elements[key].value);
    const settled=settleVoyage({...current,...settings,freight:selectedLots()});
    showRecord({...settled,name:current.source.name+" → "+current.market.name,
      shipName:loadout.elements.shipName.value.trim()||"Free Trader"});
  } catch (error) {
    byId("sale-error").textContent=error.message;
    byId("sale-error").hidden=false;
  }
});
const save=archiveDesk("classic-traveller-voyages-v1","CT-MERCHANT-1985-TRADE",saved=>{
  byId("market-quote").hidden=true;
  showRecord(saved);
  byId("save-status").textContent="Opened saved voyage.";
});
byId("save").addEventListener("click",()=>{
  try {
    record=save(record);
    byId("save-status").textContent="Voyage saved in this browser.";
  } catch {
    byId("save-status").textContent="Browser storage is unavailable. Download the JSON to keep the record.";
  }
});
byId("download").addEventListener("click",()=>downloadRecord(record,"merchant-voyage.json"));
byId("print").addEventListener("click",()=>window.print());
byId("quote-button").disabled=false;
