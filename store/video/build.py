import sys
LANG = sys.argv[1] if len(sys.argv) > 1 else 'tr'
sys.path.insert(0,'.')
from pets import pet, accessory_thumb

CSS = r"""
*{box-sizing:border-box;margin:0;padding:0}
body{width:1920px;height:1080px;overflow:hidden;background:#FFFBF5;font-family:'Inter',sans-serif;color:#2B2438}
#stage{position:relative;width:1920px;height:1080px;overflow:hidden;background:#FFFBF5}
.blob{position:absolute;border-radius:50%;filter:blur(2px)}
.scene{position:absolute;inset:0;display:none}
.left{position:absolute;left:140px;top:300px;width:760px}
.kicker{display:inline-flex;align-items:center;gap:12px;background:#E9E1FF;color:#2E1D6B;font-weight:700;font-size:26px;padding:10px 22px;border-radius:999px;margin-bottom:28px}
.kicker .dot{width:12px;height:12px;border-radius:50%;background:#6E4FC7}
.title{font-family:'Inter Display','Inter',sans-serif;font-weight:800;font-size:84px;line-height:1.05;letter-spacing:-2px;color:#2B2438}
.sub{margin-top:28px;font-size:36px;line-height:1.38;color:#5C5470;max-width:720px}
.card{position:absolute;background:#fff;border:2px solid #F2E6D6;border-radius:36px;box-shadow:0 24px 60px rgba(110,79,199,.13)}
.emoji{font-family:'Noto Color Emoji';}
.pill{display:inline-flex;align-items:center;gap:12px;border-radius:999px;font-weight:700}
.row{position:absolute;left:40px;width:640px;height:116px;border-radius:28px;display:flex;align-items:center;padding:0 28px;gap:22px}
.bubble{width:68px;height:68px;border-radius:50%;background:rgba(255,255,255,.75);display:flex;align-items:center;justify-content:center;font-size:38px}
.hname{font-size:32px;font-weight:700}
.chip{display:inline-block;margin-top:8px;font-size:22px;font-weight:700;padding:4px 14px;border-radius:999px;background:#E9E1FF;color:#2E1D6B}
.check{margin-left:auto;width:56px;height:56px;border-radius:50%;border:4px solid #5C5470;background:#fff;display:flex;align-items:center;justify-content:center}
.float{position:absolute;font-weight:800;font-size:40px;color:#6E4FC7;opacity:0}
svg{display:block}
"""

def kicker(text): return f'<div class="kicker"><span class="dot"></span>{text}</div>'
def left(sid,k,title,sub): return f'<div class="left" id="{sid}L">{kicker(k)}<div class="title">{title}</div><div class="sub">{sub}</div></div>'
CHECK_SVG='<svg viewBox="0 0 24 24" width="34" height="34"><path d="M5 12.5l4.5 4.5L19 7.5" fill="none" stroke="#fff" stroke-width="3.2" stroke-linecap="round" stroke-linejoin="round"/></svg>'

scenes=[]
# S0 intro
scenes.append(f'''<div class="scene" id="s0">
 <div id="s0cat" style="position:absolute;left:600px;top:170px;width:360px;height:360px">{pet('cat','happy')}</div>
 <div id="s0dog" style="position:absolute;left:960px;top:170px;width:360px;height:360px">{pet('dog','excited')}</div>
 <div id="s0word" style="position:absolute;left:0;right:0;top:575px;text-align:center;font-family:'Inter Display';font-weight:900;font-size:150px;letter-spacing:-4px;color:#6E4FC7">Pawtiva</div>
 <div id="s0tag" style="position:absolute;left:0;right:0;top:775px;text-align:center;font-size:44px;color:#5C5470;font-weight:500">Alışkanlıklarını geliştir, küçük dostunu birlikte büyüt.</div>
</div>''')
# S1 companion
def pcard(i,kind,name,desc,left_):
    return f'''<div class="card" id="s1c{i}" style="left:{left_}px;top:250px;width:370px;height:540px;padding:30px;text-align:center">
   <div style="position:relative;width:260px;height:260px;margin:0 auto">
     <div id="s1c{i}n" style="position:absolute;inset:0">{pet(kind,'normal')}</div>
     <div id="s1c{i}h" style="position:absolute;inset:0;opacity:0">{pet(kind,'happy')}</div>
   </div>
   <div style="font-size:38px;font-weight:800;margin-top:22px">{name}</div>
   <div style="font-size:24px;color:#5C5470;margin-top:12px;line-height:1.35">{desc}</div>
   <div id="s1c{i}k" style="position:absolute;right:26px;top:26px;width:58px;height:58px;border-radius:50%;background:#6E4FC7;display:flex;align-items:center;justify-content:center;opacity:0">{CHECK_SVG}</div>
 </div>'''
