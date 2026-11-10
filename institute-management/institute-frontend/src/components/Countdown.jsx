import {useNow} from '../hooks.js';
import {ts} from '../api.js';

// عدّاد دائري للمقعد المحجوز: يبدأ من مدة الحجز وينزل لين يوصل صفر
export default function Countdown({expiresAt,reservedAt,size=120}){
  const now=useNow(500);
  const end=ts(expiresAt)?.getTime()||0;
  const start=ts(reservedAt)?.getTime()||end-600000;
  const total=Math.max(1000,end-start);
  const left=Math.max(0,end-now);
  const s=Math.ceil(left/1000);
  const pct=Math.min(1,left/total);
  const r=44,c=2*Math.PI*r;
  const mm=String(Math.floor(s/60)).padStart(2,'0');
  const ss=String(s%60).padStart(2,'0');
  const cls=s===0?'done':s<=60?'low':'';
  return <div className={`ring ${cls}`} style={{width:size,height:size}}>
    <svg viewBox="0 0 100 100">
      <circle className="track" cx="50" cy="50" r={r}/>
      <circle className="bar" cx="50" cy="50" r={r} strokeDasharray={c} strokeDashoffset={c*(1-pct)}/>
    </svg>
    <div className="ring-text"><b>{s===0?'00:00':`${mm}:${ss}`}</b><small>{s===0?'expired':'to pay'}</small></div>
  </div>;
}
