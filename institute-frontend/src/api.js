// ---------- token ----------
const KEY='inst_token';
export const getToken=()=>localStorage.getItem(KEY);
export const setToken=t=>t?localStorage.setItem(KEY,t):localStorage.removeItem(KEY);

// ---------- events (toasts, refresh signals) ----------
export const emit=(name,detail)=>window.dispatchEvent(new CustomEvent(name,{detail}));
export const refresh=()=>window.dispatchEvent(new Event('inst:refresh'));
export const toast=(message,type='ok',title)=>emit('toast',{message,type,title});

// ---------- HTTP ----------
export async function api(path,{method='GET',body,form}={}){
  const headers={};
  const token=getToken();
  if(token)headers.Authorization=`Bearer ${token}`;
  if(body&&!form)headers['Content-Type']='application/json';
  const res=await fetch(path,{method,headers,body:form||(body?JSON.stringify(body):undefined)});
  const text=await res.text();
  let data=text;
  try{data=text?JSON.parse(text):null}catch{}
  if(!res.ok){
    if(res.status===401&&token&&!path.startsWith('/auth/'))emit('auth:expired');
    const msg=(data&&typeof data==='object'&&(data.message||data.error))||(typeof data==='string'&&data)||`Request failed (${res.status})`;
    const err=new Error(msg);
    err.status=res.status;
    throw err;
  }
  return data;
}

// ---------- dates & money ----------
export const ts=s=>s?new Date(String(s).replace(/(\.\d{3})\d+/,'$1')):null;
export const isLive=(r,now=Date.now())=>r.status==='HELD'&&(ts(r.expiresAt)?.getTime()??0)>now;
export const money=n=>n==null?'—':`BHD ${Number(n).toFixed(2)}`;
export const fmtDate=s=>s?new Date(`${String(s).slice(0,10)}T00:00:00`).toLocaleDateString('en-GB',{day:'numeric',month:'short',year:'numeric'}):'—';
export const fmtDateTime=s=>{const d=ts(s);return d?d.toLocaleString('en-GB',{day:'numeric',month:'short',hour:'2-digit',minute:'2-digit'}):'—'};
export function ago(s){
  const d=ts(s);if(!d)return '';
  const sec=Math.max(0,Math.floor((Date.now()-d.getTime())/1000));
  if(sec<60)return 'just now';
  if(sec<3600)return `${Math.floor(sec/60)} min ago`;
  if(sec<86400)return `${Math.floor(sec/3600)} h ago`;
  return fmtDateTime(s);
}

// ---------- constants ----------
export const DAYS=[['SUNDAY','Sun'],['MONDAY','Mon'],['TUESDAY','Tue'],['WEDNESDAY','Wed'],['THURSDAY','Thu'],['FRIDAY','Fri'],['SATURDAY','Sat']];
export const ROLE_LABEL={ADMIN:'Administrator',INSTRUCTOR:'Instructor',STUDENT:'Student'};
