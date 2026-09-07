require("dotenv").config();
const http=require("http");
const express=require("express");
const helmet=require("helmet");
const cors=require("cors");
const jwt=require("jsonwebtoken");
const bcrypt=require("bcryptjs");
const {Pool}=require("pg");
const {WebSocketServer}=require("ws");

const app=express();
app.use(helmet());
app.use(cors());
app.use(express.json());

const pool=process.env.DATABASE_URL?new Pool({connectionString:process.env.DATABASE_URL,ssl:{rejectUnauthorized:false}}):null;
const secret=process.env.JWT_SECRET||"change-me";

async function init(){
 if(!pool)return;
 await pool.query(`CREATE TABLE IF NOT EXISTS users(
 id SERIAL PRIMARY KEY,
 username VARCHAR(64) UNIQUE NOT NULL,
 display_name VARCHAR(128) NOT NULL,
 password_hash TEXT NOT NULL,
 role VARCHAR(16) NOT NULL DEFAULT 'USER',
 status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
 created_at TIMESTAMP DEFAULT NOW()
 )`);
}

app.get("/health",(_,res)=>res.json({status:"ok"}));

app.post("/auth/register",async(req,res)=>{
 const {username,displayName,password}=req.body;
 if(!username||!displayName||!password)return res.status(400).json({error:"missing fields"});
 const hash=await bcrypt.hash(password,12);
 try{
  const r=await pool.query("INSERT INTO users(username,display_name,password_hash) VALUES($1,$2,$3) RETURNING id,username,status",[username,displayName,hash]);
  res.status(201).json({user:r.rows[0]});
 }catch(e){res.status(409).json({error:"username exists"});}
});

app.post("/auth/login",async(req,res)=>{
 const {username,password}=req.body;
 const r=await pool.query("SELECT * FROM users WHERE username=$1",[username]);
 if(!r.rowCount)return res.status(401).json({error:"invalid credentials"});
 const u=r.rows[0];
 if(!await bcrypt.compare(password,u.password_hash))return res.status(401).json({error:"invalid credentials"});
 if(u.status!=="ACTIVE")return res.status(403).json({error:"account pending approval",status:u.status});
 const token=jwt.sign({id:u.id,role:u.role},secret,{expiresIn:"15m"});
 res.json({token,user:{id:u.id,username:u.username,displayName:u.display_name,role:u.role}});
});

function auth(req,res,next){
 const h=req.headers.authorization||"";
 const t=h.startsWith("Bearer ")?h.slice(7):null;
 if(!t)return res.sendStatus(401);
 try{req.user=jwt.verify(t,secret);next();}catch(e){res.sendStatus(401);}
}

app.get("/admin/pending",auth,async(req,res)=>{
 if(!["OWNER","ADMIN"].includes(req.user.role))return res.sendStatus(403);
 const r=await pool.query("SELECT id,username,display_name,status FROM users WHERE status='PENDING'");
 res.json(r.rows);
});

app.post("/admin/users/:id/approve",auth,async(req,res)=>{
 if(!["OWNER","ADMIN"].includes(req.user.role))return res.sendStatus(403);
 await pool.query("UPDATE users SET status='ACTIVE' WHERE id=$1",[req.params.id]);
 res.json({status:"approved"});
});

const server=http.createServer(app);
const wss=new WebSocketServer({server,path:"/ws"});
wss.on("connection",ws=>ws.send(JSON.stringify({type:"system",event:"connected"})));

init().then(()=>server.listen(process.env.PORT||3000,"0.0.0.0"));
