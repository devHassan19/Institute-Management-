import {useEffect,useState} from 'react';
import {Link,useNavigate,useSearchParams} from 'react-router-dom';
import {api,toast} from './api.js';
import {useAuth} from './App.jsx';

const DAYS=[['SUNDAY','Sun'],['MONDAY','Mon'],['TUESDAY','Tue'],['WEDNESDAY','Wed'],['THURSDAY','Thu'],['FRIDAY','Fri'],['SATURDAY','Sat']];
const dayAr=d=>(DAYS.find(x=>x[0]===d)||[])[1]||d;
const fmt=d=>d?String(d).replace('T',' ').slice(0,16):'—';
const bind=(f,setF,k)=>({value:f[k]??'',onChange:e=>setF({...f,[k]:e.target.value})});

/* ---------- Auth ---------- */
function AuthShell({title,sub,children}){
  return <div className="auth"><div className="authform"><div className="brand"><b>I</b>Institute</div><h1>{title}</h1><p className="muted">{sub}</p>{children}</div>
    <div className="authart"><span/><span/><span/><h2>Everything your institute needs, in one place</h2><p>Courses, classes, instructors and enrollments, made calm and simple.</p></div></div>;
}
export function Login(){
  const{login}=useAuth(),[f,setF]=useState({email:'',password:''}),[err,setErr]=useState(''),[busy,setBusy]=useState(false);
  const submit=async e=>{e.preventDefault();setBusy(true);setErr('');try{await login(f.email,f.password)}catch(x){setErr(x.message);setBusy(false)}};
  return <AuthShell title="Welcome back" sub="Sign in to manage courses and classes."><form onSubmit={submit}>
    <label>Email<input type="email" required {...bind(f,setF,'email')}/></label>
    <label>Password<input type="password" required {...bind(f,setF,'password')}/></label>
    {err&&<p className="err">{err}</p>}<button className="btn" disabled={busy}>Sign in</button></form>
    <p className="alt"><Link to="/forgot">Forgot password?</Link></p><p className="alt">Don’t have an account? <Link to="/register">Create one</Link></p></AuthShell>;
}
export function Register(){
  const nav=useNavigate(),[f,setF]=useState({}),[img,setImg]=useState(null),[err,setErr]=useState(''),[busy,setBusy]=useState(false);
  const submit=async e=>{e.preventDefault();setBusy(true);setErr('');
    const fd=new FormData();['username','emailAddress','password','firstName','lastName','mobileNumber'].forEach(k=>fd.append(k,f[k]||''));if(img)fd.append('image',img);
    try{await api('/auth/users/register',{method:'POST',form:fd});toast('Account created. Check your email to verify it, then sign in.');nav('/login')}catch(x){setErr(x.message)}setBusy(false)};
  return <AuthShell title="Create account" sub="Sign up as a student and start joining classes."><form onSubmit={submit}>
    <div className="two"><label>First name<input required {...bind(f,setF,'firstName')}/></label><label>Last name<input required {...bind(f,setF,'lastName')}/></label></div>
    <label>Username<input required {...bind(f,setF,'username')}/></label>
    <label>Email<input type="email" required {...bind(f,setF,'emailAddress')}/></label>
    <label>Mobile number<input required {...bind(f,setF,'mobileNumber')}/></label>
    <label>Password<input type="password" required minLength={6} {...bind(f,setF,'password')}/></label>
    <label>Profile photo (optional)<input type="file" accept="image/*" onChange={e=>setImg(e.target.files[0])}/></label>
    {err&&<p className="err">{err}</p>}<button className="btn" disabled={busy}>Create account</button></form>
    <p className="alt">Already have an account? <Link to="/login">Sign in</Link></p></AuthShell>;
}
export function Forgot(){
  const[sp]=useSearchParams(),nav=useNavigate(),[step,setStep]=useState(sp.get('token')?2:1),[f,setF]=useState({email:'',token:sp.get('token')||'',newPassword:''}),[err,setErr]=useState('');
  const submit=async e=>{e.preventDefault();setErr('');try{
    if(step===1){await api('/auth/users/forgot-password',{method:'POST',body:{email:f.email}});toast('We sent a reset code to your email');setStep(2)}
    else{await api('/auth/users/reset-password',{method:'POST',body:{token:f.token,newPassword:f.newPassword}});toast('Password changed');nav('/login')}}catch(x){setErr(x.message)}};
  return <AuthShell title="Reset password" sub={step===1?'Enter your email and we’ll send you a reset code.':'Paste the code you received and choose a new password.'}><form onSubmit={submit}>
    {step===1?<label>Email<input type="email" required {...bind(f,setF,'email')}/></label>:<>
      <label>Code<input required {...bind(f,setF,'token')}/></label>
      <label>New password<input type="password" required minLength={6} {...bind(f,setF,'newPassword')}/></label></>}
    {err&&<p className="err">{err}</p>}<button className="btn">{step===1?'Send code':'Save password'}</button></form>
    {step===1&&<p className="alt"><a href="#" onClick={e=>{e.preventDefault();setStep(2)}}>I already have a code</a></p>}<p className="alt"><Link to="/login">Back to sign in</Link></p></AuthShell>;
}

