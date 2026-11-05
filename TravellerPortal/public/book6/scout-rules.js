export const VERSION = "1.0";
export const attributes = ["STR", "DEX", "END", "INT", "EDU", "SOC"];
export const offices = ["Survey", "Communications", "Exploration", "Detached Duty",
  "Technical", "Operations", "Administration"];
const duties = ["Training", "Base duty", "Routine duty", "Mission", "Special mission", "Wartime mission", "Transfer"];
const fieldOffices = [0,0,0,0,1,1,1,1,2,2,2];
const adminOffices = [3,3,4,4,5,5,5,6,6,6,6];
const fieldDuties = [5,0,0,1,2,2,3,3,4,4,6];
const adminDuties = [5,0,1,0,2,2,1,3,3,3,4];
const initialSkills = ["Pilot","Pilot","Pilot","Administration","Computer","Leader","Administration"];
const ordinaryTitles = ["Recruit","Apprentice / Clerk","Journeyman","Skilled Worker",
  "Assistant Team Leader","Team Leader","Assistant Supervisor","Supervisor","Senior Supervisor"];
const administratorTitles = ["Administrator Trainee","Junior Administrator","Administrator",
  "Group Administrator","Senior Administrator","Scout Commander","Scout Leader","Senior Scout Leader","Sector Scout Leader"];
const equivalentRanks = [0,1,1,2,3,4,5,5,6,6];

// Per office, in duty order. Training uses a school in place of a skill throw.
const survival = [[3,3,3,4,5,6],[3,4,4,5,6,6],[3,3,4,5,7,6],
  [0,0,0,4,6,7],[0,0,0,3,5,6],[0,0,0,3,4,5],[0,0,0,3,3,5]];
const promotion = [[],[],[],[0,0,8,7,5,5],[10,9,9,8,7,6],[8,9,8,7,6,4],[0,7,7,7,6,5]];
const skillTarget = [[0,8,7,6,6,7],[0,0,7,7,6,6],[0,0,8,7,5,6],
  [0,0,7,6,6,3],[0,7,7,6,7,6],[0,8,8,6,5,4],[0,7,7,7,7,7]];
const officeSkills = [
  ["Jack-of-all-Trades","Pilot","Vacc Suit","Vacc Suit","Survey","Space","Survival","Navigation","Engineering","Survey"],
  ["Communications","Zero-G Combat","Pilot","Ship’s Boat","Vacc Suit","Communications","Space","Survival","Pilot","Navigation"],
  ["Air/Raft","Vehicle","Gun Combat","Vacc Suit","Recon","Space","Survival","Jack-of-all-Trades","Vehicle","Gun Combat"],
  ["Electronics","Administration","Communications","Computer","Computer","Gun Combat","Forgery","Brawling","Streetwise","Brawling"],
  ["+STR","Mechanical","Vehicle","Vacc Suit","Electronics","Gravitics","Engineering","+EDU","Computer","Medical"],
  ["Mechanical","Gun Combat","Vehicle","Engineering","Computer","Navigation","Pilot","Ship’s Boat","Gunnery","Ship Tactics"],
  ["Electronics","Administration","Communications","Computer","Computer","Broker","+INT","+EDU","Liaison","Administration"],
];
const life = ["+STR","Gambling","Gun Combat","Carousing","Gun Combat","Brawling","+EDU","+STR","+END","+DEX"];
const field = ["Jack-of-all-Trades","+EDU","Gun Combat","Carousing","Gun Combat","Gambling","+EDU","Jack-of-all-Trades","Streetwise","+INT"];
const bureaucracy = ["+INT","Gambling","Carousing","+EDU","Computer","Vehicle","+INT","+EDU","Streetwise","+INT"];
const administrator = ["Administration","Administration","Computer","Vacc Suit","Gun Combat","Space","Liaison","+EDU","Leader","+SOC"];
const missions = ["Hunting","Bribery","Equestrian","Forgery","Streetwise","Liaison","Survival","Gun Combat","Space","+SOC"];
const schoolAssignments = [
  ["Specialist","Specialist","Specialist","Field","Field","Field"],
  ["Specialist","Specialist","Ship","Ship","Field","Field"],
  ["Specialist","Intelligence","Contact","Contact","Field","Field"],
  ["Specialist","Specialist","Specialist","Intelligence","Intelligence","Administrator"],
  ["Specialist","Specialist","Technical","Technical","Technical","Administrator"],
  ["Specialist","Specialist","Ship","Ship","Field","Administrator"],
  ["Specialist","Specialist","Specialist","Ship","Field","Administrator"],
];
const schoolSkills = {
  Specialist: ["Medical","Mechanical","Computer","Administration","+STR","Gunnery"],
  Ship: ["Pilot","Navigation","Engineering","Gunnery","Space","Space"],
  Intelligence: ["Forgery","Streetwise","Brawling","Bribery","Gun Combat","Survival"],
  Technical: ["Computer","Electronics","Gravitics","Mechanical","Naval Architecture","+EDU"],
  Field: ["Vehicle","Air/Raft","Recon","Survival","Navigation","Survey"],
  Contact: ["Survey","Liaison","Streetwise","Survival","Pilot","Gun Combat"],
};
const material = ["Low passage","+INT","+EDU","Cutlass","Gun Combat","Type S scout/courier on loan",""];
const cash = [20000,20000,30000,30000,50000,50000,50000];
const codes = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ";
const profile = values => values.map(value => codes[value] ?? "[" + value + "]").join("");

