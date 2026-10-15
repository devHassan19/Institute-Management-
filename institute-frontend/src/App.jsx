import {createContext,useContext,useEffect,useState} from 'react';
import {Routes,Route,Navigate,NavLink,Outlet} from 'react-router-dom';
import {api,getToken,setToken} from './api.js';
import {Avatar,Login,Register,Forgot,Dashboard,Profile,Courses,Classes,Instructors,Students,Enrollments,Audit} from './pages.jsx';

const Ctx=createContext();export const useAuth=()=>useContext(Ctx);
const IC={'/':'M3 11l9-8 9 8v9a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z','/courses':'M4 19.5A2.5 2.5 0 0 1 6.5 17H20M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z','/classes':'M3 6a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2zM16 2v4M8 2v4M3 10h18','/instructors':'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z','/students':'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75','/enrollments':'M9 11l3 3 8-8M20 12v7a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2h9','/audit':'M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01','/profile':'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20zM12 13a3 3 0 1 0 0-6 3 3 0 0 0 0 6zM6.2 19a6 6 0 0 1 11.6 0'};
const ROLE={ADMIN:'Admin',INSTRUCTOR:'Instructor',STUDENT:'Student'};

function Notifs(){
  const[items,setItems]=useState([]),[open,setOpen]=useState(false),[unread,setUnread]=useState(0);
  useEffect(()=>{const ac=new AbortController();
    (async()=>{try{
      const r=await fetch('/api/notifications/subscribe',{headers:{Authorization:'Bearer '+getToken(),Accept:'text/event-stream'},signal:ac.signal});
      const rd=r.body.getReader(),dec=new TextDecoder();let buf='';
      for(;;){const{done,value}=await rd.read();if(done)break;buf+=dec.decode(value,{stream:true});let i;
        while((i=buf.indexOf('\n\n'))>=0){const ev=buf.slice(0,i);buf=buf.slice(i+2);
          let d=ev.split('\n').filter(l=>l.startsWith('data:')).map(l=>l.slice(5).trim()).join('\n');
          try{const j=JSON.parse(d);d=j.message||j.text||d}catch{}
          if(d&&d!=='connected'){setItems(x=>[{t:d,at:new Date()},...x].slice(0,20));setUnread(n=>n+1)}}}
    }catch{}})();
    return()=>ac.abort()},[]);
  return <div className="bell"><button className="icon" aria-label="Notifications" onClick={()=>{setOpen(!open);setUnread(0)}}>
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><path d="M6 9a6 6 0 1 1 12 0c0 6 2 7 2 7H4s2-1 2-7M10 20a2 2 0 0 0 4 0"/></svg>
    {unread>0&&<i>{unread}</i>}</button>
    {open&&<div className="pop">{items.length?items.map((n,k)=><p key={k}>{n.t}<small>{n.at.toLocaleTimeString('en')}</small></p>):<p className="muted">No new notifications</p>}</div>}</div>;
}

function Layout(){
  const{user,logout}=useAuth(),a=user.role==='ADMIN',s=user.role==='STUDENT',[toast,setToast]=useState(null);
  const nav=[['/','Dashboard'],['/courses','Courses'],['/classes','Classes'],['/instructors','Instructors'],
    ...(s?[['/enrollments','My enrollments']]:[]),...(a?[['/students','Students'],['/enrollments','Enrollments'],['/audit','Audit log']]:[]),['/profile','Profile']];
  useEffect(()=>{const h=e=>{setToast(e.detail);setTimeout(()=>setToast(null),2800)};window.addEventListener('toast',h);return()=>window.removeEventListener('toast',h)},[]);
  return <div className="shell"><aside><div className="brand"><b>I</b>Institute</div>
    <nav>{nav.map(([to,l])=><NavLink key={to} to={to} end={to==='/'}><svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d={IC[to]}/></svg>{l}</NavLink>)}</nav>
    <button className="ghost" onClick={logout}>Sign out</button></aside>
    <main><header><div><small className="muted">{ROLE[user.role]||user.role}</small><strong>{user.firstName||user.username}</strong></div><Avatar user={user} size={42}/><Notifs/></header><Outlet/></main>
    {toast&&<div className="toastwrap"><div className={'toast '+toast.t} role="status"><span className="ti">{toast.t==='err'?'!':'✓'}</span>{toast.m}</div></div>}</div>;
}

export default function App(){
  const[user,setUser]=useState(null),[ready,setReady]=useState(false);
  const load=async()=>{if(!getToken()){setUser(null);return setReady(true)}
    try{setUser(await api('/api/me'))}catch{setUser({role:'STUDENT',username:'User'})}setReady(true)};
  useEffect(()=>{load()},[]);
  const value={user,reload:load,
    login:async(email,password)=>{const r=await api('/auth/users/login',{method:'POST',body:{email,password}});setToken(r.message);await load()},
    logout:()=>{setToken(null);setUser(null)}};
  if(!ready)return null;
  return <Ctx.Provider value={value}><Routes>
    <Route path="/login" element={user?<Navigate to="/"/>:<Login/>}/><Route path="/register" element={<Register/>}/><Route path="/forgot" element={<Forgot/>}/>
    <Route element={user?<Layout/>:<Navigate to="/login"/>}>
      <Route index element={<Dashboard/>}/><Route path="courses" element={<Courses/>}/><Route path="classes" element={<Classes/>}/>
      <Route path="instructors" element={<Instructors/>}/><Route path="students" element={<Students/>}/>
      <Route path="enrollments" element={<Enrollments/>}/><Route path="audit" element={<Audit/>}/><Route path="profile" element={<Profile/>}/>
    </Route><Route path="*" element={<Navigate to="/"/>}/></Routes></Ctx.Provider>;
}
