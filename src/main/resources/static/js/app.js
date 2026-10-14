const $ = id => document.getElementById(id);
const STAT = ['APPLIED', 'SHORTLISTED', 'INTERVIEW', 'SELECTED', 'REJECTED'];

function esc(s) {
  return String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
}

async function api(url, method = 'GET', body) {
  const opt = { method, headers: {}, credentials: 'same-origin' };
  if (body !== undefined) { opt.headers['Content-Type'] = 'application/json'; opt.body = JSON.stringify(body); }
  const r = await fetch(url, opt);
  if (r.status === 401) { location.href = '/index.html'; throw new Error('Session expired'); }
  const t = await r.text();
  let d = null;
  try { d = JSON.parse(t); } catch (e) {}
  if (!r.ok) throw new Error((d && (d.message || d.error)) || 'Request failed (' + r.status + ')');
  return d;
}

function show(id) {
  document.querySelectorAll('.tab').forEach(t => t.style.display = t.id === id ? 'block' : 'none');
  document.querySelectorAll('nav button[data-t]').forEach(b => b.classList.toggle('active', b.dataset.t === id));
  if (typeof load !== 'undefined' && load[id]) load[id]().catch(e => alert(e.message));
}

async function logout() {
  await fetch('/logout', { method: 'POST', credentials: 'same-origin' });
  location.href = '/index.html';
}
