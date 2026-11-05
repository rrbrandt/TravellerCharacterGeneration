export const VERSION = "1.0";
const digits = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ";
const urpValues = [...Array.from({length:21},(_,index)=>index),30,40,50,75,100,125,150,175,200,300,400,500,1000];
const encode = value => digits[urpValues.findLastIndex(boundary=>value>=boundary)] || "0";
const powerTier = tl => tl>=16 ? [3.5,.4] : tl===15 ? [3,.5] : tl>=13 ? [1.5,.75] : [1,1];

export const chassis = [5,10,20,50,80,100,150,200,350,500,750,1000,2000,3000].map((liters,index)=>({
  name:digits[index]+" · "+liters+" L",liters,cost:[1500,2500,4000,7500,9500,1000,1500,2000,2500,3000,3500,4000,5000,6000][index],
}));
export const frames = [
  ["Open frame",.5,.5,"None"],["Box / wedge",1,1,"Mesh"],["Cylinder / cone",1,1.1,"Mesh"],
  ["Sphere",.8,1.5,"Mesh"],["Dome / disk",.9,1.2,"Mesh"],["Contoured",1.2,2,"Mesh"],
  ["Pseudo-biological",1.5,8,"Jack"],
];
export const plants = [
  ["Fuel cell A",10,20,20,600,.10],["Fuel cell B",20,30,35,800,.15],["Fuel cell C",30,40,55,1000,.20],
  ["Fuel cell D",40,50,75,1200,.25],["Fuel cell E",50,60,95,1400,.30],["Fuel cell F",70,80,130,1500,.35],
  ["Fuel cell G",90,100,165,2000,.40],["Fusion 250",245,250,1000,300000,.75],
  ["Fusion 500",500,500,2000,250000,1],["Fusion 750",1000,750,3000,225000,1.2],
  ["Fusion 1000",2000,1000,4000,200000,1.5],
];
const batteries = [[1,850],[3,3000],[4,5000],[7,10000],[11,15000],[18,20000]];
export const movement = [
  ["Stationary","N"],["Legs","0"],["Wheels","A"],["Tracks","B"],
  ["Air cushion","C",5,10,15,50,1500],["Ultra-heavy grav","D",100,20,40,1000,2000],
  ["Heavy grav","E",8,20,12,400,10000],["Light grav","F",1,3,2,100,30000],
];
export const appendages = [
  ["Very light arm",1,1,750,3,0],["Light arm",2,5,500,2,5],["Medium arm",5,20,700,1,20],
  ["Heavy arm",10,50,1000,0,50],["Very light tentacle",3,5,1000,5,0],
  ["Light tentacle",5,10,750,4,3],["Medium tentacle",10,20,1200,3,7],["Heavy tentacle",15,30,1500,2,10],
];
export const equipment = {
  voder:["Speech synthesizer",2,3,1200],spot:["Spotlight",1,1,50],touch:["Touch sensors",1,.5,150],
  power:["Power interface",1,.5,100],link:["Brain interface",1,1,1200],
  radiation:["Radiation sensor",2,.5,1200],radio:["Radio · 50 km",1,.4,250],
  zero:["Zero-G maneuver kit",1,4,2000],aqua:["Aqua maneuver kit",5,10,1100],
  tools:["Mechanical tool package",5,25,2500],medical:["Medical instrument kit",1.5,15,6000],
  janitor:["Janitorial tool package",4.5,78,3500],welder:["Light laser welder",5,10,5000],
};
export const weapons = [
  ["None",0,0,0,0],["Body / snub pistol",1,1,700,1],["Automatic pistol",1,2,700,1],
  ["SMG",1,5,800,2],["Automatic rifle",2,5,1500,2],
  ["Gauss rifle",3.5,6,2000,3],["Laser carbine",5,8,3000,2],["Laser rifle",15,14,5000,3],
];
export const logic = [
  ["Low data",8,2,0,0,10,400,0],["High data",9,8,5,0,10,3000,1],
  ["Low autonomous",12,15,10,1,25,7000,2],["High autonomous",13,20,12,3,25,10000,2],
  ["Low artificial intelligence",17,25,4,21,25,20000,3],
  ["High artificial intelligence",18,30,2,28,25,50000,3],
];
export const commands = [
  ["Limited basic",1,1,500,0,0],["Basic",2,2,1000,1,1],["Full",3,5,5000,2,2],
];
export const programs = {
  cargo:["Cargo handling",2,200],rescue:["Rescue",4,200],vehicle:["Vehicle",4,400],
  weapon:["Weapon handling",2,300],security:["Security",2,200],recon:["Recon",2,400],
  admin:["Administration",4,400],language:["Additional language",2,300],
  melee:["Close combat",3,400],emotion:["Emotion simulation",2,400],
};
const base = {
  name:"Dockside utility",purpose:"Move and sort freight",tl:12,size:5,frame:1,armor:"base",reflective:false,
  plant:1,battery:0,batteryDrive:12,fuel:12,sealed:false,move:2,units:1,legs:2,payload:0,
  arm:1,arms:1,tentacle:4,tentacles:0,head:0,gear:["radio"],weapon:0,law:0,
  circuit:false,logic:0,command:0,linear:10,parallel:0,synaptic:0,storage:15,synStorage:0,budget:0,
  apps:{cargo:{level:1,resident:true}},
};
export const presets = {
  utility:base,
  rescue:{...base,name:"Grav rescue scout",purpose:"Recover casualties off-road",tl:14,size:7,frame:4,
    plant:1,fuel:24,move:6,payload:45,arms:2,gear:["radio","voder","medical","radiation"],
    logic:2,command:2,linear:10,parallel:10,synaptic:2,storage:34,
    apps:{vehicle:{level:1,resident:true},rescue:{level:1,resident:false}}},
  station:{...base,name:"Station watch",purpose:"Monitor a compartment",tl:13,size:3,frame:2,
    plant:-1,battery:18,fuel:0,move:0,arms:0,gear:["radio","voder"],logic:1,command:1,
    linear:7,parallel:5,synaptic:0,storage:15,
    apps:{security:{level:1,resident:true}}},
};

