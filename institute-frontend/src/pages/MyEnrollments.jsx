import {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {api,fmtDate,refresh,toast} from '../api.js';
import {useLoader} from '../hooks.js';
import {Button,Empty,Icon,PageHeader,Skeleton} from '../components/ui.jsx';

export default function MyEnrollments(){
  const nav=useNavigate();
  const{data,error,reload}=useLoader(()=>api('/api/myEnrollmrnt'),{events:['inst:refresh']});
  const[q,setQ]=useState('');

  const s=q.trim().toLowerCase();
  const rows=(data||[]).filter(e=>!s||[e.aClass?.name,e.aClass?.course?.name].some(v=>v&&v.toLowerCase().includes(s)));

  async function cancel(e){
    if(!window.confirm(`Cancel your enrollment in ${e.aClass?.name}?`))return;
    try{
      await api(`/api/myEnrollmrnts/${e.id}`,{method:'DELETE'});
      toast('Enrollment cancelled','info');
      reload();refresh();
    }catch(err){toast(err.message,'err')}
  }

  return <section className="page">
    <PageHeader title="My enrollments" subtitle="Classes you have paid for and are confirmed in."
      actions={<div className="search"><Icon name="search" size={16}/><input placeholder="Search your classes…" value={q} onChange={e=>setQ(e.target.value)}/></div>}/>

    {error
      ?<div className="panel"><Empty icon="layers" title="Couldn't load enrollments" text={error.message}/></div>
      :data===null
        ?<div className="grid">{[0,1,2].map(i=><Skeleton key={i} h={200}/>)}</div>
        :!rows.length
          ?<div className="panel"><Empty icon="book" title="No confirmed classes yet"
              text="Reserve a seat and complete payment to see your classes here."
              action={<Button icon="calendar" onClick={()=>nav('/classes')}>Browse classes</Button>}/></div>
          :<div className="grid">
            {rows.map(e=><article className="class-card enr" key={e.id}>
              <div className="cc-top"><span className="badge good">Enrolled</span></div>
              <h3>{e.aClass?.name}</h3>
              <p className="muted">{e.aClass?.course?.name}</p>
              <div className="cc-meta">
                <span><Icon name="calendar" size={14}/>{fmtDate(e.aClass?.startDate)} – {fmtDate(e.aClass?.endDate)}</span>
                <span><Icon name="user" size={14}/>{e.aClass?.instructor?.name||'Instructor to be announced'}</span>
              </div>
              <div className="cc-foot">
                <Button variant="danger" size="sm" onClick={()=>cancel(e)}>Cancel enrollment</Button>
              </div>
            </article>)}
          </div>}
  </section>;
}
