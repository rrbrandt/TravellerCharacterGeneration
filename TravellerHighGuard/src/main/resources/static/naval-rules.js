const branches = ["Line", "Flight", "Gunnery", "Engineering", "Medical", "Technical Services"];
const navies = ["Planetary", "Subsector", "Imperial"];
const attributes = ["STR", "DEX", "END", "INT", "EDU", "SOC"];
const duties = ["Training", "Shore duty", "Patrol", "Siege", "Strike", "Battle", "Special duty"];
const assignments = [5, 1, 3, 4, 2, 0, 2, 4, 1, 6, 6, 6];
const officerTitles = ["Ensign", "Sublieutenant", "Lieutenant", "Lieutenant Commander",
  "Commander", "Captain", "Commodore", "Fleet Admiral", "Sector Admiral", "Grand Admiral"];
const ratingTitles = ["Spacehand Recruit", "Spacehand Apprentice", "Able Spacehand",
  "Petty Officer Third Class", "Petty Officer Second Class", "Petty Officer First Class",
  "Chief Petty Officer", "Senior Chief Petty Officer", "Master Chief Petty Officer"];
const bookOneRanks = [0, 1, 1, 2, 3, 4, 5, 5, 6, 6, 6];

// Each branch lists survival, decoration, promotion and skill targets in duty order.
// Zero denotes automatic survival or an unavailable award. Negative targets are enlisted only.
const resolution = [
  [[0,4,4,5,6,6], [0,12,11,10,7,6], [-6,-7,7,8,7,6], [7,7,6,6,5,5]],
  [[3,3,3,3,3,4], [0,0,10,9,9,8], [0,11,11,10,9,9], [7,0,7,7,6,6]],
  [[0,3,4,5,5,6], [0,12,11,10,9,7], [-6,-6,8,8,7,6], [8,0,7,5,6,6]],
  [[0,0,3,4,5,5], [0,0,12,11,7,7], [-7,-7,5,8,6,6], [7,8,6,7,6,5]],
  [[0,0,3,3,3,4], [0,0,0,0,11,10], [-7,6,7,8,6,6], [8,6,7,7,7,6]],
  [[0,3,3,3,3,3], [0,0,0,0,9,8], [-7,8,9,8,7,7], [7,8,9,7,7,7]],
];
const branchSkills = [
  ["Mechanical", "Electronics", "Gun Combat", "Navigation", "Computer", "Liaison", "Zero-G Combat", "Vacc Suit"],
  ["Vacc Suit", "Administration", "Gun Combat", "Communications", "Ship's Boat", "Navigation", "Pilot", "Pilot"],
  ["Forward Observer", "Gun Combat", "Communications", "Computer", "Gunnery", "Gunnery", "Gunnery", "Gunnery"],
  ["Mechanical", "Electronics", "Engineering", "Mechanical", "Vacc Suit", "Engineering", "Engineering", "Engineering"],
  ["Administration", "Jack-of-all-Trades", "Electronics", "Administration", "Medical", "Computer", "Medical", "Medical"],
  ["Mechanical", "Mechanical", "Electronics", "Electronics", "Computer", "Computer", "Gravitics", "Jack-of-all-Trades"],
];
const serviceSkills = {
  "Navy life": ["Brawling","+STR","Carousing","Gambling","+END","+DEX","+END","+EDU","Carousing","Vacc Suit"],
  Shipboard: ["Gambling","+DEX","Blade Combat","Mechanical","Ship's Boat","Vacc Suit","Zero-G Combat","Communications","Administration","Jack-of-all-Trades"],
  "Shore duty": ["Carousing","Vehicle","Forward Observer","Vacc Suit","Liaison","Vehicle","Forward Observer","Survival","Vacc Suit","Battle Dress"],
  "Petty officer": ["Vacc Suit","Blade Combat","Gun Combat","Mechanical","Medical","Leader","Zero-G Combat","+EDU","Instruction","Administration"],
  Command: ["Vehicle","+END","Gun Combat","Ship's Boat","Pilot","Ship Tactics","Leader","+SOC","Leader","Ship Tactics"],
  Staff: ["Computer","Electronics","Gun Combat","Administration","Bribery","Ship Tactics","Fleet Tactics","+INT","Ship Tactics","Fleet Tactics"],
};
const gunnery = ["Lasers", "Missiles", "Particle Accelerators", "Energy Weapons", "Meson Weapons", "Screens"];
const cash = [1000,5000,5000,10000,20000,50000,50000];
const material = ["Low passage","+INT","+EDU","Blade","Travellers’ Aid Society membership","High passage","+SOC"];
const branchRoll = [[5,0,0,3,3,2,2,4], [5,0,0,3,2,0,1,4]];

