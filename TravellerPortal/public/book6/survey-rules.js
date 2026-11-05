export const VERSION = "1.0";
const digits = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ";
const starTypes = ["B","B","A","M","M","M","M","M","K","G","F","F","F"];
const starClasses = ["Ia","Ib","II","III","IV","V","V","V","V","V","V","VI","D"];
const companionTypes = ["B","B","A","F","F","G","G","K","K","M","M","M","M"];
const companionClasses = ["Ia","Ib","II","III","IV","D","D","V","V","VI","D","D","D"];
const orbits = [.2,.4,.7,1,1.6,2.8,5.2,10,19.6,38.8,77.2,154,307.6,614.8,1229.2,2458];
const starports = ["A","A","A","B","B","C","C","D","E","E","X"];
const atmospheres = ["Vacuum","Trace","Very thin, tainted","Very thin","Thin, tainted","Thin",
  "Standard","Standard, tainted","Dense","Dense, tainted","Exotic","Corrosive","Insidious",
  "Dense at low altitude","Ellipsoidal pressure bands","Thin at low altitude"];
const gasQuantity = [0,1,1,1,2,2,3,3,4,4,4,5,5];
const beltQuantity = [3,2,2,2,2,2,2,1,1,1,1,1,1];
const clamp = (number, min, max) => Math.min(max, Math.max(min, number));
const ehex = value => digits[value] ?? "0";
const distance = orbit => {
  const lower = Math.floor(orbit);
  const at = n => orbits[n] ?? orbits.at(-1) * 2 ** (n - 15);
  return at(lower) + (at(lower + 1) - at(lower)) * (orbit - lower);
};

export function randomStream(seed) {
  let state = 2166136261;
  for (const letter of seed) state = Math.imul(state ^ letter.codePointAt(0), 16777619);
  return () => {
    state = (state + 0x6D2B79F5) | 0;
    let value = Math.imul(state ^ state >>> 15, state | 1);
    value ^= value + Math.imul(value ^ value >>> 7, value | 61);
    return ((value ^ value >>> 14) >>> 0) / 4294967296;
  };
}

export function parseProfile(value) {
  const upp = value.trim().toUpperCase();
  if (!/^[A-EX][0-9AS][0-9A-F][0-9A][0-9A][0-9A-D][0-9A-HJ-NP-Z]-[0-9A-HJ-NP-Z]$/.test(upp)) {
    throw new Error("Enter a mainworld profile such as A788899-C.");
  }
  const [size, atmosphere, water, population, government, law, tech] =
    [1,2,3,4,5,6,8].map(index => digits.indexOf(upp[index]));
  return {upp, port: upp[0], size: upp[1] === "S" ? 0 : size, sizeCode: upp[1],
    atmosphere, water, population, government, law, tech};
}

function profile(body) {
  return body.port + body.sizeCode + [body.atmosphere, body.water, body.population, body.government, body.law]
    .map(ehex).join("") + "-" + ehex(body.tech);
}

function makeStar(name, type, size, random) {
  const subtype = Math.floor(random() * 10);
  if ((size === "IV" && (type === "M" || (type === "K" && subtype >= 5))) ||
      (size === "VI" && (["B","A"].includes(type) || (type === "F" && subtype < 5)))) size = "V";
  // Use representative luminosities to estimate zones for this preview.
  const base = {B:400,A:35,F:3,G:1,K:.38,M:.04}[type];
  const luminosity = size === "D" ? .005 :
    base * (1.45 - subtype * .09) * ({Ia:10000,Ib:3000,II:300,III:30,IV:3,V:1,VI:.35}[size]);
  const habitable = Math.sqrt(luminosity);
  let hz = 0;
  while (hz < 19 && Math.abs(Math.log(distance(hz + 1) / habitable)) <
    Math.abs(Math.log(distance(hz) / habitable))) hz++;
  return {name, type, size, spectrum: type + subtype + " " + size, luminosity, habitable: hz, bodies: []};
}

