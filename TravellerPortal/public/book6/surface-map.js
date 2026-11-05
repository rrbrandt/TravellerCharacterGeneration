import { randomStream } from "./survey-rules.js";

export const terrain = {
  Ocean: "#327999", Ice: "#c5dde2", Highlands: "#9b826d",
  Lowlands: "#7d9b71", Woodland: "#4a795e", Arid: "#b99a63", Barren: "#887d75",
};
const side = 150, height = side * Math.sqrt(3) / 2, resolution = 9, margin = 14;
const radius = side / resolution / Math.sqrt(3);
export const mapViewBox = `0 0 ${side * 5.5 + margin * 2} ${height * 3 + margin * 2}`;
const north = [0,0,1], south = [0,0,-1];
const z = 1 / Math.sqrt(5), ring = 2 / Math.sqrt(5);
const vertex = (index, lower) => {
  const angle = (index + (lower ? .5 : 0)) * Math.PI * 2 / 5;
  return [ring * Math.cos(angle), ring * Math.sin(angle), lower ? -z : z];
};
const hex = Array.from({length:6}, (_, index) => {
  const angle = (index * 60 + 30) * Math.PI / 180;
  return [radius * Math.cos(angle), radius * Math.sin(angle)];
});
const unit = point => {
  const length = Math.hypot(...point);
  return point.map(value => value / length);
};
const dot = (a,b) => a.reduce((sum,value,index) => sum + value * b[index],0);
const pointString = points => points.map(point => point.map(value => value.toFixed(3)).join(",")).join(" ");

export function surfaceFor(body, seed) {
  const random = randomStream(seed + "|" + body.id + "|surface-v1");
  const waves = Array.from({length:14}, (_, index) => ({
    direction: unit(Array.from({length:3}, () => random() - .5)),
    phase: random() * Math.PI * 2,
    frequency: 2.7 + index * 1.3,
    amplitude: 1 / (1 + index * .43),
  }));
  const unique = new Map();
  const faces = [];
  function face(vertices, layout) {
    const tiles = [];
    const corners = layout.map(([x,y]) => [margin + x * side, margin + y * height]);
    for (let row = 0; row <= resolution; row++) {
      for (let column = 0; column <= resolution - row; column++) {
        const weight = [resolution - row - column,column,row].map(value => value / resolution);
        const position = unit([0,1,2].map(axis => vertices.reduce((sum,v,index) => sum + v[axis] * weight[index],0)));
        const key = position.map(value => Math.round(value * 1e7)).join(",");
        let cell = unique.get(key);
        if (!cell) {
          const elevation = waves.reduce((sum,wave) => sum + Math.sin(dot(wave.direction,position) * wave.frequency +
            wave.phase) * wave.amplitude,0);
          cell = {key, elevation, latitude:position[2], moisture:Math.sin(dot(waves[4].direction,position) * 8)};
          unique.set(key,cell);
        }
        const [x,y] = [0,1].map(axis => corners.reduce((sum,v,index) => sum + v[axis] * weight[index],0));
        tiles.push({cell,x,y,points:pointString(hex.map(([dx,dy]) => [x+dx,y+dy]))});
      }
    }
    faces.push({points:pointString(corners),tiles});
  }
  for (let index = 0; index < 5; index++) {
    const next = (index + 1) % 5;
    const upper = vertex(index,false), upperNext = vertex(next,false);
    const lower = vertex(index,true), lowerNext = vertex(next,true);
    face([upper,upperNext,north],[[index,1],[index+1,1],[index+.5,0]]);
    face([upper,upperNext,lower],[[index,1],[index+1,1],[index+.5,2]]);
    face([lower,lowerNext,upperNext],[[index+.5,2],[index+1.5,2],[index+1,1]]);
    face([lower,lowerNext,south],[[index+.5,2],[index+1.5,2],[index+1,3]]);
  }
  const cells = [...unique.values()].sort((a,b) => a.elevation - b.elevation);
  const wetCount = Math.round(cells.length * body.water / 10);
  for (const [index,cell] of cells.entries()) {
    const polar = Math.abs(cell.latitude);
    const wet = index < wetCount;
    const frozen = body.zone === "Outer" || (body.zone !== "Inner" && polar > .82);
    cell.terrain = wet ? (frozen ? "Ice" : "Ocean") :
      body.atmosphere <= 1 || body.atmosphere >= 10 ? "Barren" :
      frozen && polar > .58 ? "Ice" :
      index > Math.max(wetCount, cells.length * .86) ? "Highlands" :
      body.zone === "Inner" || body.water <= 2 ? "Arid" : cell.moisture > .1 ? "Woodland" : "Lowlands";
  }
  const land = cells.filter(cell => cell.terrain !== "Ocean" && cell.terrain !== "Ice");
  const available = [...(land.length ? land : cells)];
  const sites = [];
  const add = (type,name) => {
    const [cell] = available.splice(Math.floor(random() * available.length),1);
    sites.push({key:cell.key, type, name});
  };
  if (body.port && !["X","Y"].includes(body.port)) {
    add(body.main ? "Starport" : "Spaceport", (body.main ? "Starport " : "Spaceport ") + body.port);
  }
  const stems = ["Meridian","Northwatch","Haven","Pioneer","Caldera","Farpoint","Aster","New Dawn"];
  const cities = body.population >= 5 ? Math.min(8, body.population - 3) : Number(body.population > 0);
  for (let index = 0; index < cities; index++) {
    const name = stems.splice(Math.floor(random() * stems.length),1)[0];
    add(body.population >= 5 ? "City" : "Settlement", name);
  }
  for (const facility of body.facilities) add("Outpost", facility);
  return {faces, cells, sites};
}
