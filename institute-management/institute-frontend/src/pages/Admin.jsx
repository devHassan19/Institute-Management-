import ResourcePage from '../components/ResourcePage.jsx';
import {Badge} from '../components/ui.jsx';
import {fmtDate,fmtDateTime} from '../api.js';

export function Courses(){
  return <ResourcePage title="Courses" subtitle="Subjects offered by the institute." path="/api/courses"
    icon="book" emptyText="No courses yet" newLabel="New course"
    canCreate canEdit canDelete searchKeys={['name','description']}
    columns={[{label:'Name',key:'name'},{label:'Duration',key:'duration'},{label:'Description',key:'description'}]}
    fields={[
      {name:'name',label:'Course name'},
      {name:'duration',label:'Duration (hours)',type:'number'},
      {name:'description',label:'Description',type:'textarea',required:false},
    ]}
    toBody={m=>({name:m.name,description:m.description,duration:+m.duration})}/>;
}

export function Instructors(){
  return <ResourcePage title="Instructors" subtitle="Teachers who run the classes." path="/api/instructors"
    icon="users" emptyText="No instructors yet" newLabel="New instructor"
    canCreate canEdit canDelete searchKeys={['name','email','phone']}
    columns={[{label:'Name',key:'name'},{label:'Email',key:'email'},{label:'Phone',key:'phone'}]}
    fields={[
      {name:'name',label:'Full name'},
      {name:'email',label:'Email',type:'email'},
      {name:'phone',label:'Phone'},
    ]}/>;
}

export function Students(){
  return <ResourcePage title="Students" subtitle="Everyone registered as a student." path="/api/students"
    icon="user" emptyText="No students yet" searchKeys={['name']}
    columns={[{label:'Name',key:'name'},{label:'Registered',render:r=>fmtDate(r.createdAt)}]}/>;
}

export function Enrollments(){
  return <ResourcePage title="Enrollments" subtitle="Every confirmed enrollment across the institute." path="/api/enrollmrnts"
    icon="list" emptyText="No enrollments yet" canDelete searchKeys={['student.name','aClass.name']}
    columns={[
      {label:'Student',key:'student.name'},
      {label:'Class',key:'aClass.name'},
      {label:'Course',key:'aClass.course.name'},
    ]}/>;
}

export function Audit(){
  return <ResourcePage title="Audit log" subtitle="Every important action taken in the system." path="/api/auditLog"
    icon="shield" emptyText="No activity yet" searchKeys={['action','description','entityName']}
    columns={[
      {label:'Time',render:r=>fmtDateTime(r.createdAt)},
      {label:'Action',render:r=><Badge s={r.action}/>},
      {label:'Entity',render:r=>`${r.entityName||'—'} #${r.entityId??'—'}`},
      {label:'Details',key:'description'},
      {label:'User',key:'userId'},
    ]}/>;
}
