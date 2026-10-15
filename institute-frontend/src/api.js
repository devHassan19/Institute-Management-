const K='inst_token';
export const getToken=()=>localStorage.getItem(K);
export const setToken=t=>t?localStorage.setItem(K,t):localStorage.removeItem(K);
export const toast=(m,t='ok')=>window.dispatchEvent(new CustomEvent('toast',{detail:{m,t}}));
export async function api(path,{method='GET',body,form}={}){
  const h={},t=getToken();if(t)h.Authorization='Bearer '+t;if(body)h['Content-Type']='application/json';
  const r=await fetch(path,{method,headers:h,body:form||(body&&JSON.stringify(body))});
  const txt=await r.text();let d=txt;try{d=JSON.parse(txt)}catch{}
  if(!r.ok){
    if(r.status===401&&t&&!path.includes('login')){setToken(null);location.href='/login'}
    throw new Error((d&&(d.message||d.error||d.detail))||(typeof d==='string'&&d)||'Something went wrong ('+r.status+')');
  }
  return d;
}
