export const lines = ["Free Trader","Fledgling line","Interface line","Subsector-wide line","Sector-wide line","Megacorporation"];
export const departments = ["Deck","Engineering","Purser","Administration","Sales","Free Trader"];
const statsNames = ["STR","DEX","END","INT","EDU","SOC"];
const digits = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ";
const assignments = [
  ["Down","Route","Route","Route","Route","Route","Route","Charter","Speculative trade","Exploratory trade","Special duty","Special duty"],
  ["Down","Route","Route","Route","Route","Charter","Speculative trade","Speculative trade","Exploratory trade","Exploratory trade","Special duty","Up"],
  ["Up","Route","Exploratory trade","Route","No business","Route","No business","Charter","Speculative trade","Exploratory trade","Smuggling","Piracy"],
];
const ordinaryDuties = ["Route","Charter","Exploratory trade","Speculative trade","No business","Smuggling","Piracy"];
// Each posting lists survival, skill eligibility and bonus targets.
const resolution = [
  [[0,7,0],[3,7,0],[4,5,11],[3,6,12]],
  [[0,7,0],[0,6,0],[4,5,12],[3,6,11]],
  [[0,6,0],[0,5,0],[3,8,12],[0,7,11]],
  [[0,6,0],[0,5,0],[3,8,12],[3,7,11]],
  [[0,7,13],[0,7,14],[4,5,10],[3,5,9]],
  [[3,7,11],[4,6,10],[5,5,8],[5,5,7],[3,3,0],[6,5,6],[7,4,5]],
];
const skillsByDepartment = [
  ["Navigation","Administration","Pilot","Legal","Ship’s Boat","Leader"],
  ["Mechanical","Electronics","Engineering","Administration","Engineering","Gravitics"],
  ["Steward","Medical","Liaison","Gunnery","Steward","Liaison"],
  ["Administration","Liaison","Bribery","Administration","Administration","Streetwise"],
  ["Trader","Broker","Computer","Liaison","Trader","Broker"],
  ["Steward","Trader","Broker","Administration","Gunnery","Leader"],
];
const medicSkills = ["Steward","Medical","Medical","Medical","Computer","Medical"];
const life = ["Brawling","Carousing","Gambling","Trader","+EDU","Carousing"];
const officer = ["Brawling","Vehicle","Ship’s Boat","Gun Combat","Liaison","Liaison"];
const commerce = ["Streetwise","Broker","Trader","Liaison","Administration","Legal"];
const ranks = [
  ["Apprentice","4th Officer","3rd Officer","2nd Officer","1st Officer","Captain","Senior Captain","Line Commodore"],
  ["Assistant Drive Hand","Drive Hand","Assistant Engineer","Engineer","Chief Engineer"],
  ["Steward","Junior Purser","Assistant Purser","Purser","Chief Purser"],
  ["Clerk","Assistant Manager","Manager","Assistant Station Head","Station Head"],
  ["Apprentice Clerk","Assistant Broker","Broker","Senior Broker","Lead Broker"],
  ["","4th Officer","3rd Officer","2nd Officer","1st Officer","Captain","Owner/Captain"],
];
const exams = [[6,6,6,7,7,9,8,8],[5,8,7,7,9],[5,5,5,6,7],
  [5,6,7,6,7],[4,5,6,7,8],[0,4,6,7,7,8,8]];
const cash = [[1000,2000,10000,20000,30000,40000,30000],
  [1000,5000,10000,20000,50000,40000,40000],[1000,5000,10000,20000,30000,40000,50000],
  [1000,5000,10000,20000,40000,40000,100000],[1000,5000,10000,20000,30000,40000,50000],
  [1000,5000,10000,20000,30000,40000,50000]];
