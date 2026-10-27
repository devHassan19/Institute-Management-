import {useState} from 'react';
import {useAuth} from '../context.js';
import {api,ROLE_LABEL,toast} from '../api.js';
import {Avatar,Button,Field,Icon} from '../components/ui.jsx';

export default function Profile(){
  const{user,reload}=useAuth();
  const[f,setF]=useState({
    firstName:user.firstName||'',
    lastName:user.lastName||'',
    mobileNumber:user.mobileNumber||'',
  });
  const[img,setImg]=useState(null);
  const[busy,setBusy]=useState(false);
  const[pw,setPw]=useState({oldPassword:'',newPassword:''});
  const preview=img?URL.createObjectURL(img):null;
  const set=k=>e=>setF(x=>({...x,[k]:e.target.value}));

  async function save(e){
    e.preventDefault();
    setBusy(true);
    const fd=new FormData();
    Object.entries(f).forEach(([k,v])=>fd.append(k,v));
    if(img)fd.append('image',img);
    try{
      await api('/api/me',{method:'PUT',form:fd});
      toast('Profile saved');
      setImg(null);
      reload();
    }catch(err){toast(err.message,'err')}
    finally{setBusy(false)}
  }

  async function changePw(e){
    e.preventDefault();
    try{
      await api('/auth/users/changePassword',{method:'PUT',body:pw});
      toast('Password updated');
      setPw({oldPassword:'',newPassword:''});
    }catch(err){toast(err.message,'err')}
  }

  return <section className="page">
    <div className="phead"><div><h1>My profile</h1><p>Keep your details and photo up to date.</p></div></div>
    <div className="profile-grid">
      <div className="panel profile-card">
        <label className="avatar-pick">
          <Avatar user={user} size={112} src={preview}/>
          <span className="avatar-edit"><Icon name="edit" size={14}/></span>
          <input type="file" accept="image/*" hidden onChange={e=>setImg(e.target.files[0]||null)}/>
        </label>
        <h2>{[user.firstName,user.lastName].filter(Boolean).join(' ')||user.username}</h2>
        <span className="badge violet">{ROLE_LABEL[user.role]||user.role}</span>
        <div className="kv"><span>Username</span><b>{user.username}</b></div>
        <div className="kv"><span>Email</span><b>{user.email}</b></div>
        {img&&<p className="muted small">New photo selected. Save to apply it.</p>}
      </div>

      <div className="stack">
        <form className="panel pad stack" onSubmit={save}>
          <h2>Personal details</h2>
          <div className="form-grid">
            <Field label="First name"><input required value={f.firstName} onChange={set('firstName')}/></Field>
            <Field label="Last name"><input required value={f.lastName} onChange={set('lastName')}/></Field>
          </div>
          <Field label="Mobile number"><input required value={f.mobileNumber} onChange={set('mobileNumber')}/></Field>
          <div className="modal-foot"><Button type="submit" disabled={busy}>{busy?'Saving…':'Save changes'}</Button></div>
        </form>

        <form className="panel pad stack" onSubmit={changePw}>
          <h2>Change password</h2>
          <Field label="Current password"><input type="password" required value={pw.oldPassword} onChange={e=>setPw(x=>({...x,oldPassword:e.target.value}))}/></Field>
          <Field label="New password" hint="At least 6 characters"><input type="password" required minLength={6} value={pw.newPassword} onChange={e=>setPw(x=>({...x,newPassword:e.target.value}))}/></Field>
          <div className="modal-foot"><Button variant="ghost" type="submit">Update password</Button></div>
        </form>
      </div>
    </div>
  </section>;
}
