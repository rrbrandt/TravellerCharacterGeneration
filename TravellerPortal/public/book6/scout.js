import { VERSION, attributes, scoutCareer } from "./scout-rules.js";

const byId = id => document.getElementById(id);
const storageKey = "classic-traveller-scouts-v1";
let career, record, archive = [];
try {
  const stored = JSON.parse(localStorage.getItem(storageKey) || "[]");
  if (Array.isArray(stored)) archive = stored.filter(item => item?.ruleset === "CT-SCOUTS-1983" && item.schemaVersion === 1);
} catch {
  // A restricted browser can still roll and download characters.
}
byId("version").textContent = "VERSION " + VERSION;

function list(element, values) {
  element.replaceChildren();
  for (const value of values.length ? values : ["None"]) {
    const li = document.createElement("li");
    li.textContent = value;
    element.append(li);
  }
}

function showArchive() {
  const element = byId("saved");
  element.replaceChildren();
  if (!archive.length) list(element, ["No saved Scouts in this browser."]);
  for (const item of archive) {
    const li = document.createElement("li");
    const button = document.createElement("button");
    button.textContent = item.name + " · " + item.upp;
    button.addEventListener("click", () => {
      career = undefined;
      render({done: true, value: item}, true);
      byId("notice").textContent = "Opened saved Scout.";
    });
    li.append(button);
    element.append(li);
  }
}

function render(result, alreadySaved = false) {
  record = result.done ? result.value : result.value.record;
  const term = !result.done && result.value.kind === "term";
  byId("name").textContent = record.name;
  const status = term ? "Term complete. Continue service or muster out." :
    !result.done && !record.years ? "Enlisted. Continue career to roll the first year." : record.status;
  byId("status").textContent = status + " · Age " + record.age;
  byId("upp").textContent = "UPP " + record.upp + " / Starting " + record.initialUpp;
  byId("upp").title = record.stats.map((value, index) => attributes[index] + " " + value).join(" · ");
  byId("posting").textContent = [record.track, record.office, record.rank,
    record.years + " years served", record.terms + " completed terms"].filter(Boolean).join(" · ");
  list(byId("skills"), Object.entries(record.skills).sort(([a], [b]) => a.localeCompare(b))
    .map(([name, level]) => name + " " + level));
  list(byId("benefits"), [...(record.credits ? ["Cr " + record.credits.toLocaleString()] : []), ...record.benefits]);
  byId("sheet").hidden = false;
  byId("history").hidden = false;
  byId("next").hidden = result.done;
  byId("next").textContent = term ? "Re-enlist" : "Continue career";
  byId("leave").hidden = !term;
  for (const id of ["save", "export", "print"]) byId(id).hidden = !result.done;
  byId("save").disabled = alreadySaved;
  byId("notice").textContent = "";
  const log = byId("log");
  const expanded = new Set([...log.querySelectorAll("details[open]")].map(item => item.dataset.index));
  log.replaceChildren();
  record.log.forEach((entry, index) => {
    const details = document.createElement("details");
    const summary = document.createElement("summary");
    const events = document.createElement("ul");
    details.dataset.index = index;
    details.open = expanded.has(String(index)) || index === record.log.length - 1;
    summary.textContent = entry.title;
    list(events, entry.notes);
    details.append(summary, events);
    log.append(details);
  });
}

byId("recruit").addEventListener("submit", event => {
  event.preventDefault();
  const options = Object.fromEntries(new FormData(event.currentTarget));
  options.name = options.name.trim();
  for (const key of ["terms", "cashRolls", "honorsOffice"]) options[key] = Number(options[key]);
  career = scoutCareer(options);
  let result = career.next();
  if (event.submitter?.value !== "step") {
    while (!result.done) result = career.next(result.value.defaultStay);
  }
  byId("log").replaceChildren();
  render(result);
  byId("name").focus();
});

byId("next").addEventListener("click", () => render(career.next(true)));
byId("leave").addEventListener("click", () => render(career.next(false)));
byId("save").addEventListener("click", () => {
  const updated = [record, ...archive];
  try {
    localStorage.setItem(storageKey, JSON.stringify(updated));
    archive = updated;
    byId("save").disabled = true;
    byId("notice").textContent = "Scout saved in this browser.";
    showArchive();
  } catch {
    byId("notice").textContent = "Browser storage is unavailable. Download the JSON record to keep it.";
  }
});
byId("export").addEventListener("click", () => {
  const url = URL.createObjectURL(new Blob([JSON.stringify(record, null, 2)], {type: "application/json"}));
  const link = document.createElement("a");
  link.href = url;
  link.download = "scout-character.json";
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
showArchive();