const material = [
  ["Low passage","+END","+EDU","Gun Combat","Blade Combat","Middle passage","Free Trader"],
  ["Middle passage","+INT","+EDU","Gun Combat","Middle passage","Middle passage","Free Trader"],
  ["Middle passage","+INT","+EDU","Gun Combat","Middle passage","Middle passage","Far Trader"],
  ["Middle passage","+INT","+EDU","Gun Combat","Middle passage","High passage","Fat Trader"],
  ["High passage","+INT","+2EDU","Gun Combat","High passage","High passage","Travellers’ Aid Society membership"],
  ["High passage","+2INT","+2EDU","Gun Combat","High passage","Travellers’ Aid Society membership","+SOC"],
];
const schools = {
  "Security training":[-1,4,"Zero-G Combat","Zero-G Weapons","Vacc Suit","Brawling","Computer"],
  "Trade station":[3,4,"Broker","Liaison"], "Helm training":[0,5,"Navigation","Pilot","Ship’s Boat"],
  "Drive training":[1,5,"Electronics","Engineering","Gravitics","Mechanical"],
  "Steward training":[2,4,"Administration","Liaison","Steward"],
  "Command school":[0,5,"Administration","Leader","Legal","Ship Tactics"],
  "Deck school":[0,5,"Communications","Computer","Gunnery"],
  "Engineering school":[1,5,"Administration","Computer","Electronics","Engineering","Mechanical","Gravitics"],
  "Purser school":[2,4,"Administration","Computer","Liaison"], "Business school":[4,5,"Administration","Computer","Legal","Liaison"],
};
const enlistTarget = [7,7,7,7,8,9];
const minimumPort = [5,5,5,3,2,1];
const defaultDie = () => 1 + Math.floor(Math.random() * 6);