scenes.append(f'''<div class="scene" id="s1">{left('s1','Maskot','Yol arkadaşını seç','Minik Kedi ya da Minik Köpek. Dilersen sonra Ayarlar’dan değiştir; ilerlemen her zaman korunur.')}
 {pcard(0,'cat','Minik Kedi','Meraklı ve sakin. Küçük zaferleri sever.',1010)}
 {pcard(1,'dog','Minik Köpek','Neşeli ve sadık. Her adımı seninle kutlar.',1420)}
</div>''')
# S2 habits
habits=[('💧','Su iç','#E0F1FF'),('📚','10 sayfa oku','#E9E1FF'),('🚶','Kısa yürüyüş','#DDF6EA'),('🧘','5 dk meditasyon','#FFE6D9')]
rows=''.join(f'''<div class="row" id="s2r{i}" style="top:{150+i*136}px;background:{c}">
   <div class="bubble emoji">{e}</div>
   <div><div class="hname">{n}</div><div class="chip" id="s2chip{i}">+10 XP</div></div>
   <div class="check" id="s2chk{i}"><div id="s2tick{i}" style="opacity:0">{CHECK_SVG}</div></div>
 </div>''' for i,(e,n,c) in enumerate(habits))
scenes.append(f'''<div class="scene" id="s2">{left('s2','Alışkanlıklar','Küçük günlük adımlar','Bugün yaptıklarını işaretle. Her biri +10 XP ve +5 PawCoins kazandırır.')}
 <div class="card" id="s2panel" style="left:1060px;top:170px;width:720px;height:740px">
   <div style="position:absolute;left:40px;top:44px;font-size:40px;font-weight:800">Bugün</div>
   <div style="position:absolute;left:40px;top:98px;font-size:24px;color:#5C5470" id="s2sum">4 alışkanlıktan 0 tanesi tamamlandı</div>
   <svg viewBox="0 0 100 100" style="position:absolute;right:40px;top:30px;width:96px;height:96px;transform:rotate(-90deg)">
     <circle cx="50" cy="50" r="40" fill="none" stroke="#E9E1FF" stroke-width="12"/>
     <circle id="s2ring" cx="50" cy="50" r="40" fill="none" stroke="#6E4FC7" stroke-width="12" stroke-linecap="round" stroke-dasharray="251.3" stroke-dashoffset="251.3"/>
   </svg>
   {rows}
 </div>
 <div class="float" id="s2f">+10 XP</div>
</div>''')
# S3 XP
scenes.append(f'''<div class="scene" id="s3">{left('s3','Seviye','Birlikte büyüyün','XP biriktikçe seviye atlarsın. Kaçırılan bir gün ilerlemeni asla silmez.')}
 <div style="position:absolute;left:1150px;top:120px;width:440px;height:440px">
   <div id="s3n" style="position:absolute;inset:0">{pet('cat','happy')}</div>
   <div id="s3c" style="position:absolute;inset:0;opacity:0">{pet('cat','celebrating')}</div>
 </div>
 <div id="s3badge" style="position:absolute;left:1520px;top:150px;background:#F5C24C;color:#2B2438;font-weight:900;font-size:40px;padding:16px 30px;border-radius:999px;box-shadow:0 12px 30px rgba(245,194,76,.45);opacity:0">Seviye 2!</div>
 <div class="card" style="left:1050px;top:640px;width:640px;height:190px;padding:34px 40px">
   <div style="display:flex;justify-content:space-between;font-size:30px;font-weight:700"><span id="s3lvl">Seviye 1</span><span id="s3xp" style="color:#5C5470">70 / 100 XP</span></div>
   <div style="margin-top:26px;height:30px;border-radius:999px;background:#E9E1FF;overflow:hidden"><div id="s3bar" style="height:100%;width:70%;border-radius:999px;background:linear-gradient(90deg,#6E4FC7,#C2547F)"></div></div>
 </div>
</div>''')
# S4 shop
items=[('cap','Şapka','40'),('glasses','Gözlük','60'),('scarf','Atkı','70'),('headphones','Kulaklık','90'),('flower','Çiçek','50'),('crown','Taç','200')]
icards=''.join(f'''<div class="card" id="s4i{i}" style="left:{990+i*148}px;top:700px;width:136px;height:210px;border-radius:26px;padding:14px 8px;text-align:center">
   <div style="width:96px;height:72px;margin:6px auto 0">{accessory_thumb(k)}</div>
   <div style="font-size:24px;font-weight:800;margin-top:12px;white-space:nowrap">{n}</div>
   <div id="s4p{i}" style="font-size:20px;font-weight:800;color:#A4481E;margin-top:6px;white-space:nowrap">{('🔒 Seviye 5' if k=='crown' else p+' <span class=emoji>🪙</span>')}</div>
 </div>''' for i,(k,n,p) in enumerate(items))
