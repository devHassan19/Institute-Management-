import {ago,fmtDateTime,ts} from '../api.js';
import {useLoader} from '../hooks.js';
import {api} from '../api.js';
import {Empty,Icon,PageHeader,Skeleton} from '../components/ui.jsx';

// يحدد شكل الإشعار حسب نصه
function kind(m=''){
  if(/available|offered|pay within|seat held|reserved/i.test(m))return{tone:'warm',icon:'seat'};
  if(/expired/i.test(m))return{tone:'bad',icon:'clock'};
  if(/paid|payment received|enrolled/i.test(m))return{tone:'good',icon:'check'};
  if(/waiting list|position|number|full/i.test(m))return{tone:'violet',icon:'clock'};
  return{tone:'info',icon:'bell'};
}
const isToday=s=>{const d=ts(s);return!!d&&d.toDateString()===new Date().toDateString()};

function Group({title,items}){
  if(!items.length)return null;
  return <div className="feed-group">
    <div className="group-t">{title}</div>
    <div className="panel">
      {items.map(n=>{
        const k=kind(n.message);
        return <div className="fi" key={n.id}>
          <span className={`fi-ic ${k.tone}`}><Icon name={k.icon} size={18}/></span>
          <div><p>{n.message}</p><small>{ago(n.createdAt)} · {fmtDateTime(n.createdAt)}</small></div>
        </div>;
      })}
    </div>
  </div>;
}

export default function Notifications(){
  const{data,error}=useLoader(()=>api('/api/notifications/list'),{every:8000,events:['inst:notify']});
  return <section className="page">
    <PageHeader title="Notifications" subtitle="Everything the system told you, saved even when you were offline."/>
    {error
      ?<div className="panel"><Empty icon="bell" title="Couldn't load notifications" text={error.message}/></div>
      :data===null
        ?<div className="stack-s">{[0,1,2].map(i=><Skeleton key={i} h={64}/>)}</div>
        :!data.length
          ?<div className="panel"><Empty icon="bell" title="You're all caught up"
              text="Seat offers, payment confirmations and waiting-list updates will show up here."/></div>
          :<>
            <Group title="Today" items={data.filter(n=>isToday(n.createdAt))}/>
            <Group title="Earlier" items={data.filter(n=>!isToday(n.createdAt))}/>
          </>}
  </section>;
}
