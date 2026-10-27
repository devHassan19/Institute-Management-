import {useEffect,useState} from 'react';

const P={
  home:'M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z',
  book:'M4 4.5A1.5 1.5 0 0 1 5.5 3H20v15H5.5A1.5 1.5 0 0 0 4 19.5zM4 19.5A1.5 1.5 0 0 0 5.5 21H20v-3',
  calendar:'M3 5h18v16H3zM3 10h18M8 3v4M16 3v4',
  users:'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM22 21v-2a4 4 0 0 0-3-3.9M16 3.1a4 4 0 0 1 0 7.8',
  user:'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z',
  bell:'M18 8a6 6 0 1 0-12 0c0 7-3 9-3 9h18s-3-2-3-9M13.7 21a2 2 0 0 1-3.4 0',
  shield:'M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z',
  layers:'M12 2 2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5',
  logout:'M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9',
  search:'M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16zM21 21l-4.3-4.3',
  plus:'M12 5v14M5 12h14',
  edit:'M12 20h9M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4z',
  trash:'M3 6h18M8 6V4h8v2M6 6l1 15h10l1-15',
  check:'M20 6 9 17l-5-5',
  clock:'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20zM12 6v6l4 2',
  card:'M2 6h20v12H2zM2 10h20M6 15h4',
  x:'M18 6 6 18M6 6l12 12',
  seat:'M6 11V6a3 3 0 0 1 3-3h6a3 3 0 0 1 3 3v5M4 11h16v5H4zM6 16v3M18 16v3',
  list:'M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01',
  spark:'M12 3v4M12 17v4M3 12h4M17 12h4M5.6 5.6l2.8 2.8M15.6 15.6l2.8 2.8M5.6 18.4l2.8-2.8M15.6 8.4l2.8-2.8',
  arrow:'M5 12h14M13 6l6 6-6 6',
  lock:'M5 11h14v10H5zM8 11V7a4 4 0 0 1 8 0v4',
  mail:'M3 5h18v14H3zM3 6l9 7 9-7',
  phone:'M22 16.9v3a2 2 0 0 1-2.2 2 19.8 19.8 0 0 1-8.6-3.1 19.5 19.5 0 0 1-6-6A19.8 19.8 0 0 1 2.1 4.2 2 2 0 0 1 4.1 2h3a2 2 0 0 1 2 1.7c.1 1 .4 1.9.7 2.8a2 2 0 0 1-.5 2.1L8 9.9a16 16 0 0 0 6 6l1.3-1.3a2 2 0 0 1 2.1-.4c.9.3 1.8.6 2.8.7a2 2 0 0 1 1.7 2z',
};

export function Icon({name,size=18,className=''}){
  return <svg className={`ic ${className}`} width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d={P[name]||P.layers}/></svg>;
}

export function Button({variant='primary',size,icon,className='',children,...rest}){
  return <button className={`btn ${variant} ${size||''} ${className}`.trim()} {...rest}>{icon&&<Icon name={icon} size={16}/>}{children}</button>;
}

const TONE={HELD:'warm',CONFIRMED:'good',COMPLETED:'good',PAID:'good',ACTIVE:'good',EXPIRED:'bad',CANCELLED:'bad',FAILED:'bad',ERROR:'bad',INACTIVE:'bad',WAITING:'violet',WAITLISTED:'violet',OFFERED:'sky',REFUNDED:'sky',HELD_OFFER:'sky'};
export function Badge({s}){
  return <span className={`badge ${TONE[s]||''}`}>{String(s).replace(/_/g,' ')}</span>;
}

export function Avatar({user,size=40,src}){
  const img=src||(user?.image?`/${user.image}`:null);
  const letter=(user?.firstName||user?.username||'?').charAt(0).toUpperCase();
  return <span className="av" style={{width:size,height:size,fontSize:Math.round(size*.38)}}>{img?<img src={img} alt=""/>:letter}</span>;
}

export function Field({label,hint,children}){
  return <label className="field"><span>{label}</span>{children}{hint&&<small>{hint}</small>}</label>;
}

export function Modal({open,onClose,title,wide,closable=true,children}){
  useEffect(()=>{
    if(!open)return;
    const onKey=e=>{if(e.key==='Escape'&&closable)onClose?.()};
    document.addEventListener('keydown',onKey);
    const prev=document.body.style.overflow;
    document.body.style.overflow='hidden';
    return()=>{document.removeEventListener('keydown',onKey);document.body.style.overflow=prev};
  },[open,closable,onClose]);
  if(!open)return null;
  return <div className="overlay" onMouseDown={()=>closable&&onClose?.()}>
    <div className={`modal ${wide?'wide':''}`} role="dialog" aria-modal="true" onMouseDown={e=>e.stopPropagation()}>
      {title&&<div className="modal-head"><h2>{title}</h2>{closable&&<button className="icon-btn" onClick={onClose} aria-label="Close"><Icon name="x"/></button>}</div>}
      {children}
    </div>
  </div>;
}

export function Empty({icon='layers',title,text,action}){
  return <div className="empty"><div className="eico"><Icon name={icon} size={26}/></div><b>{title}</b>{text&&<p>{text}</p>}{action}</div>;
}

export function Skeleton({h=56}){
  return <div className="skeleton" style={{height:h}}/>;
}

export function Stat({label,value,icon,tone='pri'}){
  return <div className="stat"><span className={`si ${tone}`}><Icon name={icon} size={20}/></span><div><span>{label}</span><strong>{value}</strong></div></div>;
}

export function PageHeader({title,subtitle,actions}){
  return <div className="phead"><div><h1>{title}</h1>{subtitle&&<p>{subtitle}</p>}</div>{actions&&<div className="phead-actions">{actions}</div>}</div>;
}

const TOAST_TITLE={ok:'Done',err:'Something went wrong',info:'Heads up',notify:'New notification'};
export function Toaster(){
  const[items,setItems]=useState([]);
  useEffect(()=>{
    const onToast=e=>{
      const id=`${Date.now()}-${Math.random()}`;
      setItems(x=>[...x.slice(-2),{id,...e.detail}]);
      setTimeout(()=>setItems(x=>x.filter(i=>i.id!==id)),e.detail.duration||4500);
    };
    window.addEventListener('toast',onToast);
    return()=>window.removeEventListener('toast',onToast);
  },[]);
  return <div className="toasts" aria-live="polite">
    {items.map(t=>{
      const type=t.type||'ok';
      const icon=type==='err'?'x':type==='notify'?'bell':type==='info'?'clock':'check';
      return <div key={t.id} className={`toast ${type}`}>
        <span className="toast-ic"><Icon name={icon} size={16}/></span>
        <div><b>{t.title||TOAST_TITLE[type]}</b><p>{t.message}</p></div>
      </div>;
    })}
  </div>;
}