export function generateSurvey({name, seed, uwp = ""}) {
  name = name.trim() || "Uncharted system";
  seed = seed.trim() || globalThis.crypto.randomUUID();
  const anchor = uwp.trim() ? parseProfile(uwp) : null;
  const random = randomStream(seed);
  const die = () => 1 + Math.floor(random() * 6);
  const log = [];
  const roll = (label, count = 2, dm = 0) => {
    const dice = Array.from({length: count}, die);
    const total = dice.reduce((sum, value) => sum + value, dm);
    log.push(label + " · " + dice.join(" + ") + (dm ? " (DM " + (dm > 0 ? "+" : "") + dm + ")" : "") + " → " + total);
    return total;
  };
  const select = choices => choices[Math.floor(random() * choices.length)];
  const nature = roll("Stellar multiplicity");
  const dm = anchor && ((anchor.atmosphere >= 4 && anchor.atmosphere <= 9) || anchor.population >= 8) ? 4 : 0;
  const primaryType = roll("Primary spectral type", 2, dm);
  const primaryClass = roll("Primary luminosity class", 2, dm);
  const primary = makeStar(name + " A", starTypes[Math.min(primaryType, 12)], starClasses[Math.min(primaryClass, 12)], random);
  const stars = [primary];
  const companions = nature === 12 ? 2 : nature >= 8 ? 1 : 0;
  for (let index = 0; index < companions; index++) {
    const star = makeStar(name + " " + "BC"[index],
      companionTypes[Math.min(12, roll("Companion spectral type", 2, primaryType))],
      companionClasses[Math.min(12, roll("Companion luminosity class", 2, primaryClass))], random);
    const orbit = roll("Companion orbit", 2, index * 4);
    star.orbit = orbit <= 3 ? "Close" : orbit >= 12 ? "Far" :
      orbit - 3 + (orbit >= 7 ? roll("Companion orbit extension", 1) : 0);
    if (star.orbit === "Far") star.distance = roll("Wide separation in 1,000 AU", 1) * 1000;
    stars.push(star);
  }

  const zoneFor = (star, orbit) => orbit < star.habitable ? "Inner" : orbit >= star.habitable + 1 ? "Outer" : "Habitable";
  const register = (star, orbit, kind, extra = {}) => {
    const id = stars.indexOf(star) + ":" + orbit;
    const body = {id, name: star.name + " / " + orbit, orbit, zone: zoneFor(star, orbit),
      distance: distance(orbit), kind, moons: [], facilities: [], ...extra};
    star.bodies.push(body);
    return body;
  };
  const worlds = [];
  function physical(body, size, moon = false, zoneIndex = body.orbit) {
    const inner = body.zone === "Inner", outer = body.zone === "Outer";
    const label = body.name;
    let atmosphere = size < (moon ? 2 : 1) ? 0 :
      clamp(roll(label + " atmosphere", 2, size - 7 - (outer || (inner && moon) ? 4 : inner ? 2 : 0)), 0, 15);
    if (size && zoneIndex >= body.habitable + 2 && roll(label + " exotic atmosphere") === 12) atmosphere = 10;
    const water = inner || size < (moon ? 1 : 2) ? 0 :
      clamp(roll(label + " hydrographics", 2, size - 7 - (outer ? (moon ? 4 : 2) : 0) -
        (atmosphere <= 1 || atmosphere >= 10 ? 4 : 0)), 0, 10);
    let population = Math.max(0, roll(label + " population", 2,
      -2 - (inner ? 5 : outer ? (moon ? 4 : 3) : 0) -
      (moon && size <= 4 ? 2 : 0) - ([5,6,8].includes(atmosphere) || (!moon && atmosphere === 0) ? 0 : 2)));
    if (anchor && (moon ? population >= anchor.population : population > anchor.population)) {
      population = Math.max(0, anchor.population - 1);
    }
    Object.assign(body, {size, sizeCode: size ? ehex(size) : "S", atmosphere, water, population});
    worlds.push(body);
  }

  function addMoon(star, parent, anchored = false, imported = null) {
    const sizeRoll = anchored ? imported.size :
      parent.kind === "Gas giant" ? roll(parent.name + " satellite size", 2, parent.large ? -4 : -6) :
      parent.size - roll(parent.name + " satellite size subtraction", 1);
    const ring = !anchored && sizeRoll === 0;
    const used = new Set(parent.moons.map(moon => moon.orbit));
    let orbit;
    for (let attempt = 0; attempt < 36; attempt++) {
      if (ring) orbit = [1,1,1,2,2,3][die() - 1];
      else {
        const band = roll(parent.name + " satellite orbit band", 2, -parent.moons.length);
        const face = roll(parent.name + " satellite radius");
        orbit = band <= 7 ? face + 1 : band >= 12 && parent.kind === "Gas giant" ?
          face * 25 + 25 : face * 5 + 5;
      }
      if (!used.has(orbit)) break;
      if (ring && used.has(1) && used.has(2) && used.has(3)) return;
    }
    if (used.has(orbit)) return;
    const body = {id: parent.id + ":" + orbit, name: parent.name + " · " + orbit, orbit,
      zone: parent.zone, distance: parent.distance, habitable: star.habitable,
      kind: ring ? "Ring" : "Moon", moons: [], facilities: []};
    parent.moons.push(body);
    if (anchored) {
      Object.assign(body, imported, {name, main: true});
      worlds.push(body);
    } else if (!ring) physical(body, Math.max(0, sizeRoll), true, parent.orbit);
  }

  stars.forEach((star, starIndex) => {
    const nearby = starIndex ? [] : stars.slice(1).filter(companion => typeof companion.orbit === "number");
    const largest = roll(star.name + " maximum orbit", 2,
      (["Ia","Ib","II"].includes(star.size) ? 8 : star.size === "III" ? 4 : 0) -
      (star.type === "M" ? 4 : star.type === "K" ? 2 : 0));
    const upper = starIndex && typeof star.orbit === "number" ? Math.min(largest, Math.floor(star.orbit / 2)) : largest;
    const slots = Array.from({length: Math.max(0, upper + 1)}, (_, index) => index).filter(orbit =>
      nearby.every(companion => orbit <= Math.floor(companion.orbit / 2) || orbit >= companion.orbit + 2));
    if (starIndex && star.orbit === "Close") return;
    const primaryAnchor = anchor && !starIndex;
    const anchorOrbit = primaryAnchor ? (slots.includes(star.habitable) || !slots.length ? star.habitable :
      slots.reduce((best, orbit) => Math.abs(orbit - star.habitable) < Math.abs(best - star.habitable) ? orbit : best)) : null;
    if (primaryAnchor && !slots.includes(anchorOrbit)) slots.push(anchorOrbit);
    const hotStar = Number(["A","B"].includes(star.type));
    const empty = roll(star.name + " empty orbit check", 1, hotStar) >= 5 ?
      [1,1,2,3,3,3][Math.min(6, roll("Empty orbit count", 1, hotStar)) - 1] : 0;
    const remaining = [...slots];
    const remove = orbit => remaining.splice(remaining.indexOf(orbit), 1);
    for (let index = 0; index < empty; index++) {
      const orbit = select(remaining.filter(slot => slot !== anchorOrbit));
      if (orbit === undefined) break;
      remove(orbit);
      register(star, orbit, "Empty orbit");
    }
    const giantSlots = remaining.filter(orbit => orbit >= star.habitable && !(orbit === anchorOrbit && anchor.sizeCode === "0"));
    const giantCount = roll(star.name + " gas giant presence") <= 9 ?
      Math.min(gasQuantity[roll("Gas giant count")], Math.max(1, giantSlots.length)) : 0;
    for (let index = 0; index < giantCount; index++) {
      const candidates = remaining.filter(orbit => orbit >= star.habitable && !(orbit === anchorOrbit && anchor.sizeCode === "0"));
      let orbit = select(candidates);
      if (orbit === undefined) {
        orbit = Math.max(star.habitable, ...slots) + 1;
        while (star.bodies.some(body => body.orbit === orbit) || nearby.some(companion =>
          orbit > Math.floor(companion.orbit / 2) && orbit < companion.orbit + 2)) orbit++;
        slots.push(orbit);
      } else remove(orbit);
      const giant = register(star, orbit, "Gas giant", {large: roll("Gas giant size", 1) <= 3});
      if (orbit === anchorOrbit) addMoon(star, giant, true, anchor);
    }
    const beltCount = roll(star.name + " belt presence", 2, -giantCount) <= 6 ?
      beltQuantity[Math.max(0, roll("Belt count", 2, -giantCount))] : 0;
    for (let index = 0; index < beltCount; index++) {
      const candidates = remaining.filter(orbit => orbit !== anchorOrbit);
      const preferred = candidates.filter(orbit => star.bodies.some(body => body.kind === "Gas giant" && body.orbit === orbit + 1));
      const orbit = select(preferred.length ? preferred : candidates);
      if (orbit === undefined) break;
      remove(orbit);
      const belt = register(star, orbit, "Planetoid belt", {size:0, sizeCode:"0", atmosphere:0, water:0,
        population:Math.max(0, roll("Belt population", 2, -2 - (zoneFor(star,orbit) === "Inner" ? 5 :
          zoneFor(star,orbit) === "Outer" ? 3 : 0)))});
      if (anchor && belt.population > anchor.population) belt.population = Math.max(0, anchor.population - 1);
      worlds.push(belt);
    }
    for (const orbit of remaining) {
      if (orbit === anchorOrbit) {
        const main = register(star, orbit, anchor.sizeCode === "0" ? "Planetoid belt" : "Planet",
          {...anchor, name, main:true});
        worlds.push(main);
      } else {
        const body = register(star, orbit, "Planet", {habitable: star.habitable});
        const size = Math.max(0, roll(body.name + " size", 2, -2 - ([5,4,2][orbit] || 0) - (star.type === "M" ? 2 : 0)));
        physical(body, size);
      }
    }
    const captured = roll(star.name + " captured planet check", 1, hotStar) >= 5 ?
      Math.ceil(Math.min(6, roll("Captured planet count", 1, hotStar)) / 2) : 0;
    for (let index = 0; index < captured; index++) {
      const base = roll("Captured planet orbit");
      const offset = roll("Captured planet deviation", 2, -7);
      const orbit = base + offset / 10;
      if (offset && !Number.isInteger(orbit) && !star.bodies.some(body => body.orbit === orbit) &&
          nearby.every(companion => orbit <= Math.floor(companion.orbit / 2) || orbit >= companion.orbit + 2)) {
        const body = register(star, orbit, "Captured planet", {habitable:star.habitable});
        physical(body, Math.max(0, roll(body.name + " size", 2, -2 - (star.type === "M" ? 2 : 0))));
      }
    }
    for (const body of star.bodies) {
      const count = body.kind === "Gas giant" ? roll(body.name + " satellite count", 2, body.large ? 0 : -4) :
        (body.size > 0 && body.kind !== "Planetoid belt" ? roll(body.name + " satellite count", 1, -3) : 0);
      for (let index = body.moons.length; index < count; index++) addMoon(star, body);
      body.moons.sort((a,b) => a.orbit - b.orbit);
    }
    star.bodies.sort((a,b) => a.orbit - b.orbit);
  });
  if (!worlds.length) {
    let orbit = primary.habitable;
    while (primary.bodies.some(body => body.orbit === orbit)) orbit++;
    log.push("No eligible worlds rolled · reserved a survey orbit");
    physical(register(primary, orbit, "Planet", {habitable:primary.habitable}), Math.max(1, roll("Fallback world size", 2, -2)));
    primary.bodies.sort((a,b) => a.orbit - b.orbit);
  }
  const main = worlds.find(body => body.main) ?? [...worlds].sort((a,b) =>
    b.population - a.population || Number(b.zone === "Habitable") - Number(a.zone === "Habitable") ||
    a.distance - b.distance)[0];
  main.main = true;
  main.name = name;
  if (!anchor) {
    main.government = clamp(roll("Mainworld government", 2, main.population - 7), 0, 13);
    main.law = clamp(roll("Mainworld law", 2, main.government - 7), 0, 18);
    main.port = starports[roll("Mainworld starport") - 2];
    main.tech = Math.max(0, roll("Mainworld technology", 1, ({A:6,B:4,C:2,X:-4}[main.port] || 0) +
      (main.size < 2 ? 2 : main.size < 5 ? 1 : 0) +
      (main.atmosphere < 4 || main.atmosphere > 9 ? 1 : 0) +
      (main.water === 10 ? 2 : main.water === 9 ? 1 : 0) +
      (main.population === 10 ? 4 : main.population === 9 ? 2 : main.population >= 1 && main.population <= 5 ? 1 : 0) +
      ([0,5].includes(main.government) ? 1 : main.government === 13 ? -2 : 0)));
    main.upp = profile(main);
  }
  if (["A","B"].includes(main.port) && roll("Naval base") >= 8) main.facilities.push("Naval base");
  if (["A","B","C","D"].includes(main.port) &&
      roll("Scout base", 2, -({A:3,B:2,C:1,D:0}[main.port])) >= 7) main.facilities.push("Scout base");
  const industrial = main.population >= 9 && [0,1,2,4,7,9].includes(main.atmosphere);
  const poor = main.atmosphere >= 2 && main.atmosphere <= 5 && main.water <= 3;
  for (const body of worlds) {
    if (!body.main) {
      const {population, atmosphere, water} = body;
      body.government = !population ? 0 : [0,1,2,3,6][Math.min(5,
        roll(body.name + " government", 1, main.government === 6 ? population : Number(main.government >= 7))) - 1];
      body.law = body.government ? Math.max(0, roll(body.name + " law", 1, main.law - 3)) : 0;
      if (body.zone === "Habitable" && atmosphere >= 4 && atmosphere <= 9 && water >= 4 && water <= 8 && population >= 2) body.facilities.push("Farming");
      if (industrial && population >= 2) body.facilities.push("Mining");
      if (body.government === 6 && population >= 5) body.facilities.push("Colony");
      if (main.population && main.tech >= 9 && roll(body.name + " research laboratory", 2, main.tech >= 10 ? 2 : 0) >= 11) body.facilities.push("Research laboratory");
      if (population && !poor && roll(body.name + " military base", 2,
        Number(main.population >= 8) + 2 * Number(atmosphere === main.atmosphere) +
        Number(main.facilities.length > 0)) >= 12) body.facilities.push("Military base");
      body.tech = Math.max(0, main.tech - Number(!body.facilities.some(item => ["Military base","Research laboratory"].includes(item))));
      if (![5,6,8].includes(atmosphere)) body.tech = Math.max(7, body.tech);
      const port = roll(body.name + " spaceport", 1, population >= 6 ? 2 : population <= 1 ? population - 3 : 0);
      body.port = port >= 6 ? "F" : port >= 4 ? "G" : port === 3 ? "H" : "Y";
      body.upp = profile(body);
    }
    body.atmosphereName = atmospheres[body.atmosphere];
  }
  return {schemaVersion:1, version:VERSION, ruleset:"CT-SCOUTS-1983-SURVEY", name, seed, anchor:anchor?.upp ?? null,
    mainId:main.id, stars, log};
}