export function generateCareer(options, die = () => 1 + Math.floor(Math.random() * 6)) {
  const log = [];
  const skills = {};
  const medals = {};
  const benefits = [];
  const stats = options.upp ? [...options.upp].map(value => parseInt(value, 16))
    : attributes.map(() => die() + die());
  const initialUpp = stats.map(value => value.toString(16).toUpperCase()).join("");
  let age = 18, navy = options.navy, branch, officer = 0, rating = 1;
  let terms = 0, years = 0, credits = 0, pension = 0, college = false;
  let forcedBranch, drafted = false, honors = false;
  let status = "Mustered out";
  let notes = [];
  const section = title => { notes = []; log.push({title, notes}); };
  const roll = (label, target, dm = 0, count = 2) => {
    const dice = Array.from({length: count}, die);
    const total = dice.reduce((sum, value) => sum + value, dm);
    const modifier = dm ? (dm > 0 ? " + " : " − ") + Math.abs(dm) : "";
    notes.push(label + " · " + dice.join(" + ") + modifier + " = " + total +
      (target ? " vs " + target + "+ · " + (total >= target ? "pass" : "fail") : ""));
    return total;
  };
  const check = (label, target, dm = 0, count = 2) => roll(label, target, dm, count) >= target;
  const change = (index, amount) => {
    stats[index] += amount;
    notes.push(attributes[index] + (amount > 0 ? " +" : " ") + amount);
  };
  const gain = (skill, levels = 1) => {
    if (skill.startsWith("+")) {
      change(attributes.indexOf(skill.slice(1)), levels);
      return;
    }
    if (skill === "Gunnery") skill += " (" + (options.gunnery || "Lasers") + ")";
    if (skill === "Gun Combat") skill = "Handgun";
    if (skill === "Blade Combat") skill = "Cutlass";
    if (skill === "Vehicle") skill = "Vehicle (" + (navy === 2 || options.tech[navy] >= 8 ? "Grav" : "Wheeled") + ")";
    skills[skill] = (skills[skill] || 0) + levels;
    notes.push(skill + " " + skills[skill]);
  };
  const medal = name => {
    medals[name] = (medals[name] || 0) + 1;
    notes.push("Awarded " + name);
  };
  const rank = () => (officer ? "O" + officer + " · " + officerTitles[officer - 1]
    : "E" + rating + " · " + ratingTitles[rating - 1]);
  const allowedBranches = () => [0, 1, 2, 3, 4, ...(navy === 2 ? [5] : [])];
  const chooseBranch = () => {
    const choice = options.branch;
    if (stats[5] >= 9 && allowedBranches().includes(choice) && (officer || choice !== 1)) return choice;
    const dm = 2 * (Number(stats[4] >= 9) + Number(stats[3] >= 10)) - 2 * Number(navy === 2);
    const index = Math.max(0, Math.min(7, roll("Branch", 0, dm, 1)));
    const selected = branchRoll[Number(Boolean(officer))][index];
    return allowedBranches().includes(selected) ? selected : 0;
  };
  const learnBranch = (selected = branch) => gain(branchSkills[selected][roll(branches[selected] + " skill", 0, navy, 1) - 1]);
  const learnService = table => {
    let dm = 0;
    if (table === "Navy life" || table === "Shipboard") dm = officer ? 4 : 0;
    if (table === "Shore duty") dm = officer >= 4 ? 1 : 0;
    if (table === "Petty officer") dm = rating >= 7 ? 4 : rating >= 5 ? 2 : 0;
    if (table === "Staff" || table === "Command") dm = officer >= 7 ? 4 : officer >= 4 ? 2 : 0;
    gain(serviceSkills[table][roll(table + " skill", 0, dm, 1) - 1]);
  };
  const school = (list, target, cap = list.length, instructor = true) => {
    if (instructor && list.some(skill => skills[skill] >= 3)) {
      gain("Instruction");
      return;
    }
    let earned = 0;
    for (const skill of list) {
      if (check(skill, target, 0, 1) && earned < cap) {
        gain(skill);
        earned++;
      }
    }
  };

  section("Entry record");
  if (options.entry === "academy" && stats[5] < 8) notes.push("Naval Academy requires SOC 8+. Proceeded to enlistment.");
  if (options.entry === "academy" && stats[5] >= 8 &&
      check("Naval Academy admission", 10, 2 * Number(stats[5] >= 10))) {
    if (check("Naval Academy graduation", 9, 2 * Number(stats[3] >= 8))) {
      age += 4;
      officer = 1;
      change(4, Math.max(1, roll("Academy education", 0, -3 + Number(stats[3] >= 9), 1)));
      honors = check("Academy honors", 9, Number(stats[3] >= 9));
      school(["Vacc Suit", "Navigation", "Engineering"], 4, 3, false);
    } else {
      age++;
      drafted = true;
      notes.push("Academy washout. Drafted into the Navy.");
    }
  }
  if (options.entry === "college" && check("College admission", 9, 2 * Number(stats[4] >= 9))) {
    if (check("College graduation", 7, 2 * Number(stats[3] >= 8))) {
      age += 4;
      college = true;
      if (options.notc && check("Naval Officer Training Corps", 8, Number(stats[5] >= 10))) officer = 1;
      change(4, Math.max(1, roll("College education", 0, -2 + Number(stats[3] >= 9), 1)));
      honors = check("College honors", 10, Number(stats[4] >= 10));
    } else age++;
  }
  if (options.school === "medical" && honors && check("Medical school admission", 9, 2 * Number(stats[4] >= 10))) {
    if (check("Medical school graduation", 8, 2 * Number(stats[3] >= 9))) {
      age += 4;
      change(4, 1);
      gain("Medical", 3);
      gain("Administration");
      if (check("Medical school honors", 11, Number(stats[4] >= 11))) {
        gain("Medical");
        gain("Computer");
      }
      officer = 3;
      forcedBranch = 4;
    } else age++;
  }
  if (options.school === "flight" && officer &&
      (honors || (options.entry === "academy" && check("Flight school admission", 9, Number(stats[1] >= 9))))) {
    age++;
    if (check("Flight school graduation", 7, Number(stats[3] >= 8))) {
      school(["Pilot", "Ship's Boat", "Navigation"], 4, 3, false);
      gain("Pilot");
      forcedBranch = 1;
    }
  }
  const available = [options.tech[0] >= 7, options.tech[1] >= 9, true];
  const order = [navy, ...[2, 1, 0].filter(candidate => candidate !== navy)].filter(candidate => available[candidate]);
  if (!available[navy]) notes.push(navies[navy] + " Navy unavailable at the entered tech level.");
  const enlistDm = Number(stats[3] >= 8) + 2 * Number(stats[4] >= 9);
  const accepted = officer || drafted ? order[0] : order.find(candidate =>
    check(navies[candidate] + " enlistment", 7 + candidate, enlistDm));
  if (accepted === undefined) {
    status = "Enlistment unsuccessful";
  } else {
    navy = accepted;
    branch = forcedBranch ?? chooseBranch();
    notes.push(navies[navy] + " Navy · " + (branch === 0 && !officer ? "Crew" : branches[branch]) + " · " + rank());
    notes.push("Entered service at age " + age);
  }

  function assignment(duty, command, promoted, decorationDm) {
    const [survival, decoration, promotion, skill] = resolution[branch].map(row => row[duty]);
    const branchLevel = Math.max(...branchSkills[branch].map(name => name === "Gunnery"
      ? Math.max(...gunnery.map(type => skills["Gunnery (" + type + ")"] || 0)) : skills[name] || 0));
    const hazard = decoration && survival ? options.risk : 0;
    if (survival) {
      const survivalDm = [Number(branchLevel >= 2), skills.Pilot || 0, 0, Number(skills.Engineering >= 4), 0, 0][branch];
      const result = roll("Survival", survival, survivalDm - hazard);
      if (result < survival) {
        status = "Killed in service";
        notes.push(status);
        return {promoted, decorationDm};
      }
      if (result === survival) {
        notes.push("Wounded in the line of duty");
        if (duty >= 4) medal("Purple Heart");
      }
    } else notes.push("Automatic survival");
    if (decoration) {
      const modifier = hazard + (branch === 1 && duty >= 4 ? bookOneRanks[officer] : 0) +
        Number(branch === 2 && stats[1] >= 10);
      const margin = roll("Decoration", decoration, modifier) - decoration;
      if (margin >= 0) {
        const award = Math.min(2, Math.floor(margin / 3));
        medal(["MCUF", "MCG", "SEH"][award]);
        decorationDm += award + 1;
      }
    }
    const canPromote = officer ? officer < 10 && !promoted && promotion > 0 : rating < 9 && promotion !== 0;
    const promotionDm = [Number(stats[4] >= 8) + Number(stats[5] >= 9), 0,
      Number(stats[1] >= 9), 0, Number(skills.Medical >= 5), Number(branchLevel >= 3)][branch];
    if (canPromote && check("Promotion", Math.abs(promotion), decorationDm + promotionDm)) {
      if (officer) { officer++; promoted = true; }
      else rating++;
      notes.push("Promoted to " + rank());
    }
    if (skill && check("Skill eligibility", skill)) {
      const table = options.skillTable;
      if (table === "branch") learnBranch();
      else if (table === "life") learnService("Navy life");
      else if (officer) learnService(command ? "Command" : "Staff");
      else if (rating >= 4) learnService("Petty officer");
      else learnService(duty <= 1 ? "Shore duty" : "Shipboard");
    }
    return {promoted, decorationDm};
  }

  function commandDuty() {
    if (!officer || !options.command) return false;
    const dm = (officer <= 2 ? -2 : officer <= 4 ? -1 : 0) +
      Number(stats[5] >= 11) - Number(stats[3] <= 7) - Number(stats[4] <= 7);
    return check("Command duty", 7 + branch, dm);
  }

  const crossTrained = new Set();
  let aide = false;
  function specialDuty(promoted, decorationDm) {
    const dm = Number(options.specialBonus && (college || stats[5] >= 11));
    let duty = roll("Special duty", 0, dm, 1);
    if (!officer && age > 34 && duty >= 6) duty = roll("OCS age waiver / reassignment", 0, dm, 1);
    if (duty === 1) {
      const original = branch;
      const candidates = allowedBranches().filter(candidate => candidate !== branch && (officer || candidate !== 1));
      if (officer) {
        const modifier = 2 * (Number(stats[4] >= 9) + Number(stats[3] >= 10)) - 2 * Number(navy === 2);
        // Resolve repeated ineligible branch results as a conditional draw over the valid faces.
        const faces = [];
        for (let face = 1; face <= 6; face++) {
          const candidate = branchRoll[1][Math.max(0, Math.min(7, face + modifier))];
          if (candidates.includes(candidate)) faces.push(candidate);
        }
        if (faces.length) {
          let face = roll("Cross-training branch draw", 0, 0, 1);
          for (let reroll = 0; reroll < 20 && face > faces.length; reroll++) {
            face = roll("Cross-training branch redraw", 0, 0, 1);
          }
          branch = faces[face - 1] ?? faces[0];
        } else branch = candidates[0];
      } else branch = candidates.includes(options.branch) ? options.branch : candidates[0];
      crossTrained.add(branch);
      notes.push("Cross-training · " + branches[branch]);
      if (officer) {
        const command = commandDuty();
        let result = assignments[roll("Cross-training assignment", 0) - 2];
        for (let reroll = 0; reroll < 20 && result === 6; reroll++) {
          result = assignments[roll("Cross-training assignment reroll", 0) - 2];
        }
        if (result === 6) result = 2;
        notes.push(duties[result] + (command ? " · command" : " · staff"));
        ({promoted, decorationDm} = assignment(result, command, promoted, decorationDm));
      } else learnBranch();
      branch = original;
    } else if (duty === 2 && officer) {
      notes.push("Intelligence School");
      school(["Forgery", "Gun Combat", "Bribery", "Streetwise", "Interrogation"], 4);
    } else if (duty === 2) {
      notes.push("Specialist School");
      gain(["Administration","Medical","Liaison","Mechanical","Electronics","Gravitics",
        "Vehicle","Navigation","Computer","Ship's Boat","Communications","Vacc Suit"]
        [roll("Specialty", 0, options.specialist, 1) - 1]);
    } else if (duty === 3) {
      notes.push("Recruiting duty");
      gain("Recruiting");
      if (!officer && check("Recruiting administration", 4, 0, 1)) gain("Administration");
    } else if (duty === 4 && officer) {
      aide = check("Flag aide posting", 5, 0, 1);
      notes.push(aide ? "Flag aide. Next assignment may be selected." : "Naval attaché");
      change(5, 1);
      if (!aide && officer < 10) {
        officer++;
        notes.push("Promoted to " + rank());
      }
    } else if (duty === 4) {
      notes.push("Gunnery School");
      school(gunnery.map(type => "Gunnery (" + type + ")"), 5);
    } else if (duty === 5 && officer) {
      notes.push("Command College");
      school(["Ship Tactics", "Fleet Tactics", "Leader", "Administration"], 4, 3);
    } else if (duty === 5) {
      notes.push("Engineering School");
      school(["Mechanical", "Electronics", "Gravitics", "Engineering"], 5);
    } else if (officer) {
      notes.push("Staff College");
      school(["Fleet Tactics", "Administration", "Liaison", "Computer"], 4);
    } else {
      officer = 1;
      notes.push("Officer Candidate School · commissioned as " + rank());
      if (allowedBranches().includes(options.branch) && (stats[5] >= 9 || crossTrained.has(options.branch))) branch = options.branch;
      learnService("Command");
      learnService("Staff");
      learnBranch();
    }
    return {promoted, decorationDm};
  }

  function ageAtTermEnd() {
    if (age < 34) return;
    const old = age >= 66;
    for (let index = 0; index < (old ? 4 : 3); index++) {
      const target = old ? 9 : (age >= 50 ? 9 : 8) - Number(index === 1);
      if (check("Aging at " + age + " · " + attributes[index], target)) continue;
      change(index, old && index < 3 ? -2 : -1);
      if (stats[index] > 0) continue;
      stats[index] = 0;
      if (check("Aging crisis", 8)) stats[index] = 1;
      else {
        status = "Died after an aging crisis";
        notes.push(status);
        return;
      }
    }
  }

  while (status === "Mustered out") {
    let retained, promoted = false, decorationDm = 0;
    const length = 4 - (age - 18) % 4;
    for (let year = 0; year < length; year++) {
      section("Term " + (terms + 1) + " · Year " + (year + 1) + " · Age " + age);
      if (!years) {
        notes.push(officer ? "Basic and advanced officer training" : "Basic and advanced training");
        log.at(-1).title += " / Basic training";
        for (let index = 0; index < 2; index++) {
          if (officer && options.skillTable === "duty") learnService("Staff");
          else learnBranch();
        }
      } else {
        const wasRetained = retained !== undefined;
        const command = aide ? options.command : commandDuty();
        const duty = aide ? options.aideDuty : retained ??
          assignments[roll("Assignment", 0, Number(college && !officer)) - 2];
        if (aide) notes.push("Selected posting as flag aide");
        aide = false;
        log.at(-1).title += " / " + duties[duty];
        notes.push(duties[duty] + (wasRetained ? " · retained" : "") +
          (officer ? (command ? " · command" : " · staff") : ""));
        ({promoted, decorationDm} = duty === 6 ? specialDuty(promoted, decorationDm)
          : assignment(duty, command, promoted, decorationDm));
        if (status !== "Mustered out") break;
        retained = !wasRetained && duty !== 6 && year < length - 1 &&
          check("Assignment retention", 6, 0, 1) ? duty : undefined;
      }
      age++;
      years++;
    }
    if (status !== "Mustered out") break;
    terms++;
    section("End of term " + terms);
    ageAtTermEnd();
    if (status !== "Mustered out") break;
    if (terms === 30) {
      status = "Preview stopped at 30 terms";
      notes.push("Career remains active.");
      break;
    }
    const reenlistment = roll("Re-enlistment", 6, Number(Boolean(officer) || rating >= 4));
    if (reenlistment >= 12) notes.push("Re-enlistment required");
    else if (terms >= 7 || terms >= options.terms || reenlistment < 6) {
      notes.push(terms >= 7 ? "Retirement after seven or more terms" :
        terms >= options.terms ? "Elected to muster out" : "Re-enlistment denied");
      break;
    }
    if (options.transfer && options.branch !== branch && crossTrained.has(options.branch)) {
      branch = options.branch;
      notes.push("Transferred to " + branches[branch]);
    }
  }

  if (status === "Mustered out") {
    section("Mustering out · Book 1");
    const equivalentRank = bookOneRanks[officer];
    const benefitCount = terms + (equivalentRank >= 5 ? 3 : equivalentRank >= 3 ? 2 : equivalentRank ? 1 : 0);
    for (let index = 0; index < benefitCount; index++) {
      if (index < options.cashRolls) {
        const result = cash[roll("Cash benefit", 0, Number(Boolean(skills.Gambling)), 1) - 1];
        credits += result;
        notes.push("Received Cr " + result.toLocaleString("en-US"));
      } else {
        const result = material[roll("Material benefit", 0, Number(equivalentRank >= 5), 1) - 1];
        if (result.startsWith("+")) gain(result, result === "+INT" ? 1 : 2);
        else if (result === "Blade" && benefits.includes("Cutlass")) gain("Cutlass");
        else if (result.includes("Society") && benefits.includes(result)) notes.push("Already a member of the Travellers’ Aid Society");
        else {
          benefits.push(result === "Blade" ? "Cutlass" : result);
          notes.push("Received " + benefits.at(-1));
        }
      }
    }
    pension = terms >= 5 ? 4000 + (terms - 5) * 2000 : 0;
  }
  return { name: options.name || "Unnamed recruit", initialUpp,
    upp: stats.map(value => "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ"[value] ?? "[" + value + "]").join(""),
    stats, age, years, terms, status, navy: branch === undefined ? null : navies[navy],
    branch: branch === 0 && !officer ? "Crew" : branches[branch] ?? null,
    rank: branch === undefined ? null : rank(), credits, pension, skills, medals, benefits, log };
}
