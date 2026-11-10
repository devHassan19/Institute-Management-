import {Link,useNavigate} from 'react-router-dom';
import {useAuth} from '../context.js';
import {ago,api,fmtDate,isLive,money} from '../api.js';
import {useLoader,useNow} from '../hooks.js';
import Countdown from '../components/Countdown.jsx';
import {Button,Empty,Skeleton,Stat} from '../components/ui.jsx';

export default function Dashboard(){
  const{user}=useAuth();
  const nav=useNavigate();
  const now=useNow(1000);
  const student=user.role==='STUDENT';

  const classes=useLoader(()=>api('/api/classes'),{events:['inst:refresh']});
  const held=useLoader(()=>student?api('/api/reservations/my'):Promise.resolve([]),{every:5000,events:['inst:refresh']});
  const wl=useLoader(()=>student?api('/api/waitlist/my'):Promise.resolve([]),{every:5000,events:['inst:refresh']});
  const enr=useLoader(()=>student?api('/api/myEnrollmrnt'):api('/api/enrollmrnts'),{events:['inst:refresh']});
  const audit=useLoader(()=>user.role==='ADMIN'?api('/api/auditLog'):Promise.resolve([]));

  const live=(held.data||[]).filter(r=>isLive(r,now));
  const first=live[0];
  const waits=(wl.data||[]).filter(w=>w.status==='WAITING');
  const today=new Date().toISOString().slice(0,10);
  const upcoming=(classes.data||[])
    .filter(c=>String(c.endDate)>=today)
    .sort((a,b)=>String(a.startDate).localeCompare(String(b.startDate)))
    .slice(0,4);
  const hour=new Date().getHours();
  const greeting=hour<12?'Good morning':hour<18?'Good afternoon':'Good evening';
  const name=user.firstName||user.username;

  return <section className="page">
    <div className="hero">
      <div className="hero-copy">
        <span className="eyebrow light">{greeting}</span>
        <h1>{name}, {student?'ready to learn?':'here is your institute today.'}</h1>
        <p>{student
          ?(first?`Your seat in ${first.className} is waiting. Pay before the timer ends.`:'Browse open classes and hold a seat in seconds.')
          :'Manage classes, courses and enrollments from one place.'}</p>
        <div className="hero-actions">
          {student&&first
            ?<Button variant="white" icon="card" onClick={()=>nav('/reservations')}>Pay now</Button>
            :<Button variant="white" icon="calendar" onClick={()=>nav('/classes')}>{student?'Browse classes':'Manage classes'}</Button>}
        </div>
      </div>
      {student&&first&&<div className="hero-ring">
        <Countdown expiresAt={first.expiresAt} reservedAt={first.reservedAt} size={130}/>
      </div>}
    </div>

    <div className="stats">
      {student?<>
        <Stat label="Seats on hold" value={live.length} icon="seat" tone="warm"/>
        <Stat label="Waiting lists" value={waits.length} icon="clock" tone="violet"/>
        <Stat label="Confirmed classes" value={(enr.data||[]).length} icon="book" tone="good"/>
        <Stat label="Open classes" value={(classes.data||[]).length} icon="calendar" tone="sky"/>
      </>:<>
        <Stat label="Classes" value={(classes.data||[]).length} icon="calendar" tone="pri"/>
        <Stat label="Enrollments" value={(enr.data||[]).length} icon="list" tone="good"/>
        <Stat label="Audit events" value={(audit.data||[]).length} icon="shield" tone="violet"/>
        <Stat label="Upcoming" value={upcoming.length} icon="clock" tone="sky"/>
      </>}
    </div>

    <div className="two">
      <div className="panel">
        <div className="panel-h"><h2>Upcoming classes</h2><Link to="/classes" className="small">View all</Link></div>
        {classes.data===null
          ?<div className="stack-s pad">{[0,1,2].map(i=><Skeleton key={i} h={58}/>)}</div>
          :!upcoming.length
            ?<Empty icon="calendar" title="No upcoming classes" text="New classes will appear here."/>
            :upcoming.map(c=><div className="list-row" key={c.id}>
              <div><b>{c.name}</b><div className="muted small">{c.course?.name} · starts {fmtDate(c.startDate)}</div></div>
              <div className="r"><strong>{money(c.fee)}</strong></div>
            </div>)}
      </div>

      <div className="panel">
        <div className="panel-h"><h2>{student?'Your waiting lists':'Recent activity'}</h2></div>
        {student
          ?(!waits.length
            ?<Empty icon="clock" title="Not waiting anywhere" text="When a class is full you will be added to its waiting list."/>
            :waits.map(w=><div className="list-row" key={w.id}>
              <div><b>{w.className}</b><div className="muted small">You are next in line after {w.position-1} student{w.position-1===1?'':'s'}</div></div>
              <span className="mini-pos">{w.position}</span>
            </div>))
          :(audit.data===null
            ?<div className="stack-s pad">{[0,1,2].map(i=><Skeleton key={i} h={46}/>)}</div>
            :!(audit.data||[]).length
              ?<Empty icon="shield" title="No activity yet"/>
              :(audit.data||[]).slice(0,6).map(a=><div className="list-row" key={a.id}>
                <div><b>{a.action}</b><div className="muted small">{a.description}</div></div>
                <small className="muted">{ago(a.createdAt)}</small>
              </div>))}
      </div>
    </div>
  </section>;
}