export function* scoutCareer(options, die = () => Math.floor(Math.random() * 6) + 1) {
  const stats = options.upp ? [...options.upp].map(value => codes.indexOf(value.toUpperCase()))
    : attributes.map(() => die() + die());
  const record = {
    name: options.name || "Unnamed Scout", ruleset: "CT-SCOUTS-1983", schemaVersion: 1,
    initialUpp: profile(stats), stats, age: 18, years: 0, terms: 0, office: null, track: null,
    rank: null, status: "In progress", skills: {}, credits: 0, benefits: [], log: [],
  };
  let ordinary = 0, admin = 0, notes, attendedAdminSchool = false;
  const start = title => { notes = []; record.log.push({title, notes}); };
  const roll = (label, target = 0, dm = 0, count = 2) => {
    const dice = Array.from({length: count}, die);
    const total = dice.reduce((sum, face) => sum + face, 0) + dm;
    notes.push(label + " · " + dice.join(" + ") + (dm ? (dm > 0 ? " + " : " − ") + Math.abs(dm) : "") +
      " = " + total + (target ? " vs " + target + "+ · " + (total >= target ? "pass" : "fail") : ""));
    return total;
  };
  const award = (skill, amount = 1) => {
    if (skill.startsWith("+")) {
      stats[attributes.indexOf(skill.slice(1))] += amount;
      notes.push(skill.slice(1) + " +" + amount);
      return;
    }
    skill = {"Gun Combat": options.gun || "Auto Pistol", Space: options.space || "Navigation",
      Vehicle: options.vehicle || "Grav Vehicle"}[skill] ?? skill;
    record.skills[skill] = (record.skills[skill] || 0) + amount;
    notes.push(skill + " " + record.skills[skill]);
  };
  const learn = (label, table, bonus) => award(table[roll(label + " skill", 0,
    options.advanced ? 4 * Number(bonus) : 0, 1) - 1]);
  const chooseOffice = () => (record.track === "Field" ? fieldOffices : adminOffices)[roll("Office") - 2];
  const updateRank = () => {
    record.rank = admin ? "O" + admin + " · IS-" + (admin + 9) + " · " + administratorTitles[admin - 1]
      : ordinary ? "E" + ordinary + " · IS-" + ordinary + " · " + ordinaryTitles[ordinary - 1] : "Unranked";
  };
  const finish = status => {
    record.status = status;
    record.upp = profile(stats);
    return record;
  };
  const snapshot = () => { record.upp = profile(stats); return record; };
  start("Entry record");
  let college = false, honors = false;
  if (options.college && roll("College admission", 9, 2 * Number(stats[4] >= 9)) >= 9) {
    college = roll("College graduation", 7, 2 * Number(stats[3] >= 8)) >= 7;
    record.age += college ? 4 : 1;
    if (college) {
      award("+EDU", Math.max(1, roll("Education increase", 0, -2 + Number(stats[3] >= 9), 1)));
      honors = roll("College honors", 10, Number(stats[4] >= 10)) >= 10;
    }
  }
  if (!college && roll("Scout enlistment", 7, Number(stats[3] >= 6) + 2 * Number(stats[0] >= 8)) < 7) {
    if (!options.draft || roll("Scout draft · requires 4 exactly", 0, 0, 1) !== 4) return finish("Enlistment unsuccessful");
    notes.push("Drafted into the Scouts");
  }
  record.track = college ? "Bureaucracy" : "Field";
  if (college) {
    notes.push("Automatic entry as a college graduate");
    if (honors) admin = 1;
    else ordinary = 1;
  }
  let office = honors && adminOffices.includes(options.honorsOffice) ? options.honorsOffice : chooseOffice();
  record.office = offices[office];
  updateRank();
  notes.push(record.track + " · " + record.office + " · " + record.rank);
  yield {kind: "year", record: snapshot()};

  const officeBonus = term => office <= 2 ? term >= 3 : office === 4 ? stats[4] >= 9 : admin >= 1;
  const learnOffice = term => learn(offices[office], officeSkills[office], officeBonus(term));
  let finalStatus = "Mustered out";
  let injured = false;
  while (record.terms < 20) {
    const term = record.terms + 1;
    const length = 4 - (record.age - 18) % 4;
    let adminPromoted = false;
    for (let year = 1; year <= length; year++) {
      const heading = "Term " + term + " · Year " + year + " · Age " + record.age;
      start(heading);
      if (!record.years) {
        record.log.at(-1).title += " / Initial training";
        award(initialSkills[office]);
      } else {
        const assignment = () => {
          const dm = admin && options.adminDutyBonus ? 2 : 0;
          const result = roll("Duty assignment", 0, dm);
          if (result - dm === 2) return 5;
          return record.track === "Field" ? fieldDuties[result - 2] : adminDuties[Math.min(12, result) - 2];
        };
        let duty = assignment();
        if (duty === 6 && options.declineTransfer) {
          notes.push("Declined first transfer roll");
          duty = assignment();
        }
        if (duty === 6) {
          record.track = "Bureaucracy";
          ordinary = Math.min(9, term);
          updateRank();
          office = chooseOffice();
          record.office = offices[office];
          notes.push("Transferred to " + record.office + " · " + record.rank);
          duty = assignment();
        }
        record.log.at(-1).title += " / " + duties[duty];
        notes.push(record.office + " · " + duties[duty]);
        const target = survival[office][duty];
        const pilot = record.skills.Pilot || 0;
        const survivalDm = [Number(stats[2] >= 9),Number(stats[2] >= 9) + Number(pilot >= 3),
          Number(stats[2] >= 9),Number(admin >= 1),0,Number(pilot >= 2),Number(stats[3] >= 9)][office];
        if (target && roll("Survival", target, survivalDm) < target) {
          if (!options.injury) return finish("Died in service");
          injured = true;
          finalStatus = "Discharged after failed survival";
          notes.push("Optional survival rule · current term earns no benefit roll");
          break;
        }
        if (!target) notes.push("Automatic survival");
        const promote = promotion[office][duty];
        const promotionDm = office === 4 ? Number(stats[4] >= 10) : office === 5 ? Number(stats[3] >= 9) : 0;
        if (promote && (admin ? !adminPromoted && admin < 9 : ordinary < 9) &&
            roll("Promotion", promote, promotionDm) >= promote) {
          if (admin) { admin++; adminPromoted = true; }
          else ordinary++;
          updateRank();
          notes.push("Promoted · " + record.rank);
          if (admin) learn("Administrator rank", administrator, admin >= 4);
          else if (options.skillTable === "life") learn("Scout life", life, stats[0] >= 9);
          else learnOffice(term);
        }
        if (duty === 0) {
          const schools = schoolAssignments[office];
          let school = schools[roll("School assignment", 0, 0, 1) - 1];
          for (let attempt = 0; attendedAdminSchool && school === "Administrator" && attempt < 20; attempt++) {
            school = schools[roll("Repeat administrator school · reroll", 0, 0, 1) - 1];
          }
          if (attendedAdminSchool && school === "Administrator") school = schools[0];
          notes.push(school + " school");
          if (school === "Administrator") {
            attendedAdminSchool = true;
            if (!admin) { admin = 1; updateRank(); notes.push(record.rank); }
            award("Administration");
          } else {
            const count = ["Ship","Contact","Intelligence"].includes(school) ? 2 : 1;
            for (let index = 0; index < count; index++) learn(school, schoolSkills[school], false);
          }
        } else {
          const targetSkill = skillTarget[office][duty];
          const skillDm = office === 0 ? Number(term >= 3) : office === 2 ? Number(stats[3] >= 9) : 0;
          if (targetSkill && roll("Skill eligibility", targetSkill, skillDm) >= targetSkill) {
            if (options.skillTable === "life") learn("Scout life", life, stats[0] >= 9);
            else if (options.skillTable === "track" && office <= 2) learn("Field service", field, term >= 3);
            else if (options.skillTable === "track") learn("Bureaucracy", bureaucracy, admin >= 1);
            else if (options.skillTable === "rank" && admin) learn("Administrator rank", administrator, admin >= 4);
            else learnOffice(term);
          }
          if (duty >= 4) learn("Special or wartime mission", missions, admin >= 1);
        }
      }
      record.years++;
      record.age++;
      yield {kind: "year", record: snapshot()};
    }
    if (injured) break;
    record.terms++;
    start("End of term " + term);
    notes.push("Term completed at age " + record.age);
    if (record.age >= 34) {
      for (let index = 0; index < (record.age >= 66 ? 4 : 3); index++) {
        const target = record.age >= 66 ? 9 : (record.age >= 50 ? 9 : 8) - Number(index === 1);
        if (roll("Aging · " + attributes[index], target) >= target) continue;
        const loss = record.age >= 66 && index < 3 ? 2 : 1;
        stats[index] = Math.max(0, stats[index] - loss);
        notes.push(attributes[index] + " −" + loss);
        if (!stats[index]) {
          if (roll("Aging crisis", 8) < 8) return finish("Died after an aging crisis");
          stats[index] = 1;
          notes.push(attributes[index] + " restored to 1");
        }
      }
    }
    if (record.track === "Bureaucracy" && !admin && ordinary < term) {
      notes.push("Released under the Bureaucracy’s rank requirement");
      break;
    }
    const reenlist = roll("Re-enlistment", 3);
    if (reenlist === 12) notes.push("Re-enlistment required");
    else if (term >= 7 || reenlist < 3) {
      notes.push(term >= 7 ? "Service limit reached" : "Re-enlistment denied");
      break;
    } else {
      const stay = yield {kind: "term", record: snapshot(), defaultStay: term < options.terms};
      if (!stay) {
        notes.push("Elected to muster out");
        break;
      }
    }
    if (record.terms === 20) return finish("Preview stopped during mandatory service");
    if (options.reassign) {
      office = chooseOffice();
      record.office = offices[office];
      notes.push("Next office · " + record.office);
    }
  }

  start("Mustering out · Book 1");
  const rank = equivalentRanks[admin];
  const benefitCount = record.terms + (rank >= 5 ? 3 : rank >= 3 ? 2 : rank ? 1 : 0);
  notes.push(benefitCount + " benefit rolls");
  for (let index = 0; index < benefitCount; index++) {
    if (index < options.cashRolls) {
      const amount = cash[roll("Cash benefit", 0, Number(Boolean(record.skills.Gambling)), 1) - 1];
      record.credits += amount;
      notes.push("Cr " + amount.toLocaleString("en-US"));
    } else {
      let item = material[roll("Material benefit", 0, Number(rank >= 5), 1) - 1];
      if (item.startsWith("+")) award(item, 2);
      else {
        if (item === "Gun Combat") item = options.gun || "Auto Pistol";
        if (!item) notes.push("No material benefit");
        else if (item.startsWith("Type S") && record.benefits.includes(item)) notes.push("Scout ship loan already granted");
        else if (item !== "Low passage" && record.benefits.includes(item)) award(item);
        else { record.benefits.push(item); notes.push(item); }
      }
    }
  }
  return finish(finalStatus);
}