/* ---------- Dashboard & profile ---------- */
export function Dashboard(){
  const{user}=useAuth(),a=user.role==='ADMIN',[d,setD]=useState({});
  useEffect(()=>{const g=(k,p)=>api(p).then(r=>setD(x=>({...x,[k]:r}))).catch(()=>{});
    g('courses','/api/courses');g('classes','/api/classes');g('instructors','/api/instructors');
    if(a){g('students','/api/students');g('enr','/api/enrollmrnts')}else if(user.role==='STUDENT')g('enr','/api/myEnrollmrnt')},[]);
  const today=new Date().toISOString().slice(0,10);
  const cards=[['Courses',d.courses,'mint'],['Classes',d.classes,'lav'],['Instructors',d.instructors,'peach'],['Students',d.students,'sky'],[a?'Enrollments':'My enrollments',d.enr,'rose']].filter(c=>c[1]);
  const next=(d.classes||[]).filter(c=>c.endDate>=today).sort((x,y)=>x.startDate>y.startDate?1:-1).slice(0,5);
  return <section><div className="head"><div><h1>Welcome back, {user.firstName||user.username}</h1><p className="muted">Here’s a quick look at the institute today.</p></div></div>
    <div className="stats">{cards.map(([l,v,c])=><div key={l} className={'stat '+c}><span>{l}</span><strong>{v.length}</strong></div>)}</div>
    <div className="card pad"><h2>Upcoming classes</h2>{next.length?next.map(c=><div className="li" key={c.id}><div><b>{c.name}</b><small className="muted">{c.course?.name} with {c.instructor?.name}</small></div>
      <small className="muted">{c.startDate}</small></div>):<p className="empty">No upcoming classes.</p>}</div></section>;
}
export function Avatar({user,size=40,src}){
  const img=src||(user.image?'/'+user.image:null),n=(user.firstName||user.username||'?').trim()[0];
  return <span className="av" style={{width:size,height:size,fontSize:size/2.4}}>{img?<img src={img} alt=""/>:n}</span>;
}
export function Profile(){
  const{user,logout,reload}=useAuth(),[p,setP]=useState({firstName:user.firstName||'',lastName:user.lastName||'',mobileNumber:user.mobileNumber||''}),
    [img,setImg]=useState(null),[busy,setBusy]=useState(false),[f,setF]=useState({oldPassword:'',newPassword:''});
  const saveP=async e=>{e.preventDefault();setBusy(true);const fd=new FormData();Object.entries(p).forEach(([k,v])=>fd.append(k,v));if(img)fd.append('image',img);
    try{await api('/api/me',{method:'PUT',form:fd});await reload();setImg(null);toast('Profile saved')}catch(x){toast(x.message,'err')}setBusy(false)};
  const savePw=async e=>{e.preventDefault();try{await api('/auth/users/changePassword',{method:'PUT',body:f});toast('Password changed');setF({oldPassword:'',newPassword:''})}catch(x){toast(x.message,'err')}};
  return <section><div className="head"><h1>Profile</h1></div><div className="grid2">
    <form className="card pad" onSubmit={saveP}><h2>My details</h2>
      <label className="pick"><Avatar user={user} size={96} src={img?URL.createObjectURL(img):null}/><input type="file" accept="image/*" hidden onChange={e=>setImg(e.target.files[0]||null)}/>Change photo</label>
      <div className="who"><b>{user.email}</b><small className="muted">{user.role}</small></div>
      <div className="two"><label>First name<input required {...bind(p,setP,'firstName')}/></label><label>Last name<input required {...bind(p,setP,'lastName')}/></label></div>
      <label>Mobile number<input required {...bind(p,setP,'mobileNumber')}/></label>
      <div className="row"><button type="button" className="btn ghost" onClick={logout}>Sign out</button><button className="btn" disabled={busy}>Save changes</button></div></form>
    <form className="card pad" onSubmit={savePw}><h2>Change password</h2>
      <label>Current<input type="password" required {...bind(f,setF,'oldPassword')}/></label>
      <label>New<input type="password" required minLength={6} {...bind(f,setF,'newPassword')}/></label><button className="btn">Save</button></form></div></section>;
}

