(() => {
  const nobleTitles = {11:'Knight / Dame',12:'Baron / Baroness',13:'Marquis / Marchioness',14:'Count / Countess',15:'Duke / Duchess'};
  const meanings = {
    strength: ['physical strength', 'Physical power and muscular force.'],
    dexterity: ['coordination', 'Agility, balance and fine control.'],
    endurance: ['stamina', 'Resilience and staying power.'],
    intelligence: ['reasoning ability', 'Understanding and problem solving.'],
    education: ['education', 'Accumulated knowledge and learning.'],
    socialStanding: ['social standing', 'Position in society and social influence.']
  };
  function update() {
    for (const row of document.querySelectorAll('[data-reference]')) {
      const key = row.dataset.reference;
      const input = document.querySelector(`[name="characteristics.${key}"]`);
      const raw = input?.value.trim() || '';
      const score = Number(raw);
      const valid = raw !== '' && Number.isInteger(score) && score >= 1 && score <= 15;
      row.querySelector('.reference-score').textContent = valid ? `${score}${score >= 10 ? ' / ' + score.toString(16).toUpperCase() : ''}` : '—';
      let text;
      if (!valid) text = 'Enter a whole-number score from 1 to 15 to see its meaning.';
      else {
        const band = score <= 2 ? 'Very low' : score <= 5 ? 'Below-average' : score <= 8 ? 'Typical' : score <= 10 ? 'Above-average' : score <= 12 ? 'Exceptional' : 'Extraordinary';
        text = `${band} ${meanings[key][0]}. ${meanings[key][1]}`;
        if (key === 'education') text += score >= 8 ? ' EDU 8+: eligible for the advanced education 8+ skill table.' : ' EDU below 8: the advanced education 8+ skill table is unavailable.';
        if (key === 'socialStanding') {
          text += score >= 11 ? ` Imperial noble standing: ${nobleTitles[score]}.` : ' No noble title from this Social Standing score.';
        }
      }
      row.querySelector('.reference-meaning').textContent = text;
      if (key === 'socialStanding') {
        for (const titleRow of document.querySelectorAll('[data-noble-score]')) {
          const selected = valid && (score <= 10 ? titleRow.dataset.nobleScore === 'none' : Number(titleRow.dataset.nobleScore) === score);
          titleRow.classList.toggle('current-title', selected);
          if (selected) titleRow.setAttribute('aria-current','true'); else titleRow.removeAttribute('aria-current');
        }
      }
    }
  }
  document.querySelector('.character-edit-row')?.addEventListener('input', update);
  update();
})();
