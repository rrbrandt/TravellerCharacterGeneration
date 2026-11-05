import { generateSurvey, VERSION } from "./survey-rules.js";
import { mapViewBox, surfaceFor, terrain } from "./surface-map.js";

const byId = id => document.getElementById(id);
const form = byId("survey-form");
const map = byId("map");
const archiveKey = "classic-traveller-surveys-v1";
byId("version").textContent = "VERSION " + VERSION;
let archive = [];
let survey, body, chart, selectedSite, moving;
try {
  const stored = JSON.parse(localStorage.getItem(archiveKey) || "[]");
  if (Array.isArray(stored)) archive = stored.filter(record => record?.ruleset === "CT-SCOUTS-1983-SURVEY" && record.schemaVersion === 1);
} catch {
  // Generation and downloads work without browser storage.
}

function list(element, values) {
  element.replaceChildren(...values.map(value => {
    const item = document.createElement("li");
    item.textContent = value;
    return item;
  }));
}

function modeChanged() {
  const expanding = form.elements.mode.value === "continuation";
  byId("profile-field").hidden = !expanding;
  form.elements.uwp.disabled = !expanding;
  form.elements.uwp.required = expanding;
}
form.elements.mode.addEventListener("change", modeChanged);
const params = new URLSearchParams(location.search);
const surfaceShortcut = params.get("surface") === "1";
if (surfaceShortcut) {
  byId("page-title").textContent = "Generate surface";
  byId("page-intro").textContent = "Roll a system or enter an existing mainworld profile. The surface map will open for the mainworld or the first solid world available.";
  byId("generate").textContent = "Generate system and surface";
}
if (params.has("uwp")) {
  form.elements.mode.value = "continuation";
  form.elements.uwp.value = params.get("uwp").slice(0,9);
  form.elements.name.value = (params.get("name") || "").slice(0,60);
}
modeChanged();
byId("regina").addEventListener("click", () => {
  form.elements.name.value = "Regina";
  form.elements.mode.value = "continuation";
  form.elements.uwp.value = "A788899-C";
  modeChanged();
  form.elements.uwp.focus();
});

function showSurvey(record) {
  survey = record;
  const bodies = record.stars.flatMap(star => star.bodies.flatMap(parent => [parent,...parent.moons]));
  const main = bodies.find(item => item.id === record.mainId);
  byId("system-name").textContent = record.name;
  byId("system-summary").textContent = record.stars.length + (record.stars.length === 1 ? " star" : " stars") +
    " · Mainworld " + main.upp + (record.anchor ? " · Imported profile preserved" : "");
  byId("seed").textContent = "Seed " + record.seed;
  byId("save-status").textContent = "";
  byId("rolls").replaceChildren();
  byId("dice").open = false;
  byId("roster").replaceChildren();
  for (const star of record.stars) {
    const heading = document.createElement("h3");
    heading.className = "star";
    heading.textContent = [star.name,star.spectrum,star.orbit !== undefined ?
      (typeof star.orbit === "number" ? "Companion orbit " + star.orbit :
        star.distance ? star.distance.toLocaleString() + " AU separation" : star.orbit + " companion") : ""]
      .filter(Boolean).join(" · ");
    byId("roster").append(heading);
    for (const parent of star.bodies) {
      for (const item of [parent,...parent.moons]) {
        const row = document.createElement("button");
        row.className = "secondary orbit-row" + (parent === item ? "" : " moon");
        row.dataset.body = item.id;
        for (const [index,value] of [String(item.orbit),
          item.main ? item.name + " · Mainworld" : item.kind === "Gas giant" ?
            (item.large ? "Large gas giant" : "Small gas giant") : item.kind,
          item.upp || item.zone].entries()) {
          const cell = document.createElement(index === 0 ? "small" : "span");
          cell.textContent = value;
          row.append(cell);
        }
        row.addEventListener("click", () => showBody(item));
        byId("roster").append(row);
      }
    }
  }
  byId("report").hidden = false;
  const mapped = surfaceShortcut ? (hasSurface(main) ? main : bodies.find(hasSurface)) : undefined;
  showBody(mapped ?? main);
  if (mapped) showSurface();
  else byId("system-name").focus();
}