/* ---------- Generic table: list, search, add, edit, delete ---------- */
function Field({f,m,setM}){
  const[opts,setOpts]=useState([]);useEffect(()=>{f.src&&api(f.src).then(setOpts).catch(()=>{})},[]);
  const set=v=>setM({...m,[f.name]:v}),v=m[f.name]??'',has=k=>(m.days||[]).includes(k);
  if(f.type==='days')return <div className="lbl">{f.label}<div className="chips">{DAYS.map(([k,l])=>
    <button type="button" key={k} className={has(k)?'on':''} onClick={()=>set(has(k)?m.days.filter(x=>x!==k):[...(m.days||[]),k])}>{l}</button>)}</div></div>;
  return <label>{f.label}{f.type==='select'?<select required value={v} onChange={e=>set(e.target.value)}><option value="" disabled>Select…</option>{opts.map(o=><option key={o.id} value={o.id}>{o.name}</option>)}</select>
    :f.type==='textarea'?<textarea rows="3" value={v} onChange={e=>set(e.target.value)}/>:<input type={f.type||'text'} required={f.req!==false} value={v} onChange={e=>set(e.target.value)}/>}</label>;
}
function Crud({title,hint,path,deletePath,columns,fields=[],canCreate,canEdit,canDelete,actions=[],toBody=x=>x,fromRow=x=>x,newLabel='Add',sort,ctl,toolbar,footer}){
  const[own,setOwn]=useState(null),[q,setQ]=useState(''),[m,setM]=useState(null),[busy,setBusy]=useState(false),ctrl=!!ctl,rows=ctrl?ctl.rows:own;
  const load=ctrl?ctl.reload:()=>api(path).then(r=>setOwn(sort?[...r].sort(sort):r)).catch(e=>{setOwn([]);toast(e.message,'err')});
  useEffect(()=>{if(!ctrl){setOwn(null);load()}},[path]);
  const shown=ctrl?(rows||[]):(rows||[]).filter(r=>JSON.stringify(r).toLowerCase().includes(q.toLowerCase()));
  const save=async e=>{e.preventDefault();setBusy(true);try{await api(m.id?`${path}/${m.id}`:path,{method:m.id?'PUT':'POST',body:toBody(m)});toast(m.id?'Changes saved':'Added');setM(null);load()}catch(x){toast(x.message,'err')}setBusy(false)};
  const del=async r=>{if(!confirm('Are you sure you want to delete this?'))return;try{await api(deletePath?deletePath(r.id):`${path}/${r.id}`,{method:'DELETE'});toast('Deleted');load()}catch(x){toast(x.message,'err')}};
  return <section><div className="head"><div><h1>{title}</h1>{hint&&<p className="muted">{hint}</p>}</div>
    <div className="tools">{ctrl?toolbar:<input className="search" placeholder="Search…" value={q} onChange={e=>setQ(e.target.value)}/>}{canCreate&&<button className="btn" onClick={()=>setM({})}>{newLabel}</button>}</div></div>
    <div className="card tablewrap">{rows===null?<p className="empty">Loading…</p>:!shown.length?<p className="empty">{(ctrl?ctl.filtered:q)?'No results match your search.':'Nothing here yet.'}</p>:
      <table><thead><tr>{columns.map(c=><th key={c.label}>{c.label}</th>)}<th/></tr></thead><tbody>{shown.map(r=><tr key={r.id}>
        {columns.map(c=><td key={c.label}>{(c.get?c.get(r):r[c.key])??'—'}</td>)}
        <td className="acts">{actions.filter(a=>!a.show||a.show(r)).map(a=><button key={a.label} className="btn sm soft" onClick={()=>a.run(r)}>{a.label}</button>)}
          {canEdit&&fields.length>0&&<button className="btn sm ghost" onClick={()=>setM(fromRow(r))}>Edit</button>}
          {canDelete&&<button className="btn sm danger" onClick={()=>del(r)}>Delete</button>}</td></tr>)}</tbody></table>}</div>
    {footer}
    {m&&<div className="overlay" onClick={()=>setM(null)}><form className="modal" onClick={e=>e.stopPropagation()} onSubmit={save}><h2>{m.id?'Edit':newLabel}</h2>
      {fields.map(f=><Field key={f.name} f={f} m={m} setM={setM}/>)}
      <div className="row"><button type="button" className="btn ghost" onClick={()=>setM(null)}>Cancel</button><button className="btn" disabled={busy}>Save</button></div></form></div>}</section>;
}

