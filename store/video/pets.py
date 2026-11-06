import math
O='#2B2438'
def oval(c,cx,cy,w,h,stroke=True): return f'<ellipse cx="{cx}" cy="{cy}" rx="{w/2}" ry="{h/2}" fill="{c}" '+(f'stroke="{O}" stroke-width="1.8"' if stroke else '')+'/>'
def poly(c,pts,stroke=True): return f'<polygon points="{" ".join(f"{x},{y}" for x,y in pts)}" fill="{c}" '+(f'stroke="{O}" stroke-width="1.8" stroke-linejoin="round"' if stroke else '')+'/>'
def path(d,fill,stroke=True,sw=1.8): return f'<path d="{d}" fill="{fill}" '+(f'stroke="{O}" stroke-width="{sw}" stroke-linejoin="round" stroke-linecap="round"' if stroke else '')+'/>'
def face(mood,cat):
    s=[f'<circle cx="31" cy="55" r="4.2" fill="#F9B4CB" fill-opacity=".55"/>',f'<circle cx="69" cy="55" r="4.2" fill="#F9B4CB" fill-opacity=".55"/>']
    for x in (39,61):
        if mood=='normal': s+=[f'<circle cx="{x}" cy="46" r="3.6" fill="{O}"/>',f'<circle cx="{x-1.1}" cy="44.8" r="1.2" fill="#fff"/>']
        elif mood in('happy','celebrating'): s+=[f'<path d="M{x-4} 47.5 Q{x} 41.5 {x+4} 47.5" stroke="{O}" stroke-width="2.4" fill="none" stroke-linecap="round"/>']
        elif mood=='excited': s+=[f'<circle cx="{x}" cy="46" r="5" fill="{O}"/>',f'<circle cx="{x-1.6}" cy="44.2" r="1.8" fill="#fff"/>',f'<circle cx="{x+1.8}" cy="47.6" r=".9" fill="#fff"/>']
        else: s+=[f'<path d="M{x-4} 46 Q{x} 49.5 {x+4} 46" stroke="{O}" stroke-width="2.2" fill="none" stroke-linecap="round"/>']
    s.append(poly('#F9B4CB',[(47,52.5),(53,52.5),(50,56)],False) if cat else oval(O,50,53.5,8,5.5,False))
    if mood in('excited','celebrating'): s+=[oval(O,50,61,9,7,False),oval('#F08AA6',50,63,5.5,3.2,False)]
    else: s.append(f'<path d="M50 56.5 Q47.5 60.5 44.5 58.5 M50 56.5 Q52.5 60.5 55.5 58.5" stroke="{O}" stroke-width="1.6" fill="none" stroke-linecap="round"/>')
    if mood=='celebrating':
        for cx,cy in [(14,22),(86,30),(20,70)]: s.append(f'<path d="M{cx-3} {cy}H{cx+3}M{cx} {cy-3}V{cy+3}" stroke="#F5C24C" stroke-width="1.6" stroke-linecap="round"/>')
    return ''.join(s)
def cat(mood):
    f,fd,ac,mz='#F6C9A2','#E2A273','#F9B4CB','#FFF1E4'
    s=[f'<path d="M72 82 C92 80 94 58 82 54" stroke="{O}" stroke-width="9.6" fill="none" stroke-linecap="round"/>',f'<path d="M72 82 C92 80 94 58 82 54" stroke="{f}" stroke-width="6.4" fill="none" stroke-linecap="round"/>',
       oval(f,50,74,52,34),poly(f,[(23,34),(28,9),(45,21)]),poly(f,[(77,34),(72,9),(55,21)]),poly(ac,[(28,28),(30,15),(39,22)],False),poly(ac,[(72,28),(70,15),(61,22)],False),oval(f,50,46,60,54)]
    s+=[f'<line x1="{x}" y1="21" x2="{x}" y2="28" stroke="{fd}" stroke-width="2.4" stroke-linecap="round"/>' for x in (44,50,56)]
    s+=[oval(mz,50,57,22,13,False),face(mood,True)]
    for a,b,c,d in [(30,56,17,54),(30,59,17,61),(70,56,83,54),(70,59,83,61)]: s.append(f'<line x1="{a}" y1="{b}" x2="{c}" y2="{d}" stroke="{O}" stroke-opacity=".45" stroke-width="1.2" stroke-linecap="round"/>')
    s+=[oval(f,38,89,13,7),oval(f,62,89,13,7)]
    return ''.join(s)