export function brainRequirements(design) {
  const [,minTL,baseCpu,parallel,synaptic,baseStorage,logicCost] = logic[design.logic];
  const [,commandCpu,commandStorage,commandCost,requiredLogic] = commands[design.command];
  let cpu=baseCpu+commandCpu, storage=baseStorage+commandStorage, cost=logicCost+commandCost, levels=0;
  const factor=design.tl<12 ? 2 : 1;
  const installed=[];
  for (const [key,app] of Object.entries(design.apps)) {
    if (!app.level || !programs[key]) continue;
    const [name,space,price]=programs[key];
    const units=space*app.level*factor;
    if (app.resident) cpu+=units;
    else storage+=units;
    cost+=price*app.level*factor;
    levels+=key==="emotion"?0:app.level;
    installed.push({key,name,...app,units});
  }
  return {cpu,storage,parallel,synaptic,minTL,requiredLogic,cost,levels,installed};
}

const synapticLimit = tl => [0,0,0,0,.1,.15,.25,.4,.6,.85,1][Math.min(18,tl)-8];
export function sizeBrain(design) {
  const need=brainRequirements(design);
  const limit=synapticLimit(design.tl);
  const total=Math.max(need.cpu,need.parallel+need.synaptic,limit ? Math.ceil(need.synaptic/limit) : 0);
  return {linear:total-need.parallel-need.synaptic,parallel:need.parallel,synaptic:need.synaptic,
    storage:Math.max(need.storage,need.levels*10),synStorage:0};
}

