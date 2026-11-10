import {useState} from 'react';
import {api,refresh,toast} from '../api.js';
import {useLoader} from '../hooks.js';
import {Button,Empty,Field,Icon,Modal,PageHeader,Skeleton} from './ui.jsx';

const get=(o,k)=>k.split('.').reduce((a,p)=>a?.[p],o);
const toForm=r=>{const f={...r};if(r.course)f.course=r.course.id;if(r.instructor)f.instructor=r.instructor.id;return f};

function SelectField({label,path,value,onChange}){
  const{data}=useLoader(()=>api(path));
  return <Field label={label}>
    <select required value={value??''} onChange={e=>onChange(e.target.value)}>
      <option value="">Select…</option>
      {(data||[]).map(o=><option key={o.id} value={o.id}>{o.name}</option>)}
    </select>
  </Field>;
}

// صفحة جدول عامة: بحث، إضافة، تعديل، حذف (حسب الصلاحيات)
export default function ResourcePage({
  title,subtitle,path,columns,searchKeys=[],fields=[],toBody=x=>x,
  canCreate=false,canEdit=false,canDelete=false,newLabel='Add',icon='layers',emptyText='Nothing here yet',
}){
  const{data,error,reload}=useLoader(()=>api(path));
  const[q,setQ]=useState('');
  const[form,setForm]=useState(null);
  const[saving,setSaving]=useState(false);

  const s=q.trim().toLowerCase();
  const rows=(data||[]).filter(r=>!s||searchKeys.some(k=>String(get(r,k)??'').toLowerCase().includes(s)));
  const actions=canEdit||canDelete;

  async function save(e){
    e.preventDefault();
    setSaving(true);
    const id=form.id;
    try{
      await api(id?`${path}/${id}`:path,{method:id?'PUT':'POST',body:toBody(form)});
      toast(id?'Saved':'Added');
      setForm(null);reload();refresh();
    }catch(err){toast(err.message,'err')}
    finally{setSaving(false)}
  }

  async function remove(r){
    if(!window.confirm('Delete this item? This cannot be undone.'))return;
    try{
      await api(`${path}/${r.id}`,{method:'DELETE'});
      toast('Deleted');reload();refresh();
    }catch(err){toast(err.message,'err')}
  }

  const setField=(k,v)=>setForm(x=>({...x,[k]:v}));

  return <section className="page">
    <PageHeader title={title} subtitle={subtitle} actions={<>
      <div className="search"><Icon name="search" size={16}/><input placeholder="Search…" value={q} onChange={e=>setQ(e.target.value)}/></div>
      {canCreate&&<Button icon="plus" onClick={()=>setForm({})}>{newLabel}</Button>}
    </>}/>

    <div className="panel">
      {error
        ?<Empty icon="layers" title="Couldn't load data" text={error.message}/>
        :data===null
          ?<div className="stack-s pad">{[0,1,2,3].map(i=><Skeleton key={i} h={46}/>)}</div>
          :!rows.length
            ?<Empty icon={icon} title={q?'No matches':emptyText} text={q?'Try a different search term.':''}/>
            :<div className="table-wrap">
              <table className="tbl">
                <thead><tr>{columns.map(c=><th key={c.label}>{c.label}</th>)}{actions&&<th className="r">Actions</th>}</tr></thead>
                <tbody>
                  {rows.map(r=><tr key={r.id}>
                    {columns.map(c=><td key={c.label}>{c.render?c.render(r):(get(r,c.key)??'—')}</td>)}
                    {actions&&<td className="r"><div className="row-actions">
                      {canEdit&&<button className="icon-btn sm" onClick={()=>setForm(toForm(r))} aria-label="Edit"><Icon name="edit" size={15}/></button>}
                      {canDelete&&<button className="icon-btn sm danger" onClick={()=>remove(r)} aria-label="Delete"><Icon name="trash" size={15}/></button>}
                    </div></td>}
                  </tr>)}
                </tbody>
              </table>
            </div>}
    </div>

    <Modal open={!!form} onClose={()=>setForm(null)} title={form?.id?`Edit ${title.toLowerCase()}`:`New ${title.toLowerCase()}`}>
      {form&&<form className="stack" onSubmit={save}>
        {fields.map(f=>f.select
          ?<SelectField key={f.name} label={f.label} path={f.select} value={form[f.name]} onChange={v=>setField(f.name,v)}/>
          :f.type==='textarea'
            ?<Field key={f.name} label={f.label}><textarea rows={3} required={f.required!==false} value={form[f.name]??''} onChange={e=>setField(f.name,e.target.value)}/></Field>
            :<Field key={f.name} label={f.label}><input type={f.type||'text'} step={f.type==='number'?'any':undefined} required={f.required!==false} value={form[f.name]??''} onChange={e=>setField(f.name,e.target.value)}/></Field>
        )}
        <div className="modal-foot">
          <Button variant="ghost" type="button" onClick={()=>setForm(null)}>Cancel</Button>
          <Button type="submit" disabled={saving}>{saving?'Saving…':'Save'}</Button>
        </div>
      </form>}
    </Modal>
  </section>;
}