/* ---------- Pages ---------- */
export const Courses=()=>{const a=useAuth().user.role==='ADMIN';
  return <Crud title="Courses" hint="Subjects offered by the institute." path="/api/courses" canCreate={a} canEdit={a} canDelete={a} newLabel="New course"
    columns={[{label:'Name',key:'name'},{label:'Description',key:'description'},{label:'Duration',key:'duration'}]}
    fields={[{name:'name',label:'Course name'},{name:'description',label:'Description',type:'textarea',req:false},{name:'duration',label:'Duration',type:'number'}]}
    toBody={m=>({name:m.name,description:m.description,duration:+m.duration})}/>};
export const Instructors=()=>{const a=useAuth().user.role==='ADMIN';
  return <Crud title="Instructors" path="/api/instructors" canCreate={a} canEdit={a} canDelete={a} newLabel="New instructor"
    columns={[{label:'Name',key:'name'},{label:'Email',key:'email'},{label:'Phone',key:'phone'}]}
    fields={[{name:'name',label:'Name'},{name:'email',label:'Email',type:'email'},{name:'phone',label:'Phone'}]}/>};
export const Students=()=>
  <Crud title="Students" path="/api/students" canEdit fields={[{name:'name',label:'Name'}]}
    columns={[{label:'Name',key:'name'},{label:'Registered',get:r=>fmt(r.createdAt)}]}/>;
const ClassesView=({ctl,toolbar,footer})=>{const{user}=useAuth(),a=user.role==='ADMIN';
  return <Crud ctl={ctl} toolbar={toolbar} footer={footer} title="Classes" hint="Course schedules with their instructors and dates." path="/api/classes" canCreate={a} canEdit={a} canDelete={a} newLabel="New class"
    columns={[{label:'Class',key:'name'},{label:'Course',get:r=>r.course?.name},{label:'Instructor',get:r=>r.instructor?.name},
      {label:'Days',get:r=>(r.days||[]).map(dayAr).join(', ')},{label:'Period',get:r=>`${r.startDate} to ${r.endDate}`},{label:'Capacity',key:'capacity'}]}
    fields={[{name:'name',label:'Class name'},{name:'course',label:'Course',type:'select',src:'/api/courses'},{name:'instructor',label:'Instructor',type:'select',src:'/api/instructors'},
      {name:'days',label:'Class days',type:'days'},{name:'startDate',label:'Start date',type:'date'},{name:'endDate',label:'End date',type:'date'},{name:'capacity',label:'Capacity',type:'number'}]}
    fromRow={r=>({...r,course:r.course?.id,instructor:r.instructor?.id})}
    toBody={m=>({name:m.name,capacity:+m.capacity,startDate:m.startDate,endDate:m.endDate,days:m.days||[],course:{id:+m.course},instructor:{id:+m.instructor}})}
    actions={[{label:'Enroll me',show:()=>user.role==='STUDENT',run:async r=>{try{await api('/api/enrollmrnts',{method:'POST',body:{aClass:{id:r.id}}});toast('You’re enrolled in this class')}catch(e){toast(e.message,'err')}}}]}/>};
