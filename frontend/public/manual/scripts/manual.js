/* Manual de usuario del frontend de ProyecTWIN
   Renderizador, navegación, búsqueda, filtros, progreso y recorridos. */
(function () {
  'use strict';

  const PARTS = window.MANUAL_PARTS || [];
  const CHAPTERS = window.MANUAL_CHAPTERS || [];
  const GLOSSARY = window.MANUAL_GLOSSARY || [];

  const $ = (sel, root = document) => root.querySelector(sel);
  const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));
  const norm = (txt) => String(txt || '')
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '');

  const chaptersRoot = $('#chapters');
  const navRoot = $('#side-nav');
  const searchInput = $('#search');
  const searchResults = $('#search-results');
  const progressBar = $('#progress-bar');
  const progressLabel = $('#progress-label');
  const crumbs = $('#crumbs');
  const tourModal = $('#tour-modal');
  const tourDialog = $('.tour-dialog', tourModal);
  const tourImage = $('#tour-image');
  const tourStage = $('#tour-stage');
  const tourViewport = $('#tour-viewport');
  const tourCard = $('#tour-card');
  const tourPins = $('#tour-pins');
  const tourDots = $('#tour-dots');
  const tourSpotlight = $('#tour-spotlight');
  const tourText = $('#tour-text');
  const tourTitle = $('#tour-title');
  const tourCount = $('#tour-count');
  const tourCardStep = $('#tour-card-step');
  const tourPrev = $('#tour-prev');
  const tourNext = $('#tour-next');
  const lightbox = $('#lightbox');
  const lightboxImg = $('#lightbox-img');
  const glossaryModal = $('#glossary-modal');
  const partById = Object.fromEntries(PARTS.map((p) => [p.id, p]));
  const readKey = 'proyectwin-manual-read-v1';
  const themeKey = 'proyectwin-manual-theme';
  let currentRole = 'todos';
  let currentQuery = '';
  let readSet = new Set();
  let activeTour = null;

  try {
    readSet = new Set(JSON.parse(localStorage.getItem(readKey) || '[]'));
  } catch {
    readSet = new Set();
  }

  function figureHtml(fig, chapter, index) {
    if (!fig) return '';
    const chapterId = chapter.id;
    const figPrefix = String(chapter.num || '').replace(/[^0-9]/g, '') || '0';
    const pins = (fig.pins || []).map((pin) => `
      <button type="button" class="pin" style="left:${pin.left}%;top:${pin.top}%"
        data-n="${pin.n}" data-title="${escapeAttr(pin.title)}" data-text="${escapeAttr(pin.text)}"
        aria-label="Punto ${pin.n}: ${escapeAttr(pin.title)}">${pin.n}</button>`).join('');
    const legend = (fig.pins || []).map((pin) => `
      <li data-n="${pin.n}"><span class="pin-num">${pin.n}</span><span><b>${pin.title}.</b> ${pin.text}</span></li>`).join('');
    return `
      <figure class="shot" id="fig-${chapterId}-${index}" data-chapter="${chapterId}" data-fig="${index}">
        <div class="shot-frame">
          <img src="${fig.src}" alt="${escapeAttr(fig.alt || fig.caption || '')}">
          ${pins}
          <button type="button" class="shot-zoom" aria-label="Ampliar imagen">⤢</button>
        </div>
        <figcaption><span class="fig-label">Figura ${figPrefix}.${index + 1}.</span> ${fig.caption || ''}</figcaption>
        ${legend ? `<ol class="pin-legend">${legend}</ol>` : ''}
        ${pins ? `<button type="button" class="tour-btn"><span aria-hidden="true">▶</span> Recorrer esta pantalla</button>` : ''}
      </figure>`;
  }

  function escapeAttr(txt) {
    return String(txt || '').replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;');
  }

  function tableHtml(table) {
    if (!table) return '';
    const head = (table.head || []).map((h) => `<th>${h}</th>`).join('');
    const rows = (table.rows || []).map((row) => `<tr>${row.map((cell) => `<td>${cell}</td>`).join('')}</tr>`).join('');
    return `<div class="table-wrap"><table><thead><tr>${head}</tr></thead><tbody>${rows}</tbody></table></div>`;
  }

  function chapterHtml(chapter) {
    const part = partById[chapter.part] || { title: '' };
    const figures = (chapter.figures || []).map((fig, i) => figureHtml(fig, chapter, i)).join('');
    const fields = chapter.fields ? `<h3>Campos y validaciones</h3>${tableHtml(chapter.fields)}` : '';
    const extra = (chapter.extra || []).map((block) => `<h3>${block.title}</h3>${block.html}`).join('');
    const related = (chapter.related || []).length
      ? `<div class="related">${chapter.related.map((r) => `<a href="#${r.id}">${r.label}</a>`).join('')}</div>`
      : '';
    const relatedBlock = related ? `<h3>Ver también</h3>${related}` : '';
    const errorsBlock = chapter.errors && chapter.errors.length
      ? `<h3>Si algo sale mal</h3><div class="callout danger"><div class="callout-head"><span class="callout-icon" aria-hidden="true">⛔</span><span>Qué hacer</span></div><div class="callout-body"><ul>${chapter.errors.map((x) => `<li>${x}</li>`).join('')}</ul></div></div>`
      : '';
    const tail = errorsBlock && relatedBlock
      ? `<div class="chapter-tail">${errorsBlock}${relatedBlock}</div>`
      : `${errorsBlock}${relatedBlock}`;
    const prev = CHAPTERS[CHAPTERS.indexOf(chapter) - 1];
    const next = CHAPTERS[CHAPTERS.indexOf(chapter) + 1];

    return `
      <article class="chapter" id="${chapter.id}" data-id="${chapter.id}" data-roles="${(chapter.roles || ['todos']).join(' ')}" data-part="${chapter.part}">
        <header class="chapter-head">
          <div>
            <p class="chapter-kicker">${part.title || chapter.kicker || ''}</p>
            <p class="chapter-num">${chapter.num || 'Introducción'}</p>
            <h1>${chapter.title}</h1>
          </div>
          <button type="button" class="mark-read" data-read="${chapter.id}"><span class="mark-read-icon">○</span> Marcar como leída</button>
        </header>
        <div class="chapter-body">
          ${chapter.purpose ? `<h3>Para qué sirve</h3><p>${chapter.purpose}</p>` : ''}
          ${chapter.route ? `<h3>Cómo llegar</h3><p>${chapter.route}</p>` : ''}
          ${chapter.whatYouSee ? `<h3>Qué ves</h3><ul>${chapter.whatYouSee.map((x) => `<li>${x}</li>`).join('')}</ul>` : ''}
          ${figures}
          ${fields}
          ${chapter.steps ? `<h3>Paso a paso</h3><ol>${chapter.steps.map((x) => `<li>${x}</li>`).join('')}</ol>` : ''}
          ${chapter.after ? `<h3>Qué pasa después</h3><ul>${chapter.after.map((x) => `<li>${x}</li>`).join('')}</ul>` : ''}
          ${chapter.tip ? `<div class="callout tip"><div class="callout-head"><span class="callout-icon" aria-hidden="true">💡</span><span>Consejo</span></div><div class="callout-body"><p>${chapter.tip}</p></div></div>` : ''}
          ${extra}
          ${tail}
        </div>
        <footer class="chapter-foot">
          <div class="chapter-progress"><span>Avance del manual</span><div class="progress"><i></i></div></div>
          <nav class="chapter-pager" aria-label="Navegación entre capítulos">
            ${prev ? `<a href="#${prev.id}">← ${prev.title}</a>` : ''}
            ${next ? `<a href="#${next.id}">${next.title} →</a>` : ''}
          </nav>
        </footer>
      </article>`;
  }

  function render() {
    $('#stat-chapters').textContent = String(CHAPTERS.length);
    const navParts = PARTS.map((part) => {
      const items = CHAPTERS.filter((c) => c.part === part.id);
      return `<section class="nav-part" data-part="${part.id}">
        <button type="button" class="nav-part-head" aria-expanded="true"><span>${part.title}</span><span class="nav-caret">▾</span></button>
        <ul class="nav-list">${items.map((c) => `
          <li class="nav-chapter" data-roles="${(c.roles || ['todos']).join(' ')}">
            <a href="#${c.id}" data-target="${c.id}"><span class="nav-num" aria-hidden="true">${c.num === 'Introducción' ? '' : (c.num || '')}</span><span class="nav-title">${c.title}</span><span class="nav-check" aria-hidden="true">✓</span></a>
          </li>`).join('')}</ul>
      </section>`;
    }).join('');
    navRoot.innerHTML = navParts;

    chaptersRoot.innerHTML = PARTS.map((part) => {
      const items = CHAPTERS.filter((c) => c.part === part.id);
      return `<section class="part-block" data-part="${part.id}">
        <div class="part-divider"><span class="part-eyebrow">${part.eyebrow || ''}</span><h2>${part.title}</h2><p>${part.desc || ''}</p></div>
        ${items.map(chapterHtml).join('')}
      </section>`;
    }).join('');

    const toc = $('#print-toc');
    if (toc) {
      toc.innerHTML = `<h2>Contenido</h2>${PARTS.map((part) => {
        const items = CHAPTERS.filter((c) => c.part === part.id);
        return `<h3>${part.title}</h3><ol>${items.map((c) => `<li><span class="toc-num">${c.num || '·'}</span><a class="toc-title" href="#${c.id}">${c.title}</a></li>`).join('')}</ol>`;
      }).join('')}`;
    }

    bindUi();
    applyReadState();
    refreshVisibility();
  }

  function bindUi() {
    $$('.nav-part-head').forEach((btn) => {
      btn.addEventListener('click', () => {
        const part = btn.closest('.nav-part');
        part.classList.toggle('collapsed');
        btn.setAttribute('aria-expanded', String(!part.classList.contains('collapsed')));
      });
    });

    $$('.mark-read').forEach((btn) => {
      btn.addEventListener('click', () => {
        const id = btn.dataset.read;
        if (readSet.has(id)) readSet.delete(id); else readSet.add(id);
        localStorage.setItem(readKey, JSON.stringify(Array.from(readSet)));
        applyReadState();
      });
    });

    $$('.shot-frame img, .shot-zoom').forEach((el) => {
      el.addEventListener('click', () => {
        const fig = el.closest('.shot');
        const img = fig ? fig.querySelector('img') : null;
        if (!img) return;
        lightboxImg.src = img.src;
        lightboxImg.alt = img.alt;
        lightbox.hidden = false;
        document.body.style.overflow = 'hidden';
      });
    });

    $$('.pin').forEach((pin) => pin.addEventListener('click', (event) => {
      event.stopPropagation();
      openPin(pin);
    }));

    $$('.tour-btn').forEach((btn) => btn.addEventListener('click', () => startTour(btn.closest('.shot'))));

    $$('.chapter').forEach((chapter) => {
      chapter.addEventListener('click', (event) => {
        const link = event.target.closest('a[href^="#"]');
        if (!link) return;
        const id = link.getAttribute('href').slice(1);
        const target = document.getElementById(id);
        if (!target) return;
        event.preventDefault();
        document.getElementById(id).scrollIntoView({ behavior: 'smooth', block: 'start' });
      });
    });
  }

  function openPin(pin) {
    $$('.pin-popover').forEach((p) => p.remove());
    const frame = pin.closest('.shot-frame');
    const shot = pin.closest('.shot');
    if (!frame || !shot) return;
    const pop = document.createElement('div');
    pop.className = 'pin-popover';
    pop.innerHTML = `<b>${pin.dataset.title}</b><span>${pin.dataset.text}</span><br><button type="button">Cerrar</button>`;
    const left = Math.min(68, Math.max(2, parseFloat(pin.style.left) || 10));
    const top = Math.min(82, Math.max(2, parseFloat(pin.style.top) || 10));
    pop.style.left = left + '%';
    pop.style.top = top + '%';
    pop.querySelector('button').addEventListener('click', () => pop.remove());
    frame.appendChild(pop);
    pin.classList.add('active');
    setTimeout(() => pin.classList.remove('active'), 1200);
  }

  function startTour(figure) {
    const pinEls = $$('.pin', figure);
    const image = $('.shot-frame img', figure);
    if (!pinEls.length || !image) return;

    const pins = pinEls.map((pin) => ({
      n: pin.dataset.n || '',
      title: pin.dataset.title || '',
      text: pin.dataset.text || '',
      left: parseFloat(pin.style.left) || 50,
      top: parseFloat(pin.style.top) || 50,
    }));
    const caption = $('figcaption', figure)?.textContent.trim() || 'Recorrido por la pantalla';

    activeTour = { pins, index: 0, returnFocus: document.activeElement };
    $('#tour-caption').textContent = caption;
    tourPins.innerHTML = pins.map((pin, i) => `
      <button type="button" class="tour-pin" data-index="${i}" style="left:${pin.left}%;top:${pin.top}%"
        aria-label="Paso ${i + 1}: ${escapeAttr(pin.title)}">${pin.n}</button>`).join('');
    tourDots.innerHTML = pins.map((pin, i) => `
      <button type="button" class="tour-dot" data-index="${i}" aria-label="Ir al paso ${i + 1}"></button>`).join('');
    $$('.tour-pin', tourPins).forEach((btn) => btn.addEventListener('click', () => jumpTour(Number(btn.dataset.index))));
    $$('.tour-dot', tourDots).forEach((btn) => btn.addEventListener('click', () => jumpTour(Number(btn.dataset.index))));

    tourModal.hidden = false;
    document.body.classList.add('tour-open');
    document.body.style.overflow = 'hidden';
    tourImage.alt = image.alt || '';
    tourImage.onload = () => {
      if (!activeTour) return;
      showTourStep();
      positionTourCard();
    };
    tourImage.src = image.src;

    showTourStep();
    if (tourImage.complete) positionTourCard();
    $('#tour-close').focus();
  }

  function jumpTour(index) {
    if (!activeTour) return;
    activeTour.index = Math.max(0, Math.min(activeTour.pins.length - 1, index));
    showTourStep();
  }

  function nextTourStep() {
    if (!activeTour) return;
    if (activeTour.index >= activeTour.pins.length - 1) closeTour();
    else jumpTour(activeTour.index + 1);
  }

  function prevTourStep() {
    if (activeTour) jumpTour(activeTour.index - 1);
  }

  function showTourStep() {
    if (!activeTour) return;
    const { pins, index } = activeTour;
    const pin = pins[index];

    tourTitle.textContent = pin.title;
    tourText.textContent = pin.text;
    tourCardStep.textContent = `Paso ${index + 1} de ${pins.length}`;
    tourCount.textContent = `${index + 1} / ${pins.length}`;
    tourPrev.disabled = index === 0;
    tourNext.textContent = index === pins.length - 1 ? 'Terminar ✓' : 'Siguiente →';

    $$('.tour-pin', tourPins).forEach((btn, i) => btn.classList.toggle('active', i === index));
    $$('.tour-dot', tourDots).forEach((btn, i) => {
      btn.classList.toggle('active', i === index);
      if (i === index) btn.setAttribute('aria-current', 'step');
      else btn.removeAttribute('aria-current');
    });

    tourSpotlight.style.left = `${pin.left}%`;
    tourSpotlight.style.top = `${pin.top}%`;
    requestAnimationFrame(positionTourCard);
  }

  function positionTourCard() {
    if (!activeTour) return;
    if (window.matchMedia('(max-width: 720px)').matches) {
      tourCard.style.left = '';
      tourCard.style.top = '';
      tourCard.classList.remove('arrow-left', 'arrow-right');
      return;
    }

    const viewRect = tourViewport.getBoundingClientRect();
    const stageRect = tourStage.getBoundingClientRect();
    if (!stageRect.width) return;

    const pin = activeTour.pins[activeTour.index];
    const pinX = stageRect.left - viewRect.left + (stageRect.width * pin.left) / 100;
    const pinY = stageRect.top - viewRect.top + (stageRect.height * pin.top) / 100;
    const gap = 54;
    const cardW = tourCard.offsetWidth;
    const cardH = tourCard.offsetHeight;

    let side = pinX < viewRect.width / 2 ? 'right' : 'left';
    if (side === 'right' && pinX + gap + cardW > viewRect.width - 10) side = 'left';
    if (side === 'left' && pinX - gap - cardW < 10) side = 'right';

    let x = side === 'right' ? pinX + gap : pinX - gap - cardW;
    x = Math.max(10, Math.min(viewRect.width - cardW - 10, x));
    let y = pinY - cardH / 2;
    y = Math.max(10, Math.min(viewRect.height - cardH - 10, y));

    tourCard.style.left = `${Math.round(x)}px`;
    tourCard.style.top = `${Math.round(y)}px`;
    tourCard.classList.toggle('arrow-left', side === 'right');
    tourCard.classList.toggle('arrow-right', side === 'left');
    tourCard.style.setProperty('--arrow-top', `${Math.round(Math.max(20, Math.min(cardH - 20, pinY - y)))}px`);
  }

  function closeTour() {
    if (!activeTour) return;
    const returnFocus = activeTour.returnFocus;
    activeTour = null;
    tourModal.hidden = true;
    document.body.classList.remove('tour-open');
    document.body.style.overflow = '';
    if (returnFocus && typeof returnFocus.focus === 'function' && document.contains(returnFocus)) {
      returnFocus.focus();
    }
  }

  function applyReadState() {
    $$('.chapter[data-id]').forEach((chapter) => {
      const done = readSet.has(chapter.dataset.id);
      chapter.classList.toggle('done', done);
      const btn = chapter.querySelector('.mark-read');
      if (btn) btn.innerHTML = `<span class="mark-read-icon">${done ? '✓' : '○'}</span> ${done ? 'Leída' : 'Marcar como leída'}`;
      const nav = document.querySelector(`.nav-chapter a[data-target="${chapter.dataset.id}"]`);
      if (nav) nav.closest('.nav-chapter').classList.toggle('done', done);
    });
    const pct = CHAPTERS.length ? Math.round((readSet.size / CHAPTERS.length) * 100) : 0;
    progressBar.style.width = pct + '%';
    progressLabel.textContent = pct + ' % leído';
  }

  function refreshVisibility() {
    const query = norm(currentQuery);
    $$('.chapter[data-id]').forEach((chapter) => {
      const roles = chapter.dataset.roles.split(' ');
      const roleOk = currentRole === 'todos' || roles.includes('todos') || roles.includes(currentRole);
      const textOk = !query || norm(chapter.innerText).includes(query);
      chapter.hidden = !(roleOk && textOk);
      chapter.querySelectorAll('.chapter-progress .progress i').forEach((bar) => {
        bar.style.width = (roleOk ? 100 : 0) + '%';
      });
    });
    $$('.part-block').forEach((block) => {
      const visible = $$('.chapter[data-id]:not([hidden])', block).length;
      block.hidden = visible === 0;
    });
    $$('.nav-chapter').forEach((item) => {
      const roles = item.dataset.roles.split(' ');
      item.hidden = !(currentRole === 'todos' || roles.includes('todos') || roles.includes(currentRole));
    });
  }

  function search(q) {
    currentQuery = q.trim();
    const query = norm(currentQuery);
    if (!query) {
      searchResults.hidden = true;
      refreshVisibility();
      return;
    }
    refreshVisibility();
    const matches = CHAPTERS.filter((c) => norm(document.getElementById(c.id)?.innerText || '').includes(query));
    searchResults.innerHTML = matches.length
      ? matches.slice(0, 12).map((c) => `<a href="#${c.id}"><b>${c.title}</b><small>${partById[c.part]?.title || ''}</small></a>`).join('')
      : '<div class="empty-search">No se encontraron capítulos con esa búsqueda.</div>';
    searchResults.hidden = false;
    $$('#search-results a').forEach((link) => link.addEventListener('click', () => {
      currentQuery = '';
      searchInput.value = '';
      searchResults.hidden = true;
      refreshVisibility();
    }));
  }

  function setRole(role) {
    currentRole = role;
    $$('.role-chip').forEach((chip) => chip.classList.toggle('active', chip.dataset.role === role));
    refreshVisibility();
  }

  function setTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem(themeKey, theme);
    $$('#theme-toggle, #theme-toggle-2').forEach((btn) => {
      btn.textContent = theme === 'dark' ? '☀' : '◐';
      btn.setAttribute('aria-label', theme === 'dark' ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro');
    });
  }

  function toggleTheme() {
    setTheme(document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark');
  }

  function initTheme() {
    let saved = null;
    try { saved = localStorage.getItem(themeKey); } catch { /* noop */ }
    setTheme(saved === 'dark' ? 'dark' : 'light');
  }

  function updateCrumbs() {
    const chapters = $$('.chapter[data-id]:not([hidden])');
    let current = chapters[0];
    for (const chapter of chapters) {
      const rect = chapter.getBoundingClientRect();
      if (rect.top <= 150) current = chapter; else break;
    }
    if (!current) return;
    const id = current.dataset.id;
    const part = partById[current.dataset.part];
    crumbs.innerHTML = `<span>Manual</span> · <b>${part?.title || ''}</b> · ${current.querySelector('h1')?.textContent || ''}`;
    $$('.nav-chapter a').forEach((a) => a.classList.toggle('active', a.dataset.target === id));
  }

  function openGlossary() {
    $('#glossary-list').innerHTML = GLOSSARY.map((g) => `<dt>${g.term}</dt><dd>${g.text}</dd>`).join('');
    glossaryModal.hidden = false;
  }

  function closeGlossary() { glossaryModal.hidden = true; }

  function initEvents() {
    $('#menu-btn').addEventListener('click', () => document.body.classList.toggle('sidebar-open'));
    $('#side-close').addEventListener('click', () => document.body.classList.remove('sidebar-open'));
    document.addEventListener('click', (event) => {
      if (event.target.closest('#menu-btn, #sidebar')) return;
      if (document.body.classList.contains('sidebar-open')) document.body.classList.remove('sidebar-open');
    });

    $$('.role-chip').forEach((chip) => chip.addEventListener('click', () => setRole(chip.dataset.role)));
    searchInput.addEventListener('input', (event) => search(event.target.value));
    $('#search-open').addEventListener('click', () => {
      searchInput.focus();
      searchInput.scrollIntoView({ block: 'center' });
    });

    $('#theme-toggle').addEventListener('click', toggleTheme);
    $('#theme-toggle-2').addEventListener('click', toggleTheme);
    $('#open-glossary').addEventListener('click', openGlossary);
    $('#close-glossary').addEventListener('click', closeGlossary);
    glossaryModal.addEventListener('click', (event) => { if (event.target === glossaryModal) closeGlossary(); });

    $('#lightbox-close').addEventListener('click', () => { lightbox.hidden = true; document.body.style.overflow = ''; });
    lightbox.addEventListener('click', (event) => { if (event.target === lightbox) { lightbox.hidden = true; document.body.style.overflow = ''; } });

    $('#tour-close').addEventListener('click', closeTour);
    $('#tour-prev').addEventListener('click', prevTourStep);
    $('#tour-next').addEventListener('click', nextTourStep);
    tourModal.addEventListener('click', (event) => { if (event.target === tourModal) closeTour(); });
    tourModal.addEventListener('keydown', (event) => {
      if (event.key !== 'Tab') return;
      const focusable = $$('button:not([disabled])', tourDialog);
      if (!focusable.length) return;
      const first = focusable[0];
      const last = focusable[focusable.length - 1];
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
      }
    });
    window.addEventListener('resize', () => { if (activeTour) positionTourCard(); });
    if ('ResizeObserver' in window) new ResizeObserver(() => positionTourCard()).observe(tourCard);

    document.addEventListener('keydown', (event) => {
      if (event.key === 'Escape') {
        lightbox.hidden = true;
        document.body.style.overflow = '';
        closeGlossary();
        closeTour();
        searchResults.hidden = true;
        document.body.classList.remove('sidebar-open');
      }
      if (activeTour && !/INPUT|TEXTAREA|SELECT/.test(document.activeElement.tagName)) {
        if (event.key === 'ArrowRight') { event.preventDefault(); nextTourStep(); }
        if (event.key === 'ArrowLeft') { event.preventDefault(); prevTourStep(); }
      }
      if (event.key === '/' && !/INPUT|TEXTAREA|SELECT/.test(document.activeElement.tagName)) {
        event.preventDefault();
        searchInput.focus();
      }
      if ((event.key === 't' || event.key === 'T') && !/INPUT|TEXTAREA|SELECT/.test(document.activeElement.tagName)) {
        toggleTheme();
      }
    });

    window.addEventListener('scroll', updateCrumbs, { passive: true });
  }

  render();
  initTheme();
  initEvents();
  updateCrumbs();
})();
