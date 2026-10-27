import {useCallback,useEffect,useState} from 'react';
import {Navigate,Route,Routes,useNavigate} from 'react-router-dom';
import {AuthCtx,useAuth} from './context.js';
import {api,getToken,setToken,toast} from './api.js';
import Layout from './components/Layout.jsx';
import {Icon,Toaster} from './components/ui.jsx';
import {Forgot,Login,Register} from './pages/Auth.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Classes from './pages/Classes.jsx';
import Reservations from './pages/Reservations.jsx';
import MyEnrollments from './pages/MyEnrollments.jsx';
import Notifications from './pages/Notifications.jsx';
import Profile from './pages/Profile.jsx';
import {Audit,Courses,Enrollments,Instructors,Students} from './pages/Admin.jsx';

function OnlyRole({roles,children}){
  const{user}=useAuth();
  return roles.includes(user.role)?children:<Navigate to="/" replace/>;
}

function Splash(){
  return <div className="splash"><div className="logo lg"><Icon name="spark" size={26}/></div></div>;
}

export default function App(){
  const[user,setUser]=useState(null);
  const[ready,setReady]=useState(false);
  const nav=useNavigate();

  const loadMe=useCallback(async()=>{
    if(!getToken()){setUser(null);return}
    try{setUser(await api('/api/me'))}
    catch{setToken(null);setUser(null)}
  },[]);

  useEffect(()=>{loadMe().finally(()=>setReady(true))},[loadMe]);

  useEffect(()=>{
    const onExpired=()=>{
      setUser(null);
      toast('Your session has expired. Please sign in again.','err','Signed out');
      nav('/login');
    };
    window.addEventListener('auth:expired',onExpired);
    return()=>window.removeEventListener('auth:expired',onExpired);
  },[nav]);

  const login=async(email,password)=>{
    const r=await api('/auth/users/login',{method:'POST',body:{email,password}});
    setToken(r.message);
    await loadMe();
    toast('Welcome back');
  };

  const logout=()=>{
    setToken(null);
    setUser(null);
    toast('You have signed out','info');
    nav('/login');
  };

  if(!ready)return <Splash/>;

  return <AuthCtx.Provider value={{user,login,logout,reload:loadMe}}>
    <Routes>
      <Route path="/login" element={user?<Navigate to="/" replace/>:<Login/>}/>
      <Route path="/register" element={user?<Navigate to="/" replace/>:<Register/>}/>
      <Route path="/forgot" element={user?<Navigate to="/" replace/>:<Forgot/>}/>

      <Route element={user?<Layout/>:<Navigate to="/login" replace/>}>
        <Route index element={<Dashboard/>}/>
        <Route path="classes" element={<Classes/>}/>
        <Route path="reservations" element={<OnlyRole roles={['STUDENT']}><Reservations/></OnlyRole>}/>
        <Route path="my-enrollments" element={<OnlyRole roles={['STUDENT']}><MyEnrollments/></OnlyRole>}/>
        <Route path="notifications" element={<OnlyRole roles={['STUDENT']}><Notifications/></OnlyRole>}/>
        <Route path="courses" element={<OnlyRole roles={['ADMIN']}><Courses/></OnlyRole>}/>
        <Route path="instructors" element={<OnlyRole roles={['ADMIN']}><Instructors/></OnlyRole>}/>
        <Route path="students" element={<OnlyRole roles={['ADMIN']}><Students/></OnlyRole>}/>
        <Route path="enrollments" element={<OnlyRole roles={['ADMIN']}><Enrollments/></OnlyRole>}/>
        <Route path="audit" element={<OnlyRole roles={['ADMIN']}><Audit/></OnlyRole>}/>
        <Route path="profile" element={<Profile/>}/>
      </Route>
      <Route path="*" element={<Navigate to="/" replace/>}/>
    </Routes>
    <Toaster/>
  </AuthCtx.Provider>;
}