scenes.append(f'''<div class="scene" id="s4">{left('s4','Market','PawCoins ile süsle','Şapka, gözlük, atkı ve daha fazlası. Satın almadan önce dostunun üzerinde dene. Gerçek parayla satın alma yok.')}
 <div style="position:absolute;left:1180px;top:120px;width:400px;height:400px">{pet('dog','happy',('cap','glasses','scarf'),ids={'cap':'s4cap','glasses':'s4gl','scarf':'s4sc'})}</div>
 <div id="s4coin" class="pill" style="position:absolute;left:1600px;top:150px;background:#FFF0C7;color:#5C3D00;font-size:34px;padding:14px 26px"><span class="emoji">🪙</span><span id="s4bal">180</span></div>
 {icards}
</div>''')
# S5 7 days
days=[('Cmt','31'),('Paz','1'),('Pzt','2'),('Sal','3'),('Çar','4'),('Per','5'),('Cum','6')]
if LANG=='en': days=[('Sat','31'),('Sun','1'),('Mon','2'),('Tue','3'),('Wed','4'),('Thu','5'),('Fri','6')]
dcells=''.join(f'''<div id="s5d{i}" style="position:absolute;left:{40+i*92}px;top:110px;width:82px;height:118px;border-radius:22px;border:2px solid #F2E6D6;background:#fff;text-align:center;padding-top:16px">
   <div style="font-size:21px;color:#5C5470;font-weight:600">{d}</div><div style="font-size:34px;font-weight:800;margin-top:4px">{n}</div>
   {'<div style="width:9px;height:9px;border-radius:50%;background:#6E4FC7;margin:6px auto 0"></div>' if i==6 else ''}
 </div>''' for i,(d,n) in enumerate(days))
scenes.append(f'''<div class="scene" id="s5">{left('s5','Son 7 gün','Unuttuğun günü tamamla','Dün işaretlemeyi mi unuttun? Son 7 günden birine geç ve sonradan işaretle.')}
 <div class="card" style="left:1080px;top:200px;width:720px;height:620px">
   <div style="position:absolute;left:40px;top:40px;font-size:30px;font-weight:800">Günler</div>
   {dcells}
   <div style="position:absolute;left:40px;top:280px;font-size:32px;font-weight:800" id="s5head">Bugünün alışkanlıkları</div>
   <div style="position:absolute;left:40px;top:328px;font-size:22px;color:#5C5470;opacity:0" id="s5note">Son 7 günün herhangi birini güncelleyebilirsin.</div>
   <div class="row" id="s5row" style="top:400px;background:#E9E1FF">
     <div class="bubble emoji">📚</div>
     <div><div class="hname">10 sayfa oku</div><div class="chip" id="s5chip">+10 XP</div></div>
     <div class="check" id="s5chk"><div id="s5tick" style="opacity:0">{CHECK_SVG}</div></div>
   </div>
 </div>
 <div class="float" id="s5f">+10 XP</div>
</div>''')
# S6 stats
vals=[2,3,1,4,3,4,2]
bars=''.join(f'''<div style="position:absolute;left:{70+i*86}px;bottom:96px;width:52px;height:260px;display:flex;flex-direction:column;justify-content:flex-end;align-items:center">
   <div style="font-size:22px;font-weight:700;margin-bottom:8px" id="s6v{i}">{v}</div>
   <div id="s6b{i}" style="width:52px;height:{v*58}px;border-radius:14px 14px 8px 8px;background:{'#6E4FC7' if i==6 else '#C9B8FF'};transform-origin:bottom;transform:scaleY(0)"></div>
 </div><div style="position:absolute;left:{62+i*86}px;bottom:52px;width:68px;text-align:center;font-size:21px;color:#5C5470">{days[i][0]}</div>''' for i,v in enumerate(vals))
