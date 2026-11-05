export const byId = id => document.getElementById(id);
export const credits = value => "Cr " + value.toLocaleString();

export function showList(element, values) {
  element.replaceChildren(...(values.length ? values : ["None"]).map(value => {
    const item = document.createElement("li");
    item.textContent = value;
    return item;
  }));
}

export function archiveDesk(key, ruleset, open) {
  let records = [];
  try {
    const stored = JSON.parse(localStorage.getItem(key) || "[]");
    if (Array.isArray(stored)) records = stored.filter(record => record?.ruleset === ruleset && record.schemaVersion === 1);
  } catch {
    // Downloads remain available when storage is blocked.
  }
  function display() {
    const list = byId("saved");
    list.replaceChildren();
    if (!records.length) showList(list,["No records saved in this browser."]);
    for (const record of records) {
      const li = document.createElement("li");
      const button = document.createElement("button");
      button.className = "secondary";
      button.textContent = record.name;
      button.addEventListener("click", () => open(record));
      li.append(button);
      list.append(li);
    }
  }
  display();
  return record => {
    const saved = {...record,id:record.id || crypto.randomUUID()};
    const next = [saved,...records.filter(entry => entry.id !== saved.id)];
    localStorage.setItem(key,JSON.stringify(next));
    records = next;
    display();
    return saved;
  };
}

export function downloadRecord(record,filename) {
  const url = URL.createObjectURL(new Blob([JSON.stringify(record,null,2)],{type:"application/json"}));
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  link.click();
  setTimeout(() => URL.revokeObjectURL(url),1000);
}
