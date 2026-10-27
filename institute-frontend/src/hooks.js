import {useCallback,useEffect,useRef,useState} from 'react';

// يعطيك الوقت الحالي ويتحدّث كل فترة (للعدّادات)
export function useNow(ms=1000){
  const[now,setNow]=useState(()=>Date.now());
  useEffect(()=>{const t=setInterval(()=>setNow(Date.now()),ms);return()=>clearInterval(t)},[ms]);
  return now;
}

// يحمّل بيانات ويعيد تحميلها: كل فترة، أو عند أحداث معيّنة (مثل inst:refresh)
export function useLoader(fn,{every=0,events=[]}={}){
  const[data,setData]=useState(null);
  const[error,setError]=useState(null);
  const fnRef=useRef(fn);
  fnRef.current=fn;
  const evKey=events.join('|');
  const run=useCallback(()=>{
    fnRef.current().then(d=>{setData(d);setError(null)}).catch(e=>setError(e));
  },[]);
  useEffect(()=>{
    run();
    const t=every?setInterval(run,every):null;
    const names=evKey?evKey.split('|'):[];
    names.forEach(n=>window.addEventListener(n,run));
    return()=>{if(t)clearInterval(t);names.forEach(n=>window.removeEventListener(n,run))};
  },[run,every,evKey]);
  return{data,error,reload:run,setData};
}
