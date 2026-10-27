import {useEffect,useState} from 'react';
import {NavLink,Outlet,useLocation,useNavigate} from 'react-router-dom';
import {useAuth} from '../context.js';
import {ago,api,emit,getToken,ROLE_LABEL} from '../api.js';
import {Avatar,Icon} from './ui.jsx';

const NAV={
  ADMIN:[
    ['/','Overview','home'],['/classes','Classes','calendar'],['/courses','Courses','book'],
    ['/instructors','Instructors','users'],['/students','Students','user'],
    ['/enrollments','Enrollments','list'],['/audit','Audit log','shield'],
  ],
  STUDENT:[
    ['/','Overview','home'],['/classes','Browse classes','calendar'],['/reservations','Reservations','seat'],
    ['/my-enrollments','My enrollments','book'],['/notifications','Notifications','bell'],
  ],
};

// الجرس: يستقبل الإشعارات لحظياً (SSE) ويعرض آخر الرسائل
function Bell(){
  const[open,setOpen]=useState(false);
  const[unread,setUnread]=useState(0);
  const[recent,setRecent]=useState([]);
  const nav=useNavigate();

  useEffect(()=>{
    let alive=true;
    const ac=new AbortController();
    const load=()=>api('/api/notifications/list').then(d=>alive&&setRecent(d.slice(0,6))).catch(()=>{});
    load();

    (async()=>{
      while(alive){
        try{
          const res=await fetch('/api/notifications/subscribe',{
            headers:{Authorization:`Bearer ${getToken()}`,Accept:'text/event-stream'},
            signal:ac.signal,
          });
          const reader=res.body.getReader();
          const dec=new TextDecoder();
          let buf='';
          for(;;){
            const{done,value}=await reader.read();
            if(done)break;
            buf+=dec.decode(value,{stream:true});
            let i;
            while((i=buf.indexOf('\n\n'))>=0){
              const block=buf.slice(0,i);
              buf=buf.slice(i+2);
              const msg=block.split('\n').filter(l=>l.startsWith('data:')).map(l=>l.slice(5).trim()).join('\n');
              if(!msg||msg==='connected')continue;
              setUnread(n=>n+1);
              emit('toast',{type:'notify',message:msg});
              window.dispatchEvent(new Event('inst:refresh'));
              window.dispatchEvent(new Event('inst:notify'));
              load();
            }
          }
        }catch(e){
          if(ac.signal.aborted)return;
        }
        await new Promise(r=>setTimeout(r,3000));
      }
    })();

    return()=>{alive=false;ac.abort()};
  },[]);

  return <div className="bell">
    <button className="icon-btn" onClick={()=>{setOpen(o=>!o);setUnread(0)}} aria-label="Notifications">
      <Icon name="bell"/>
      {unread>0&&<i className="dot-count">{unread}</i>}
    </button>
    {open&&<div className="pop" onMouseLeave={()=>setOpen(false)}>
      <div className="pop-head"><b>Notifications</b><button className="link" onClick={()=>{setOpen(false);nav('/notifications')}}>View all</button></div>
      {recent.length
        ?recent.map(n=><div className="pop-item" key={n.id}><p>{n.message}</p><small>{ago(n.createdAt)}</small></div>)
        :<p className="pop-empty">No notifications yet</p>}
    </div>}
  </div>;
}

export default function Layout(){
  const{user,logout}=useAuth();
  const{pathname}=useLocation();
  const items=NAV[user.role]||NAV.STUDENT;
  const title=items.find(i=>i[0]===pathname)?.[1]||(pathname==='/profile'?'Profile':'');

  return <div className="shell">
    <aside className="side">
      <div className="brand">
        <span className="logo"><Icon name="spark" size={18}/></span>
        <div><b>Institute</b><small>Management</small></div>
      </div>
      <nav>
        {items.map(([to,label,ic])=><NavLink key={to} to={to} end={to==='/'} className="nav-link">
          <Icon name={ic}/><span>{label}</span>
        </NavLink>)}
      </nav>
      <div className="me">
        <NavLink to="/profile" className="me-link"><Avatar user={user} size={38}/></NavLink>
        <div><b>{user.firstName||user.username}</b><small>{ROLE_LABEL[user.role]||user.role}</small></div>
        <button className="icon-btn" title="Sign out" onClick={logout} aria-label="Sign out"><Icon name="logout"/></button>
      </div>
    </aside>

    <div className="main">
      <header className="top">
        <div><small>{ROLE_LABEL[user.role]} workspace</small><h3>{title}</h3></div>
        <div className="top-r">
          {user.role==='STUDENT'&&<Bell/>}
          <NavLink to="/profile" className="avatar-link"><Avatar user={user} size={40}/></NavLink>
        </div>
      </header>
      <main className="content"><Outlet/></main>
    </div>
  </div>;
}