badges=[('🌱','İlk Adım','Kazanıldı',1.0),('🌟','Kusursuz Gün','Kazanıldı',1.0),('🔥','İstikrar','5 / 7 gün',5/7)]
bcards=''.join(f'''<div class="card" id="s6a{i}" style="left:{1040+i*250}px;top:760px;width:230px;height:200px;border-radius:28px;padding:22px;text-align:center">
   <div class="emoji" style="font-size:52px">{e}</div><div style="font-size:24px;font-weight:800;margin-top:6px">{n}</div>
   <div style="font-size:19px;color:{'#1E7A52' if p==1 else '#5C5470'};font-weight:700;margin-top:4px">{s}</div>
   <div style="margin:10px auto 0;width:160px;height:10px;border-radius:999px;background:#E9E1FF;overflow:hidden"><div style="width:{p*100:.0f}%;height:100%;background:{'#1E7A52' if p==1 else '#6E4FC7'}"></div></div>
 </div>''' for i,(e,n,s,p) in enumerate(badges))
scenes.append(f'''<div class="scene" id="s6">{left('s6','İstatistikler','İlerlemeni gör','Haftalık grafik, aylık takvim, geçmiş ve kazandığın başarımlar. Hepsi gerçek kayıtlarından hesaplanır.')}
 <div class="card" style="left:1040px;top:130px;width:730px;height:590px">
   <div style="position:absolute;left:40px;top:40px;font-size:32px;font-weight:800">Bu hafta</div>
   <div style="position:absolute;right:40px;top:44px;font-size:24px;color:#5C5470">19 tamamlama</div>
   {bars}
 </div>
 {bcards}
</div>''')
# S7 personalize
scenes.append(f'''<div class="scene" id="s7">{left('s7','Sana göre','Hatırlatma, dil ve tema','Günde bir nazik hatırlatma, Türkçe ya da İngilizce, açık ya da koyu tema.')}
 <div class="card" id="s7n" style="left:1040px;top:140px;width:740px;height:170px;border-radius:30px;padding:30px 34px;display:flex;gap:24px;align-items:center">
   <div style="width:84px;height:84px;border-radius:24px;background:#E9E1FF;flex:none;padding:10px">{pet('cat','happy',shadow=False)}</div>
   <div><div style="font-size:24px;color:#5C5470;font-weight:600">Pawtiva · 20:00</div><div style="font-size:30px;font-weight:700;margin-top:6px;line-height:1.3">Küçük dostun bugünkü macera için seni bekliyor!</div></div>
 </div>
 <div class="card" id="s7l" style="left:1040px;top:360px;width:740px;height:150px;border-radius:30px;padding:30px 34px;display:flex;align-items:center;justify-content:space-between">
   <div style="position:relative;width:360px;height:60px;font-size:40px;font-weight:800">
     <div id="s7tr" style="position:absolute;left:0;top:0">Günaydın!</div>
     <div id="s7en" style="position:absolute;left:0;top:0;opacity:0">Good morning!</div>
   </div>
   <div style="position:relative;width:200px;height:72px;border-radius:999px;background:#E9E1FF;display:flex;font-size:28px;font-weight:800">
     <div id="s7knob" style="position:absolute;left:6px;top:6px;width:94px;height:60px;border-radius:999px;background:#6E4FC7"></div>
     <div id="s7trl" style="position:relative;width:100px;text-align:center;line-height:72px;color:#fff">TR</div>
     <div id="s7enl" style="position:relative;width:100px;text-align:center;line-height:72px;color:#2E1D6B">EN</div>
   </div>
 </div>
 <div class="card" id="s7a" style="left:1040px;top:560px;width:355px;height:330px;border-radius:30px;text-align:center;padding-top:26px">
   <div style="width:200px;height:200px;margin:0 auto">{pet('dog','happy')}</div><div style="font-size:30px;font-weight:800;margin-top:14px">Açık</div>
 </div>
 <div class="card" id="s7d" style="left:1425px;top:560px;width:355px;height:330px;border-radius:30px;text-align:center;padding-top:26px;background:#211B2C;border-color:#3A3248">
   <div style="width:200px;height:200px;margin:0 auto">{pet('dog','happy')}</div><div style="font-size:30px;font-weight:800;margin-top:14px;color:#F2EEF8">Koyu</div>
 </div>
</div>''')
# S8 privacy
priv=[('📴','İnternetsiz çalışır','Her şey cihazında saklanır.'),('🙅','Hesap gerekmez','Kayıt yok, giriş yok.'),('🚫','Reklam yok','Analitik ve izleme de yok.'),('💸','Abonelik yok','PawCoins gerçek parayla satılmaz.')]
prows=''.join(f'''<div class="card" id="s8r{i}" style="left:1060px;top:{170+i*180}px;width:700px;height:150px;border-radius:30px;display:flex;align-items:center;gap:28px;padding:0 34px">
   <div class="emoji" style="font-size:54px;width:80px;height:80px;border-radius:50%;background:#DDF6EA;display:flex;align-items:center;justify-content:center">{e}</div>
   <div><div style="font-size:34px;font-weight:800">{t}</div><div style="font-size:24px;color:#5C5470;margin-top:4px">{d}</div></div>
 </div>''' for i,(e,t,d) in enumerate(priv))
