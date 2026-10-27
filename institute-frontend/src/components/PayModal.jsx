import {useState} from 'react';
import {api,emit,money,toast,ts} from '../api.js';
import {dismissed} from '../store.js';
import {useNow} from '../hooks.js';
import {Button,Icon,Modal} from './ui.jsx';
import Countdown from './Countdown.jsx';

const METHODS=[
  {k:'CARD',l:'Card',d:'Visa · Mastercard',i:'card'},
  {k:'BENEFIT_PAY',l:'BenefitPay',d:'Pay from your phone',i:'phone'},
  {k:'APPLE_PAY',l:'Apple Pay',d:'One tap',i:'check'},
];

export default function PayModal({reservation:r,onClose}){
  const[method,setMethod]=useState('CARD');
  const[state,setState]=useState('idle'); // idle | busy | done
  const now=useNow(500);
  const expired=(ts(r.expiresAt)?.getTime()??0)<=now;
  const busy=state==='busy';

  const dismiss=()=>{dismissed.add(r.id);onClose()};

  async function pay(){
    setState('busy');
    try{
      await new Promise(res=>setTimeout(res,1200)); // محاكاة للبوابة
      await api(`/api/reservations/${r.id}/payment`,{method:'POST',body:{method}});
      dismissed.add(r.id);
      setState('done');
      emit('inst:refresh');
    }catch(e){
      toast(e.message,'err');
      setState('idle');
      emit('inst:refresh');
      if([403,409,422].includes(e.status))dismiss();
    }
  }

  return <Modal open onClose={dismiss} closable={!busy} wide>
    {state==='done'
      ?<div className="pay-done">
          <div className="tick"><Icon name="check" size={34}/></div>
          <h2>Payment received</h2>
          <p>You are enrolled in <b>{r.className}</b>. See you in class.</p>
          <Button onClick={onClose} className="block">Done</Button>
        </div>
      :<>
        <div className="pay-head">
          <span className="eyebrow">{r.courseName||'Class'}</span>
          <h2>{r.className}</h2>
        </div>
        <div className="pay-body">
          <Countdown expiresAt={r.expiresAt} reservedAt={r.reservedAt} size={132}/>
          <div className="pay-sum">
            <small>Amount due</small>
            <strong>{money(r.fee)}</strong>
            <span>{expired?'This hold has expired. Reserve again to get a seat.':'Your seat is held until the timer reaches zero.'}</span>
          </div>
        </div>
        <p className="label">Payment method</p>
        <div className="methods">
          {METHODS.map(m=><button type="button" key={m.k} className={method===m.k?'on':''} onClick={()=>setMethod(m.k)} disabled={busy}>
            <Icon name={m.i} size={20}/><b>{m.l}</b><small>{m.d}</small>
          </button>)}
        </div>
        <div className="modal-foot">
          <Button variant="ghost" onClick={dismiss} disabled={busy}>Pay later</Button>
          <Button onClick={pay} disabled={busy||expired}>{busy?'Processing…':expired?'Seat expired':`Pay ${money(r.fee)}`}</Button>
        </div>
      </>}
  </Modal>;
}
