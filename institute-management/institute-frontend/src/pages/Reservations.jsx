import {useEffect,useState} from 'react';
import {Link} from 'react-router-dom';
import {api,fmtDateTime,isLive,money} from '../api.js';
import {useLoader,useNow} from '../hooks.js';
import {dismissed} from '../store.js';
import Countdown from '../components/Countdown.jsx';
import PayModal from '../components/PayModal.jsx';
import {Badge,Button,Empty,PageHeader,Skeleton} from '../components/ui.jsx';

export default function Reservations(){
  const now=useNow(1000);
  const res=useLoader(()=>api('/api/reservations/my'),{every:4000,events:['inst:refresh']});
  const wl=useLoader(()=>api('/api/waitlist/my'),{every:4000,events:['inst:refresh']});
  const[pay,setPay]=useState(null);

  const live=(res.data||[]).filter(r=>isLive(r,now));
  const waiting=(wl.data||[]).filter(w=>w.status==='WAITING');
  const history=(res.data||[]).filter(r=>!isLive(r,now)).slice(0,12);

  // نافذة الدفع تفتح لحالها لأي حجز نشط ما دفعت عليه ولا أغلقته
  useEffect(()=>{
    if(pay)return;
    const next=live.find(r=>!dismissed.has(r.id));
    if(next)setPay(next);
  });

  return <section className="page">
    <PageHeader title="Reservations"
      subtitle="Seats are held for you while you pay. The timer is live."
      actions={<Link to="/classes"><Button variant="ghost" icon="calendar">Browse classes</Button></Link>}/>

    <div className="section-title">
      <h2>Awaiting payment</h2>
      {live.length>0&&<span className="pulse" aria-hidden="true"/>}
    </div>
    {res.data===null
      ?<div className="grid2"><Skeleton h={180}/></div>
      :live.length
        ?<div className="grid2">
          {live.map(r=><div className="seat-card" key={r.id}>
            <Countdown expiresAt={r.expiresAt} reservedAt={r.reservedAt} size={104}/>
            <div className="sc-body">
              <span className="eyebrow">{r.courseName}</span>
              <h3>{r.className}</h3>
              <p className="muted small">Reserved {fmtDateTime(r.reservedAt)}</p>
              <div className="sc-foot">
                <strong>{money(r.fee)}</strong>
                <Button icon="card" onClick={()=>setPay(r)}>Pay now</Button>
              </div>
            </div>
          </div>)}
        </div>
        :<div className="panel"><Empty icon="seat" title="No seats waiting for payment"
          text="Reserve a seat from the classes page and it will appear here."
          action={<Link to="/classes"><Button icon="calendar">Browse classes</Button></Link>}/></div>}

    {waiting.length>0&&<>
      <div className="section-title"><h2>Waiting list</h2></div>
      <div className="grid2">
        {waiting.map(w=><div className="seat-card wait" key={w.id}>
          <div className="position"><div>{w.position}</div><small>in line</small></div>
          <div className="sc-body">
            <span className="eyebrow">Class</span>
            <h3>{w.className}</h3>
            <p className="muted small">{w.position-1>0
              ?`${w.position-1} student${w.position-1>1?'s':''} ahead of you`
              :'You are next in line'}</p>
            <div className="sc-foot">
              <Badge s="WAITING"/>
              <span className="muted small">Payment opens for you when your seat is ready.</span>
            </div>
          </div>
        </div>)}
      </div>
    </>}

    {history.length>0&&<>
      <div className="section-title"><h2>History</h2></div>
      <div className="panel"><div className="table-wrap">
        <table className="tbl">
          <thead><tr><th>Class</th><th>Fee</th><th>Reserved</th><th>Status</th></tr></thead>
          <tbody>{history.map(r=><tr key={r.id}>
            <td><b>{r.className}</b><div className="muted small">{r.courseName}</div></td>
            <td>{money(r.fee)}</td>
            <td>{fmtDateTime(r.reservedAt)}</td>
            <td><Badge s={r.status==='HELD'?'EXPIRED':r.status}/></td>
          </tr>)}</tbody>
        </table>
      </div></div>
    </>}

    {pay&&<PayModal key={pay.id} reservation={pay} onClose={()=>setPay(null)}/>}
  </section>;
}
