(function(){
  var reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  /* Hero glitch-in headline */
  var el = document.getElementById('glitch-headline');
  if (el && !reduceMotion) {
    var finalText = 'Stop the scroll before it stops you.';
    var breakPoint = 'Stop the scro';
    el.innerHTML = '<span class="clean">' + breakPoint + '<span class="accent">—</span></span><span class="glitch-cursor"></span>';
    setTimeout(function(){
      el.innerHTML = '<span class="clean">' + finalText.replace('scroll', 'scro<span class="accent">ll</span>') + '</span>';
    }, 900);
  }

  /* Phone block sequence + step highlighting, driven by IntersectionObserver (no scroll-jank, no external deps) */
  var steps = document.querySelectorAll('.i-step');
  var phone = document.getElementById('phone');
  if (steps.length && phone) {
    var stepObserver = new IntersectionObserver(function(entries){
      entries.forEach(function(entry){
        if (entry.isIntersecting) {
          steps.forEach(function(s){ s.classList.remove('active'); });
          entry.target.classList.add('active');
          var idx = Number(entry.target.dataset.step);
          if (idx >= 2) { phone.classList.add('blocked'); }
          else { phone.classList.remove('blocked'); }
        }
      });
    }, { threshold: 0.6, rootMargin: '-20% 0px -20% 0px' });
    steps.forEach(function(s){ stepObserver.observe(s); });
  }

  /* Count-up stats */
  var counters = document.querySelectorAll('.stat-num');
  if (counters.length) {
    var countObserver = new IntersectionObserver(function(entries){
      entries.forEach(function(entry){
        if (!entry.isIntersecting) return;
        var node = entry.target;
        countObserver.unobserve(node);
        var target = Number(node.dataset.count);
        var suffix = node.dataset.suffix || '';
        if (reduceMotion) { node.textContent = target + suffix; return; }
        var start = null;
        var duration = 900;
        function step(ts){
          if (!start) start = ts;
          var progress = Math.min((ts - start) / duration, 1);
          node.textContent = Math.round(progress * target) + suffix;
          if (progress < 1) requestAnimationFrame(step);
        }
        requestAnimationFrame(step);
      });
    }, { threshold: 0.6 });
    counters.forEach(function(c){ countObserver.observe(c); });
  }

  /* Fade-up reveal for feature/app/shot cards */
  var revealTargets = document.querySelectorAll('.feature, .app-card, .shot, .pipe-node');
  revealTargets.forEach(function(t){ t.style.opacity = 0; t.style.transform = 'translateY(24px)'; t.style.transition = 'opacity 0.6s ease, transform 0.6s ease'; });
  if (!reduceMotion) {
    var revealObserver = new IntersectionObserver(function(entries){
      entries.forEach(function(entry){
        if (entry.isIntersecting) {
          entry.target.style.opacity = 1;
          entry.target.style.transform = 'translateY(0)';
          revealObserver.unobserve(entry.target);
        }
      });
    }, { threshold: 0.15, rootMargin: '0px 0px -60px 0px' });
    revealTargets.forEach(function(t){ revealObserver.observe(t); });
    setTimeout(function(){
      revealTargets.forEach(function(t){ t.style.opacity = 1; t.style.transform = 'none'; });
    }, 2500);
  } else {
    revealTargets.forEach(function(t){ t.style.opacity = 1; t.style.transform = 'none'; });
  }
})();