function hasSurface(item) {
  return ["Planet","Captured planet","Moon"].includes(item.kind);
}

function showBody(item) {
  body = item;
  chart = undefined;
  byId("surface-panel").hidden = true;
  byId("body-name").textContent = item.name;
  byId("uwp").textContent = item.upp || item.kind;
  const fields = [["Type",item.kind],["Orbital zone",item.zone],["Distance from host star",item.distance.toLocaleString(undefined,{maximumFractionDigits:2}) + " AU"]];
  if (item.kind === "Moon" || item.kind === "Ring") fields.push(["Satellite orbit",item.orbit + " parent radii"]);
  if (item.upp) fields.push(["Size",item.sizeCode],["Atmosphere",item.atmosphereName],
    ["Hydrographics",item.water * 10 + "%"],["Population code",item.population],
    ["Government",item.government],["Law level",item.law],["Technology",item.tech],
    [item.main ? "Starport" : "Spaceport",item.port]);
  const profile = byId("profile");
  profile.replaceChildren();
  for (const [term,value] of fields) {
    const label = document.createElement("dt");
    const description = document.createElement("dd");
    label.textContent = term;
    description.textContent = value;
    profile.append(label,description);
  }
  byId("facilities").textContent = item.facilities.join(" · ");
  const canMap = hasSurface(item);
  byId("surface").hidden = !canMap;
  byId("body-note").textContent = canMap ? "" : "Select a solid planet or moon to generate a surface.";
  byId("roster").querySelectorAll("button").forEach(row =>
    row.setAttribute("aria-current",String(row.dataset.body === item.id)));
}

form.addEventListener("submit", event => {
  event.preventDefault();
  byId("error").hidden = true;
  try {
    showSurvey(generateSurvey(Object.fromEntries(new FormData(form))));
  } catch (error) {
    byId("error").textContent = error.message;
    byId("error").hidden = false;
  }
});

const svg = (tag, attributes) => {
  const element = document.createElementNS("http://www.w3.org/2000/svg",tag);
  for (const [key,value] of Object.entries(attributes)) element.setAttribute(key,value);
  return element;
};

function drawMap() {
  map.setAttribute("viewBox",mapViewBox);
  map.classList.toggle("moving",moving);
  map.replaceChildren();
  const defs = svg("defs",{});
  map.append(defs);
  const pins = new Map();
  for (const [index,face] of chart.faces.entries()) {
    const id = "face-clip-" + index;
    const clip = svg("clipPath",{id});
    clip.append(svg("polygon",{points:face.points}));
    defs.append(clip);
    const group = svg("g",{"clip-path":"url(#" + id + ")","data-face":index + 1});
    for (const {cell,x,y,points} of face.tiles) {
      if (!pins.has(cell.key)) pins.set(cell.key,{x,y});
      const tile = svg("polygon",{points,fill:terrain[cell.terrain],stroke:"#223134","stroke-width":.45,"data-cell":cell.key});
      const title = svg("title",{});
      title.textContent = "Face " + (index + 1) + " · " + cell.terrain;
      tile.append(title);
      group.append(tile);
    }
    map.append(group,svg("polygon",{points:face.points,fill:"none",stroke:"#d4e5dd","stroke-width":.7,"pointer-events":"none"}));
  }
  chart.sites.forEach((site,index) => {
    const {x,y} = pins.get(site.key);
    const badge = svg("g",{"pointer-events":"none"});
    badge.append(svg("circle",{cx:x,cy:y,r:7,fill:index === selectedSite ? "#f5ce85" : "#102123",stroke:"#f1e8cd"}));
    const label = svg("text",{x,y:y+3.2,fill:index === selectedSite ? "#102123" : "#fff", "text-anchor":"middle","font-size":9});
    label.textContent = index + 1;
    badge.append(label);
    map.append(badge);
  });
}

