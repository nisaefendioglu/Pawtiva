import subprocess, os, sys
from playwright.sync_api import sync_playwright
D=[4.5,4.5,6.5,5.5,6.5,5.5,6,5.5,4.5,4.5]; FPS=30
here=os.path.dirname(os.path.abspath(__file__))
LANG=sys.argv[1] if len(sys.argv)>1 else 'tr'
PAGE='video.html' if LANG=='tr' else 'video_en.html'
SUF='' if LANG=='tr' else '_en'
NAME='pawtiva-tanitim' if LANG=='tr' else 'pawtiva-promo-en'
with sync_playwright() as p:
    b=p.chromium.launch(); pg=b.new_page(viewport={'width':1920,'height':1080})
    pg.goto('file://'+os.path.join(here,PAGE)); pg.wait_for_timeout(600)
    for n,d in enumerate(D):
        out=os.path.join(here,f'scene{n}{SUF}.mp4'); tmp=os.path.join(here,f'scene{n}{SUF}.tmp.mp4')
        ff=subprocess.Popen(['ffmpeg','-y','-loglevel','error','-f','image2pipe','-framerate',str(FPS),'-c:v','mjpeg','-i','-',
            '-c:v','libx264','-crf','18','-preset','veryfast','-pix_fmt','yuv420p','-r',str(FPS),tmp],stdin=subprocess.PIPE)
        for f in range(round(d*FPS)):
            pg.evaluate(f'draw({n},{f/FPS})')
            ff.stdin.write(pg.screenshot(type='jpeg',quality=92))
        ff.stdin.close()
        if ff.wait()==0: os.replace(tmp,out); print('scene',n,'ok',flush=True)
        else: print('scene',n,'FAILED',flush=True); sys.exit(1)
    b.close()
with open(os.path.join(here,'list'+SUF+'.txt'),'w') as fl:
    for n in range(len(D)): fl.write(f"file 'scene{n}{SUF}.mp4'\n")
r=subprocess.run(['ffmpeg','-y','-loglevel','error','-f','concat','-safe','0','-i',os.path.join(here,'list'+SUF+'.txt'),'-c','copy','-movflags','+faststart',os.path.join(here,NAME+'.tmp.mp4')])
if r.returncode==0: os.replace(os.path.join(here,NAME+'.tmp.mp4'),os.path.join(here,NAME+'.mp4')); print('done')
