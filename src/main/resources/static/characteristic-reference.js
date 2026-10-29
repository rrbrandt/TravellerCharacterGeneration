(() => {
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
        if (key === 'socialStanding' && score >= 11) text += ' SOC 11+: noble standing in the standard Imperial setting; titles depend on the campaign.';
      }
      row.querySelector('.reference-meaning').textContent = text;
    }
  }
  document.querySelector('.character-edit-row')?.addEventListener('input', update);
  update();
})();