export function calculateRobot(design) {
  const rows=[], errors=[], warnings=[];
  const add=(name,liters=0,mass=0,power=0,cost=0)=>rows.push({name,liters,mass,power,cost});
  const {tl,size,frame,plant,move,units,legs,head,gear,weapon}=design;
  const shell=chassis[size];
  const [frameName,frameMass,frameCost,baseArmor]=frames[frame];
  const pseudo=frame===6, contoured=frame===5;
  let armor=baseArmor, armorFactor=1;
  if (design.armor!=="base") {
    if (!frame || (pseudo && design.armor==="combat")) errors.push("This frame cannot take the selected armor.");
    else if (pseudo && design.armor==="mesh") {
      armor="Mesh";armorFactor=2;
      if (tl<13) errors.push("Pseudo-biological mesh armor requires TL 13.");
    } else if (design.armor==="cloth") {
      armor="Cloth";armorFactor=pseudo?5:2.5;
      if (pseudo && tl<15) errors.push("Pseudo-biological cloth armor requires TL 15.");
    } else if (design.armor==="combat") {
      armor="Combat armor";armorFactor=10;
    } else if (design.armor==="mesh") armor="Mesh";
  }
  if (design.reflective && !frame) errors.push("Reflective finish requires a closed chassis.");
  const shellMass=shell.liters*.1*(pseudo?1:frameMass*armorFactor);
  const shellCost=shell.cost*(pseudo?1:frameCost*armorFactor)*(design.reflective?2:1);
  add(frameName+" chassis · "+armor,0,shellMass,0,shellCost);
  const headSpace=shell.liters*head/100;
  if (head && (head<5||head>40)) errors.push("A head must add between 5% and 40% of chassis volume.");
  if (head) add("Rotating head · "+head+"%",0,shellMass*head/100,headSpace*.2,shellCost*head/100);

  const [outputMultiplier,massMultiplier]=powerTier(tl);
  let output=0, fuelRate=0;
  if (plant>=0) {
    const [name,rawOutput,volume,mass,cost,burn]=plants[plant];
    output=rawOutput*outputMultiplier;
    fuelRate=burn*(design.sealed && plant<=6?9:1);
    add(name,volume,mass*massMultiplier,0,cost);
    add("Fuel on board",design.fuel,design.fuel*.07);
    if (!design.fuel) warnings.push("No fuel loaded for the selected plant.");
  } else if (design.fuel) errors.push("A battery-only robot cannot load fuel.");
  const batteryTier=Math.min(17,tl)-12;
  let energy=0;
  if (design.battery) {
    if (batteryTier<0) errors.push("The battery table starts at TL 12.");
    else {
      const [storage,cost]=batteries[batteryTier];
      energy=storage*design.battery;
      add("Batteries · "+energy+" kWh",design.battery,design.battery,0,cost*design.battery);
    }
  }
  if (plant<0 && !energy) errors.push("Install a fuel plant or batteries.");

  let thrust=0,dexterity=0;
  if (move>=4) {
    const [name,,draw,space,mass,rating,cost]=movement[move];
    thrust=rating*units;
    dexterity=move===4?10:15;
    add(name+" × "+units,space*units,mass*units,draw*units,cost*units);
    if (move===4 && space*units<shell.liters*.1) errors.push("Air cushion modules must occupy at least 10% of the chassis.");
  } else if (move>0) {
    const transmission=plant>=0?output:design.batteryDrive;
    const legged=move===1;
    const suspension=shell.liters*(legged?Math.min(legs*.05,.4):move===2?.15:.2);
    const [draw,space,mass]=[[.5,.5,1],[.3,.3,.5],[.4,.6,1.5]][move-1];
    dexterity=legged?tl-8:move;
    add(movement[move][0]+" suspension",suspension,suspension*(legged&&contoured?1.2:1),0,
      suspension*[35,12,25][move-1]*(legged&&contoured?2:1));
    add(transmission+" transmission units",legged?(frame>=5?transmission*space:0):transmission*space,
      transmission*mass*(legged&&contoured?1.2:1),transmission*draw,
      transmission*15*(legged&&contoured?2:1));
    if (plant<0) output=transmission;
  }
  let strength=Math.floor(shell.liters/20), handDex=0;
  for (const [kind,count] of [[design.arm,design.arms],[design.tentacle,design.tentacles]]) {
    if (!count) continue;
    const [name,draw,mass,cost,dex,str]=appendages[kind];
    add(name+" × "+count,0,mass*count*(contoured?1.2:1),draw*count,cost*count*(contoured?2:1));
    strength+=str*count;
    handDex+=dex*count;
  }
  add(pseudo?"Separate visual, audio and olfactory sensors":"Basic sensor package",pseudo?3.5:3,pseudo?3.5:3,4,pseudo?1800:1700);
  for (const key of gear) {
    const [name,draw,mass,cost]=equipment[key];
    const scale=key==="touch"?Math.max(1,size):1;
    add(name,mass*scale,mass*scale,draw*scale,cost*scale+(key==="touch"?1000:0));
  }
  if (gear.includes("aqua") && (frame<2 || frame>5)) errors.push("Aqua maneuvering requires a cylinder, sphere, dome, or contoured chassis.");
  if (weapon) {
    const [name,draw,mass,cost,law]=weapons[weapon];
    add(name,mass,mass,draw,cost);
    if (design.law>=law) warnings.push("The weapon may be restricted at this law level. Check local authorization.");
  }

  const need=brainRequirements(design);
  const cpu=design.linear+design.parallel+design.synaptic;
  const storage=design.storage+design.synStorage;
  const ratio=synapticLimit(tl);
  if (tl<need.minTL) errors.push("Selected logic requires TL "+need.minTL+".");
  if (design.command && design.logic<need.requiredLogic) errors.push("Command program requires "+logic[need.requiredLogic][0]+" logic or better.");
  if (design.parallel && tl<9) errors.push("Parallel processors require TL 9.");
  if (cpu<need.cpu || design.parallel<need.parallel || design.synaptic<need.synaptic) errors.push("CPU capacity or processor mix cannot run these programs.");
  if (storage<need.storage) errors.push("Storage is too small for the installed programs.");
  if (design.synaptic>Math.floor(cpu*ratio) || design.synStorage>Math.floor(storage*ratio)) {
    errors.push("Synaptic units exceed the "+Math.round(ratio*100)+"% reliability limit at TL "+tl+".");
  }
  if (!need.installed.some(app=>app.resident&&app.key!=="emotion")) errors.push("Keep a primary skill program resident in the CPU.");
  if (move>=5 && !need.installed.some(app=>app.key==="vehicle"&&app.resident)) errors.push("Grav locomotion requires CPU-resident Vehicle software (grav specialty).");
  if (weapon && !design.apps.weapon?.level) errors.push("The installed weapon requires Weapon Handling software.");
  if (design.apps.emotion?.level && (design.logic<2 || !design.apps.emotion.resident)) errors.push("Emotion simulation needs autonomous logic and CPU residency.");
  add("Linear CPU × "+design.linear,design.linear*.2,design.linear*.1,0,design.linear*500);
  add("Parallel CPU × "+design.parallel,design.parallel*.5,design.parallel*.1,0,design.parallel*10000);
  add("Synaptic CPU × "+design.synaptic,design.synaptic*.1,design.synaptic*.1,0,design.synaptic*50000);
  add("Storage",design.storage*.5+design.synStorage*.1,design.storage*.1+design.synStorage*.05,1,
    design.storage*250+design.synStorage*25000);
  add("Logic, command and application software",0,0,0,need.cost);
  if (design.circuit) {
    const protectedRows=rows.filter(row=>row.name!=="Fuel on board");
    add("Electronic circuit protection",0,protectedRows.reduce((sum,row)=>sum+row.mass,0)*.5,0,
      protectedRows.reduce((sum,row)=>sum+row.cost,0)*.5);
  }
  if (pseudo) {
    add("Pseudo-biological finish and armor",0,rows.filter(row=>row.name!=="Fuel on board").reduce((sum,row)=>sum+row.mass,0)*
      (frameMass*armorFactor-1),0,rows.reduce((sum,row)=>sum+row.cost,0)*(frameCost*armorFactor-1));
  }
  const used=rows.reduce((sum,row)=>sum+row.liters,0), capacity=shell.liters+headSpace;
  const dry=rows.filter(row=>row.name!=="Fuel on board").reduce((sum,row)=>sum+row.mass,0);
  const mass=dry+(plant>=0?design.fuel*.07:0)+design.payload;
  const draw=rows.reduce((sum,row)=>sum+row.power,0);
  const price=Math.round(rows.reduce((sum,row)=>sum+row.cost,0));
  if (used>capacity+1e-8) errors.push("Components and fuel exceed available chassis volume.");
  if ((plant>=0||output>0) && draw>output+1e-8) errors.push("Power demand exceeds rated output.");
  if (thrust && mass>=thrust) errors.push("Loaded mass must be below suspension thrust.");
  if (design.budget && price>design.budget) errors.push("Design exceeds the specified budget.");
  const intelligence=Math.max(0,Math.min(tl-3,Number(design.linear>=20)+Math.min(10,Math.floor(design.parallel/5))+
    Math.floor(design.synaptic/2)+commands[design.command][5]+tl-12));
  const education=Math.floor(storage/10);
  if (need.levels>intelligence+education) errors.push("Combined skill levels exceed apparent intelligence plus education.");
  dexterity=move>=4?dexterity:Math.min(15,dexterity+handDex+logic[design.logic][7]);
  const urp=[digits[size]+digits[frame]+(plant<0?"N":digits[plant])+(energy?digits[batteryTier]:"N")+
    (move===1?digits[legs]:movement[move][1]),(head?encode(head):"N")+encode(design.arms+design.tentacles),
    encode(cpu)+encode(storage)+digits[design.logic]+digits[design.command]+encode(need.installed.length),
    [strength,dexterity,intelligence,education].map(encode).join("")].join("-");
  return {valid:!errors.length,errors,warnings,rows,used,capacity,dry,mass,draw,output,thrust,price,urp,need,cpu,storage,
    strength,dexterity,intelligence,education,educationPotential:education+design.synStorage,energy,
    fuelHours:fuelRate?design.fuel/fuelRate:0,batteryHours:energy/draw,
    maneuver:move>=5&&mass?Math.max(0,thrust/mass-1):0,maintenance:Math.round(price*.01)};
}