export function* merchantCareer(options, die = defaultDie) {
  const stats = options.upp ? [...options.upp.toUpperCase()].map(code => digits.indexOf(code)) :
    statsNames.map(() => die() + die());
  const upp = () => stats.map(value => digits[value] ?? "[" + value + "]").join("");
  const record = {version:"1.0",schemaVersion:1,ruleset:"CT-MERCHANT-1985-CAREER",name:options.name || "Unnamed merchant",
    port:options.port,initialUpp:upp(),upp:upp(),age:18,terms:0,years:0,line:null,department:null,rank:null,
    status:"In progress",skills:{},credits:0,benefits:[],history:[],log:[]};
  let line, department, rank = -1, commissionAge, medical = false, business = false, notes;
  const start = title => { notes=[]; record.log.push({title,notes}); };
  const roll = (label,target = 0,dm = 0,count = 2) => {
    const dice = Array.from({length:count},die);
    const total = dice.reduce((sum,face) => sum + face,dm);
    notes.push(label + " · " + dice.join(" + ") + (dm ? " (DM " + (dm > 0 ? "+" : "") + dm + ")" : "") +
      " = " + total + (target ? " vs " + target + "+" : ""));
    return total;
  };
  const skill = (name,level = 1) => (record.skills[name] || 0) >= level;
  const award = name => {
    if (name.startsWith("+")) {
      const amount = name[1] === "2" ? 2 : 1;
      const index = statsNames.indexOf(name.slice(amount === 2 ? 2 : 1));
      stats[index] += amount;
      notes.push(statsNames[index] + " +" + amount);
      return;
    }
    name = {"Gun Combat":options.gun || "Handgun","Blade Combat":"Sword",Vehicle:"Grav Vehicle"}[name] ?? name;
    if (name === "Zero-G Weapons" && !skill("Zero-G Combat")) {
      notes.push("Zero-G Weapons requires Zero-G Combat");
      return;
    }
    if (Object.values(record.skills).reduce((a,b) => a+b,0) >= stats[3] + stats[4]) {
      notes.push(name + " · skill limit reached");
      return;
    }
    record.skills[name] = (record.skills[name] || 0) + 1;
    notes.push(name + " " + record.skills[name]);
  };
  const departmentSkills = () => department === 2 && medical ? medicSkills : skillsByDepartment[department];
  const learn = (table, label) => award(table[roll(label + " skill",0,0,1) - 1]);
  const learnDuty = () => {
    if (options.skillTable === "life") learn(life,"Merchant life");
    else if (options.skillTable === "officer" && rank >= 0) learn(officer,"Officer");
    else if (options.skillTable === "commerce" && department !== 1) learn(commerce,"Mercantile");
    else learn(departmentSkills(),departments[department]);
  };
  const update = () => {
    record.line = lines[line];
    record.department = departments[department];
    const title = department === 2 && medical && rank >= 2 && rank <= 3 ?
      (rank === 2 ? "Assistant Medic" : "Medic") : ranks[department][rank];
    record.rank = rank >= 0 ? "O" + rank + " · " + title : "E" + Math.max(1,record.terms + 1);
    if (!record.history.includes(record.line)) record.history.push(record.line);
  };
  const snapshot = () => { record.upp=upp(); return record; };
  const finish = status => { record.status=status; return snapshot(); };
  const pickDepartment = college => {
    if (line === 0) return 5;
    return (line >= 4 ? [2,2,2,4,1,1,1] : [2,2,2,1,1,1,0])[roll("Department",0,Number(college),1)-1];
  };
  start("Recruitment");
  let college=false, honors=false, academy=false, academyHonors=false;
  if (options.college && roll("College admission",9,2*Number(stats[4]>=9)) >= 9) {
    college = roll("College graduation",7,2*Number(stats[3]>=8)) >= 7;
    record.age += college ? 4 : 1;
    if (college) {
      stats[4] += Math.max(1,roll("Education increase",0,-2+Number(stats[3]>=9),1));
      honors = roll("College honors",10,Number(stats[4]>=10)) >= 10;
      if (honors) stats[4]=Math.max(10,stats[4]+1);
    }
  }
  const order = [options.line,...[5,4,3,2,1,0].filter(candidate=>candidate!==options.line)];
  const port = "ABCDEX".indexOf(options.port);
  if (honors) notes.push("Eligible for automatic megacorporation enlistment");
  line = order.find(candidate => {
    if (candidate === 5 && honors) return true;
    if (port > minimumPort[candidate]) return false;
    return roll(lines[candidate] + " enlistment",enlistTarget[candidate],
      Number(stats[0]>=7)+2*Number(stats[3]>=6)) >= enlistTarget[candidate];
  });
  if (line === undefined) {
    if (!options.draft || roll("Merchant draft · requires 5 exactly",0,0,1) !== 5) return finish("Enlistment unsuccessful");
    line=0;
    notes.push("Drafted into the Free Traders");
  }
  if (line >= 4 && record.age === 18 && options.academy &&
      roll("Merchant Academy admission",9,2*Number(stats[4]>=10)) >= 9) {
    academy = roll("Merchant Academy graduation",9,2*Number(stats[3]>=8)) >= 9;
    record.age += academy ? 4 : 1;
    if (academy) {
      record.terms=1;
      record.years=4;
      rank=1;
      stats[4] += Math.max(1,roll("Academy education",0,-3+Number(stats[3]>=9),1));
      academyHonors = roll("Academy honors",9,Number(stats[3]>=9)) >= 9;
      notes.push("Commissioned · automatic re-enlistment after academy");
    }
  }
  department = academyHonors && options.academyDepartment >= 0 ? options.academyDepartment : pickDepartment(college);
  update();
  if (academy) for (let index=0; index<3; index++) learn(departmentSkills(),record.department);
  notes.push(record.line + " · " + record.department + " · " + record.rank);
  yield {record};

  function transfer(next, onLineChange = false) {
    if (next === department || (next < 0)) return;
    if (!onLineChange && (rank >= 5 || rank >= ranks[next].length || department === 5)) return;
    if (onLineChange && rank >= 0) rank=Math.max(next===5?1:0,Math.min(rank,ranks[next].length-1));
    department=next;
    medical=false;
    update();
    notes.push("Transferred to " + record.department);
  }
  function duty() {
    for (let attempt=0; attempt<20; attempt++) {
      const table = line===0 ? 2 : line>=4 ? 0 : 1;
      const modifier = line===0 ? Number(stats[5]<=5) :
        Number(college&&rank<0)-Number(stats[4]<=6)-Number(stats[5]>=10)+Number(rank>=4&&options.dutyBonus);
      let result = assignments[table][Math.max(2,Math.min(13,roll("Yearly assignment",0,modifier)))-2];
      if (line===0 && rank===6 && options.dutyBonus && result!=="No business") {
        result=assignments[2][Math.min(13,roll("Owner’s assignment reroll",0,modifier))-2];
      }
      if (result!=="Up" && result!=="Down") return result;
      const next=line+(result==="Up"?1:-1);
      if (next<0 || next>4 && result==="Up") return "Route";
      line=next;
      if (line===0) transfer(5,true);
      else if (rank>=4) transfer(0,true);
      else if (department===5) transfer(pickDepartment(college),true);
      update();
      notes.push("Line transfer · " + record.line);
    }
    notes.push("Assigned to a route after repeated transfers");
    return "Route";
  }
  function qualified(next) {
    switch (department) {
      case 0: return [true,skill("Navigation"),skill("Administration"),skill("Ship’s Boat")||skill("Pilot"),
        skill("Pilot"),skill("Legal"),true,true][next];
      case 1: return [true,skill("Mechanical")||skill("Electronics")||skill("Gravitics"),
        skill("Engineering")||skill("Gravitics",2),skill("Engineering",2),
        skill("Engineering",3)&&skill("Administration")][next];
      case 2: return [true,skill("Steward")||skill("Gunnery"),
        skill("Steward",2)||skill("Medical"),medical?skill("Medical",2):skill("Liaison"),skill("Administration")][next];
      case 3: return [true,skill("Administration"),skill("Administration",2)&&skill("Liaison"),
        skill("Administration",3),skill("Liaison",2)][next];
      case 4: return [true,skill("Trader"),skill("Broker")||skill("Trader",2),
        skill("Broker",2)||skill("Trader",3),skill("Broker",3)][next];
      default: return [false,true,skill("Steward")&&skill("Engineering"),skill("Navigation"),skill("Pilot"),skill("Legal"),true][next];
    }
  }
  function promote(posting,available,exception) {
    const next = rank < 0 && line === 0 ? 1 : rank + 1;
    if (!exams[department][next] || (!exception && (rank<0 ? posting!=="Route" : !available || !qualified(next)))) return;
    const medicExam = department===2 && (medical&&next===3 || next===2&&skill("Medical")&&
      (options.medic || !skill("Steward",2)));
    const target = medicExam && next===2 ? 6 : exams[department][next];
    if (roll("Examination for O" + next,target,Number(department===0&&next>=6&&business)) < target) return;
    rank=next;
    if (rank===0) commissionAge=record.age;
    if (medicExam) medical=true;
    update();
    notes.push("Promoted · " + record.rank);
    learnDuty();
  }
  let discharge=false;
  for (;record.terms<18;) {
    const term=record.terms+1;
    const duration=4-(record.age-18)%4;
    const seniorAtStart=rank>=4;
    for (let year=1;year<=duration;year++) {
      start("Term " + term + " · Year " + year + " · Age " + record.age);
      if (rank===0 && record.age>=commissionAge+4) {
        rank=-1;
        update();
        notes.push("Commission lapsed after four years at O0");
      }
      if (rank<0 && year===1) {
        notes.push("Seniority · " + record.rank);
        learnDuty();
      }
      let posting=duty(), exceptional=false;
      if (posting==="Special duty") {
        const modifier=Number(stats[4]>=9)+Number(rank>=4&&department!==0);
        const table=rank<0 ? ["Security training","Trade station","Helm training","Drive training","Steward training","Commission","Commission"] :
          ["Trade station","Command school","Deck school","Engineering school","Purser school","Business school","Department test"];
        const special=table[Math.min(7,roll("Special duty",0,modifier,1))-1];
        notes.push(special);
        if (special==="Commission" || special==="Department test") {
          if (special==="Commission") {
            rank=line===0?1:0;
            commissionAge=record.age;
            update();
          } else exceptional=true;
          posting=duty();
          if (posting==="Special duty") posting="Route";
        } else {
          const [destination,target,...awards]=schools[special];
          if (special==="Trade station") award("Trader");
          if (special==="Business school") business=true;
          for (const name of awards) if (roll(name,target,0,1)>=target) award(name);
          transfer(destination);
        }
      }
      record.log.at(-1).title += " / " + posting;
      if (posting!=="Special duty") {
        const availableTarget=(line>=4?[9,8,7,8,6]:[10,8,6,9,6])[department] ?? 8;
        const available=rank<=0 || roll("Position at current rank",availableTarget,
          Number(stats[3]>=9)+Number(line!==0&&stats[4]>=9))>=availableTarget;
        if (!available) notes.push("Served in a position one grade lower");
        const [survive,learnTarget,bonus]=resolution[department][ordinaryDuties.indexOf(posting)];
        const trained=departmentSkills().some(name=>skill(name,2));
        const survivalDm=department===5?Number(skill("Pilot",2)):department<=1?Number(trained):0;
        if (survive && roll("Survival",survive,survivalDm)<survive) {
          if (!options.injury) return finish("Died in service");
          discharge=true;
          notes.push("Optional survival rule · current term earns no mustering-out roll");
          break;
        }
        if (!survive) notes.push("Automatic survival");
        promote(posting,available,exceptional);
        const bonusDm=department===2?(record.skills.Steward||0):department===4?
          Math.max(record.skills.Broker||0,record.skills.Trader||0):department===5?Number(skill("Trader",2)):
            department===3?Number(trained):0;
        if (bonus && roll("Performance bonus",bonus,bonusDm)>=bonus) {
          const amount=cash[line][roll("Bonus cash table",0,Number(skill("Gambling")),1)-1]/2;
          record.credits+=amount;
          notes.push("Bonus Cr " + amount.toLocaleString());
        }
        if (learnTarget) {
          const result=roll("Assignment skill eligibility",learnTarget);
          if (result>=learnTarget) learnDuty();
          if (["Smuggling","Piracy"].includes(posting)&&result>=learnTarget+4) learnDuty();
        }
      }
      record.years++;
      record.age++;
      yield {record:snapshot()};
    }
    if (discharge) break;
    record.terms++;
    start("End of term " + term);
    notes.push("Age " + record.age);
    if (record.age>=34) {
      for (let index=0;index<(record.age>=66?4:3);index++) {
        const target=record.age>=66?9:(record.age>=50?9:8)-Number(index===1);
        if (roll("Aging · " + statsNames[index],target)>=target) continue;
        stats[index]=Math.max(0,stats[index]-(record.age>=66&&index<3?2:1));
        if (!stats[index]) {
          if (roll("Aging crisis",8)<8) return finish("Died after an aging crisis");
          stats[index]=1;
        }
        notes.push(statsNames[index]+" is now "+stats[index]);
      }
    }
    const reenlist=roll("Re-enlistment",4,Number(rank>=0));
    if (reenlist>=12) notes.push("Re-enlistment required");
    else if (term>=7 || reenlist<4) {notes.push(term>=7?"Service limit reached":"Re-enlistment denied");break;}
    else if (!(yield {record:snapshot(),decision:true,defaultStay:term<options.terms})) {
      notes.push("Elected to muster out");
      break;
    }
    if (seniorAtStart && rank>=4 && line!==0) transfer(0,true);
    if (record.terms===18) return finish("Preview stopped during mandatory service");
    update();
  }
  start("Mustering out");
  const rolls=record.terms+(rank>=5?3:rank>=3?2:rank>=1?1:0);
  notes.push(rolls+" benefit rolls");
  for (let index=0;index<rolls;index++) {
    if (index<options.cashRolls) {
      const amount=cash[line][roll("Cash benefit",0,Number(skill("Gambling")),1)-1];
      record.credits+=amount;
      notes.push("Cr " + amount.toLocaleString());
    } else {
      let item=material[line][roll("Material benefit",0,Number(rank>=5),1)-1];
      if (item.startsWith("+")) award(item);
      else {
        if (item==="Gun Combat" || item==="Blade Combat") {
          const weapon=item==="Gun Combat"?(options.gun||"Handgun"):"Sword";
          if (record.benefits.includes(weapon)) {award(item);continue;}
          item=weapon;
        }
        record.benefits.push(item);
        notes.push(item);
      }
    }
  }
  return finish(discharge?"Discharged after failed survival":"Mustered out");
}