function showSites() {
  const list = byId("sites");
  byId("site-panel").hidden = !chart.sites.length;
  byId("site-editor").hidden = selectedSite === undefined;
  list.replaceChildren();
  chart.sites.forEach((site,index) => {
    const li = document.createElement("li");
    const button = document.createElement("button");
    button.type = "button";
    button.className = "secondary";
    button.textContent = (index + 1) + " · " + site.type + " · " + site.name;
    button.setAttribute("aria-pressed",String(index === selectedSite));
    button.addEventListener("click", () => {
      selectedSite = index;
      moving = false;
      byId("map-message").textContent = "";
      showSites();
      drawMap();
      byId("site-name").focus();
    });
    li.append(button);
    list.append(li);
  });
  byId("site-name").value = chart.sites[selectedSite]?.name ?? "";
  byId("move").textContent = moving ? "Cancel move" : "Move on map";
}

function showSurface() {
  chart = surfaceFor(body,survey.seed);
  if (survey.surfaceEdits?.[body.id]) chart.sites = structuredClone(survey.surfaceEdits[body.id]);
  selectedSite = undefined;
  moving = false;
  byId("map-title").textContent = body.name + " · Surface map";
  byId("surface-panel").hidden = false;
  byId("map-message").textContent = "";
  drawMap();
  showSites();
  byId("map-title").focus();
}
byId("surface").addEventListener("click", showSurface);

function rememberSites() {
  survey.surfaceEdits ??= {};
  survey.surfaceEdits[body.id] = structuredClone(chart.sites);
  byId("save-status").textContent = "Surface updated. Save the survey to keep your edits.";
  showSites();
  drawMap();
}

byId("rename").addEventListener("click", () => {
  const name = byId("site-name").value.trim();
  if (!name) {
    byId("map-message").textContent = "Enter a location name.";
    return;
  }
  chart.sites[selectedSite].name = name;
  moving = false;
  rememberSites();
  byId("map-message").textContent = "Location renamed.";
});
byId("move").addEventListener("click", () => {
  moving = !moving;
  map.classList.toggle("moving",moving);
  byId("move").textContent = moving ? "Cancel move" : "Move on map";
  byId("map-message").textContent = moving ? "Choose a hex on the map." : "";
});
map.addEventListener("click", event => {
  const tile = event.target.closest("[data-cell]");
  if (!moving || !tile) return;
  chart.sites[selectedSite].key = tile.dataset.cell;
  moving = false;
  rememberSites();
  byId("map-message").textContent = "Location moved.";
});
for (const [label,color] of Object.entries(terrain)) {
  const item = document.createElement("li");
  const swatch = document.createElement("span");
  swatch.style.backgroundColor = color;
  item.append(swatch,label);
  byId("legend").append(item);
}

function showArchive() {
  const element = byId("saved");
  element.replaceChildren();
  if (!archive.length) list(element,["No saved surveys in this browser."]);
  for (const record of archive) {
    const item = document.createElement("li");
    const button = document.createElement("button");
    button.className = "secondary";
    button.textContent = record.name;
    button.addEventListener("click", () => {
      showSurvey(structuredClone(record));
      byId("save-status").textContent = "Opened saved survey.";
    });
    item.append(button);
    element.append(item);
  }
}
showArchive();
byId("dice").addEventListener("toggle", () => {
  if (survey && byId("dice").open && !byId("rolls").childElementCount) list(byId("rolls"),survey.log);
});
byId("save").addEventListener("click", () => {
  survey.id ??= crypto.randomUUID();
  const updated = [structuredClone(survey),...archive.filter(item => item.id !== survey.id)];
  try {
    localStorage.setItem(archiveKey,JSON.stringify(updated));
    archive = updated;
    showArchive();
    byId("save-status").textContent = "Survey saved in this browser.";
  } catch {
    byId("save-status").textContent = "Could not save in this browser. Download the JSON record to keep it.";
  }
});
byId("download").addEventListener("click", () => {
  const url = URL.createObjectURL(new Blob([JSON.stringify(survey,null,2)],{type:"application/json"}));
  const link = document.createElement("a");
  link.href = url;
  link.download = "scout-system-survey.json";
  link.click();
  setTimeout(() => URL.revokeObjectURL(url),1000);
});
byId("print").addEventListener("click", () => window.print());
byId("generate").disabled = false;
