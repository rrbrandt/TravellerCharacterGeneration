import { generateCareer } from "./naval-rules.js";

const byId = id => document.getElementById(id);
const form = byId("recruit");
const save = byId("save");
const saveStatus = byId("save-status");
const savedLink = byId("saved-link");
const archiveKey = "high-guard-naval-archive-v1";
const idPattern = /^[a-f0-9]{8}(?:-[a-f0-9]{4}){3}-[a-f0-9]{12}$/;
let record;
let library = [];
let revision = 0;

try {
  const saved = JSON.parse(localStorage.getItem(archiveKey) || "[]");
  if (Array.isArray(saved)) library = saved.filter(item => item && idPattern.test(item.id) && typeof item.name === "string");
} catch {
  // The record link still works when browser storage is unavailable.
}

function careerUrl(id) {
  const url = new URL(location.pathname, location.origin);
  url.searchParams.set("career", id);
  return url;
}

function renderLibrary() {
  const list = byId("saved-list");
  list.replaceChildren();
  if (!library.length) fillList(list, ["No saved characters in this browser."]);
  for (const entry of library) {
    const li = document.createElement("li");
    const link = document.createElement("a");
    link.href = careerUrl(entry.id);
    link.textContent = entry.name;
    li.append(link);
    list.append(li);
  }
}

function clearSaveState() {
  revision++;
  save.disabled = false;
  savedLink.hidden = true;
  saveStatus.textContent = "";
  if (location.search) history.replaceState(null, "", location.pathname + location.hash);
}

function showSavedLink(id) {
  savedLink.href = careerUrl(id);
  savedLink.hidden = false;
  save.disabled = true;
}

function fillList(element, entries) {
  element.replaceChildren();
  for (const value of entries.length ? entries : ["None"]) {
    const item = document.createElement("li");
    item.textContent = value;
    element.append(item);
  }
}

function render(career) {
  byId("record-title").textContent = career.name;
  byId("outcome").textContent = career.status + " · Age " + career.age;
  byId("summary").classList.toggle("ok", career.status === "Mustered out");
  byId("upp").textContent = "UPP " + career.upp + "  /  Starting " + career.initialUpp;
  byId("characteristics").textContent = career.stats.map((value, index) =>
    ["STR", "DEX", "END", "INT", "EDU", "SOC"][index] + " " + value).join(" · ");
  byId("service").textContent = [
    career.navy && career.navy + " Navy", career.branch, career.rank,
    career.years + (career.years === 1 ? " year served" : " years served"),
    career.terms + (career.terms === 1 ? " completed term" : " completed terms"),
  ].filter(Boolean).join(" · ");
  fillList(byId("skills"), Object.keys(career.skills).sort().map(name => name + " " + career.skills[name]));
  fillList(byId("medals"), Object.entries(career.medals).map(([name, count]) => name + " ×" + count));
  const benefits = [...career.benefits];
  if (career.credits) benefits.unshift("Cr " + career.credits.toLocaleString());
  if (career.pension) benefits.push("Cr " + career.pension.toLocaleString() + " annual pension");
  fillList(byId("benefits"), benefits);
  byId("log").replaceChildren();
  for (const [index, entry] of career.log.entries()) {
    const details = document.createElement("details");
    const summary = document.createElement("summary");
    const list = document.createElement("ul");
    summary.textContent = entry.title;
    fillList(list, entry.notes);
    details.open = index === 0 || index === career.log.length - 1;
    details.append(summary, list);
    byId("log").append(details);
  }
  byId("sheet").hidden = false;
  byId("history").hidden = false;
}

form.addEventListener("submit", event => {
  event.preventDefault();
  clearSaveState();
  const data = new FormData(form);
  const options = Object.fromEntries(data);
  for (const key of ["navy", "branch", "terms", "risk", "specialist", "aideDuty", "cashRolls"]) {
    options[key] = Number(options[key]);
  }
  options.name = options.name.trim();
  options.tech = [Number(options.planet), Number(options.subsector), 15];
  record = generateCareer(options);
  render(record);
});

form.addEventListener("reset", () => {
  clearSaveState();
  record = undefined;
  byId("record-title").textContent = "Awaiting a recruit";
  byId("outcome").textContent = "Roll a naval career to fill this record.";
  byId("summary").classList.remove("ok");
  byId("sheet").hidden = true;
  byId("history").hidden = true;
});

save.addEventListener("click", async () => {
  if (!record || save.disabled) return;
  const career = record;
  const currentRevision = revision;
  save.disabled = true;
  saveStatus.textContent = "Saving character…";
  try {
    const response = await fetch("./naval-records", {
      method: "POST",
      headers: {"Content-Type": "application/json", "X-High-Guard-Action": "save"},
      body: JSON.stringify({ ...career, ruleset: "CT-HG1980", schemaVersion: 1 }),
    });
    if (!response.ok) throw new Error("Save failed");
    const { id } = await response.json();
    if (!idPattern.test(id)) throw new Error("Missing save link");
    library.unshift({id, name: career.name});
    let indexed = true;
    try { localStorage.setItem(archiveKey, JSON.stringify(library)); }
    catch { indexed = false; }
    renderLibrary();
    if (revision !== currentRevision) return;
    const url = careerUrl(id);
    history.replaceState(null, "", url);
    showSavedLink(id);
    saveStatus.textContent = indexed ? "Character saved." : "Character saved. Keep the link to reopen it later.";
  } catch {
    if (revision !== currentRevision) return;
    save.disabled = false;
    saveStatus.textContent = "Could not save the character. Try again or download the record.";
  }
});

async function openSaved(id) {
  const currentRevision = revision;
  try {
    if (!idPattern.test(id)) throw new Error("Invalid save link");
    const response = await fetch("./naval-records/" + id);
    if (!response.ok) throw new Error("Record unavailable");
    const career = await response.json();
    if (revision !== currentRevision) return;
    record = career;
    render(record);
    showSavedLink(id);
    saveStatus.textContent = "Opened saved character.";
  } catch {
    if (revision === currentRevision) saveStatus.textContent = "Could not open this saved character.";
  }
}

renderLibrary();
const savedId = new URLSearchParams(location.search).get("career");
if (savedId) openSaved(savedId);

byId("download").addEventListener("click", () => {
  const url = URL.createObjectURL(new Blob(
    [JSON.stringify({ ruleset: "CT-HG1980", ...record }, null, 2)], { type: "application/json" }));
  const link = document.createElement("a");
  link.href = url;
  link.download = "high-guard-career.json";
  link.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
});

byId("print").addEventListener("click", () => window.print());
let printDetails = [];
window.addEventListener("beforeprint", () => {
  printDetails = [...byId("log").querySelectorAll("details:not([open])")];
  printDetails.forEach(details => { details.open = true; });
});
window.addEventListener("afterprint", () => {
  printDetails.forEach(details => { details.open = false; });
});