scenes.append(f'''<div class="scene" id="s8">{left('s8','Gizlilik','Verilerin sende kalır','Pawtiva sunucuya bağlanmaz. Alışkanlıkların, ilerlemen ve tercihlerin yalnızca senin cihazında.')}
 {prows}
</div>''')
# S9 outro
scenes.append(f'''<div class="scene" id="s9">
 <div id="s9cat" style="position:absolute;left:620px;top:150px;width:340px;height:340px">{pet('cat','celebrating',('flower',))}</div>
 <div id="s9dog" style="position:absolute;left:960px;top:150px;width:340px;height:340px">{pet('dog','celebrating',('cap',))}</div>
 <div id="s9word" style="position:absolute;left:0;right:0;top:540px;text-align:center;font-family:'Inter Display';font-weight:900;font-size:150px;letter-spacing:-4px;color:#6E4FC7">Pawtiva</div>
 <div id="s9tag" style="position:absolute;left:0;right:0;top:740px;text-align:center;font-size:44px;color:#5C5470;font-weight:500">Alışkanlık takibi ve sanal dost — Android için</div>
 <div id="s9pills" style="position:absolute;left:0;right:0;top:840px;display:flex;justify-content:center;gap:20px">
   <div class="pill" style="background:#E9E1FF;color:#2E1D6B;font-size:28px;padding:12px 26px">Türkçe · English</div>
   <div class="pill" style="background:#DDF6EA;color:#125238;font-size:28px;padding:12px 26px">Çevrimdışı</div>
   <div class="pill" style="background:#FFE6D9;color:#6B2E10;font-size:28px;padding:12px 26px">Reklamsız</div>
 </div>
</div>''')

