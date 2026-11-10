import {useMemo,useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {useAuth} from '../context.js';
import {api,DAYS,fmtDate,money,refresh,toast,ts} from '../api.js';
import {useLoader,useNow} from '../hooks.js';
import {Badge,Button,Empty,Field,Icon,Modal,PageHeader,Skeleton} from '../components/ui.jsx';

const PAGE=9;
const SORTS=[['name','Name A–Z'],['start','Starts soonest'],['fee-asc','Fee: low to high'],['fee-desc','Fee: high to low'],['newest','Newest first']];
const CMP={
  name:(a,b)=>a.name.localeCompare(b.name),
  start:(a,b)=>String(a.startDate).localeCompare(String(b.startDate)),
  'fee-asc':(a,b)=>(a.fee||0)-(b.fee||0),
  'fee-desc':(a,b)=>(b.fee||0)-(a.fee||0),
  newest:(a,b)=>b.id-a.id,
};

export default function Classes(){
  const{user}=useAuth();
  const nav=useNavigate();
  const now=useNow(1000);
  const admin=user.role==='ADMIN';
  const student=user.role==='STUDENT';

  const classes=useLoader(()=>api('/api/classes'),{events:['inst:refresh']});
  const courses=useLoader(()=>api('/api/courses'));
  const instructors=useLoader(()=>api('/api/instructors'));

  const[q,setQ]=useState('');
  const[course,setCourse]=useState('');
  const[sort,setSort]=useState('name');
  const[page,setPage]=useState(1);
  const[picked,setPicked]=useState([]);
  const[busy,setBusy]=useState(null);
  const[form,setForm]=useState(null);
  const[batch,setBatch]=useState(null);

  const filtered=useMemo(()=>{
    let x=[...(classes.data||[])];
    const s=q.trim().toLowerCase();
    if(s)x=x.filter(c=>[c.name,c.course?.name,c.instructor?.name].some(v=>v&&v.toLowerCase().includes(s)));
    if(course)x=x.filter(c=>String(c.course?.id)===course);
    return x.sort(CMP[sort]);
  },[classes.data,q,course,sort]);

  const pages=Math.max(1,Math.ceil(filtered.length/PAGE));
  const current=Math.min(page,pages);
  const items=filtered.slice((current-1)*PAGE,current*PAGE);
  const byId=useMemo(()=>Object.fromEntries((classes.data||[]).map(c=>[c.id,c])),[classes.data]);

  const onFilter=setter=>e=>{setter(e.target.value);setPage(1)};

  function toggle(id){
    setPicked(p=>{
      if(p.includes(id))return p.filter(x=>x!==id);
      if(p.length>=5){toast('You can reserve up to 5 classes at once','info');return p}
      return [...p,id];
    });
  }

  const minutesLeft=iso=>Math.max(1,Math.round(((ts(iso)?.getTime()??now)-Date.now())/60000));

  async function reserveOne(c){
    setBusy(c.id);
    try{
      const r=await api(`/api/classes/${c.id}/reservations`,{method:'POST'});
      refresh();
      if(r.status==='HELD'){
        toast(`Seat held for ${minutesLeft(r.expiresAt)} minutes. Complete your payment to confirm.`,'ok','Seat reserved');
        nav('/reservations');
      }else{
        toast(r.message,'info',`Waiting list · #${r.waitlistPosition}`);
      }
    }catch(e){toast(e.message,'err')}
    finally{setBusy(null)}
  }

  async function reserveMany(){
    setBusy('batch');
    try{
      const rs=await api('/api/reservations/batch',{method:'POST',body:{classIds:picked}});
      refresh();
      setBatch(rs);
      setPicked([]);
    }catch(e){toast(e.message,'err')}
    finally{setBusy(null)}
  }

  async function remove(c){
    if(!window.confirm(`Delete class "${c.name}"?`))return;
    try{
      await api(`/api/classes/${c.id}`,{method:'DELETE'});
      toast('Class deleted');
      refresh();
    }catch(e){toast(e.message,'err')}
  }

  const loading=classes.data===null&&!classes.error;

  return <section className="page">
    <PageHeader title="Classes"
      subtitle={student?'Pick a class, hold your seat, then pay before the timer ends.':'Schedules, instructors and fees.'}
      actions={<>
        <div className="search"><Icon name="search" size={16}/>
          <input placeholder="Search classes, courses, instructors…" value={q} onChange={onFilter(setQ)}/>
        </div>
        <select value={course} onChange={onFilter(setCourse)}>
          <option value="">All courses</option>
          {(courses.data||[]).map(c=><option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <select value={sort} onChange={e=>setSort(e.target.value)}>
          {SORTS.map(([v,l])=><option key={v} value={v}>{l}</option>)}
        </select>
        {admin&&<Button icon="plus" onClick={()=>setForm({})}>New class</Button>}
      </>}/>

    {classes.error
      ?<div className="panel"><Empty icon="layers" title="Couldn't load classes" text={classes.error.message}/></div>
      :loading
        ?<div className="grid">{[0,1,2,3,4,5].map(i=><Skeleton key={i} h={250}/>)}</div>
        :!items.length
          ?<div className="panel"><Empty icon="calendar" title="No classes found" text="Try another search or course filter."/></div>
          :<>
            <div className="grid">
              {items.map(c=><ClassCard key={c.id} c={c} student={student} admin={admin}
                checked={picked.includes(c.id)} busy={busy===c.id}
                onToggle={toggle} onReserve={reserveOne}
                onEdit={x=>setForm(x)} onDelete={remove}/>)}
            </div>
            <div className="pager">
              <span className="muted">{filtered.length} classes · page {current} of {pages}</span>
              <div className="pager-btns">
                <Button variant="ghost" size="sm" disabled={current<=1} onClick={()=>setPage(current-1)}>Previous</Button>
                <Button variant="ghost" size="sm" disabled={current>=pages} onClick={()=>setPage(current+1)}>Next</Button>
              </div>
            </div>
          </>}

    {student&&picked.length>0&&<div className="batchbar">
      <span><b>{picked.length}</b> selected · up to 5</span>
      <div className="bb-actions">
        <Button variant="ghost" onClick={()=>setPicked([])}>Clear</Button>
        <Button icon="seat" onClick={reserveMany} disabled={busy==='batch'}>{busy==='batch'?'Reserving…':'Reserve all'}</Button>
      </div>
    </div>}

    <Modal open={!!batch} onClose={()=>setBatch(null)} title="Reservation results" wide>
      <div className="stack-s">
        {(batch||[]).map(r=><div className="result" key={r.classId}>
          <div><b>{byId[r.classId]?.name||`Class #${r.classId}`}</b><p className="muted">{r.message}</p></div>
          <Badge s={r.status}/>
        </div>)}
      </div>
      <div className="modal-foot">
        <Button variant="ghost" onClick={()=>setBatch(null)}>Close</Button>
        {(batch||[]).some(r=>r.status==='HELD')&&<Button icon="arrow" onClick={()=>{setBatch(null);nav('/reservations')}}>Go to payments</Button>}
      </div>
    </Modal>

    <Modal open={!!form} onClose={()=>setForm(null)} title={form?.id?'Edit class':'New class'} wide>
      {form&&<ClassForm initial={form.id?form:null} courses={courses.data||[]} instructors={instructors.data||[]} onClose={()=>setForm(null)}/>}
    </Modal>
  </section>;
}

function ClassCard({c,student,admin,checked,busy,onToggle,onReserve,onEdit,onDelete}){
  const days=c.days||[];
  return <article className={`class-card ${checked?'picked':''}`}>
    <div className="cc-top">
      <span className="course-pill">{c.course?.name||'Course'}</span>
      <div className="cc-tools">
        {student&&<input type="checkbox" className="pick-box" checked={checked} onChange={()=>onToggle(c.id)} aria-label={`Select ${c.name}`}/>}
        {admin&&<>
          <button className="icon-btn sm" onClick={()=>onEdit(c)} aria-label="Edit"><Icon name="edit" size={15}/></button>
          <button className="icon-btn sm danger" onClick={()=>onDelete(c)} aria-label="Delete"><Icon name="trash" size={15}/></button>
        </>}
      </div>
    </div>
    <h3>{c.name}</h3>
    <p className="cc-inst"><Icon name="user" size={14}/>{c.instructor?.name||'Instructor to be announced'}</p>
    <div className="days">{DAYS.map(([k,l])=><span key={k} className={days.includes(k)?'on':''}>{l}</span>)}</div>
    <div className="cc-meta">
      <span><Icon name="calendar" size={14}/>{fmtDate(c.startDate)} – {fmtDate(c.endDate)}</span>
      <span><Icon name="seat" size={14}/>{c.capacity} seats</span>
    </div>
    <div className="cc-foot">
      <div><small>Fee</small><strong>{money(c.fee)}</strong></div>
      {student&&<Button size="sm" icon="seat" onClick={()=>onReserve(c)} disabled={busy}>{busy?'Reserving…':'Reserve'}</Button>}
    </div>
  </article>;
}

function ClassForm({initial,courses,instructors,onClose}){
  const[f,setF]=useState(()=>({
    name:initial?.name||'',
    course:initial?.course?.id||'',
    instructor:initial?.instructor?.id||'',
    days:initial?.days||[],
    startDate:initial?.startDate||'',
    endDate:initial?.endDate||'',
    capacity:initial?.capacity??20,
    fee:initial?.fee??50,
  }));
  const[saving,setSaving]=useState(false);
  const set=(k,v)=>setF(x=>({...x,[k]:v}));
  const toggleDay=k=>set('days',f.days.includes(k)?f.days.filter(d=>d!==k):[...f.days,k]);

  async function submit(e){
    e.preventDefault();
    if(!f.days.length)return toast('Pick at least one class day','err');
    setSaving(true);
    const body={
      name:f.name.trim(),
      course:{id:+f.course},
      instructor:{id:+f.instructor},
      days:f.days,
      startDate:f.startDate,
      endDate:f.endDate,
      capacity:+f.capacity,
      fee:+f.fee,
    };
    try{
      await api(initial?`/api/classes/${initial.id}`:'/api/classes',{method:initial?'PUT':'POST',body});
      toast(initial?'Class updated':'Class created');
      refresh();
      onClose();
    }catch(err){toast(err.message,'err')}
    finally{setSaving(false)}
  }

  return <form className="form-grid" onSubmit={submit}>
    <div className="full"><Field label="Class name"><input required value={f.name} onChange={e=>set('name',e.target.value)}/></Field></div>
    <Field label="Course">
      <select required value={f.course} onChange={e=>set('course',e.target.value)}>
        <option value="">Select…</option>
        {courses.map(c=><option key={c.id} value={c.id}>{c.name}</option>)}
      </select>
    </Field>
    <Field label="Instructor">
      <select required value={f.instructor} onChange={e=>set('instructor',e.target.value)}>
        <option value="">Select…</option>
        {instructors.map(i=><option key={i.id} value={i.id}>{i.name}</option>)}
      </select>
    </Field>
    <div className="full">
      <span className="field-label">Class days</span>
      <div className="days-pick">
        {DAYS.map(([k,l])=><button type="button" key={k} className={f.days.includes(k)?'on':''} onClick={()=>toggleDay(k)}>{l}</button>)}
      </div>
    </div>
    <Field label="Start date"><input type="date" required value={f.startDate} onChange={e=>set('startDate',e.target.value)}/></Field>
    <Field label="End date"><input type="date" required value={f.endDate} onChange={e=>set('endDate',e.target.value)}/></Field>
    <Field label="Capacity"><input type="number" min="1" required value={f.capacity} onChange={e=>set('capacity',e.target.value)}/></Field>
    <Field label="Fee (BHD)"><input type="number" min="0" step="0.01" required value={f.fee} onChange={e=>set('fee',e.target.value)}/></Field>
    <div className="full modal-foot">
      <Button variant="ghost" type="button" onClick={onClose}>Cancel</Button>
      <Button type="submit" disabled={saving}>{saving?'Saving…':initial?'Save changes':'Create class'}</Button>
    </div>
  </form>;
}