const SORTS=[['','Default order'],['id,desc','Newest first'],['name,asc','Name A–Z'],['name,desc','Name Z–A'],['startDate,asc','Starts soonest'],['startDate,desc','Starts latest'],['capacity,desc','Capacity: high to low'],['capacity,asc','Capacity: low to high']];
export function Classes(){
  const[q,setQ]=useState(''),[dq,setDq]=useState(''),[course,setCourse]=useState(''),[sort,setSort]=useState(''),[page,setPage]=useState(0),[size,setSize]=useState(10),
    [data,setData]=useState(null),[courses,setCourses]=useState([]),[tick,setTick]=useState(0);
  useEffect(()=>{api('/api/courses').then(setCourses).catch(()=>{})},[]);
  useEffect(()=>{const t=setTimeout(()=>{setDq(q);setPage(0)},350);return()=>clearTimeout(t)},[q]);
  const paged=!dq&&!course&&!sort;
  // الباك اند ما يدمج الفلاتر، فنختار endpoint واحد ونكمّل الباقي في المتصفح
  useEffect(()=>{let live=true;setData(null);
    const url=dq?`/api/classes?name=${encodeURIComponent(dq)}`:course?`/api/classes?courseId=${course}`:sort?`/api/classesSort?sort=${sort}`:`/api/classes/pagination?page=${page}&size=${size}`;
    api(url).then(r=>live&&setData(r)).catch(e=>{toast(e.message,'err');live&&setData([])});
    return()=>{live=false}},[dq,course,sort,paged?page:0,paged?size:0,tick]);
  const isPage=!!data&&!Array.isArray(data);let all=isPage?(data.content||[]):(data||[]);
  if(!isPage&&dq&&course)all=all.filter(c=>String(c.course?.id)===String(course));
  if(!isPage&&sort&&(dq||course)){const[k,d]=sort.split(',');all=[...all].sort((x,y)=>(x[k]>y[k]?1:x[k]<y[k]?-1:0)*(d==='desc'?-1:1))}
  const total=isPage?(data.totalElements??data.page?.totalElements??all.length):all.length,pages=Math.max(1,isPage?(data.totalPages??data.page?.totalPages??1):Math.ceil(total/size));
  const rows=data===null?null:isPage?all:all.slice(page*size,page*size+size);
  useEffect(()=>{if(data!==null&&page>pages-1)setPage(pages-1)});
  const start=Math.max(0,Math.min(page-2,pages-5)),nums=Array.from({length:Math.min(5,pages)},(_,i)=>start+i),active=!!(q||course||sort);
  const toolbar=<div className="filters"><input className="search" placeholder="Search classes…" value={q} onChange={e=>setQ(e.target.value)}/>
    <select value={course} onChange={e=>{setCourse(e.target.value);setPage(0)}}><option value="">All courses</option>{courses.map(c=><option key={c.id} value={c.id}>{c.name}</option>)}</select>
    <select value={sort} onChange={e=>{setSort(e.target.value);setPage(0)}}>{SORTS.map(([v,l])=><option key={v} value={v}>{l}</option>)}</select>
    {active&&<button className="btn sm ghost" onClick={()=>{setQ('');setDq('');setCourse('');setSort('');setPage(0)}}>Clear</button>}</div>;
  const footer=<div className="pager"><small className="muted">{total?`${page*size+1}–${Math.min(total,page*size+size)} of ${total}`:'No results'}</small>
    <div className="pgbtns"><select value={size} onChange={e=>{setSize(+e.target.value);setPage(0)}}>{[5,10,20,50].map(n=><option key={n} value={n}>{n} per page</option>)}</select>
      <button className="pg" disabled={page===0} onClick={()=>setPage(page-1)} aria-label="Previous page">‹</button>
      {nums.map(n=><button key={n} className={'pg'+(n===page?' on':'')} onClick={()=>setPage(n)}>{n+1}</button>)}
      <button className="pg" disabled={page>=pages-1} onClick={()=>setPage(page+1)} aria-label="Next page">›</button></div></div>;
  return <ClassesView ctl={{rows,reload:()=>setTick(t=>t+1),filtered:active}} toolbar={toolbar} footer={footer}/>;
}
export const Enrollments=()=>{const a=useAuth().user.role==='ADMIN';
  return <Crud title={a?'Enrollments':'My enrollments'} path={a?'/api/enrollmrnts':'/api/myEnrollmrnt'} deletePath={a?undefined:id=>`/api/myEnrollmrnts/${id}`} canDelete
    columns={[...(a?[{label:'Student',get:r=>r.student?.name}]:[]),{label:'Class',get:r=>r.aClass?.name},{label:'Course',get:r=>r.aClass?.course?.name}]}/>};
export const Audit=()=>
  <Crud title="Audit log" hint="Everything that happened in the system." path="/api/auditLog" sort={(x,y)=>y.id-x.id}
    columns={[{label:'Time',get:r=>fmt(r.createdAt)},{label:'Action',key:'action'},{label:'Entity',get:r=>`${r.entityName||''} ${r.entityId??''}`},{label:'Details',key:'description'},{label:'User',key:'userId'}]}/>;