JS = r"""
const D=[4.5,4.5,6.5,5.5,6.5,5.5,6,5.5,4.5,4.5];
const C=x=>Math.max(0,Math.min(1,x)), E=x=>1-Math.pow(1-C(x),3);
const P=(t,a,d)=>E((t-a)/d);
function B(x){x=C(x);const c1=1.70158,c3=c1+1;return 1+c3*Math.pow(x-1,3)+c1*Math.pow(x-1,2);}
const $=id=>document.getElementById(id);
function enter(id,t,a,dy=40,d=0.6){const p=P(t,a,d),e=$(id);e.style.opacity=p;e.style.transform=`translateY(${(1-p)*dy}px)`;}
function pop(id,t,a,d=0.55,extra=''){const x=C((t-a)/d),e=$(id);e.style.opacity=C(x*3);e.style.transform=`scale(${x<=0?0.6:0.6+0.4*B(x)}) ${extra}`;}
function fade(id,t,a,d=0.3){$(id).style.opacity=P(t,a,d);}
function breathe(id,t,ph=0){$(id).style.transform=`scaleY(${1+0.018*Math.sin((t+ph)*3.2)})`;$(id).style.transformOrigin='50% 95%';}
function leftIn(n,t){enter(n+'L',t,0.15,30,0.7);}
function check(prefix,i,t,a){
  const p=P(t,a,0.25),chk=$(prefix+'chk'+i),tick=$(prefix+'tick'+i);
  chk.style.background=p>0.5?'#1E7A52':'#fff';chk.style.borderColor=p>0.5?'#1E7A52':'#5C5470';
  chk.style.transform=`scale(${1+0.25*Math.sin(Math.PI*C((t-a)/0.35))})`;tick.style.opacity=p;
  const chip=$(prefix+'chip'+i);if(p>0.5){chip.textContent='✓ +10 XP kazanıldı';chip.style.background='#DDF6EA';chip.style.color='#125238';}else{chip.textContent='+10 XP';chip.style.background='#E9E1FF';chip.style.color='#2E1D6B';}
}
function floatUp(id,t,a,x,y){const e=$(id),q=C((t-a)/1.1);e.style.left=x+'px';e.style.top=(y-70*E(q))+'px';e.style.opacity=q<=0?0:(q<0.25?q/0.25:1-C((q-0.6)/0.4));}
function blobs(T){
  const b=document.querySelectorAll('.blob');
  b.forEach((el,i)=>{el.style.transform=`translate(${Math.sin(T*0.25+i*2)*30}px,${Math.cos(T*0.2+i)*24}px)`;});
}
const draws=[
 t=>{pop('s0cat',t,0.2,0.7);pop('s0dog',t,0.45,0.7);enter('s0word',t,0.9,50,0.8);enter('s0tag',t,1.5,30,0.8);},
 t=>{leftIn('s1',t);pop('s1c0',t,0.5);pop('s1c1',t,0.7);
     const s=P(t,2.0,0.35),c=$('s1c1');c.style.borderColor=s>0.5?'#6E4FC7':'#F2E6D6';c.style.borderWidth=s>0.5?'5px':'2px';c.style.background=s>0.5?'#F3EEFF':'#fff';
     if(t>2.0)c.style.transform=`scale(${1+0.05*s})`;$('s1c1h').style.opacity=s;$('s1c1n').style.opacity=1-s;
     const k=$('s1c1k');k.style.opacity=s;k.style.transform=`scale(${0.6+0.4*B(C((t-2.0)/0.45))})`;},
 t=>{leftIn('s2',t);enter('s2panel',t,0.35,40,0.7);
     [0,1,2,3].forEach(i=>enter('s2r'+i,t,0.6+i*0.12,24,0.5));
     const times=[1.6,2.6,3.6];let done=0;
     [0,1,2,3].forEach(i=>{if(i<3){check('s2',i,t,times[i]);if(t>=times[i]+0.12)done++;}else check('s2',i,t,99);});
     $('s2sum').textContent=`4 alışkanlıktan ${done} tanesi tamamlandı`;
     $('s2ring').setAttribute('stroke-dashoffset',251.3*(1-done/4));
     const last=times.filter(x=>t>=x).pop();if(last!==undefined)floatUp('s2f',t,last,1520,350+times.indexOf(last)*136);else $('s2f').style.opacity=0;},
 t=>{leftIn('s3',t);
     const f=P(t,0.9,1.0);let pct=70+30*f, lvl=1;
     if(t>2.1){pct=P(t,2.4,0.8)*10;lvl=2;}
     $('s3bar').style.width=pct+'%';$('s3lvl').textContent='Seviye '+lvl;$('s3xp').textContent=Math.round(lvl==1?pct:pct)+' / 100 XP';
     const c=P(t,2.0,0.25);$('s3c').style.opacity=c;$('s3n').style.opacity=1-c;
     if(t>2.0){const h=Math.abs(Math.sin(Math.PI*C((t-2.0)/0.5)))*30;$('s3c').style.transform=`translateY(${-h}px)`;}
     pop('s3badge',t,2.05,0.6);},
 t=>{leftIn('s4',t);[0,1,2,3,4,5].forEach(i=>enter('s4i'+i,t,0.5+i*0.08,30,0.5));pop('s4coin',t,0.4);
     const buys=[[1.5,'s4cap',0,40],[2.7,'s4gl',1,60],[3.9,'s4sc',2,70]];let bal=180;
     buys.forEach(([a,id,ci,price])=>{const x=C((a-t)>0?0:(t-a)/0.5);const g=$(id);g.style.opacity=x;g.style.transformOrigin='50% 50%';g.style.transformBox='fill-box';g.style.transform=`translateY(${(1-E(x))*-14}px) scale(${0.7+0.3*B(x)})`;
       if(t>=a){bal-=price;const pc=$('s4p'+ci);pc.textContent='✓ Sahip';pc.style.color='#1E7A52';const card=$('s4i'+ci);card.style.borderColor='#6E4FC7';}else{const pc=$('s4p'+ci);pc.innerHTML=price+' <span class=emoji>🪙</span>';pc.style.color='#A4481E';$('s4i'+ci).style.borderColor='#F2E6D6';}});
     $('s4bal').textContent=bal;},
 t=>{leftIn('s5',t);
     const sel=t>=1.3?5:6;for(let i=0;i<7;i++){const d=$('s5d'+i);const on=i==sel;d.style.background=on?'#E9E1FF':'#fff';d.style.borderColor=on?'#6E4FC7':'#F2E6D6';d.style.borderWidth=on?'4px':'2px';}
     $('s5head').textContent=sel==6?'Bugünün alışkanlıkları':'5 Kas 2026 günü';fade('s5note',t,1.35,0.3);if(t<1.3)$('s5note').style.opacity=0;
     check('s5','',t,2.7);if(t>=2.7)floatUp('s5f',t,2.7,1540,612);else $('s5f').style.opacity=0;},
 t=>{leftIn('s6',t);[0,1,2,3,4,5,6].forEach(i=>{const p=P(t,0.6+i*0.12,0.6);$('s6b'+i).style.transform=`scaleY(${p})`;$('s6v'+i).style.opacity=p;});
     [0,1,2].forEach(i=>pop('s6a'+i,t,2.3+i*0.35));},
 t=>{leftIn('s7',t);enter('s7n',t,0.4,-60,0.7);enter('s7l',t,1.0,30,0.6);
     const sw=t>=2.3&&t<4.0?P(t,2.3,0.35):(t>=4.0?1-P(t,4.0,0.35):0);
     $('s7knob').style.left=(6+94*sw)+'px';$('s7trl').style.color=sw<0.5?'#fff':'#2E1D6B';$('s7enl').style.color=sw>=0.5?'#fff':'#2E1D6B';
     $('s7tr').style.opacity=1-sw;$('s7en').style.opacity=sw;
     enter('s7a',t,1.5,30,0.6);enter('s7d',t,1.8,30,0.6);},
 t=>{leftIn('s8',t);[0,1,2,3].forEach(i=>enter('s8r'+i,t,0.6+i*0.3,30,0.55));},
 t=>{pop('s9cat',t,0.2,0.7);pop('s9dog',t,0.4,0.7);enter('s9word',t,0.8,50,0.8);enter('s9tag',t,1.3,30,0.7);enter('s9pills',t,1.8,30,0.7);},
];
let start=[];{let a=0;D.forEach(d=>{start.push(a);a+=d;});}
function draw(n,t){
  document.querySelectorAll('.scene').forEach((s,i)=>s.style.display=i==n?'block':'none');
  blobs(start[n]+t);draws[n](t);
  const dur=D[n],f=0.35;let o=0;
  if(n>0&&t<f)o=1-t/f; if(n<D.length-1&&t>dur-f)o=Math.max(o,(t-(dur-f))/f);
  $('fade').style.opacity=C(o);
}
"""

