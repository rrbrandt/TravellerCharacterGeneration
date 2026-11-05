import { parseProfile } from "../book6/survey-rules.js";

export const VERSION = "1.0";
const die = () => 1 + Math.floor(Math.random() * 6);
const marketMatches = {
  Ag:"De In Lo Po Wa", As:"As In Na Ri Va", De:"De Na", Fl:"Fl In", Hi:"Hi In Lo Ri",
  Ic:"In", In:"Ag As De Fl Hi Ic In Lo Na Ni Po Ri Va Wa", Lo:"In Ri", Na:"As De Na Va",
  Ni:"In", Po:"In", Ri:"Ag De Hi In Na Ri Wa", Va:"As In Na Va", Wa:"In Wa",
};
const costModifier = {Ag:-1,As:-1,Ba:1,De:1,Fl:1,Hi:-1,In:-1,Lo:1,Ni:1,Po:-1,Ri:1,Va:1};
const portCost = {A:-1000,B:0,C:1000,D:2000,E:3000,X:5000};
export const brokerMaximum = {A:4,B:3,C:2,D:1,E:0,X:0};
const actualValue = [40,50,70,80,90,100,110,120,130,150,170,200,300,400];

export function marketWorld(name, uwp) {
  const world = {...parseProfile(uwp),name:name.trim() || uwp.trim().toUpperCase()};
  const {sizeCode,atmosphere:a,water:h,population:p,government:g,law:l} = world;
  const flags = {
    Ag:a>=4&&a<=9&&h>=4&&h<=8&&p>=5&&p<=7,
    As:sizeCode==="0", Ba:p===0&&g===0&&l===0, De:a>=2&&h===0,
    Fl:a>=10&&h>=1, Hi:p>=9, Ic:a<=1&&h>=1, In:[0,1,2,4,7,9].includes(a)&&p>=9,
    Lo:p<=3, Na:a<=3&&h<=3&&p>=6, Ni:p<=6, Po:a>=2&&a<=5&&h<=3,
    Ri:[6,8].includes(a)&&p>=6&&p<=8&&g>=4&&g<=9, Va:a===0, Wa:h===10,
  };
  world.codes = Object.keys(flags).filter(code => flags[code]);
  return world;
}

export function quoteTrade(source, market, accelerate = 0) {
  const baseCost = 4000 + 100 * source.tech + portCost[source.port] +
    1000 * source.codes.reduce((sum, code) => sum + (costModifier[code] || 0), 0);
  const codes = world => world.codes.filter(code => !(code === "Va" && world.codes.includes("As")));
  const pairs = [];
  for (const from of codes(source)) {
    for (const to of codes(market)) {
      if (marketMatches[from]?.split(" ").includes(to)) pairs.push({from,to,modifier:1});
      if ((from === "Ni" && to === "Ni") || (from === "Po" && to === "Po")) pairs.push({from,to,modifier:-1});
    }
  }
  const tradeModifier = pairs.reduce((sum,pair) => sum + pair.modifier * 1000,0);
  const techDifference = source.tech - market.tech;
  const marketPrice = market.codes.includes("Ba") ? 0 : Math.max(0,Math.round((5000 + tradeModifier) * (10 + techDifference) / 10));
  return {baseCost,purchasePrice:Math.round(baseCost * (10 + accelerate) / 10),tradeModifier,techDifference,marketPrice,pairs};
}

export function rollFreight(source, market, zone, liaison = 0, roll = die) {
  if (!source.population || zone === "red") return [];
  const index = Math.min(10,source.population);
  const destinationDm = (market.population <= 4 ? -3 : market.population >= 8 ? 1 : 0) + source.tech - market.tech;
  const groups = [
    {kind:"Major", dm:[0,-4,-2,-1,0,1,2,3,4,5,6][index], size:10, allowed:zone !== "amber"},
    {kind:"Minor", dm:[0,-4,-1,0,1,2,3,4,5,6,7][index] + liaison, size:5, allowed:true},
    {kind:"Incidental", dm:[0,0,0,0,0,0,-3,-3,-2,-2,0][index], size:1, allowed:index>=6},
  ];
  const lots = [];
  for (const group of groups) {
    if (!group.allowed) continue;
    const count = Math.max(0,roll() + destinationDm + group.dm);
    for (let index=0; index<count; index++) {
      const tons = roll() * group.size;
      lots.push({id:lots.length+1,kind:group.kind,tons,fee:tons*1000});
    }
  }
  return lots;
}

export function settleVoyage({source,market,quote,freight,hold,tons,broker,expenses},roll = die) {
  if (tons < 0 || !Number.isInteger(tons)) throw new Error("Enter a whole number of speculative tons.");
  const freightTons = freight.reduce((sum,lot) => sum + lot.tons,0);
  if (freightTons + tons > hold) throw new Error("Cargo exceeds available hold capacity.");
  if (broker > brokerMaximum[market.port]) throw new Error("That broker level is unavailable at the market starport.");
  if (tons && !quote.marketPrice) throw new Error("This market cannot buy the speculative goods.");
  const dice = tons ? [roll(),roll()] : [];
  const result = dice.reduce((sum,face) => sum + face,broker);
  const percentage = tons ? actualValue[Math.max(2,Math.min(15,result))-2] : 0;
  const purchase = tons * quote.purchasePrice;
  const sales = Math.round(tons * quote.marketPrice * percentage / 100);
  const commission = Math.round(sales * broker * .05);
  const freightRevenue = freight.reduce((sum,lot) => sum + lot.fee,0);
  return {version:VERSION,schemaVersion:1,ruleset:"CT-MERCHANT-1985-TRADE",source,market,quote,freight,freightTons,
    hold,tons,broker,expenses,dice,result,percentage,purchase,sales,commission,freightRevenue,
    net:sales + freightRevenue - purchase - commission - expenses};
}