def dog(mood):
    f,fd,mz='#F4E4CF','#B98A62','#FFF8EE'
    return ''.join([f'<path d="M73 76 Q86 72 88 60" stroke="{O}" stroke-width="8.6" fill="none" stroke-linecap="round"/>',f'<path d="M73 76 Q86 72 88 60" stroke="{fd}" stroke-width="5.4" fill="none" stroke-linecap="round"/>',
       oval(f,50,74,52,34),oval(f,50,46,60,54),f'<ellipse cx="61" cy="44" rx="7" ry="6.5" fill="{fd}" fill-opacity=".55"/>',
       f'<g transform="rotate(18 22 40)">{oval(fd,22,44,15,30)}</g>',f'<g transform="rotate(-18 78 40)">{oval(fd,78,44,15,30)}</g>',oval(mz,50,58,24,15,False),face(mood,False),oval(f,38,89,13,7),oval(f,62,89,13,7)])
ACC={
'cap': path('M29 28 C29 11 71 11 71 28 Z','#7FB6E8')+path('M60 26.5 Q80 24 86 29 Q74 32 60 30 Z','#5E97CC')+f'<circle cx="50" cy="13.5" r="2.2" fill="#5E97CC" stroke="{O}" stroke-width="1.4"/>',
'crown': poly('#F5C24C',[(35,24),(35,10),(42.5,16.5),(50,6),(57.5,16.5),(65,10),(65,24)])+'<circle cx="50" cy="18" r="2.4" fill="#E86A8A"/><circle cx="41" cy="20" r="1.6" fill="#7FB6E8"/><circle cx="59" cy="20" r="1.6" fill="#7FB6E8"/>',
'glasses': f'<circle cx="39" cy="46" r="7" fill="#fff" fill-opacity=".25" stroke="{O}" stroke-width="1.7"/><circle cx="61" cy="46" r="7" fill="#fff" fill-opacity=".25" stroke="{O}" stroke-width="1.7"/><path d="M46 45 Q50 43 54 45 M32 45 L22 42.5 M68 45 L78 42.5" stroke="{O}" stroke-width="1.7" fill="none" stroke-linecap="round"/>',
'headphones': f'<path d="M21 47 C21 9 79 9 79 47" stroke="{O}" stroke-width="7" fill="none" stroke-linecap="round"/><path d="M21 47 C21 9 79 9 79 47" stroke="#9C86E0" stroke-width="4.2" fill="none" stroke-linecap="round"/><rect x="14" y="39" width="12" height="18" rx="5" fill="#9C86E0" stroke="{O}" stroke-width="1.8"/><rect x="74" y="39" width="12" height="18" rx="5" fill="#9C86E0" stroke="{O}" stroke-width="1.8"/>',
'scarf': path('M60 70 L68 70 L70 84 L61 84 Z','#F28B82')+path('M27 66 Q50 76 73 66 L73 73 Q50 83 27 73 Z','#F28B82')+'<path d="M38 71.5 L38 78.5 M50 74 L50 81 M62 71.5 L62 78.5" stroke="#fff" stroke-opacity=".7" stroke-width="2" stroke-linecap="round"/>'+f'<path d="M62 84 L62 87 M65.5 84 L65.5 87 M69 84 L69 87" stroke="{O}" stroke-width="1.4" stroke-linecap="round"/>',
'flower': ''.join(f'<circle cx="{70+4.6*math.cos(a):.2f}" cy="{24+4.6*math.sin(a):.2f}" r="3.4" fill="#F9B4CB" stroke="{O}" stroke-width="1.3"/>' for a in [i*1.2566-1.5708 for i in range(5)])+f'<circle cx="70" cy="24" r="2.6" fill="#F5C24C" stroke="{O}" stroke-width="1.2"/>',
}
ORDER=['scarf','headphones','cap','crown','glasses','flower']
def pet(kind,mood,acc=(),cls='',ids=None,shadow=True):
    body=(cat if kind=='cat' else dog)(mood)
    sh='<ellipse cx="50" cy="92.5" rx="28" ry="4.5" fill="#000" fill-opacity=".08"/>' if shadow else ''
    accs=''
    for k in ORDER:
        if k in acc:
            idattr=f' id="{ids[k]}"' if ids and k in ids else ''
            accs+=f'<g{idattr}>{ACC[k]}</g>'
    return f'<svg viewBox="0 0 100 100" class="{cls}" style="overflow:visible">{sh}{body}{accs}</svg>'
VB={'cap':'26 7 62 26','crown':'31 3 38 24','glasses':'19 36 62 20','headphones':'11 8 78 52','scarf':'25 63 50 27','flower':'61 15 18 18'}
def accessory_thumb(k):
    return f'<svg viewBox="{VB[k]}" style="width:100%;height:100%">{ACC[k]}</svg>'
