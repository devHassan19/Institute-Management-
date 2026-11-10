import {useState} from 'react';
import {Link,useNavigate,useSearchParams} from 'react-router-dom';
import {useAuth} from '../context.js';
import {api,toast} from '../api.js';
import {Button,Field,Icon} from '../components/ui.jsx';

function Shell({title,sub,foot,children}){
  return <div className="auth">
    <div className="auth-art">
      <div className="blob b1"/><div className="blob b2"/><div className="blob b3"/>
      <div className="auth-copy">
        <span className="logo lg"><Icon name="spark" size={26}/></span>
        <h1>Learn with purpose.</h1>
        <p>Hold your seat, pay within the timer, and follow every step from one calm dashboard.</p>
        <ul>
          <li><Icon name="seat" size={16}/> Live seat holds with a countdown</li>
          <li><Icon name="bell" size={16}/> Instant updates when a seat opens</li>
          <li><Icon name="card" size={16}/> Simple, quick payment</li>
        </ul>
      </div>
    </div>
    <div className="auth-form">
      <div className="auth-card">
        <h2>{title}</h2>
        <p className="sub">{sub}</p>
        {children}
        {foot&&<div className="auth-foot">{foot}</div>}
      </div>
    </div>
  </div>;
}

export function Login(){
  const{login}=useAuth();
  const[email,setEmail]=useState('');
  const[password,setPassword]=useState('');
  const[err,setErr]=useState('');
  const[busy,setBusy]=useState(false);

  async function submit(e){
    e.preventDefault();
    setBusy(true);setErr('');
    try{await login(email.trim(),password)}
    catch(x){setErr(x.message);setBusy(false)}
  }

  return <Shell title="Welcome back" sub="Sign in to reserve seats and manage your classes."
    foot={<>New here? <Link to="/register">Create an account</Link></>}>
    <form className="stack" onSubmit={submit}>
      <Field label="Email"><input type="email" required value={email} onChange={e=>setEmail(e.target.value)} autoComplete="email"/></Field>
      <Field label="Password"><input type="password" required value={password} onChange={e=>setPassword(e.target.value)} autoComplete="current-password"/></Field>
      {err&&<div className="alert-err">{err}</div>}
      <Button type="submit" className="block" disabled={busy}>{busy?'Signing in…':'Sign in'}</Button>
      <Link className="link center" to="/forgot">Forgot password?</Link>
    </form>
  </Shell>;
}

export function Register(){
  const nav=useNavigate();
  const[f,setF]=useState({firstName:'',lastName:'',username:'',emailAddress:'',mobileNumber:'',password:''});
  const[img,setImg]=useState(null);
  const[busy,setBusy]=useState(false);
  const[err,setErr]=useState('');
  const set=k=>e=>setF(x=>({...x,[k]:e.target.value}));

  async function submit(e){
    e.preventDefault();
    setBusy(true);setErr('');
    const fd=new FormData();
    Object.entries(f).forEach(([k,v])=>fd.append(k,v));
    if(img)fd.append('image',img);
    try{
      await api('/auth/users/register',{method:'POST',form:fd});
      toast('Account created. Check your email to verify it, then sign in.','ok','Welcome!');
      nav('/login');
    }catch(x){setErr(x.message)}
    finally{setBusy(false)}
  }

  return <Shell title="Create your account" sub="Register as a student and start reserving seats."
    foot={<>Already registered? <Link to="/login">Sign in</Link></>}>
    <form className="stack" onSubmit={submit}>
      <div className="form-grid">
        <Field label="First name"><input required value={f.firstName} onChange={set('firstName')}/></Field>
        <Field label="Last name"><input required value={f.lastName} onChange={set('lastName')}/></Field>
      </div>
      <Field label="Username"><input required value={f.username} onChange={set('username')}/></Field>
      <Field label="Email"><input type="email" required value={f.emailAddress} onChange={set('emailAddress')}/></Field>
      <Field label="Mobile number"><input required value={f.mobileNumber} onChange={set('mobileNumber')}/></Field>
      <Field label="Password" hint="At least 6 characters"><input type="password" required minLength={6} value={f.password} onChange={set('password')}/></Field>
      <Field label="Profile photo (optional)"><input type="file" accept="image/*" onChange={e=>setImg(e.target.files[0]||null)}/></Field>
      {err&&<div className="alert-err">{err}</div>}
      <Button type="submit" className="block" disabled={busy}>{busy?'Creating…':'Create account'}</Button>
    </form>
  </Shell>;
}

export function Forgot(){
  const[sp]=useSearchParams();
  const nav=useNavigate();
  const[step,setStep]=useState(sp.get('token')?2:1);
  const[email,setEmail]=useState('');
  const[token,setToken]=useState(sp.get('token')||'');
  const[pw,setPw]=useState('');
  const[busy,setBusy]=useState(false);
  const[err,setErr]=useState('');

  async function submit(e){
    e.preventDefault();
    setBusy(true);setErr('');
    try{
      if(step===1){
        await api('/auth/users/forgot-password',{method:'POST',body:{email}});
        toast('We sent a reset code to your email','info');
        setStep(2);
      }else{
        await api('/auth/users/reset-password',{method:'POST',body:{token,newPassword:pw}});
        toast('Password updated. Please sign in.');
        nav('/login');
      }
    }catch(x){setErr(x.message)}
    finally{setBusy(false)}
  }

  return <Shell title="Reset password"
    sub={step===1?'Enter your email and we will send you a reset code.':'Paste the code you received and choose a new password.'}
    foot={<Link to="/login">Back to sign in</Link>}>
    <form className="stack" onSubmit={submit}>
      {step===1
        ?<Field label="Email"><input type="email" required value={email} onChange={e=>setEmail(e.target.value)}/></Field>
        :<>
          <Field label="Reset code"><input required value={token} onChange={e=>setToken(e.target.value)}/></Field>
          <Field label="New password"><input type="password" required minLength={6} value={pw} onChange={e=>setPw(e.target.value)}/></Field>
        </>}
      {err&&<div className="alert-err">{err}</div>}
      <Button type="submit" className="block" disabled={busy}>{step===1?'Send code':'Save password'}</Button>
      {step===1&&<button type="button" className="link center" onClick={()=>setStep(2)}>I already have a code</button>}
    </form>
  </Shell>;
}