html=f'''<!doctype html><html><head><meta charset="utf-8"><style>{CSS}</style></head><body><div id="stage">
<div class="blob" style="left:-160px;top:-180px;width:620px;height:620px;background:#E9E1FF;opacity:.7"></div>
<div class="blob" style="right:-200px;top:520px;width:640px;height:640px;background:#DDF6EA;opacity:.65"></div>
<div class="blob" style="left:700px;top:900px;width:420px;height:420px;background:#FFE6D9;opacity:.6"></div>
<div class="blob" style="right:220px;top:-160px;width:340px;height:340px;background:#FFE3EC;opacity:.6"></div>
{''.join(scenes)}
<div id="fade" style="position:absolute;inset:0;background:#FFFBF5;opacity:0;pointer-events:none"></div>
</div><script>{JS}</script></body></html>'''
EN=[
 ('Minik Kedi ya da Minik Köpek. Dilersen sonra Ayarlar’dan değiştir; ilerlemen her zaman korunur.','A kitten or a puppy. Switch any time in Settings; your progress always stays.'),
 ('Alışkanlıklarını geliştir, küçük dostunu birlikte büyüt.','Build your habits, grow your little friend together.'),
 ('Bugün yaptıklarını işaretle. Her biri +10 XP ve +5 PawCoins kazandırır.','Tick off what you did today. Each one earns +10 XP and +5 PawCoins.'),
 ('XP biriktikçe seviye atlarsın. Kaçırılan bir gün ilerlemeni asla silmez.','Level up as your XP grows. A missed day never takes your progress away.'),
 ('Şapka, gözlük, atkı ve daha fazlası. Satın almadan önce dostunun üzerinde dene. Gerçek parayla satın alma yok.','Caps, glasses, scarves and more. Try them on your friend before buying. No real-money purchases.'),
 ('Dün işaretlemeyi mi unuttun? Son 7 günden birine geç ve sonradan işaretle.','Forgot to tick yesterday? Pick any of the last 7 days and fill it in.'),
 ('Haftalık grafik, aylık takvim, geçmiş ve kazandığın başarımlar. Hepsi gerçek kayıtlarından hesaplanır.','Weekly chart, monthly calendar, history and achievements. All from your real records.'),
 ('Günde bir nazik hatırlatma, Türkçe ya da İngilizce, açık ya da koyu tema.','One gentle reminder a day, Turkish or English, light or dark theme.'),
 ('Pawtiva sunucuya bağlanmaz. Alışkanlıkların, ilerlemen ve tercihlerin yalnızca senin cihazında.','Pawtiva never talks to a server. Your habits, progress and settings live only on your device.'),
 ('Küçük dostun bugünkü macera için seni bekliyor!','Your little friend is waiting for today’s adventure!'),
 ('Meraklı ve sakin. Küçük zaferleri sever.','Curious and calm. Loves cozy little wins.'),
 ('Neşeli ve sadık. Her adımı seninle kutlar.','Cheerful and loyal. Celebrates every step.'),
 ('Alışkanlık takibi ve sanal dost — Android için','Habit tracker & virtual pet for Android'),
 ('4 alışkanlıktan ${done} tanesi tamamlandı','${done} of 4 habits done'),
 ('4 alışkanlıktan 0 tanesi tamamlandı','0 of 4 habits done'),
 ('Son 7 günün herhangi birini güncelleyebilirsin.','You can update any of the last 7 days.'),
 ('Her şey cihazında saklanır.','Everything is stored on your device.'),
 ('Kayıt yok, giriş yok.','No sign-up, no login.'),
 ('Analitik ve izleme de yok.','No analytics or tracking either.'),
 ('PawCoins gerçek parayla satılmaz.','PawCoins are never sold for real money.'),
 ('Yol arkadaşını seç','Choose your companion'),('Küçük günlük adımlar','Small daily steps'),('Birlikte büyüyün','Grow together'),
 ('PawCoins ile süsle','Dress them up with PawCoins'),('Unuttuğun günü tamamla','Fill in a missed day'),('İlerlemeni gör','See your progress'),
 ('Hatırlatma, dil ve tema','Reminders, language & theme'),('Verilerin sende kalır','Your data stays with you'),
 ('Bugünün alışkanlıkları','Today’s habits'),('5 Kas 2026 günü','Thu, Nov 5, 2026'),('✓ +10 XP kazanıldı','✓ +10 XP earned'),
 ('İnternetsiz çalışır','Works offline'),('Hesap gerekmez','No account needed'),('Reklam yok','No ads'),('Abonelik yok','No subscriptions'),
 ('Kusursuz Gün','Perfect Day'),('İlk Adım','First Step'),('İstikrar','Consistency'),('Kazanıldı','Unlocked'),('5 / 7 gün','5 / 7 days'),
 ('19 tamamlama','19 completions'),('Bu hafta','This week'),('Günler','Days'),
 ('10 sayfa oku','Read 10 pages'),('Kısa yürüyüş','Short walk'),('5 dk meditasyon','5-min meditation'),('Su iç','Drink water'),
 ('Minik Kedi','Kitten'),('Minik Köpek','Puppy'),('✓ Sahip','✓ Owned'),
 ('>Şapka<','>Cap<'),('>Gözlük<','>Glasses<'),('>Atkı<','>Scarf<'),('>Kulaklık<','>Headset<'),('>Çiçek<','>Flower<'),('>Taç<','>Crown<'),
 ('>Açık<','>Light<'),('>Koyu<','>Dark<'),('Çevrimdışı','Offline'),('Reklamsız','No ads'),
 ('<span class="dot"></span>Maskot<','<span class="dot"></span>Companion<'),('<span class="dot"></span>Alışkanlıklar<','<span class="dot"></span>Habits<'),
 ('<span class="dot"></span>Son 7 gün<','<span class="dot"></span>Last 7 days<'),('<span class="dot"></span>İstatistikler<','<span class="dot"></span>Statistics<'),
 ('<span class="dot"></span>Sana göre<','<span class="dot"></span>Made for you<'),('<span class="dot"></span>Gizlilik<','<span class="dot"></span>Privacy<'),
 ('<span class="dot"></span>Market<','<span class="dot"></span>Shop<'),
 ('>Bugün<','>Today<'),('Seviye','Level'),
]
if LANG=='en':
    for a,c in EN: html=html.replace(a,c)
open('video.html' if LANG=='tr' else 'video_en.html','w').write(html)
print('ok', len(html))
